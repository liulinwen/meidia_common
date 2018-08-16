package cn.nubia.media.photos;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

class Path {
	 private WeakReference<MediaItem> mObject;
	 private static Path sRoot = new Path(null, "ROOT");

	 private final Path mParent;
	 private final String mSegment;
	 private IdentityCache<String, Path> mChildren;
	 
	 private Path(Path parent, String segment) {
	        mParent = parent;
	        mSegment = segment;
	 }
	 
	 public static Path fromString(String s) {
	        synchronized (Path.class) {
	            String[] segments = split(s);
	            Path current = sRoot;
	            for (int i = 0; i < segments.length; i++) {
	                current = current.getChild(segments[i]);
	            }
	            return current;
	        }
	 }
	 
	 private static String[] split(String s) {
	        int n = s.length();
	        if (n == 0) return new String[0];
	        if (s.charAt(0) != '/') {
	            throw new RuntimeException("malformed path:" + s);
	        }
	        ArrayList<String> segments = new ArrayList<String>();
	        int i = 1;
	        while (i < n) {
	            int brace = 0;
	            int j;
	            for (j = i; j < n; j++) {
	                char c = s.charAt(j);
	                if (c == '{') ++brace;
	                else if (c == '}') --brace;
	                else if (brace == 0 && c == '/') break;
	            }
	            if (brace != 0) {
	                throw new RuntimeException("unbalanced brace in path:" + s);
	            }
	            segments.add(s.substring(i, j));
	            i = j + 1;
	        }
	        String[] result = new String[segments.size()];
	        segments.toArray(result);
	        return result;
	 }
	 
	 private Path getChild(String segment) {
	        synchronized (Path.class) {
	            if (mChildren == null) {
	                mChildren = new IdentityCache<String, Path>();
	            } else {
	                Path p = mChildren.get(segment);
	                if (p != null) return p;
	            }

	            Path p = new Path(this, segment);
	            mChildren.put(segment, p);
	            return p;
	        }
	 }
	 
	 public Path getChild(int segment) {
	     return getChild(String.valueOf(segment));
	 }
	 
	 public void setObject(MediaItem object) {
	      synchronized (Path.class) {
	            //Utils.assertTrue(mObject == null || mObject.get() == null);
	            if(mObject != null && mObject.get() != null){
	            	mObject.clear();
	            }
	            mObject = new WeakReference<MediaItem>(object);
	      }
	 }

	 public MediaItem getObject() {
	    synchronized (Path.class) {
	            return (mObject == null) ? null : mObject.get();
	    }
	 }
}
