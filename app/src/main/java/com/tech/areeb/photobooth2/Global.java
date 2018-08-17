package com.tech.areeb.photobooth2;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Global extends Application {

    private static String APP_CACHE = "CACHE_PHOTOBOOTH";

        @Override
        public void onCreate() {
            super.onCreate();

            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(false);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);

        }

        public boolean writeCache(Context context,Object object){
            try {

                FileOutputStream fos = context.openFileOutput(APP_CACHE,MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(object);
                oos.close();
                fos.close();
                return true;


            }catch (Exception e){
                Log.e("Global", e.toString());
                return false;
            }

        }

        public Object readCache(Context context){
            try {
                FileInputStream fis = context.openFileInput(APP_CACHE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object object = ois.readObject();
                ois.close();
                fis.close();
                return object;
            }
            catch (Exception e){
                Log.e("Global", e.toString());
                return null;
            }
        }



}

