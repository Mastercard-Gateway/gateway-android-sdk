package com.mastercard.gateway.android.sdk;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class GatewayMapTest {

    GatewayMap map = new GatewayMap();

    @Before
    public void setUp() throws Exception {
        map.clear();
    }

    @Test
    public void testGetCorrectlyReturnsNestedValue() throws Exception {
        String key = "key1.key2.key3";
        int val = 1;

        map.put(key, val);

        Object value = map.get(key);

        assertNotNull(value);
        assertEquals(value, val);
    }

    @Test
    public void testGetReturnsNullIfNestedValueNotFound() throws Exception {
        map.put("key1.key2.key3", 1);

        Object value = map.get("key1.key2.missing");

        assertNull(value);
    }

    @Test
    public void testGetReturnsNullIfNestedValueRequestedOnPrimitiveParent() throws Exception {
        map.put("key1.key2", 1);

        Object value = map.get("key1.key2.missing");

        assertNull(value);
    }

    @Test
    public void testGetWillReturnIntermediateMapIfRequested() throws Exception {
        String key = "key1.key2";
        int val = 1;

        map.put(key, val);

        Object value = map.get("key1");

        assertNotNull(value);
        assertTrue(value instanceof Map);
        assertTrue(((Map<String, Object>) value).containsKey("key2"));
    }

    @Test
    public void testPutCorrectlyAddsNewNestedMaps() throws Exception {
        String key = "key1.key2.key3";
        int val = 1;

        map.put(key, val);

        assertTrue(map.containsKey("key1"));
        assertTrue(map.get("key1") instanceof Map);

        Map<String, Object> internalMap = (Map<String, Object>) map.get("key1");
        assertTrue(internalMap.containsKey("key2"));
        assertTrue(internalMap.get("key2") instanceof Map);

        internalMap = (Map<String, Object>) internalMap.get("key2");
        assertTrue(internalMap.containsKey("key3"));
        assertEquals(internalMap.get("key3"), val);
    }

    @Test
    public void testPutCorrectlyAppendsValuesToExistingNestedMaps() throws Exception {
        String key1 = "parent.first";
        String key2 = "parent.second";
        int val1 = 1;
        int val2 = 2;

        map.put(key1, val1);
        map.put(key2, val2);

        assertTrue(map.containsKey("parent"));

        Map<String, Object> internalMap = (Map<String, Object>) map.get("parent");

        assertTrue(internalMap.size() == 2);
        assertTrue(internalMap.containsKey("first"));
        assertTrue(internalMap.containsKey("second"));
        assertEquals(internalMap.get("first"), val1);
        assertEquals(internalMap.get("second"), val2);
    }

    @Test
    public void testPutWillOverwriteIntermediatePrimitiveTypeIfProvidingNestedValues() throws Exception {
        String key1 = "parent";
        String key2 = "parent.child";
        int val1 = 1;
        int val2 = 2;

        // add a primitive value
        map.put(key1, val1);

        assertTrue(map.containsKey("parent"));
        assertEquals(map.get("parent"), val1);

        // overwrite with map
        map.put(key2, val2);

        assertTrue(map.containsKey("parent"));
        assertTrue(map.get("parent") instanceof Map);

        Map<String, Object> internalMap = (Map<String, Object>) map.get("parent");

        assertTrue(internalMap.size() == 1);
        assertTrue(internalMap.containsKey("child"));
        assertEquals(internalMap.get("child"), val2);
    }

    @Test
    public void testContainsKeyWorksAsExpected() throws Exception {
        String testKey = "parent.child.grandchild";
        String intermediateKey = "parent.child";
        String failingKey = "parent.pet";
        String tooLong = "parent.child.grandchild.greatgrandchild";
        int testVal = 1;

        map.put(testKey, testVal);

        assertTrue(map.containsKey(testKey));
        assertTrue(map.containsKey(intermediateKey));
        assertFalse(map.containsKey(failingKey));
        assertFalse(map.containsKey(tooLong));
    }
}
