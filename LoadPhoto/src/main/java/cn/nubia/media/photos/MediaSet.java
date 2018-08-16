package cn.nubia.media.photos;

import java.util.ArrayList;
import java.util.WeakHashMap;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Log;

class MediaSet extends MediaObject{
	public interface ContentListener {
	    public void onContentDirty();
	}
	protected int mCachedCount;
	private AppContext mApp;
	private Uri mBaseUri;
	private String mWhereClause;
	private String mSortOrder;
	private Path mPath;
	protected static final int INVALID_COUNT = -1;
	private static final String[] COUNT_PROJECTION = { "count(*)" };
	private static final String TAG = "MediaSet";
    public static final Object LOCK = new Object();
    private WeakHashMap<ContentListener, Object> mListeners =
            new WeakHashMap<ContentListener, Object>();
	protected ChangeNotifier mChangeNotifier;
    
	public MediaSet(AppContext app){
		mApp = app;
		mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
		mWhereClause = Images.Media.DATA + " like '%DCIM/Camera/%'";
		mSortOrder = Images.Media.DATE_TAKEN +" DESC, "
     			+ Images.Media._ID + " DESC";
		mPath = Path.fromString("/local/all/");
		mChangeNotifier = new ChangeNotifier(this, Images.Media.EXTERNAL_CONTENT_URI, mApp);
	}
	public long reload() {
        if (mChangeNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }
	
	 public int getMediaItemCount() {
	        if (mCachedCount == INVALID_COUNT) {
	        	
	            Cursor cursor = mApp.getContentResolver().query(
	                    mBaseUri, COUNT_PROJECTION, mWhereClause,
	                    null, null);
	            if (cursor == null) {
	                Log.w(TAG, "query fail");
	                return 0;
	            }
	            try {
	                cursor.moveToNext();
	                mCachedCount = cursor.getInt(0);
	            } finally {
	                cursor.close();
	            }
	        }
	        return mCachedCount;
	}
	
	private MediaItem getCoverMediaItem() {
	    ArrayList<MediaItem> items = getMediaItem(0, 1);
	    if (items.size() > 0) return items.get(0);
	    return null;
	}
	 
	public ArrayList<MediaItem> getMediaItem(int start, int count) {
	      Uri uri = mBaseUri.buildUpon()
	                .appendQueryParameter("limit", start + "," + count).build();
	      ArrayList<MediaItem> list = new ArrayList<MediaItem>();
	      Cursor cursor = mApp.getContentResolver().query(
	                uri, MediaItem.PROJECTION, mWhereClause,
	                null, mSortOrder);
	      if (cursor == null) {
	          Log.w(TAG, "query fail: " + uri);
	          return list;
	      }

	      try {
	          while (cursor.moveToNext()) {
	              MediaItem item = loadOrUpdateItem(cursor, mApp);
	              list.add(item);
	          }
	      } finally {
	          cursor.close();
	      }
	      return list;
	}
	
	private MediaItem loadOrUpdateItem(Cursor cursor, AppContext app) {
        synchronized (LOCK) {
        	int id = cursor.getInt(0);  // _id must be in the first column
	        Path childPath = mPath.getChild(id);
            MediaItem item = childPath.getObject();
            if (item == null) {
               item = new MediaItem(childPath, app, cursor);
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }
	
	public void notifyContentChanged() {
        for (ContentListener listener : mListeners.keySet()) {
            listener.onContentDirty();
        }
    }
	
	public void addContentListener(ContentListener listener) {
	    mListeners.put(listener, null);
	}

	public void removeContentListener(ContentListener listener) {
	    mListeners.remove(listener);
    }
}
