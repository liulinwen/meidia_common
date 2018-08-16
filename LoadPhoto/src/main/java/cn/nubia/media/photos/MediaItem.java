package cn.nubia.media.photos;

import java.io.FileNotFoundException;
import java.io.IOException;

import cn.nubia.media.photos.cache.BytesBufferPool;
import cn.nubia.media.photos.cache.ImageCacheRequest;
import cn.nubia.media.photos.common.DecodeUtils;
import cn.nubia.media.photos.workthread.ThreadPool.Job;
import cn.nubia.media.photos.workthread.ThreadPool.JobContext;

import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MediaItem extends MediaObject{
    public static final int TYPE_MICROTHUMBNAIL = 1;
    public static final int TYPE_THUMBNAIL = 2;
    
	private static final int INDEX_ID = 0;   
	private static final int INDEX_TITLE = 1;   
	private static final int INDEX_DATA = 2;   
	private static final int INDEX_MIME_TYPE = 3;
	    
	public final static String[] PROJECTION = new String[] {
			Images.Media._ID,
			Images.Media.TITLE,
			Images.Media.DATA,
			Images.Media.MIME_TYPE,
    };
	
	private AppContext mApp;
	private String mStrTitle;
	private int mId;
	private String mStrType;
	private String mStrUrl;
	private Path mPath;
    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 200 * 1024;
    private static final BytesBufferPool sMicroThumbBufferPool =
            new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);
    
	public MediaItem(Path path, AppContext application, Cursor cursor){
		mApp = application;
		path.setObject(this);
		mPath = path;
		mDataVersion = nextVersionNumber();
		loadFormCurosr(cursor);
	}
	
	public MediaItem(AppContext application){
		mApp = application;
	}

	public Path getPath() {
	    return mPath;
	}
	
	private void loadFormCurosr(Cursor cursor){
		mId = cursor.getInt(INDEX_ID);
		mStrTitle = cursor.getString(INDEX_TITLE);
		mStrUrl = cursor.getString(INDEX_DATA);
		mStrType = cursor.getString(INDEX_MIME_TYPE);
	}
	
	 
	public void updateContent(Cursor cursor){
		if (updateFromCursor(cursor)) {
	         mDataVersion = nextVersionNumber();
	    }
	}
	
	private boolean updateFromCursor(Cursor cursor){
		 UpdateHelper uh = new UpdateHelper();
		 mId = uh.update(mId, cursor.getInt(INDEX_ID));
		 mStrTitle = uh.update(mStrTitle, cursor.getString(INDEX_TITLE));
		 mStrUrl = uh.update(mStrUrl, cursor.getString(INDEX_DATA));
		 mStrType = uh.update(mStrType, cursor.getString(INDEX_MIME_TYPE));
		 return uh.isUpdated();
	}
	
	public String getTitle(){
		return mStrTitle;
	}

	public Uri getUri(){
		 Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
	     return baseUri.buildUpon().appendPath(String.valueOf(mId)).build();
	}

	public String getType(){
		return mStrType;
	}

	public String getUrl(){
		return mStrUrl;
	}

	public void delete(){
		mApp.getContentResolver().delete(getUri(), null, null);
	}

	
	public Job<Bitmap> requestImage(AppContext application, int type) {
	       return new LocalImageRequest(application, type, mStrUrl);
	}
	
	 public static class LocalImageRequest extends ImageCacheRequest {
	        private String mLocalFilePath;

	        LocalImageRequest(AppContext application, int type,
							  String localFilePath) {
	            super(application, localFilePath, type, 100);
	            mLocalFilePath = localFilePath;
	        }

	        @Override
	        public Bitmap onDecodeOriginal(JobContext jc, final int type) {
	            BitmapFactory.Options options = new BitmapFactory.Options();
	            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				int targetSize = 100;
				if(type == TYPE_THUMBNAIL){
					targetSize = 300;
				}
				Bitmap bitmap = null;
				// try to decode from JPEG EXIF
				if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
					byte[] thumbData = null;
					try {
						ExifInterface exif = new ExifInterface(mLocalFilePath);
						thumbData = exif.getThumbnail();
					} catch (FileNotFoundException e) {
						Log.w(ImageCacheRequest.TAG, "failed to find file to read thumbnail: " + mLocalFilePath);
					} catch (IOException e) {
						Log.w(ImageCacheRequest.TAG, "failed to get thumbnail from: " + mLocalFilePath);
					}
					if (thumbData != null) {
						bitmap = DecodeUtils.decodeIfBigEnough(
								jc, thumbData, options, targetSize);
						if (bitmap != null) return bitmap;
					}
				}

				return DecodeUtils.decodeThumbnail(jc, mLocalFilePath, options, targetSize, type);
	        }
	    }
	 
	public static BytesBufferPool getBytesBufferPool() {
        return sMicroThumbBufferPool;
    }
}
