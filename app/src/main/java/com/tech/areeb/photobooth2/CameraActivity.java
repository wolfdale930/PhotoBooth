package com.tech.areeb.photobooth2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.tech.areeb.photobooth2.ui.camera.CameraSourcePreview;
import com.tech.areeb.photobooth2.ui.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;
import static java.io.File.createTempFile;
import static java.io.File.separator;

public class CameraActivity extends Activity {

    private CameraSource cameraSource = null;
    private CameraSourcePreview cameraSourcePreview;
    private GraphicOverlay graphicOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private Bitmap tempBitmap;
    private ImageView captureButton;
    private ImageView photoPreview;
    private ImageView postPhoto;
    private ImageView retakePhoto, clownFilter, hatFilter, shadesFilter, noseDogFilter;
    private LinearLayout filterList;
    private volatile FaceGraphic mFaceGraphic;
    private Bitmap filter;
    private File cache;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraSourcePreview = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.faceOverlay);
        captureButton = findViewById(R.id.captureButton);
        photoPreview = findViewById(R.id.photo_preview);
        postPhoto = findViewById(R.id.post_photo);
        retakePhoto = findViewById(R.id.retake_photo);
        filterList = findViewById(R.id.filter_list);
        clownFilter = findViewById(R.id.clown);
        hatFilter = findViewById(R.id.hat);
        shadesFilter = findViewById(R.id.shades);
        noseDogFilter = findViewById(R.id.nosedog);
        filter = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.shades);
        filterList.setVisibility(View.GONE);
        postPhoto.setVisibility(View.GONE);
        retakePhoto.setVisibility(View.GONE);
        photoPreview.setVisibility(View.GONE);


        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
        int ws = ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(ws == PackageManager.PERMISSION_GRANTED){
            //
        }
        else{
            if (Build.VERSION.SDK_INT>=23){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        filter = null;

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                clickPhoto();
            }
        });

        retakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraSourcePreview.setVisibility(View.VISIBLE);
                startCameraSource();
                photoPreview.setVisibility(View.GONE);
                postPhoto.setVisibility(View.GONE);
                retakePhoto.setVisibility(View.GONE);
                captureButton.setVisibility(View.VISIBLE);
                filterList.setVisibility(View.VISIBLE);
            }
        });

        postPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    tempBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    FileOutputStream fileOutputStream = new FileOutputStream(cache);
                    fileOutputStream.write(outputStream.toByteArray());
                    fileOutputStream.close();


                    Intent intent = new Intent(getApplicationContext(),PhotoPreviewActivity.class);
                    intent.putExtra("File",cache.getAbsolutePath());
                    startActivity(intent);
                }
                catch (Exception e){
                    Log.e("PostPhoto",e.toString());
                }
            }
        });

        cameraSourcePreview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                filterList.setVisibility(View.VISIBLE);
                return true;
            }
        });

        clownFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.clown);
                filter = bitmap;
                mFaceGraphic.setBitmap(bitmap);
            }
        });
        hatFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.hat);
                filter = bitmap;
                mFaceGraphic.setBitmap(bitmap);
            }
        });
        shadesFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.shades);
                filter = bitmap;
                mFaceGraphic.setBitmap(bitmap);
            }
        });

        noseDogFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.nosedog);
                filter = bitmap;
                mFaceGraphic.setBitmap(bitmap);
            }
        });


    }
    /////////////OnCreate Ends/////////////////////////////



    private void clickPhoto (){

            cameraSource.takePicture(null, new CameraSource.PictureCallback() {


                @Override
                public void onPictureTaken(byte[] bytes) {
                    try{


                        cameraSourcePreview.stop();

                        cache = createTempFile("TEMP_PHOTO",".jpg");
                        FileOutputStream fos = new FileOutputStream(cache);
                        fos.write(bytes);
                        cache.deleteOnExit();

                        float cx, cy;
                        Matrix flip = new Matrix();


                        tempBitmap = BitmapFactory.decodeFile(cache.getAbsolutePath());

                        if(filter==null){
                            photoPreview.setImageBitmap(tempBitmap);
                        }
                        else {

                            cx = tempBitmap.getWidth() / 2f;
                            cy = tempBitmap.getHeight() / 2f;

                            flip.postScale(-1, 1, cx, cy);

                            tempBitmap = Bitmap.createScaledBitmap(tempBitmap, tempBitmap.getWidth() / 3, tempBitmap.getHeight() / 3, false);
                            tempBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), flip, false);
                            Canvas canvas = new Canvas(tempBitmap);

                            FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();
                            Frame frame = new Frame.Builder().setBitmap(tempBitmap).build();
                            SparseArray<Face> faces = faceDetector.detect(frame);
                            Matrix matrix;


                            for (int i = 0; i < faces.size(); ++i) {
                                Face thisFace = faces.valueAt(i);
                                float x1 = thisFace.getPosition().x;
                                float y1 = thisFace.getPosition().y;
                                float width = thisFace.getWidth();
                                float height = thisFace.getHeight();
                                matrix = new Matrix();
                                matrix.setRotate(-thisFace.getEulerZ());
                                //Create Bitmap per face
                                filter = Bitmap.createBitmap(filter, 0, 0, filter.getWidth(), filter.getHeight(), matrix, false);
                                Bitmap scaledFilter = Bitmap.createScaledBitmap(filter, (int) width, (int) height, false);

                                canvas.drawBitmap(scaledFilter, x1, y1 + 40, null);
                            }


                            photoPreview.setImageBitmap(tempBitmap);

                        }


                        photoPreview.setVisibility(View.VISIBLE);
                        cameraSourcePreview.setVisibility(View.GONE);
                        captureButton.setVisibility(View.GONE);
                        postPhoto.setVisibility(View.VISIBLE);
                        retakePhoto.setVisibility(View.VISIBLE);
                        filterList.setVisibility(View.GONE);

                        //tempBitmap = Bitmap.createScaledBitmap(tempBitmap,tempBitmap.getWidth()/3, tempBitmap.getHeight()/3, false);

                        /*Bitmap tempBitmap = Bitmap.createScaledBitmap(loadedImage,loadedImage.getWidth()/8,loadedImage.getHeight()/8,false);
                        Bitmap shades = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.shades);
                        Canvas canvas = new Canvas(tempBitmap);






                        File tempFile = createTempFile(new Date().toString(),".png");
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        tempBitmap.compress(Bitmap.CompressFormat.PNG,100,ostream);

                        FileOutputStream fos = new FileOutputStream(tempFile);
                        fos.write(ostream.toByteArray());
                        fos.close();
                        ostream.close();
                        tempFile.deleteOnExit();


                        Intent intent = new Intent(getApplicationContext(),PhotoPreviewActivity.class);
                        intent.putExtra("File",tempFile.getAbsolutePath());
                        startActivity(intent);


                        */

                    }
                    catch (Exception e){
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("CameraActivity",e.toString());
                    }
                }
            });

    }
    ///////////////clickPhoto Ends///////////////////////////////////////////



    private void requestCameraPermission() {
        Log.w("FaceDetectionApp", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage("Permission not Granted")
                .setPositiveButton("Ok", listener)
                .show();
    }


    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w("FaceDetectionApp", "Face detector dependencies are not yet available.");
        }


        cameraSource = new CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(600, 600)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .setAutoFocusEnabled(true)
            .build();

    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(graphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;


        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay,getApplicationContext());
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            mOverlay.clear();
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }


    }


    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                cameraSourcePreview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e("FaceDetectionApp", "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        filter = null;

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        cameraSourcePreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

}
