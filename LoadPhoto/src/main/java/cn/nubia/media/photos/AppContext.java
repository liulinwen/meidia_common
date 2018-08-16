package cn.nubia.media.photos;
import cn.nubia.media.photos.cache.ImageCacheService;
import cn.nubia.media.photos.workthread.ThreadPool;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Looper;


public interface AppContext {
    public ImageCacheService getImageCacheService();
    public Context getAndroidContext();
    public ThreadPool getThreadPool();
    public ContentResolver getContentResolver();
    public Looper getMainLooper();
}