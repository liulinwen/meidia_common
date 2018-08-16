package cn.nubia.media.photos;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by llw on 2018/6/11.
 */

class DataClusterSet extends MediaSet {
    private Clustering mClusering;
    public DataClusterSet(AppContext app){
        super(app);
        mClusering = new Clustering(app);
    }

    @Override
    public long reload() {
        if (mChangeNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
            mClusering.run();
        }
        return mDataVersion;
    }

    @Override
    public int getMediaItemCount() {
        return mClusering.getTotalCount();
    }

    public ConcurrentSkipListMap<Integer,Clustering.Cluster> getClusters(){
        return mClusering.getClusters();
    }

}
