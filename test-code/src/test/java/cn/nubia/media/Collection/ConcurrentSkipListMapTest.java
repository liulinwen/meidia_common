package cn.nubia.media.Collection;

import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by xfs on 2018/6/13.
 */

public class ConcurrentSkipListMapTest {
    @Test
    public void test_headMap(){
        ConcurrentSkipListMap<Integer, Integer> maps = getIntegerIntegerConcurrentSkipListMap();

        ConcurrentNavigableMap<Integer, Integer> maps2 = maps.headMap(3);
        int size = maps2.size();
        assertEquals(size, 2);

        long startTime = System.nanoTime();
        ConcurrentNavigableMap<Integer, Integer> maps3 = maps.headMap(35);
        long entTime = System.nanoTime();
        long totalTime = (entTime - startTime) / 1000L;
        System.out.println("entried added and test_headMap in " + totalTime + " ms");

        int size_35 = maps3.size();
        assertEquals(size_35, 6);

        long startTime1 = System.nanoTime();
        int find_size_35 = this.findnear(maps, 35);
        long entTime1 = System.nanoTime();
        long totalTime1 = (entTime - startTime) / 1000L;
        System.out.println("entried added and test_findnear in " + totalTime1 + " ms");

        assertEquals(size_35, 35 - find_size_35);

        ConcurrentNavigableMap<Integer, Integer> maps4 = maps.headMap(0);
        int size_0 = maps4.size();
        assertEquals(size_0, 0);

   /*     long entTime = System.nanoTime();
        long totalTime = (entTime - startTime) / 1000000L;
        System.out.println("entried added and test_headMap in " + totalTime + " ms");*/
    }

    @Test
    public void test_lowKey(){
        ConcurrentSkipListMap<Integer, Integer> maps = getIntegerIntegerConcurrentSkipListMap();
        int key_3near = maps.lowerKey(3);
        assertEquals(key_3near, 2);
    }
    private ConcurrentSkipListMap<Integer, Integer> getIntegerIntegerConcurrentSkipListMap() {
        ConcurrentSkipListMap<Integer, Integer> maps = new ConcurrentSkipListMap<>();
      /*  for (int i = 0; i < 500000; i++) {

        }*/
        maps.put(1,1);
        maps.put(2,2);
        maps.put(24,24);
        maps.put(31, 31);
        maps.put(4, 4);
        maps.put(10, 10);
        return maps;
    }

    private int findnear(ConcurrentSkipListMap<Integer, Integer> maps, int postion){
        int count = 0;
        if(maps != null){
			Set<Integer> indexs = maps.keySet();
			for (int index:indexs) {
				if (index < postion) {
					count++;
				} else if (index == postion) {
					return postion;
				} else {
					return postion - count;
				}
			}
        }
        return postion - count;
    }
}
