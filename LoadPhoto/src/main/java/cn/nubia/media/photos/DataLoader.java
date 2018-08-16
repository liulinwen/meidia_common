package cn.nubia.media.photos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import cn.nubia.media.photos.common.Utils;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;


class DataLoader {
	private String TAG = "DataLoader";
	
	public static interface DataListener {
		public void onContentChanged(int index);
	    public void onSizeChanged(int size);
	}
	
	public interface LoadingListener {
		public void onLoadingStarted();
	    public void onLoadingFinished(boolean loadingFailed);
	}
	
	private static final int DATA_CACHE_SIZE = 500;
	 
    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;
	
    private static final int MIN_LOAD_COUNT = 32;
    private static final int MAX_LOAD_COUNT = 64;
    
	private AppContext mApp;
	private final MediaItem[] mData;
	private ArrayList<DataListener> mDataListeners = new ArrayList<DataListener>();
    private MySourceListener mSourceListener = new MySourceListener();
    private LoadingListener mLoadingListener;
	private MediaSet mSource;
	private Handler mMainHandler;
	public static final long INVALID_DATA_VERSION = -1;
	private long mFailedVersion = INVALID_DATA_VERSION;
	private long mSourceVersion = INVALID_DATA_VERSION;
	private int mSize = 0;
    private final long[] mItemVersion;
    private final long[] mSetVersion;
    
    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private int mContentStart = 0;
    private int mContentEnd = 0;

	private ReloadTask mReloadTask;
    
	public DataLoader(AppContext app, MediaSet set){
		mApp = app;
		mSource = set;
        mData = new MediaItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, INVALID_DATA_VERSION);
		mMainHandler = new Handler(mApp.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                case MSG_RUN_OBJECT:
                    ((Runnable) message.obj).run();
                    return;
                case MSG_LOAD_START:
                    if (mLoadingListener != null) mLoadingListener.onLoadingStarted();
                    return;
                case MSG_LOAD_FINISH:
                    if (mLoadingListener != null) {
                        boolean loadingFailed =
                                (mFailedVersion != INVALID_DATA_VERSION);
                        mLoadingListener.onLoadingFinished(loadingFailed);
                    }
                    return;
                }
            }
        };
		
	}
		
	public MediaItem get(int index) {
		if (!isActive(index)) {
            return mSource.getMediaItem(index, 1).get(0);
        }
        return mData[index % mData.length];
	}
	
	public boolean isActive(int index) {
	    return index >= mActiveStart && index < mActiveEnd;
	        
	}
	public int size() {
        return mSize;
    }
	
    public int getActiveStart() {
        return mActiveStart;
    }
    
	public void resume() {
	     mSource.addContentListener(mSourceListener);
	     mReloadTask = new ReloadTask();
	     mReloadTask.start();
	}

	public void pause() {
	     mReloadTask.terminate();
	     mReloadTask = null;
	     mSource.removeContentListener(mSourceListener);
	}
	
	public void destroy(){
		Log.e(TAG, "unregister");
	}

    private void clearSlot(int slotIndex) {
        mData[slotIndex] = null;
        mItemVersion[slotIndex] = INVALID_DATA_VERSION;
        mSetVersion[slotIndex] = INVALID_DATA_VERSION;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;
        int end = mContentEnd;
        int start = mContentStart;

        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
        }
        long[] itemVersion = mItemVersion;
        long[] setVersion = mSetVersion;
        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        }
        if (mReloadTask != null) mReloadTask.notifyDirty();
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd) return;

        Utils.assertTrue(start <= end
                && end - start <= mData.length && end <= mSize);

        int length = mData.length;
        mActiveStart = start;
        mActiveEnd = end;

        // If no data is visible, keep the cache content
        if (start == end) return;

        int contentStart = Utils.clamp((start + end) / 2 - length / 2,
                0, Math.max(0, mSize - length));
        int contentEnd = Math.min(contentStart + length, mSize);
        if (mContentStart > start || mContentEnd < end
                || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private class MySourceListener implements MediaSet.ContentListener {
        @Override
        public void onContentDirty() {
            if (mReloadTask != null) mReloadTask.notifyDirty();
        }
    }
    
	public void setDataListener(DataListener listener) {
		if(listener != null){
			mDataListeners.add(listener);
		}
	}

	public void setLoadingListener(LoadingListener listener) {
	     mLoadingListener = listener;
	}
	
	private <T> T executeAndWait(Callable<T> callable) {
	        FutureTask<T> task = new FutureTask<T>(callable);
	        mMainHandler.sendMessage(
	                mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
	        try {
	            return task.get();
	        } catch (InterruptedException e) {
	            return null;
	        } catch (ExecutionException e) {
	            throw new RuntimeException(e);
	        }
	}
	  
    private static class UpdateInfo {
        public long version;
        public int reloadStart;
        public int reloadCount;

        public int size;
        public ArrayList<MediaItem> items;
    }
    
	private class GetUpdateInfo implements Callable<UpdateInfo> {
	        private final long mVersion;

	        public GetUpdateInfo(long version) {
	            mVersion = version;
	        }

	        @Override
	        public UpdateInfo call() throws Exception {
	            if (mFailedVersion == mVersion) {
	                // previous loading failed, return null to pause loading
	                return null;
	            }
	            UpdateInfo info = new UpdateInfo();
	            long version = mVersion;
	            info.version = mSourceVersion;
	            info.size = mSize;
	            long setVersion[] = mSetVersion;
	            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
	                int index = i % DATA_CACHE_SIZE;
	                if (setVersion[index] != version) {
	                    info.reloadStart = i;
	                    info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
	                    return info;
	                }
	            }
	            return mSourceVersion == mVersion ? null : info;
	        }
	 }
	   
	 private class UpdateContent implements Callable<Void> {

	        private UpdateInfo mUpdateInfo;

	        public UpdateContent(UpdateInfo info) {
	            mUpdateInfo = info;
	        }

	        @Override
	        public Void call() throws Exception {
	            UpdateInfo info = mUpdateInfo;
	            mSourceVersion = info.version;
	            if (mSize != info.size) {
	                mSize = info.size;
	                notifiySizeChanged(mSize);
	                if (mContentEnd > mSize) mContentEnd = mSize;
	                if (mActiveEnd > mSize) mActiveEnd = mSize;
	            }

	            ArrayList<MediaItem> items = info.items;

	            mFailedVersion = INVALID_DATA_VERSION;
	            if ((items == null) || items.isEmpty()) {
	                if (info.reloadCount > 0) {
	                    mFailedVersion = info.version;
	                    Log.d(TAG, "loading failed: " + mFailedVersion);
	                }
	                return null;
	            }
	            int start = Math.max(info.reloadStart, mContentStart);
	            int end = Math.min(info.reloadStart + items.size(), mContentEnd);

	            for (int i = start; i < end; ++i) {
	                int index = i % DATA_CACHE_SIZE;
	                mSetVersion[index] = info.version;
	                MediaItem updateItem = items.get(i - info.reloadStart);
	                long itemVersion = updateItem.getDataVersion();
	                if (mItemVersion[index] != itemVersion) {
	                    mItemVersion[index] = itemVersion;
	                    mData[index] = updateItem;
	                    if (i >= mActiveStart && i < mActiveEnd) {
	                        notifiyContentChanged(i);
	                    }
	                }
	            }
	            return null;
	        }
	}
	
	private void notifiyContentChanged(int index){
		for(int i = 0; i < mDataListeners.size(); ++i){
			mDataListeners.get(i).onContentChanged(index);
		}
	}
	
	private void notifiySizeChanged(int size){
		for(int i = 0; i < mDataListeners.size(); ++i){
			mDataListeners.get(i).onSizeChanged(size);
		}
	}
	
	private class ReloadTask extends Thread {

	        private volatile boolean mActive = true;
	        private volatile boolean mDirty = true;
	        private boolean mIsLoading = false;

	        private void updateLoading(boolean loading) {
	            if (mIsLoading == loading) return;
	            mIsLoading = loading;
	            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
	        }

	        @Override
	        public void run() {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	            boolean updateComplete = false;
	            while (mActive) {
	                synchronized (this) {
	                    if (mActive && !mDirty && updateComplete) {
	                        updateLoading(false);
	                        if (mFailedVersion != INVALID_DATA_VERSION) {
	                            Log.d(TAG, "reload pause");
	                        }
	                        Utils.waitWithoutInterrupt(this);
	                        if (mActive && (mFailedVersion != INVALID_DATA_VERSION)) {
	                            Log.d(TAG, "reload resume");
	                        }
	                        continue;
	                    }
	                    mDirty = false;
	                }
	                updateLoading(true);
	                long version = mSource.reload();
	                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
	                updateComplete = info == null;
	                if (updateComplete) continue;
	                if (info.version != version) {
	                    info.size = mSource.getMediaItemCount();
	                    info.version = version;
	                }
	                if (info.reloadCount > 0) {
	                    info.items = mSource.getMediaItem(info.reloadStart, info.reloadCount);
	                }
	                executeAndWait(new UpdateContent(info));
	            }
	            updateLoading(false);
	        }

	        public synchronized void notifyDirty() {
	            mDirty = true;
	            notifyAll();
	        }

	        public synchronized void terminate() {
	            mActive = false;
	            notifyAll();
	        }
	    }

	public ConcurrentSkipListMap<Integer,Clustering.Cluster> getClusters(){
			if(mSource instanceof DataClusterSet){
				return ((DataClusterSet) mSource).getClusters();
			}
			return null;
	}
	
}
