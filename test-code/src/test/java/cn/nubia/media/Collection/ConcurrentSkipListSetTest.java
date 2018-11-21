package cn.nubia.media.Collection;


import java.util.concurrent.ConcurrentSkipListSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ConcurrentSkipListSetTest {

    @Test
    public void test_lowKey(){
        ConcurrentSkipListSet<Integer> sets = getIntegerIntegerConcurrentSkipSet();
        int key_3near = sets.lower(3);
        assertEquals(key_3near, 2);
    }

    private ConcurrentSkipListSet<Integer> getIntegerIntegerConcurrentSkipSet() {
        ConcurrentSkipListSet<Integer> sets = new ConcurrentSkipListSet<>();
        sets.add(1);
        sets.add(2);
        sets.add(24);
        sets.add(31);
        sets.add(4);
        sets.add(10);
        return sets;
    }
}
