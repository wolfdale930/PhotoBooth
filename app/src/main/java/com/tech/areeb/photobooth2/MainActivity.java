package com.tech.areeb.photobooth2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements MyRecyclerViewAdapter.ItemClickListener{

    private MyRecyclerViewAdapter adapter;
    private FloatingActionButton fab;
    private ProgressBar loading;
    private RecyclerView recyclerView;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    ArrayList<GalleryImages> galleryImages = new ArrayList<>();
    FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fab_camera);
        recyclerView = findViewById(R.id.photo_grid);
        loading = findViewById(R.id.loading);



        ////////////////AUTHENTICATION NEEDED FOR FIREBASE STORAGE ACCESSING///////////////////
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
        } else {
            signInAnonymously();
        }
        ///////////////////////////////////////////////

        fetchFromFirebase();


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        //RecycleAdapter
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MyRecyclerViewAdapter(getApplicationContext(),galleryImages);
        adapter.setClickListener(MainActivity.this);
        recyclerView.setAdapter(adapter);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);

            }
        });



    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                        /* perform your actions here*/

            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("MainActivity", "signFailed****** ", exception);
                    }
                });
    }

    ////////////////////Rating Result form DisplayImageActivity//////////////////
    @Override
    protected void onActivityResult(int ReqCode, int ResCode, Intent intent){
        super.onActivityResult(ReqCode,ResCode,intent);
        if(ReqCode == 99){
            if (ResCode == RESULT_OK){
                Bundle bundle = intent.getExtras();
                int pos;
                Float rate;
                pos = bundle.getInt("Position");
                rate = bundle.getFloat("Rating");
                Log.e("MainActivity",pos + "");
                galleryImages.get(pos).setImageRating(rate);
                adapter.notifyItemChanged(pos);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getApplicationContext(),DisplayImageActivity.class);
        intent.putExtra("SelectedElement",galleryImages.get(position));
        intent.putExtra("Position",position);
        startActivityForResult(intent,99);
    }


    private void fetchFromFirebase(){
        galleryImages.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Gallery");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot temp: dataSnapshot.getChildren()){

                    storageReference = FirebaseStorage.getInstance().getReference().child("Gallery/Thumbnail/" + temp.getKey() + ".png" );
                    try {
                        final File tempFile = File.createTempFile(temp.getKey(), ".png");
                        storageReference.getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                String rate = temp.child("Ratings").getValue().toString();
                                Float flot = Float.parseFloat(rate);
                                galleryImages.add(new GalleryImages(tempFile.getAbsolutePath(), tempFile.getName(),temp.getKey(),flot));
                                adapter.notifyDataSetChanged();
                                loading.setVisibility(View.GONE);
                            }
                        });

                    }
                    catch (Exception e){
                        Log.e("MainActiviy",e.toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }


    /*
    private void fetchFromFirebase(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Gallery");
        File file =  new File(Environment.getExternalStorageDirectory(),"Wallpapers");
        File[] list = null;
        if(file.isDirectory()){
            list = file.listFiles();
        }
        for(final File temp: list){
            Uri uri = Uri.fromFile(temp);
            storageReference = FirebaseStorage.getInstance().getReference().child("Gallery/" + temp.getName());
            UploadTask task = storageReference.putFile(uri);
            databaseReference.child(temp.getName().replace(".png","")).child("Ratings").setValue(0);
            task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.e("FirebaseStorage","Success: " + temp.getPath());
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("FirebaseStorage","Failed: " + e.toString());
                }
            });
        }
    }
    */

}
