package com.tech.areeb.photobooth2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class PhotoPreviewActivity extends Activity{

    private ScrollView windowBackground;
    private ImageView previewPhoto;
    private EditText titleEditText;
    private EditText authorEditText;
    private TextView title;
    private TextView author;
    private Button saveButton;
    private Button postButton;
    private File imageFile;
    private String tempFile;
    private Bitmap bitmap;
    private Bitmap bitmapThumb;
    private StorageReference pic;
    private StorageReference thumb;
    private DatabaseReference databaseReference;
    private byte[] picByte;
    private byte[] thumbByte;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);

        previewPhoto = findViewById(R.id.previewPhoto);
        titleEditText = findViewById(R.id.photoTitleEditText);
        authorEditText = findViewById(R.id.photoAuthorEditText);
        saveButton = findViewById(R.id.saveButton);
        postButton = findViewById(R.id.postButton);
        windowBackground = findViewById(R.id.photoPreviewBackground);
        title = findViewById(R.id.title);
        author = findViewById(R.id.author);

        tempFile = getIntent().getStringExtra("File");
        File file = new File(tempFile);
        file.deleteOnExit();


        if(tempFile==null){
            Toast.makeText(this,"Cannot save image",Toast.LENGTH_SHORT).show();
            finish();
        }
        else {


            bitmap = BitmapFactory.decodeFile(tempFile);

            bitmapThumb = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/8,bitmap.getHeight()/8,false);
            Palette.from(bitmapThumb).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatch = palette.getMutedSwatch();
                    if(swatch == null){
                        swatch = palette.getDarkMutedSwatch();
                    }
                    windowBackground.setBackgroundColor(swatch.getRgb());
                    saveButton.setTextColor(~swatch.getBodyTextColor());
                    saveButton.setBackgroundColor(swatch.getBodyTextColor());
                    postButton.setTextColor(~swatch.getBodyTextColor());
                    postButton.setBackgroundColor(swatch.getBodyTextColor());
                    title.setTextColor(swatch.getBodyTextColor());
                    author.setTextColor(swatch.getBodyTextColor());
                    titleEditText.setTextColor(swatch.getTitleTextColor());
                    authorEditText.setTextColor(swatch.getTitleTextColor());
                    titleEditText.setHintTextColor(swatch.getTitleTextColor());
                    authorEditText.setHintTextColor(swatch.getTitleTextColor());


                }
            });

            previewPhoto.setImageBitmap(bitmap);

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveToStorage();
                }
            });

            postButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postToWeb();
                }
            });


        }



    }


    private void saveToStorage(){


        try {

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyPhotos");

            boolean success = true;
            if (!dir.exists()) {
                Toast.makeText(getBaseContext(), dir.exists() + "",
                        Toast.LENGTH_SHORT).show();
                success = dir.mkdirs();
            }
            if (success) {
                java.util.Date date = new java.util.Date();
                imageFile = new File(dir.getAbsolutePath() + File.separator + date.toString() + titleEditText.getText() + ".png");

                imageFile.createNewFile();
            } else {
                Toast.makeText(getBaseContext(), "Image Not saved",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(ostream.toByteArray());
            fout.close();
            Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
            finish();
        }
        catch (Exception e){
            Log.e("PhotoPreviewActivity", e.toString());
            Toast.makeText(getApplicationContext(),"Error writing to Storage",Toast.LENGTH_SHORT).show();
        }
    }

    private void postToWeb(){
        pic = FirebaseStorage.getInstance().getReference().child("Gallery/" + titleEditText.getText() + ".png");
        thumb = FirebaseStorage.getInstance().getReference().child("Gallery/Thumbnail/" + titleEditText.getText() + ".png");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Gallery");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100,outputStream);
        picByte = outputStream.toByteArray();

        bitmapThumb.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        thumbByte = outputStream.toByteArray();

        UploadTask uploadPic = pic.putBytes(picByte);
        UploadTask uploadThumb = thumb.putBytes(thumbByte);
        uploadPic.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                databaseReference.child(titleEditText.getText().toString()).child("Ratings").setValue(0);
                databaseReference.child(titleEditText.getText().toString()).child("Author").setValue(authorEditText.getText().toString());
                Toast.makeText(getApplicationContext(), "Posted",Toast.LENGTH_SHORT).show();
            }
        });
        uploadThumb.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Post Failed",Toast.LENGTH_SHORT).show();
            }
        });

        saveToStorage();

    }


}
