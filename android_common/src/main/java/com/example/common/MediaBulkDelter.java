package com.example.common;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by llw on 2016-08-31.
 */
public class MediaBulkDelter {
    private StringBuilder mWhereClause = new StringBuilder();
    private ArrayList<String> mWhereArgs = new ArrayList<String>(100);
    final ContentResolver mCR;
    private Uri mBaseUri;
    public MediaBulkDelter(ContentResolver cr, Uri uri){
        mCR = cr;
        mBaseUri = uri;
    }

    public void delete(String id){
        if(mWhereClause.length() != 0){
            mWhereClause.append(",");
        }
        mWhereClause.append("?");
        mWhereArgs.add(id);
        if(mWhereArgs.size() > 100){
            flush();
        }
    }

    public void flush(){
        int size = mWhereArgs.size();
        if(size > 0){
            String[] foo = new String[size];
            foo = mWhereArgs.toArray(foo);
            int numrows = mCR.delete(mBaseUri, MediaStore.MediaColumns._ID + " IN (" + mWhereClause.toString() + ")", foo);
            mWhereClause.setLength(0);
            mWhereArgs.clear();
        }
    }
}
