package com.mastercard.gateway.android.sdk;


import java.util.HashMap;
import java.util.Map;

public class GatewayMap extends HashMap<String, Object> {

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     * <p>
     * Supports dot-notation to search for nested keys. ie: a key
     * value of 'parent.child' will return the value if this object contains a
     * top-level key of 'parent', who's value is a Map, and that Map
     * contains the key 'child'.
     *
     * @param key The key to lookup, dot-syntax supported
     * @return The associated value, or null if no value exists
     */
    @Override
    public Object get(Object key) {
        String stringKey = (String) key;

        // if not dot-notation, defer to super
        if (!stringKey.contains(".")) {
            return super.get(key);
        }

        // if does not contain key, return null
        if (!containsKey(key)) {
            return null;
        }

        // we can now safely assume the key is present. just iterate and return value

        // split keys by dot notation
        String[] keys = stringKey.split("\\.", 0);

        Object value = null;
        Map<String, Object> map = this;
        for (int i = 0; i < keys.length; i++) {
            // get the value for the current key in the chain
            value = map.get(keys[i]);

            // if we are at the leaf key, break out of loop
            if (i == keys.length - 1) {
                break;
            }

            // intermediate key, so update map and continue
            map = (Map<String, Object>) value;
        }

        // if all else fails, return null
        return value;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     * <p>
     * Supports dot-notation to append nested keys. ie: a key
     * value of 'parent.child' will append a top-level Map under key 'parent'
     * (if missing), and populate that map with the key-value for 'child',
     * returning any previously existing values at that location.
     *
     * @param key The key to use, dot-syntax supported
     * @param value The value
     * @return The previous value, or null if no previous mapping existed
     */
    @Override
    public Object put(String key, Object value) {
        // if not dot-notation, defer to super
        if (!key.contains(".")) {
            return super.put(key, value);
        }

        // split keys by dot notation
        String[] keys = key.split("\\.", 0);

        Map<String, Object> map = this;
        for (int i = 0; i < keys.length; i++) {
            // check if we are at a leaf key
            if (i == keys.length - 1) {
                // add the value to the map and return the previous
                return map.put(keys[i], value);
            }

            // we are at an intermediate key...

            // check if value exists for this key
            if (map.containsKey(keys[i])) {
                // if value is a map, update reference and continue
                Object temp = map.get(keys[i]);
                if (temp instanceof Map) {
                    map = (Map<String, Object>) temp;
                    continue;
                }

                // if a primitive type was found here, we just continue on and overwrite it with a new map below
            }

            // build new map and append it
            Map<String, Object> newMap = new HashMap<>();
            map.put(keys[i], newMap);
            map = newMap;
        }

        // if all else fails, return null
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     * <p>
     * Supports dot-notation to search for nested keys. ie: a key
     * value of 'parent.child' will return true if this object contains a
     * top-level key of 'parent', who's value is a Map, and that Map
     * contains the key 'child'.
     *
     * @param key
     * @return
     */
    @Override
    public boolean containsKey(Object key) {
        String stringKey = (String) key;

        // if not dot-notation, defer to super
        if (!stringKey.contains(".")) {
            return super.containsKey(key);
        }

        // split keys by dot notation
        String[] keys = stringKey.split("\\.", 0);

        Map<String, Object> map = this;
        for (int i = 0; i < keys.length; i++) {
            // if the map does not contain the key, return null
            if (!map.containsKey(keys[i])) {
                return false;
            }

            // if we are at the leaf key, return true
            if (i == keys.length - 1) {
                return true;
            }

            Object value = map.get(keys[i]);

            // ensure the intermediate value found is a map and update the loop
            if (value instanceof Map) {
                map = (Map<String, Object>) value;
                continue;
            }

            // if we hit a primitive type at an intermediate position, return false
            return false;
        }

        // if all else fails, return false
        return false;
    }
}