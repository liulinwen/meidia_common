package com.example.llw.myapplication;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.common.ContentResolverHelp;
import com.example.common.MediaCodecListTest;
import com.example.common.RuntimeHelper;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ContentResolverHelp mCR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // MyClass mc = new MyClass();
        mCR = new ContentResolverHelp(this.getApplicationContext());
        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        loadimage();
    }

    public void deleteSingle(View v) {
        mCR.deleteSingle(this.getApplicationContext());
    }

    public void deleteCollection(View v) {
        mCR.deleteCollection(this.getApplicationContext());
    }

    public void deleteCollection2(View v) {
        mCR.deletefragments(this.getApplicationContext());
    }

    public void insert(View v) {
        mCR.insert(this.getApplicationContext());
    }

    public void doscan(View v) {
        mCR.doscan(this.getApplicationContext());
    }

    public void doQueryMultiShoot(View v) {
        mCR.queryMultiShoot(this.getApplicationContext());
    }

    public void doqueryGroupBy(View v) {
        mCR.queryGroupBy(this.getApplicationContext());
    }

    public void scanDirectoriesWithJava(View v) {
        //MediaCodecListTest.PrintCodec();
        //RuntimeHelper.setprop("adb shell");
        try {
            Runtime.getRuntime().exec("setprop log.tag.MediaProvider DEBUG");
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*String str = RuntimeHelper.getprop("adb shell getprop log.tag.MediaProvider");
        if(str.equals("DEBUG")){
            Log.i("llw", "setprop str is: "+ str);
        }*/
    }

    public void loadBitmap(View v) {
        loadimage();
    }

    private void loadimage() {
        InputStream is = null;
        try {
            is = this.getAssets().open("test.jpg");
            //long time = System.currentTimeMillis();
            BitmapRegionDecoder bd = BitmapRegionDecoder.newInstance(is, false);
            long time = System.currentTimeMillis();
            //Debug.startNativeTracing();
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferQualityOverSpeed = true;
            Bitmap b = bd.decodeRegion(new Rect(100, 100, 1034, 1034), op);
            long time2 = System.currentTimeMillis();
            Log.i("llw", "use time is: " + (time2 - time));
            // ((ImageView)v).setImageBitmap(b);
            ImageView v = (ImageView) this.findViewById(R.id.imageView2);
            v.setImageBitmap(b);
            TextView text = (TextView)this.findViewById(R.id.textView8);
            text.append("use time is: "+(time2-time));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }
}
