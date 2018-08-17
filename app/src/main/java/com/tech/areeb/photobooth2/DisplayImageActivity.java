package com.tech.areeb.photobooth2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;


public class DisplayImageActivity extends Activity {

    private ImageView photo;
    private RatingBar ratings;
    int Position;
    Float Rating;
    TextView titleText,bodyText;
    RelativeLayout layout;
    LinearLayout TitleAuthorLayout;
    DatabaseReference databaseReference;
    GalleryImages temp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_images);

        photo = findViewById(R.id.photo_expanded);
        ratings = findViewById(R.id.ratings_expanded);
        titleText = findViewById(R.id.photo_title);
        bodyText = findViewById(R.id.photo_author);
        layout = findViewById(R.id.layout);
        TitleAuthorLayout = findViewById(R.id.TitleAuthorLayout);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Gallery");

        temp = (GalleryImages) getIntent().getExtras().getSerializable("SelectedElement");
        Position = getIntent().getIntExtra("Position",0);


        if(temp == null){
            finish();
        }

        File file = new File(getApplicationContext().getCacheDir(),temp.getImageFileName());



        Picasso.with(getApplicationContext()).load(file).into(photo);
        titleText.setTextColor(temp.getImageTitleTextColor());
        titleText.setText(temp.getImageTitle());
        bodyText.setTextColor(temp.getImageBodyTextColor());
        layout.setBackgroundColor(temp.getImageRGB());
        ratings.setRating(temp.getImageRating());






    }

    @Override
    public void finish() {
        databaseReference.child(temp.getImageTitle()).child("Ratings").setValue(ratings.getRating());
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("Position",Position);
        bundle.putFloat("Rating",ratings.getRating());
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
        super.finish();
    }





}
