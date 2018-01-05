package com.mastercard.gateway.android.sdk;


import com.google.gson.Gson;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GatewayMapTest {

    Gson gson = new Gson();

    @Test
    public void testSimpleOperations() {

        GatewayMap map = new GatewayMap();

        assert map.size() == 0;

        assertFalse(map.containsKey("one"));
        assertFalse(map.containsKey("two"));

        map.put("one", 1);

        assert map.size() == 1;
        assertEquals(map.get("one"), 1);
        assertTrue(map.containsKey("one"));
        assertFalse(map.containsKey("two"));

        map.put("two", 2);

        assert map.size() == 2;
        assertEquals(map.get("two"), 2);
        assertEquals(map.get("one"), 1);
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("two"));

        for (int i = 1; i <= 1000; i++) {
            map.put("i" + i, i);
            assert map.size() == 2 + i;
            assertEquals(map.get("i" + i), i);
        }

        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("two"));

        assert map.size() == 1002;
        map.remove("one");
        assert map.size() == 1001;
        assert map.get("one") == null;
        assertFalse(map.containsKey("one"));
        assertTrue(map.containsKey("two"));

        map.remove("two");
        assert map.size() == 1000;
        assert map.get("two") == null;
        assertFalse(map.containsKey("one"));
        assertFalse(map.containsKey("two"));

        for (int i = 1; i <= 1000; i++) {
            map.remove("i" + i);
            assert map.size() == 1000 - i;
            assert map.get("i" + i) == null;
        }

        assert map.size() == 0;
        assertFalse(map.containsKey("one"));
        assertFalse(map.containsKey("two"));

    }

    @Test
    public void testDotNotation() {

        GatewayMap map = new GatewayMap();

        assert map.size() == 0;
        assertFalse(map.containsKey("one.two"));

        map.put("one.two", 12);
        assert map.size() == 1;

        assertEquals(map.get("one.two"), 12);
        assertTrue(map.containsKey("one.two"));


        Object o = map.get("one");
        assert o instanceof Map;

        Map m = (Map) o;
        assert m.size() == 1;
        assert m.containsKey("two");
        assertEquals(m.get("two"), 12);

        assertEquals(map.toString(), "{one={two=12}}");

        map.put("one.four", 14);
        map.put("one.six", 16);

        assert map.containsKey("one.four");
        assert map.containsKey("one.six");

        map.put("k1.k2.k3.k4.k5.k6", 123456);
        assert map.containsKey("k1.k2.k3.k4.k5.k6");
        assertEquals(map.get("k1.k2.k3.k4.k5.k6"), 123456);


        map.put("k1.k2.k3.k4.k5.k6", 654321);
        assert map.containsKey("k1.k2.k3.k4.k5.k6");
        assertEquals(map.get("k1.k2.k3.k4.k5.k6"), 654321);

    }

    @Test
    public void cannotOverrideNestedMap() {

        GatewayMap map = new GatewayMap();
        map.put("a", 1);

        try {
            map.put("a.b", 2);
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Exception " + e + " thrown");
        }

    }

    @Test
    public void allConstructorsWork() {


        testMap(new GatewayMap(), 0);
        testMap(new GatewayMap(10, 1.4f), 0);
        testMap(new GatewayMap(20), 0);

        Map map = new HashMap();
        map.put("k1", 9);
        map.put("k9", 99);
        GatewayMap pm = new GatewayMap(map);
        assert pm.size() == 2;
        assertEquals(pm.get("k1"), 9);
        assertEquals(pm.get("k9"), 99);
        testMap(pm, 2);
        assertEquals(pm.get("k1"), 1);
        assertEquals(pm.get("k9"), 99);


    }

    private void testMap(GatewayMap m, int size) {
        assert m.size() == size;
        m.put("k1", 1);
        m.put("k2.k3", 2);
        m.put("k4.k5.k6", 3);

        assertEquals(m.get("k1"), 1);
        assertEquals(m.get("k2.k3"), 2);
        assertEquals(m.get("k4.k5.k6"), 3);

        assert m.containsKey("k1");
        assert m.containsKey("k2.k3");
        assert m.containsKey("k4.k5.k6");
    }


    @Test
    public void testJsonConstructorWithMap() {

        String json = "{\n" +
                "                 \"k1\":\"v1\",\n" +
                "                 \"k2\":{\n" +
                "                          \"k21\": \"v21\",\n" +
                "                          \"k22\": \"v22\",\n" +
                "                          \"k23\" : {\n" +
                "                                \"k231\": \"v231\"\n" +
                "                          }\n" +
                "                 }\n" +
                "            }";

        GatewayMap map = new GatewayMap(json);

        assertEquals(map.get("k1"), "v1");
        assert map.get("k2") instanceof Map;
        assertEquals(map.get("k2.k21"), "v21");
        assertEquals(map.get("k2.k22"), "v22");
        assert map.get("k2.k23") instanceof Map;
        assertEquals(map.get("k2.k23.k231"), "v231");

    }

    @Test
    public void testJsonConstructorWithArray() {

        String json = "{\n" +
                "            \"a1\": [\n" +
                "                \"v1\", \"v2\"\n" +
                "                ],\n" +
                "            \"a2\": [\n" +
                "                {\"m1\": \"v21\"},\n" +
                "                {\"m2\": \"v22\"}\n" +
                "                ]\n" +
                "        }";
        GatewayMap map = new GatewayMap(json);

        assertEquals(map.get("a1[0]"), "v1");
        assertEquals(map.get("a1[1]"), "v2");
        assertEquals(map.get("a2[0].m1"), "v21");
        assertEquals(map.get("a2[1].m2"), "v22");

        assert map.containsKey("a1[0]");
        assert map.containsKey("a1[1]");
        assert map.containsKey("a2[0].m1");
        assert map.containsKey("a2[1].m2");

        assert !map.containsKey("a1[2]");
        assert !map.containsKey("a2[0].m3");

    }

    @Test
    public void testArrayNotationWithNestedMaps() {

        GatewayMap map = new GatewayMap();

        map.put("a1[0].k1", 1);
        map.put("a1[0].k2", 2);
        map.put("a1[0].k3", 3);
        assert map.size() == 1;
        assert map.containsKey("a1[0].k1");
        assert map.containsKey("a1[0].k2");
        assert map.containsKey("a1[0].k3");
        assertEquals(map.get("a1[0].k1"), 1);
        assertEquals(map.get("a1[0].k2"), 2);
        assertEquals(map.get("a1[0].k3"), 3);

        List l  =  (List) map.get("a1");
        assert l.size() == 1;

        map.put("a1[1].k1", 11);
        map.put("a1[2].k1", 21);

        l  = (List) map.get("a1");
        assert l.size() == 3;
        assertEquals(map.get("a1[1].k1"), 11);
        assertEquals(map.get("a1[2].k1"), 21);


    }



    @Test
    @Ignore  // Currently not supported
    public void testArrayNotationWithNestedValues() {

        GatewayMap map = new GatewayMap();

        map.put("a1[0]", 1);
        assert map.size() == 1;
        assertEquals(map.get("a1[0]"), 1);

    }

    @Test
    public void arrayNotationWithNoIndexAppendsToArray() {
        GatewayMap map = new GatewayMap();

        map.put("a[].k1", 1);
        assertEquals(map.get("a[0].k1"), 1);

        map.put("a[].k1", 2);
        assertEquals(map.get("a[0].k1"), 1);
        assertEquals(map.get("a[1].k1"), 2);

    }

    // TODO test containsKey with illegal index

    @Test
    public void arrayGetWithNoIndexReturnsLastValue() {

        // TODO Test with "a[]  =1 "

        GatewayMap map = new GatewayMap();
        map.put("a[].k1", 1);
        assertEquals(map.get("a[].k1"), 1);

        map.put("a[].k1", 2);
        assertEquals(map.get("a[].k1"), 2);

        map.put("a[].k1", 3);
        assertEquals(map.get("a[].k1"), 3);

    }

    @Test
    public void arrayCanBePutInNestedMap() {

        GatewayMap map = new GatewayMap();
        map.put("k1.k2[0].k3", 3);

        assertEquals(map.get("k1.k2[0].k3"), 3);
        assert map.containsKey("k1.k2[0].k3");
    }

    @Test
    public void arrayGetWithOutOfBoundsIndexThrowsException() {

        // TODO Test with "a[]  =1 "

        GatewayMap map = new GatewayMap();
        map.put("a[0].k1", 1);
        assertEquals(map.get("a[0].k1"), 1);
        try {
            map.get("a[1].k1");
            fail("IndexOutOfBoundsException not raised");
        } catch (IndexOutOfBoundsException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IndexOutOfBoundsException but got '" + e + "'");
        }
    }

    @Test
    public void arrayContainsKeyWithOutOfBoundsIndexThrowsException() {

        // TODO Test with "a[]  =1 "

        GatewayMap map = new GatewayMap();
        map.put("a[0].k1", 1);
        assertEquals(map.get("a[0].k1"), 1);
        try {
            map.containsKey("a[1].k1");
            fail("IndexOutOfBoundsException not raised");
        } catch (IndexOutOfBoundsException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IndexOutOfBoundsException but got '" + e + "'");
        }
    }

    @Test
    public void arrayPutForNotArrayThrowsException() {


        GatewayMap map = new GatewayMap();
        map.put("a", 1);
        assertEquals(map.get("a"), 1);
        try {
            map.put("a[0].k1", 2);
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got '" + e + "'");
        }

        try {
            map.put("a[0]", 2);
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got '" + e + "'");
        }

    }


    @Test
    public void arrayGetForNotArrayThrowsException() {

        GatewayMap map = new GatewayMap();
        map.put("a", 1);
        assertEquals(map.get("a"), 1);
        try {
            map.get("a[0].k1");
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got '" + e + "'");
        }

        try {
            map.get("a[0]");
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got '" + e + "'");
        }

    }

    @Test
    public void arrayContainsKeyForNotArrayThrowsException() {

        GatewayMap map = new GatewayMap();
        map.put("a", 1);
        assertEquals(map.get("a"), 1);
        try {
            map.containsKey("a[0].k1");
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got '" + e + "'");
        }

        try {
            map.containsKey("a[0]");
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got '" + e + "'");
        }

    }

    @Test
    public void arrayGetWithIllegalIndexThrowsException() {

        // TODO Test with "a[]  =1 "

        GatewayMap map = new GatewayMap();
        map.put("a[0].k1", 1);
        assertEquals(map.get("a[0].k1"), 1);
        try {
            map.get("a[xx].k1");
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Exception " + e + " thrown");
        }

        try {
            map.put("a[xx].k1", 2);
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Exception " + e + " thrown");
        }
    }


    @Test
    public void incompleteArrayNotationJustAddsKey() {
        GatewayMap map = new GatewayMap();

        map.put("test[0", 1);
        assert map.containsKey("test[0");
        assertEquals(map.get("test[0"), 1);

    }

    @Test
    public void putMapValueAddsAllKeyValues() {
        GatewayMap map = new GatewayMap();

        Map m1 = new HashMap();
        m1.put("k1", "v1");
        m1.put("k2", "v2");
        m1.put("k3", "v3");

        map.put("m1", m1);
        assert map.size() == 1;
        assert map.get("m1") instanceof Map;

        assertEquals(map.get("m1.k1"), "v1");
        assertEquals(map.get("m1.k2"), "v2");
        assertEquals(map.get("m1.k3"), "v3");

        map.put("m2.m3.m4", m1);
        assertEquals(map.get("m2.m3.m4.k1"), "v1");


        // TODO test nested path maps
    }

    @Test
    public void fluentBuilderAddsToMap() {
        GatewayMap map = new GatewayMap()
                .set("k1", "v1")
                .set("k2", "v2")
                .set("k3", "v3");
        assertEquals(map.get("k1"), "v1");
        assertEquals(map.get("k2"), "v2");
        assertEquals(map.get("k3"), "v3");

        map.set("k4.k5", "v45")
                .set("k4.k6", "v46");

        assertEquals(map.get("k4.k5"), "v45");
        assertEquals(map.get("k4.k6"), "v46");

    }

    @Test
    public void keyValueConstructorAddsKey() {
        assertEquals(new GatewayMap("k1", "v1").get("k1"), "v1");
        assertEquals(new GatewayMap("k1.k2", "v12").get("k1.k2"), "v12");
    }


    @Test
    public void removeSimpleKeyRemovesKey() {

        GatewayMap map = new GatewayMap();

        map.remove("k1");

        map.put("k1", "v1");
        assert map.containsKey("k1");
        assertEquals(map.get("k1"), "v1");

        map.remove("k1");
        assert !map.containsKey("k1");
        assertEquals(map.get("k1"), null);


    }

    @Test
    public void removeNestedPropertyKeyRemovesKey() {

        GatewayMap map = new GatewayMap();

        map.put("k1.k2", "v1");
        assert map.containsKey("k1");
        assert map.containsKey("k1.k2");
        assertEquals(map.get("k1.k2"), "v1");

        map.remove("k1.k2");
        assert map.containsKey("k1");
        assert !map.containsKey("k1.k2");
        assertEquals(map.get("k1.k2"), null);

        map.put("k2.k1", "v21");
        map.put("k2.k2", "v22");
        map.put("k2.k3", "v23");
        assert ((Map)map.get("k2")).size() == 3;

        assertEquals(map.get("k2.k1"), "v21");
        assertEquals(map.get("k2.k2"), "v22");
        assertEquals(map.get("k2.k3"), "v23");

        map.remove("k2.k2");
        assertEquals(map.get("k2.k1"), "v21");
        assertEquals(map.get("k2.k2"), null);
        assertEquals(map.get("k2.k3"), "v23");

        map.remove("k2.k3");
        assertEquals(map.get("k2.k1"), "v21");
        assertEquals(map.get("k2.k2"), null);
        assertEquals(map.get("k2.k3"), null);

        map.remove("k2.k1");
        assertEquals(map.get("k2.k1"), null);
        assertEquals(map.get("k2.k2"), null);
        assertEquals(map.get("k2.k3"), null);

        assert ((Map)map.get("k2")).size() == 0;

    }

    @Test
    public void removeNestedMapRemovesTheMap() {

        GatewayMap map = new GatewayMap();


        map.put("k1.k2.k3", "v1");
        map.put("k1.k2.k4", "v2");
        map.put("k1.k5", "v3");

        map.remove("k1.k2");

        assert map.containsKey("k1.k5");
        assert !map.containsKey("k1.k2");
        assert !map.containsKey("k1.k2.k3");
    }

    @Test
    public void removeArrayElementKeysRemovesKey() {

        GatewayMap map = new GatewayMap();

        map.put("a[].k1", "1");
        map.put("a[].k1", "2");
        map.put("a[].k1", "3");

        map.put("k2.a[].k1", "4");
        map.put("k2.a[].k1", "5");

        System.out.println(" m " + map);

        assert ((List)map.get("a")).size() == 3;

        assertEquals(map.get("a[0].k1"), "1");
        assertEquals(map.get("a[1].k1"), "2");
        assertEquals(map.get("a[2].k1"), "3");
        assertEquals(map.get("k2.a[0].k1"), "4");
        assertEquals(map.get("k2.a[1].k1"), "5");

        map.remove("a[2].k1");
        assert !map.containsKey("a[2].k1");
        assertEquals(map.get("a[2].k1"), null);
        assert ((List)map.get("a")).size() == 3;

        map.remove("k2.a[0].k1");
        assertEquals(map.get("k2.a[0].k1"), null);
        assertEquals(map.get("k2.a[1].k1"), "5");
        System.out.println(" m " + map);

    }

    @Test
    public void removeArrayElementRemovesElementAndShifts() {

        GatewayMap map = new GatewayMap();

        map.put("a[].k1", "1");
        map.put("a[].k1", "2");
        map.put("a[].k1", "3");
        map.put("a[].k1", "4");
        map.put("a[].k1", "5");

        System.out.println(" m " + map);

        assert ((List)map.get("a")).size() == 5;
        assertEquals(map.get("a[0].k1"), "1");
        assertEquals(map.get("a[1].k1"), "2");
        assertEquals(map.get("a[2].k1"), "3");
        assertEquals(map.get("a[3].k1"), "4");
        assertEquals(map.get("a[4].k1"), "5");

        map.remove("a[2]");
        assert ((List)map.get("a")).size() == 4;
        assertEquals(map.get("a[0].k1"), "1");
        assertEquals(map.get("a[1].k1"), "2");
        assertEquals(map.get("a[2].k1"), "4");
        assertEquals(map.get("a[3].k1"), "5");

        map.remove("a[3]");
        assert ((List)map.get("a")).size() == 3;
        assertEquals(map.get("a[0].k1"), "1");
        assertEquals(map.get("a[1].k1"), "2");
        assertEquals(map.get("a[2].k1"), "4");

        map.remove("a[0]");
        assert ((List)map.get("a")).size() == 2;
        assertEquals(map.get("a[0].k1"), "2");
        assertEquals(map.get("a[1].k1"), "4");

        map.remove("a[1]");
        assert ((List)map.get("a")).size() == 1;
        assertEquals(map.get("a[0].k1"), "2");

        map.remove("a[0]");
        assert ((List)map.get("a")).size() == 0;
        System.out.println(" m " + map);

    }

    @Test
    public void arrayRemoveWithOutOfBoundsIndexThrowsException() {

        // TODO Test with "a[]  =1 "

        GatewayMap map = new GatewayMap();
        map.put("a[0].k1", 1);
        assertEquals(map.get("a[0].k1"), 1);
        try {
            map.remove("a[1].k1");
            fail("IndexOutOfBoundsException not raised");
        } catch (IndexOutOfBoundsException e) {
            // Ignore
        } catch (Exception e) {
            fail("Expected IndexOutOfBoundsException but got '" + e + "'");
        }
    }

    @Test
    public void arrayRemoveWithNoIndexRemovesLast() {

        GatewayMap map = new GatewayMap();
        map.put("a[].k1", 1);
        map.put("a[].k1", 2);
        map.put("a[].k1", 3);

        assert ((List)map.get("a")).size() == 3;
        assertEquals(map.get("a[0].k1"), 1);
        assertEquals(map.get("a[1].k1"), 2);
        assertEquals(map.get("a[2].k1"), 3);

        map.remove("a[]");
        assert ((List)map.get("a")).size() == 2;
        assertEquals(map.get("a[0].k1"), 1);
        assertEquals(map.get("a[1].k1"), 2);

        map.remove("a[]");
        assert ((List)map.get("a")).size() == 1;
        assertEquals(map.get("a[0].k1"), 1);

        map.remove("a[]");
        assert ((List)map.get("a")).size() == 0;
    }

    @Test
    public void arrayRemoveForNonArrayThrowsException() {
        GatewayMap map = new GatewayMap();

        map.put("a", 1);
        try {
            map.remove("a[0]");
            fail("IllegalArgumentException not raised");
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (Exception e) {
            fail("Exception " + e + " thrown");
        }
    }



    @Test
    public void normalizeMapConvertsObjects() {
        GatewayMap m = new GatewayMap()
                .set("a", 1)
                .set("b", new Integer(2))
                .set("c.c1", 1.0)
                .set("c.c2", new Float(2.0))
                .set("d", "v1")
                .set("e", new StringBuffer("v2"))
                .set("f", new StringBuilder("v3"))
                .set("g", new CustomObject(4))
                .set("h", true);

        Map<String, Object> n = GatewayMap.normalize(m);

        assertEquals(1, n.get("a"));
        assertEquals("v1", n.get("d"));
        assertEquals("v2", n.get("e"));
        assertEquals("v3", n.get("f"));
        assertEquals("[4]", n.get("g"));

        String actualJson = gson.toJson(n);

        // Test JSONValue converts to JSON correctly
        assertEquals("{\"a\":1,\"b\":2,\"c\":{\"c1\":1.0,\"c2\":2.0},\"d\":\"v1\",\"e\":\"v2\",\"f\":\"v3\",\"g\":\"[4]\",\"h\":true}", actualJson);
    }

    @Test
    public void normalizeMapConvertsListObjects() {
        GatewayMap m = new GatewayMap();

        m.put("a[].a1", 1);
        m.put("a[].a2", "v1");
        m.put("a[].a3", new StringBuffer("v2"));
        m.put("a[].a4", new StringBuilder("v3"));
        m.put("a[].a5", new CustomObject(4));

        Map<String, Object> n = GatewayMap.normalize(m);

        assertEquals(1, n.get("a[0].a1"));
        assertEquals("v1", n.get("a[1].a2"));
        assertEquals("v2", n.get("a[2].a3"));
        assertEquals("v3", n.get("a[3].a4"));
        assertEquals("[4]", n.get("a[4].a5"));

        String actualJson = gson.toJson(n);

        // Test JSONValue converts to JSON correctly
        assertEquals("{\"a\":[{\"a1\":1},{\"a2\":\"v1\"},{\"a3\":\"v2\"},{\"a4\":\"v3\"},{\"a5\":\"[4]\"}]}", actualJson);
    }

    /*
    @Test
    public void arrayValues() {
        GatewayMap map = new GatewayMap();
        map.put("a[0]", "v1");
        System.out.println(map);
    }
    */

    class CustomObject {
        int value;

        public CustomObject(int v) {
            value = v;
        }

        public String toString() {
            return "[" + value + "]";
        }

    }


}
