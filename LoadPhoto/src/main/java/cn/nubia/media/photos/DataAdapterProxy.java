package cn.nubia.media.photos;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class DataAdapterProxy  implements DataLoader.LoadingListener{
	public final static String TAG = "DataAdapterProxy";
	private DataLoader mDataLoader;
	private ImageLoad mImageLoad;
	private ImageLoad.Listener mListener;
	private boolean bLoadFinished = true;
	private int mSize = -1;
	private boolean SUPPORT_DATE = false;

	public  DataAdapterProxy(AppContext app, ImageLoad.Listener listener) {
		Log.e(TAG, "DataFragment()");
		mDataLoader = new DataLoader(app, SUPPORT_DATE ? new DataClusterSet(app) : new MediaSet(app));
		if(mDataLoader != null){
			mDataLoader.setLoadingListener(this);
		}
		mListener = listener;
		mImageLoad = new ImageLoad(MediaItem.TYPE_THUMBNAIL,
				app, mDataLoader);
	}

	public void onPause() {
		if(mDataLoader != null){
			mDataLoader.pause();
		}
		if(mImageLoad != null){
			mImageLoad.setListener(null);
			mImageLoad.pause();
		}

	}
	public void onDestroyView() {
		Log.e(TAG, "onDestroyView()");
		if(mDataLoader != null){
			mDataLoader.setLoadingListener(null);
		}
	}

	public void onResume() {
		Log.e(TAG, "onResume()");
		mDataLoader.resume();
		if(mImageLoad != null){
			mImageLoad.setListener(mListener);
			mImageLoad.resume();

		}
	}

	//DataLoader********************************
	public MediaItem getMediaItem(int index) {
		if(mDataLoader != null){
			Map<Integer,Clustering.Cluster> maps =  mDataLoader.getClusters();
			if(maps != null){
				Clustering.Cluster c = maps.get(index);
				if(c == null){
					return mDataLoader.get(this.findImageIndex(index));
				}
			}else{
				return mDataLoader.get(index);
			}
		}
		return null;
	}
	
	public int size() {
		if(mDataLoader != null){
			Map<Integer,Clustering.Cluster> maps =  mDataLoader.getClusters();
			if(maps != null){
				return mDataLoader.size() + maps.size();
			}else {
				return mDataLoader.size();
			}
		}
		return -1;
    }

	public Object get(int index){
		if(mImageLoad != null){
			Map<Integer,Clustering.Cluster> maps =  mDataLoader.getClusters();
			if(maps != null){
				Clustering.Cluster c = maps.get(index);
				if(c != null){
					return c.getCaption();
				}else{
					return mImageLoad.get(this.findImageIndex(index));
				}
			}else{
				return mImageLoad.get(index);
			}
		}
		return null;
	}
	
	public void setActiveWindow(int start, int end) {
		 if(mImageLoad != null ){
			mImageLoad.setActiveWindow(start, end);
		}
	}

	//DataLoader.LoadingListener
	@Override
	public void onLoadingStarted() {
		
	}

	@Override
	public void onLoadingFinished(boolean loadingFailed) {
		Log.e(TAG, "loadFinished() size:"+size());
		bLoadFinished = true;
		mSize = size();
		int end = Math.min(mSize, 10);
		mImageLoad.setActiveWindow(0,end);
	}

	//TODO
	//0 text; 2:Image
	public int getType(int postion){
		if(mImageLoad != null){
			Map<Integer,Clustering.Cluster> maps =  mDataLoader.getClusters();
			if(maps != null){
				Clustering.Cluster c = maps.get(postion);
				if(c != null){
					return 0;
				}else{
					return 2;
				}
			}else{
				return 2;
			}
		}
		return -1;
	}

	private int findImageIndex(int postion){
		ConcurrentSkipListMap<Integer,Clustering.Cluster> maps =  mDataLoader.getClusters();
		if(maps != null){
			ConcurrentNavigableMap<Integer, Clustering.Cluster> lowmaps = maps.headMap(postion);
			return postion - lowmaps.size();
			/*Set<Integer> indexs = maps.keySet();
			for (int index:indexs) {
				if (index < postion) {
					count++;
				} else if (index == postion) {
					return postion;
				} else {
					return postion - count;
				}
			}*/
		}
		return postion;
	}

}
