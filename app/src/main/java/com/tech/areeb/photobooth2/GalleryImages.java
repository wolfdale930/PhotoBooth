package com.tech.areeb.photobooth2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;

import java.io.Serializable;

public class GalleryImages implements Serializable {

    private String imagePath;
    private String imageFileName;
    private int imageRGB;
    private int imageTitleTextColor;
    private int imageBodyTextColor;
    private Float imageRating;
    private String imageTitle;
    private String imageAuthor;
    private Context context;

    public GalleryImages(String imagePath, String imageFileName, String imageTitle, Float imageRating){
        this.imagePath = imagePath;
        this.imageFileName = imageFileName;
        this.imageTitle = imageTitle;
        this.imageRating = imageRating;
        new setColors().execute();
    }


    public String getImageFileName(){
        return imageFileName;
    }

    public int getImageRGB(){
        return imageRGB;
    }

    public int getImageTitleTextColor(){
        return imageTitleTextColor;
    }

    public int getImageBodyTextColor(){
        return imageBodyTextColor;
    }

    public Float getImageRating(){
        return imageRating;
    }

    public void setImageRating(Float imageRating){
        this.imageRating = imageRating;
    }

    public String getImageTitle(){
        return imageTitle;
    }

    public String getImageAuthor(){
        return imageAuthor;
    }

    public class setColors extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                Palette.from(bitmap)
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch swatch = palette.getDominantSwatch();
                                if (swatch == null) {
                                    swatch = palette.getMutedSwatch();
                                }
                                imageRGB = swatch.getRgb();
                                imageTitleTextColor = swatch.getTitleTextColor();
                                imageBodyTextColor = swatch.getBodyTextColor();
                            }
                        });
                return null;


        }
    }
}
