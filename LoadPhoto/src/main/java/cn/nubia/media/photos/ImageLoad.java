package cn.nubia.media.photos;

import cn.nubia.media.photos.common.Utils;
import cn.nubia.media.photos.workthread.Future;
import cn.nubia.media.photos.workthread.FutureListener;
import cn.nubia.media.photos.workthread.ThreadPool;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageLoad implements DataLoader.DataListener{
	public interface Listener {

		public void onSizeChanged(int size);
	    public void onImageChanged();
	}
	
	private static final int MSG_UPDATE_ENTRY = 0;
    private static final int DATA_CACHE_SIZE = 80;
	private static final String TAG = "ImageLoad";
    
	private AppContext mApp;
	private DataLoader mSource;
	private final BitmapLoader[] mData;
	
	private int mActiveStart = 0;
	private int mActiveEnd = 0;

	private int mContentStart = 0;
	private int mContentEnd = 0;
	private int mSize = 0;

	private ThreadPool mThreadPool;
	private Handler mHandler;
	private int mActiveRequestCount = 0;
	private boolean mIsActive = false;
	private Listener mListener;
    private int mImageType;
	public ImageLoad(int type, AppContext app, DataLoader data){
		mApp = app;
		mImageType = type;
		mSource = data;
		mSource.setDataListener(this);
		mData = new BitmapLoader[DATA_CACHE_SIZE];
		mSize = mSource.size();
		mThreadPool = mApp.getThreadPool();
		mHandler = new Handler(app.getMainLooper()) {
	            @Override
	            public void handleMessage(Message message) {
	                Utils.assertTrue(message.what == MSG_UPDATE_ENTRY);
	                ((ThumbnailLoader) message.obj).updateEntry();
	            }
	        };
	        
	}

	
	public void resume() {
	     mIsActive = true;
	     Log.i(TAG, "resume mContentStart is:"+ mContentStart+ 
		    		  ", mContentEnd is: "+ mContentEnd);
	     for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
	         prepareSlotContent(i);
	     }
	     updateAllImageRequests();
	}

	public void pause() {
		Log.i(TAG, "pause mContentStart is:"+ mContentStart+ 
	    		  ", mContentEnd is: "+ mContentEnd);
	     mIsActive = false;
	     for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
	          freeSlotContent(i);
	     }
	}
	public void setActiveWindow(int start, int end) {
		  if(!mIsActive){
			  return;
		  }
		  if (!(start <= end && end - start <= mData.length && end <= mSize)) {
	            Utils.fail("%s, %s, %s, %s", start, end, mData.length, mSize);
	      }
	      BitmapLoader data[] = mData;

	      mActiveStart = start;
	      mActiveEnd = end;
	      Log.i(TAG, "setActiveWindow mActiveStart is:"+ mActiveStart+
	    	  ", mActiveEnd is: "+ mActiveEnd);
	      int contentStart = Utils.clamp((start + end) / 2 - data.length / 2,
	                0, Math.max(0, mSize - data.length));
	      int contentEnd = Math.min(contentStart + data.length, mSize);
	      setContentWindow(contentStart, contentEnd);
	      if (mIsActive) updateAllImageRequests();
	}
	
	private void setContentWindow(int contentStart, int contentEnd) {
		if (contentStart == mContentStart && contentEnd == mContentEnd) return;

        if (!mIsActive) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
            mSource.setActiveWindow(contentStart, contentEnd);
            return;
        }

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeSlotContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart, n = mContentStart; i < n; ++i) {
                prepareSlotContent(i);
            }
            for (int i = mContentEnd; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        }
        mContentStart = contentStart;
        mContentEnd = contentEnd;
        Log.i(TAG, "setContentWindow mContentStart is:"+ mContentStart+ 
	    		  ", mContentEnd is: "+ mContentEnd);
    }
	

	public Bitmap get(int index){
		BitmapLoader loader = mData[index % mData.length];
		if(loader!= null){
			return loader.getBitmap();
		}
		return null;
	}
	
	private boolean isActive(int index) {
	     return index >= mActiveStart && index <= mActiveEnd;
	}
	
	private void prepareSlotContent(int slotIndex) {
		Log.i(TAG, "prepareSlotContent slotIndex is:"+ slotIndex);
		MediaItem item = mSource.get(slotIndex);
		if(item != null){
			BitmapLoader entry = new ThumbnailLoader(slotIndex, mImageType);
			mData[slotIndex % mData.length] = entry;
		}
    }

	
	private void freeSlotContent(int slotIndex) {
		Log.i(TAG, "freeSlotContent slotIndex is:"+ slotIndex);
		 BitmapLoader data[] = mData;
	     int index = slotIndex % data.length;
	     BitmapLoader entry = data[index];
	     if(entry != null){
	    	 entry.recycle();
	    	 data[index] = null;
	     }
	}
    
    private class ThumbnailLoader extends BitmapLoader  {
        private final int mIndex;
        private final int mType;

        public ThumbnailLoader(int index, int type) {
        	mIndex = index;
            mType = type;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
        	
        	MediaItem item = mSource.get(mIndex);
        	//Log.i(TAG, "submitBitmapTask mIndex is: "+mIndex+ ", item is: "+ item.getUrl());
            return mThreadPool.submit(
            		item.requestImage(mApp, mType), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY, this).sendToTarget();
        }

        public void updateEntry() {
            Bitmap bitmap = getBitmap();
           // if (bitmap == null) return; // error or recycled
            if (isActive(mIndex)) {
            	//Log.e(TAG, "updateEntry mIndex is: "+mIndex);
                if (mListener != null) mListener.onImageChanged();
            }
            --mActiveRequestCount;
            if (mActiveRequestCount == 0) {
            	//mListener.onContentChanged(mIndex, bitmap);
            	requestNonactiveImages();
            }
        }
    }
    
    private void updateAllImageRequests() {
        mActiveRequestCount = 0;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            if (requestSlotImage(i)) ++mActiveRequestCount;
        }
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }
    
 // We would like to request non active slots in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
            requestSlotImage(mActiveEnd + i);
            requestSlotImage(mActiveStart - 1 - i);
        }
    }
    
    // return whether the request is in progress or not
    private boolean requestSlotImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return false;
        BitmapLoader entry = mData[slotIndex % mData.length];
        if (entry == null) return false;
        entry.startLoad();
        return entry.isRequestInProgress();
    }
    
    private void cancelNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
            cancelSlotImage(mActiveEnd + i);
            cancelSlotImage(mActiveStart - 1 - i);
        }
    }
    
    private void cancelSlotImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return;
        BitmapLoader item = mData[slotIndex % mData.length];
        if (item != null) item.cancelLoad();
    }
    
    public void setListener(Listener listener) {
    	if(mListener != listener){
    		mListener = listener;
    	}
    }

	@Override
	public void onSizeChanged(int size) {
		Log.e(TAG, "ImageLoad onSizeChanged() size:"+size+ " mSize"+mSize);
		if (mSize != size) {
            mSize = size;
            if (mListener != null) mListener.onSizeChanged(mSize);
            if (mContentEnd > mSize) mContentEnd = mSize;
            if (mActiveEnd > mSize) mActiveEnd = mSize;
        }
	}

	@Override
	public void onContentChanged(int index) {
		if (index >= mContentStart && index < mContentEnd && mIsActive) {
	          freeSlotContent(index);
	          prepareSlotContent(index);
	          updateAllImageRequests();
	          if (mListener != null && isActive(index)) {
	                mListener.onImageChanged();
	          }
	     }
		
	}
}
