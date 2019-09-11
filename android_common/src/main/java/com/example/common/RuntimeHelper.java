package com.example.common;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by llw on 2017-7-13.
 */

public class RuntimeHelper {
    public static Process exec(ArrayList<String> lists){
        Process p = null;
        try {
           p = Runtime.getRuntime().exec(lists.toArray(new String[]{}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static Process exec(String str){
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static void setprop(String str){
        Process p = exec(str);
    }

    @NonNull
    public static String getprop(String str){
        Process p = exec(str);
        InputStreamReader read = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(read);
        StringBuilder result = new StringBuilder();
        String line=null;
        //直到读完为止
        try {
            while((line = br.readLine())!=null)
            {
               result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

}
