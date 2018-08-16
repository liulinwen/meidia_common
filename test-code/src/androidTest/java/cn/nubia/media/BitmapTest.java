package cn.nubia.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class BitmapTest {
    @Test
    public void testCreate() throws FileNotFoundException {
        long startTime = System.nanoTime();
        Bitmap b = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+ "/test.jpg");
        long entTime = System.nanoTime();
        long totalTime = (entTime - startTime) / 1000L;
        Log.i("BitmapTest","decodeFile use " + totalTime + " ms");

        long startTime1 = System.nanoTime();
        BitmapFactory.decodeStream(new FileInputStream(Environment.getExternalStorageDirectory()+ "/test.jpg"));
        long entTime1 = System.nanoTime();
        long totalTime1 = (entTime1 - startTime1) / 1000L;
        System.out.println("decodeStream_FileInputStream use " + totalTime1 + " ms");
    }

    @Test
    public void intto(){

        ArrayList<Integer> str = toBinary(257);

        ArrayList<Integer> str1 = toBinary(2);

        ArrayList<Integer> str2  = toBinary(16777216);

        ArrayList<Integer> str3  = toBinary(8);
    }


    public  ArrayList<Integer> toBinary(int i) {
        return toUnsignedString0(i, 1);
    }

    private  ArrayList<Integer> toUnsignedString0(int val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        ArrayList<Integer> buf = new ArrayList<Integer>();
        formatUnsignedInt(val, shift, buf, 0, chars);
        return buf;

    }

     void formatUnsignedInt(int val, int shift, ArrayList<Integer> buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            if( (val & mask) == 1 ){
                buf.add(len - (offset + charPos)) ;
            }
            val >>>= shift;
        } while (val != 0 && charPos-- > 0);
    }

}
