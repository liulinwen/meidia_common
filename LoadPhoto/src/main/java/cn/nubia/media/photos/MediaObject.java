package cn.nubia.media.photos;

class MediaObject {
	private static long sVersionSerial = 0;
	protected long mDataVersion;
	
	public static synchronized long nextVersionNumber() {
        return ++sVersionSerial;
    }
	public long getDataVersion() {
	    return mDataVersion;
	}
	
}
