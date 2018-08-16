package cn.nubia.media.photos;

import cn.nubia.media.photos.cache.ImageCacheService;
import cn.nubia.media.photos.workthread.ThreadPool;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Looper;


public class AppContxtImp implements AppContext {

    private ImageCacheService mImageCacheService;
    private Object mLock = new Object();
    private ThreadPool mThreadPool;
    private Context mAppContext;
    public AppContxtImp(Context appContext){
        mAppContext = appContext;
    }

    public Context getAndroidContext() {
        return mAppContext;
    }

    public ImageCacheService getImageCacheService() {
        // This method may block on file I/O so a dedicated lock is needed here.
        	synchronized (mLock) {
            if (mImageCacheService == null) {
                mImageCacheService = new ImageCacheService(getAndroidContext());
            }
            return mImageCacheService;
        }
    }

	@Override
	public ThreadPool getThreadPool() {
		 if (mThreadPool == null) {
	         mThreadPool = new ThreadPool();
	     }
	     return mThreadPool;
	}

    @Override
    public ContentResolver getContentResolver() {
        return mAppContext.getContentResolver();
    }

    @Override
    public Looper getMainLooper(){
        return mAppContext.getMainLooper();
    }
}
