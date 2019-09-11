package com.example.common;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by llw on 2016-08-30.
 */
public class ContentResolverHelp {
    public final static Uri URI_FILE = MediaStore.Files.getContentUri("external");
    public final static String PATH = "/storage/emulated/0/test/";
    public final static String CAMERA_PATH = "/storage/emulated/0/DCIM/Camera/连拍/";
    private Context mContext;

    public  ContentResolverHelp(Context c){
        mContext = c;
    }

    public long deleteSingle(Context c){
        ArrayList<String> list = query(c);
        long time1 = getCurrentTime();
        for(String id : list){
            c.getContentResolver().delete(URI_FILE, "_id=?", new String[]{id});
        }
        long time2 = getCurrentTime();
        return computerTime(time1, time2);
    }

    public long deleteCollection(Context c){
        ArrayList<String> list = query(c);
        long time1 = getCurrentTime();
        StringBuilder sb = new StringBuilder();
        for(String id:list){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(id);
        }
        c.getContentResolver().delete(URI_FILE, "_id in ("+sb.toString() + ")", null);
        long time2 = getCurrentTime();
        return computerTime(time1, time2);
    }

    public long deleteCollection2(Context c){
        ArrayList<String> list = query(c);

        long time1 = getCurrentTime();
        ArrayList<ContentProviderOperation> ops = new  ArrayList<ContentProviderOperation>(list.size());
        for(String id:list){
            ContentProviderOperation op = ContentProviderOperation.newDelete(URI_FILE)
                    .withSelection("_id=?",  new String[]{id}).build();
            ops.add(op);
        }
        try {
            ContentProviderResult[] results = c.getContentResolver().applyBatch("media", ops);
            //results[0].count
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        long time2 = getCurrentTime();
        return computerTime(time1, time2);
    }

    public long deletdlike(final Context c){
        final Map<String, String> list = queryIDAndData(c);

        Thread thread  = new Thread(new Runnable() {
            @Override
            public void run() {
                long time1 = getCurrentTime();
                for(Map.Entry id : list.entrySet()){
                    c.getContentResolver().delete(URI_FILE, "_data like ?", new String[]{(String)id.getValue()});
                }
                long time2 = getCurrentTime();
                computerTime(time1, time2);
            }
        });
        thread.start();
        return -1;
    }

    public void deletefragments(final Context c){
        final Map<String, String> list = queryIDAndData(c);

        long time1 = getCurrentTime();
        for(Map.Entry id : list.entrySet()){
            deleteFile((String)id.getValue());
        }
        long time2 = getCurrentTime();
        computerTime2(time1, time2, "deleteFile");

        time1 = getCurrentTime();
        Uri.Builder builder = URI_FILE.buildUpon();
        builder.appendQueryParameter("deletedata", "false");
        MediaBulkDelter mbd = new MediaBulkDelter(c.getContentResolver(), builder.build());
        for(Map.Entry id : list.entrySet()){
            mbd.delete((String)id.getKey());
        }
        mbd.flush();
        time2 = getCurrentTime();
        computerTime2(time1, time2,"deleteDB");
        //ImageReader
    }

    @NonNull
    private Map<String, String> queryIDAndData(Context c) {
        final Map<String, String> list = new HashMap<String, String>();
        Cursor cursor = c.getContentResolver().query(URI_FILE, new String[]{MediaStore.Files.FileColumns._ID, "_data"},
                "_data like ?", new String[]{CAMERA_PATH + "%"}, null);
        if(cursor != null){
            while(cursor.moveToNext()){
                list.put(cursor.getString(0), cursor.getString(1));
            }
        }

        cursor.close();
        return list;
    }

    private long getCurrentTime(){
        return System.currentTimeMillis();
    }

    private long computerTime2(long time1, long time2, String tag){
        long s = time2 - time1;
        Log.i("ContentResolverHelp",tag +  " is: "+ s);
        Toast.makeText(mContext, tag +  " is: "+ s, Toast.LENGTH_LONG).show();
        return s;
    }

    private long computerTime(long time1, long time2){
       return  computerTime2(time1, time2, "computerTime");
    }

    private void deleteFile(String path){
        File f = new File(path);
        f.delete();
    }

    private ArrayList<String> query(Context c){
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor = c.getContentResolver().query(URI_FILE, new String[]{MediaStore.Files.FileColumns._ID, "_data"},
                "_data like ?", new String[]{PATH + "%"}, null);
        if(cursor != null){
            while(cursor.moveToNext()){
                list.add(String.valueOf(cursor.getInt(0)));
            }
        }
        cursor.close();
        return list;
    }

    public void insert(Context c){
        long time1 = getCurrentTime();
        File fc = new File(PATH);
        if(!fc.exists()){
            fc.mkdir();
        }
        ContentValues[] values = new ContentValues[1000];
        for(int i=0; i<values.length; ++i){
            File f = new File(PATH + i + ".jpg");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ContentValues v = new ContentValues();
            v.put(MediaStore.Files.FileColumns.DATA, PATH + i + ".jpg");
            values[i] = v;
        }
        c.getContentResolver().bulkInsert(URI_FILE, values);
        long time2 = getCurrentTime();

        computerTime2(time1, time2, "insert");
    }

    public void doscan(Context c){
        c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED));
    }

    //adb shell setprop log.tag.SQLiteQueryBuilder DEBUG
    //adb shell setprop log.tag.MediaProvider DEBUG
    public void queryMultiShoot(Context c){
        Uri.Builder builder = URI_FILE.buildUpon();
        builder.appendQueryParameter("limit", "0,1");
        Cursor cursor = c.getContentResolver().query(builder.build(),
                new String[]{"_id", "title", "mime_type", "_data", "bucket_display_name", "count(*)"},
                "_id IN (SELECT _id FROM files JOIN (SELECT bucket_id, MAX(datetaken) AS MD FROM files WHERE (media_type = 1) AND (_data LIKE '%/DCIM/Camera/multiShoot/%' OR _data LIKE '%/DCIM/Camera/连拍/%') GROUP BY bucket_id) GB ON GB.MD=files.datetaken AND GB.bucket_id=files.bucket_id AND files.media_type=1 GROUP BY files.bucket_id)",
                null,"datetaken ASC, _id");
        Log.i("ContentResolverHelp", "queryMultiShoot cursor is: "+ cursor.getCount());
        if(cursor != null) {
            cursor.close();
        }
    }

    public void queryGroupBy(Context c){
        Cursor cursor = c.getContentResolver().query(URI_FILE,
                new String[]{MediaStore.Images.ImageColumns._ID, "count(*)"},
                "(media_type = 1) AND (_data LIKE '%/DCIM/Camera/multiShoot/%' OR _data LIKE '%/DCIM/Camera/连拍/%') GROUP BY date(datetaken/1000)",
                null,"datetaken ASC, _id");
        Log.i("ContentResolverHelp", "queryGroupBy cursor is: "+ cursor.getCount());
        if(cursor != null) {
            cursor.close();
        }
    }



}
