package cn.nubia.media.photos;

import android.database.Cursor;
import android.provider.MediaStore;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by llw on 2018/6/11.
 */

class Clustering {
    public class Cluster{
        private long mData;
        private int mSize;
        private String mCaption;
        public Cluster(long data, int size, String caption){
            mData = data;
            mSize = size;
            //mCaption = generateCaption(data);//TODO
            mCaption = caption;
        }

        public int getSize(){
            return mSize;
        }

        public long getData(){
            return mData;
        }

        public String getCaption(){
            return mCaption;
        }

        private String generateCaption(long data){
            //TODO data to string
            return String.valueOf(data);
        }

    }
    private  AppContext mAppContext;
    // this map is key sort
    private ConcurrentSkipListMap<Integer, Cluster> mClusters = new ConcurrentSkipListMap<Integer, Cluster>();
    private int mTotalCount;

    private static final String[] PROJECTION = {
            "date(datetaken/1000, 'unixepoch', 'localtime')",
            "count(*)",
            MediaStore.Images.ImageColumns.DATE_TAKEN
    };
    private static final String GROUP_BY = ") GROUP BY date(datetaken/1000, 'unixepoch', 'localtime'";
    private static final String ORDER_BY = "datetaken DESC";

    public Clustering(AppContext app){
        mAppContext = app;
    }

    public void run(){
        mClusters.clear();
        mTotalCount = 0;
        StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Images.Media.DATA + " != ''").append(GROUP_BY);

        Cursor c = mAppContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                selection.toString(),
                null,
                ORDER_BY);

        if(c != null){
            try {
                while (c.moveToNext()) {
                    String dataString = c.getString(0);
                    int count = c.getInt(1);
                    long data = c.getLong(2);
                    if (dataString == null) {
                        continue;
                    }
                    mClusters.put(mTotalCount+mClusters.size(),
                            new Cluster(data, count, dataString));
                    mTotalCount += count;
                }
            }finally {
                if(c != null){
                    c.close();
                }
            }
        }
    }

    public int getTotalCount(){
        return mTotalCount;
    }

    public ConcurrentSkipListMap<Integer,Cluster> getClusters(){
        return mClusters;
    }


}
