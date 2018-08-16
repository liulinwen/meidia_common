package cn.nubia.media.photos;

import cn.nubia.media.photos.common.Utils;

class UpdateHelper {
	 private boolean mUpdated = false;

	    public int update(int original, int update) {
	        if (original != update) {
	            mUpdated = true;
	            original = update;
	        }
	        return original;
	    }

	    public long update(long original, long update) {
	        if (original != update) {
	            mUpdated = true;
	            original = update;
	        }
	        return original;
	    }

	    public double update(double original, double update) {
	        if (original != update) {
	            mUpdated = true;
	            original = update;
	        }
	        return original;
	    }

	    public <T> T update(T original, T update) {
	        if (!Utils.equals(original, update)) {
	            mUpdated = true;
	            original = update;
	        }
	        return original;
	    }

	    public boolean isUpdated() {
	        return mUpdated;
	    }
}
