package cn.nubia.media;

import org.junit.Test;

/**
 * Created by xfs on 2018/6/15.
 */

public class NoMeidaFileTest {
    @Test
    public void test(){
        boolean b = isNoMediaFile("/storage/emulated/0/tencent/MobileQQ/photo/._1335732279_IMG20180615135706_1529042231000_wifi_0.jpg");
        boolean b2 = isNoMediaFile("/storage/emulated/0/tencent/MobileQQ/photo/_-1791634135_1529043203042_1529043203000_wifi_0.jpg");
        int i = 9;
    }

    private boolean isNoMediaFile(String path){
        int lastSlash = path.lastIndexOf('/');
        if(lastSlash > 0 && lastSlash + 2 < path.length()){
            if(path.regionMatches(lastSlash+1, "._", 0, 2)){
                return true;
            }
        }
        return false;
    }
}
