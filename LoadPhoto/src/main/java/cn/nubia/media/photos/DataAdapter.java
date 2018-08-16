package cn.nubia.media.photos;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
//1.new AppContext use Application Context
//2.new DataAdapter

public abstract class DataAdapter extends BaseAdapter implements ImageLoad.Listener{

	protected AppContext mAppContext;
	protected DataAdapterProxy mDataProxy;
	
	public DataAdapter(AppContext app){
		mDataProxy = new DataAdapterProxy(app, this);
		mAppContext = app;
	}

	public void onResume(){
		mDataProxy.onResume();
	}

	public void onPause(){
		mDataProxy.onPause();
	}

	public int getCount() {
		if(mDataProxy == null) return 0;
	       return mDataProxy.size();
	 }
	 //0 text; 1:Image
	public int getType(int postion){
		return 1;
	}
	 public Object getItem(int position) {
		if(mDataProxy == null) return null;
	    return mDataProxy.get(position);
	 }

	 public long getItemId(int position) {
	        return position;
	 }


	// ListView onScroll
	public void setActiveWindow(int start, int end) {
		if(mDataProxy != null ){
			mDataProxy.setActiveWindow(start, end);
		}
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

	//ImageLoad load finished,need update view
	@Override
	public void onSizeChanged(int size){
		this.notifyDataSetChanged();
	}

	@Override
	public void onImageChanged(){
		this.notifyDataSetChanged();
	}



}
