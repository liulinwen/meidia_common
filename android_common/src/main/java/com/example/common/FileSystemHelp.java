package com.example.common;

import java.io.File;

/**
 * Created by llw on 2017-02-09.
 */

public class FileSystemHelp {
    static {
        System.loadLibrary("Jnitest");//加载so文件，不要带上前缀lib和后缀.so
    }

    public static void scanDirectoriesWithJava(String directory){
        File fd = new File(directory);
        File[] files =  fd.listFiles();
        for(File f:files){
            if(f.isDirectory()){
                scanDirectoriesWithJava(f.getAbsolutePath());
            } else{
                //TODO
            }
        }
    }

    //public native static void scanDirectoriesWithCC(String directory);

}
