package cn.nubia.media.Collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ArrayListTest {
    @Test
    public void testForEach(){
        ArrayList<Integer> arrayList = new ArrayList();
        LinkedList<Integer> linkList = new LinkedList();

        for(int i=0; i<500000; i++){
            arrayList.add(i);
            arrayList.add(i);
        }
        forEach(arrayList);
        forEach(linkList);
    }


    private <E> void forEach(List<? extends E> lists){
        System.out.println("Test started for: " +lists.getClass());

        long startTime = System.nanoTime();
        for(E e: lists){

        }
        long entTime = System.nanoTime();
        long totalTime = (entTime - startTime)/1000;
        System.out.println("5W entried forEach use " + totalTime + " ms");

        long startTime1 = System.nanoTime();
        for(int i=0; i<lists.size(); ++i){
            E e = lists.get(i);
        }
        long entTime1 = System.nanoTime();
        long totalTime1 = (entTime1- startTime1)/1000;
        System.out.println("5W entried frEach for get use " + totalTime1 + " ms");
    }
}
