/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nubia.media.photos.cache;

import cn.nubia.media.photos.common.BitmapUtils;
import cn.nubia.media.photos.common.DecodeUtils;
import cn.nubia.media.photos.AppContext;
import cn.nubia.media.photos.cache.BytesBufferPool.BytesBuffer;
import cn.nubia.media.photos.MediaItem;
import cn.nubia.media.photos.workthread.ThreadPool.Job;
import cn.nubia.media.photos.workthread.ThreadPool.JobContext;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.ExecutionException;

public abstract class ImageCacheRequest implements Job<Bitmap> {
    protected static final String TAG = "ImageCacheRequest";

    protected AppContext mApplication;
    private String mPath;
    private int mType;
    private int mTargetSize;
    
    public ImageCacheRequest(AppContext application,
            String path, int type, int targetSize) {
        mApplication = application;
        mPath = path;
        mType = type;
        mTargetSize = targetSize;

    }

    @Override
    public Bitmap run(JobContext jc) {
     /*   FutureTarget<Bitmap> futureTarget =
                Glide.with(mApplication.getAndroidContext())
                        .asBitmap()
                        .load(mPath)
                        .apply(RequestOptions.overrideOf(200).fitCenter())
                        .submit();


        try {
            return futureTarget.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
        ImageCacheService cacheService = mApplication.getImageCacheService();

        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
        try {
            boolean found = cacheService.getImageData(mPath, mType, buffer);
            if (jc.isCancelled()) return null;
            if (found) {
            	BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = DecodeUtils.decodeUsingPool(jc,
                            buffer.data, buffer.offset, buffer.length, options);
                
                if (bitmap == null && !jc.isCancelled()) {
                    Log.w(TAG, "decode cached failed mPath is: "+ mPath);
                }
                return bitmap;
            }
        } finally {
            MediaItem.getBytesBufferPool().recycle(buffer);
        }
        Bitmap bitmap = onDecodeOriginal(jc, mType);
        if (jc.isCancelled()) return null;

        if (bitmap == null) {
            Log.w(TAG, "decode orig failed mPath is: "+ mPath);
            return null;
        }

        byte[] array = BitmapUtils.compressToBytes(bitmap);
        if (jc.isCancelled()) return null;

        cacheService.putImageData(mPath, mType, array);
        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int type);
}
