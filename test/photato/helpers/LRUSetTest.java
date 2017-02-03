package photato.helpers;

import org.junit.Assert;
import org.junit.Test;

public class LRUSetTest {

    @Test
    public void testLRUSet() {
        LRUSet<Integer> lruSet = new LRUSet<>();

        // Empty
        Assert.assertNull(lruSet.removeLast());
        Assert.assertEquals(0, lruSet.size());
        Assert.assertEquals(0, lruSet.totalWeight());
        Assert.assertTrue(lruSet.values().isEmpty());

        // One element
        lruSet.add(1, 10);
        Assert.assertEquals(1, lruSet.size());
        Assert.assertEquals(10, lruSet.totalWeight());
        Assert.assertEquals(1, lruSet.values().size());
        Assert.assertEquals(1, lruSet.removeLast().intValue());
        Assert.assertEquals(0, lruSet.size());

        // 2 elements
        lruSet.add(1, 10);
        lruSet.add(2, 20);
        Assert.assertEquals(2, lruSet.size());
        Assert.assertEquals(30, lruSet.totalWeight());
        Assert.assertEquals(2, lruSet.values().size());
        Assert.assertEquals(1, lruSet.removeLast().intValue());
        Assert.assertEquals(2, lruSet.removeLast().intValue());
        Assert.assertEquals(0, lruSet.size());

        // 2 elements + ping
        lruSet.add(3, 30);
        lruSet.add(4, 40);
        lruSet.ping(3);
        Assert.assertEquals(2, lruSet.size());
        Assert.assertEquals(70, lruSet.totalWeight());
        Assert.assertEquals(2, lruSet.values().size());
        Assert.assertEquals(4, lruSet.removeLast().intValue());
        Assert.assertEquals(30, lruSet.totalWeight());
        Assert.assertEquals(3, lruSet.removeLast().intValue());
    }

}
