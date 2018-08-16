package cn.nubia.media.photos;

import java.util.concurrent.atomic.AtomicBoolean;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

class ChangeNotifier {

    private MediaSet mMediaSet;
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);
    private final Handler mDefaultMainHandler;
	private ContentObserver mContentObserver;
    private AppContext mApp;
    public ChangeNotifier(MediaSet set, Uri uri, AppContext application) {
        mMediaSet = set;
        mApp = application;
        mDefaultMainHandler = new Handler(application.getMainLooper());
        mContentObserver = new ContentObserver(mDefaultMainHandler){

			@Override
			public void onChange(boolean selfChange) {
				notifyContentChanged(selfChange);
			}
			
		};
		application.getContentResolver().registerContentObserver(uri,
				true, mContentObserver);
    }

	// Returns the dirty flag and clear it.
    public boolean isDirty() {
        return mContentDirty.compareAndSet(true, false);
    }

    public void release(){
    	mApp.getContentResolver().unregisterContentObserver(mContentObserver);
    }
    public void fakeChange() {
    	notifyContentChanged(false);
    }

    protected void notifyContentChanged(boolean selfChange) {
        if (mContentDirty.compareAndSet(false, true)) {
            mMediaSet.notifyContentChanged();
        }
    }
}
