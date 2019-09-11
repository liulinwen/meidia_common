package com.example.common;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by llw on 2017-7-6.
 */

public class MediaCodecListTest {

    public static class CodeInfo{
        @NonNull
        public static String MediCodeInfoToString(MediaCodecInfo info){
            StringBuilder sb = new StringBuilder();
           for(String type:info.getSupportedTypes()){
                if(type.startsWith("video") && info.isEncoder()) {
                    sb.append("----------------------------"+ "\n");
                    sb.append(info.getName()+ "\n");
                    sb.append("isEncode is: "+ info.isEncoder()+ "\n");
                    sb.append(type + "\n");
                    MediaCodecInfo.CodecCapabilities capabilitiesForType = info.getCapabilitiesForType(type);
                    @SuppressLint("NewApi") MediaFormat format = capabilitiesForType.getDefaultFormat();
                    sb.append(format.toString() + "\n");
                    for(int cf:capabilitiesForType.colorFormats){
                        sb.append(cf + ",");
                    }
                }
            }
            return sb.toString();
        }
    }

    public static void  PrintCodec(){
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            Log.i("MediaCodecListTest" ,"info is: "+ CodeInfo.MediCodeInfoToString(info));
        }
    }
}

