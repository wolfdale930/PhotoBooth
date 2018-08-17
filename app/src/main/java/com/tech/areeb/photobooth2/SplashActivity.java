package com.tech.areeb.photobooth2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import java.io.File;
import java.util.List;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.z_splash);

        File file = getCacheDir();
        File[] cacheList;
        cacheList = file.listFiles();
        for (File sel : cacheList){
            if(sel.isDirectory()){

            }
            else {
                sel.delete();
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                myStartActivity();

            }
        },50);
    }

    private void myStartActivity() {

        startActivity(new Intent(SplashActivity.this,MainActivity.class));
        this.finish();
    }
}
