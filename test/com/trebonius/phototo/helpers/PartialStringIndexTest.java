package com.trebonius.phototo.helpers;

import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

public class PartialStringIndexTest {

    private static class StringContainer {

        public final String value;

        public StringContainer(String value) {
            this.value = value;
        }

    }

    @Test
    public void testContains() {
        PartialStringIndex<Object> myMap = new PartialStringIndex<>();

        StringContainer toti = new StringContainer("toti");
        StringContainer tota = new StringContainer("tota");
        StringContainer tata = new StringContainer("tata");
        StringContainer toto = new StringContainer("toto");
        StringContainer tototo = new StringContainer("tototo");
        StringContainer tututo = new StringContainer("tututo");
        StringContainer tututo2 = new StringContainer("tututo2");
        
        StringContainer multiple = new StringContainer("****");

        for (int i = 0; i < 2; i++) { // To check proper reinitialisation of the map
            myMap.add("toti", toti);
            myMap.add("tota", tota);
            myMap.add("tata", tata);
            myMap.add("toto", toto);
            myMap.add("tototo", tototo);
            myMap.add("tututo", tututo);
            myMap.add("tututo", tututo2);

            Collection<Object> res = myMap.findContains("to");
            Assert.assertEquals(6, res.size());
            Assert.assertTrue(res.contains(toti));
            Assert.assertTrue(res.contains(tota));
            Assert.assertTrue(res.contains(toto));
            Assert.assertTrue(res.contains(tototo));
            Assert.assertTrue(res.contains(tututo));
            Assert.assertTrue(res.contains(tututo2));

            res = myMap.findContains("toto");
            Assert.assertEquals(2, res.size());
            Assert.assertTrue(res.contains(toto));
            Assert.assertTrue(res.contains(tototo));

            res = myMap.findContains("ttoto");
            Assert.assertEquals(0, res.size());
            res = myMap.findContains("kkk");
            Assert.assertEquals(0, res.size());

            res = myMap.findContains("tututo");
            Assert.assertEquals(2, res.size());
            Assert.assertTrue(res.contains(tututo));
            Assert.assertTrue(res.contains(tututo2));

            res = myMap.findContains("tota");
            Assert.assertEquals(1, res.size());
            Assert.assertTrue(res.contains(tota));

            myMap.remove(tututo2);
            res = myMap.findContains("tututo");
            Assert.assertEquals(1, res.size());
            Assert.assertTrue(res.contains(tututo));
            res = myMap.findContains("to");
            Assert.assertTrue(res.contains(tututo));
            Assert.assertFalse(res.contains(tututo2));

            res = myMap.findContains("ot");
            Assert.assertEquals(4, res.size());

            myMap.remove(tututo);
            Assert.assertEquals(0, myMap.findContains("tututo").size());
            res = myMap.findContains("to");
            Assert.assertFalse(res.contains(tututo));
            Assert.assertFalse(res.contains(tututo2));
            Assert.assertFalse(res.contains(tata));

            res = myMap.values();
            Assert.assertEquals(5, res.size());
            Assert.assertEquals(5, myMap.size());
            Assert.assertTrue(res.contains(toti));
            Assert.assertTrue(res.contains(tota));
            Assert.assertTrue(res.contains(tata));
            Assert.assertTrue(res.contains(toto));
            Assert.assertTrue(res.contains(tototo));
            
            myMap.add("mult", multiple);
            myMap.add("add", multiple);
            Assert.assertEquals(1, myMap.findContains("mult").size());
            Assert.assertEquals(1, myMap.findContains("add").size());
            myMap.remove(multiple);
            Assert.assertEquals(0, myMap.findContains("mult").size());
            Assert.assertEquals(0, myMap.findContains("add").size());
            
            
            myMap.clear();
            Assert.assertEquals(0, myMap.values().size());
            Assert.assertEquals(0, myMap.size());
            res = myMap.findContains("to");
            Assert.assertEquals(0, res.size());
        }
    }

    @Test
    public void testPrefix() {
        PartialStringIndex<Object> myMap = new PartialStringIndex<>(true);

        StringContainer toti = new StringContainer("toti");
        StringContainer tota = new StringContainer("tota");
        StringContainer tata = new StringContainer("tata");
        StringContainer toto = new StringContainer("toto");
        StringContainer tototo = new StringContainer("tototo");
        StringContainer tututo = new StringContainer("tututo");
        StringContainer tututo2 = new StringContainer("tututo2");

        myMap.add("toti", toti);
        myMap.add("tota", tota);
        myMap.add("tata", tata);
        myMap.add("toto", toto);
        myMap.add("tototo", tototo);
        myMap.add("tututo", tututo);
        myMap.add("tututo", tututo2);

        Collection<Object> res = myMap.findContains("to");
        Assert.assertEquals(4, res.size());
        Assert.assertTrue(res.contains(toti));
        Assert.assertTrue(res.contains(tota));
        Assert.assertTrue(res.contains(toto));
        Assert.assertTrue(res.contains(tototo));

        res = myMap.findContains("toto");
        Assert.assertEquals(2, res.size());
        Assert.assertTrue(res.contains(toto));
        Assert.assertTrue(res.contains(tototo));

        res = myMap.findContains("ttoto");
        Assert.assertEquals(0, res.size());
        res = myMap.findContains("kkk");
        Assert.assertEquals(0, res.size());

    }

}
