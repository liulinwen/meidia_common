package cn.nubia.media.generic;

import org.junit.Test;

import java.util.ArrayList;

public class Generic {
    @Test
    public void test(){

    }

    private <E>void read(ArrayList<? extends E> lists){
        E t = lists.get(0);

    }

    private void write(ArrayList<? super Number> lists){
        lists.add(123);
       // Number b = lists.get(0);
    }
}
