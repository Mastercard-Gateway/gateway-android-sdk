package com.mastercard.gateway.android.sdk;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Map object that extends the LinkedHashMap map with support for insertion and retrieval of keys using special
 * key path values.  The key path support nested maps and array values.
 * <p>
 * A key path consists of a sequence of key values separated by '.' characters.  Each part of the key path
 * consists of a separate map.  For example a key path of 'k1.k2.k3' is a map containing a key 'k1' whose
 * value is a map containing a key 'k2' whose values is a map containing a key 'k3'.   A key path can also
 * contain an array notation '[&lt;number&gt;]' in which case the value of 'a' in the map is a list containing
 * a map.  For example 'a[1].k2' refers to the key value 'k2' in the 2nd element of the list referred to by
 * the value of key 'a' in the map.  If no index value is given (i.e., '[]') then a put() method appends
 * to the list while a get() method returns the last value in the list.
 * <p>
 * When using the array index notation the value inserted must be a map; inserting values is not permitted.
 * For example using <code>put("a[3].k1", 1)</code> is permitted while <code>put("a[3]", 1)</code> results
 * in an <code>IllegalArgumentException</code>.
 * <p>
 * Examples:
 * <pre>
 * GatewayMap map  = new GatewayMap();
 * map.put("sourceOfFunds.provided.card.nameOnCard", "Joseph Cardholder");
 * map.put("sourceOfFunds.provided.card.number", "5111111111111118");
 * map.put("sourceOfFunds.provided.card.securityCode", "100");
 * map.put("sourceOfFunds.provided.card.expiry.month", "05");
 * map.put("sourceOfFunds.provided.card.expiry.year", "21")
 * map.put("customer.email", "test@example.com");
 * map.put("customer.firstName", "Joe");
 * map.put("customer.lastName", "Cardholder");
 * </pre>
 * There is also an set() method which is similar to put() but returns the map providing a fluent map builder.
 * <pre>
 * GatewayMap map = new GatewayMap()
 *      .set("sourceOfFunds.provided.card.nameOnCard", "Joe Cardholder")
 *      .set("sourceOfFunds.provided.card.number", "5111111111111118")
 *      .set("sourceOfFunds.provided.card.securityCode", "100")
 *      .set("sourceOfFunds.provided.card.expiry.month", "05")
 *      .set("sourceOfFunds.provided.card.expiry.year", "21")
 *      .set("customer.email", "test@example.com")
 *      .set("customer.firstName", "Joe")
 *      .set("customer.lastName", "Cardholder");
 * </pre>
 * Both of these examples construct a GatewayMap containing the keys 'sourceOfFunds' and 'customer'.  The
 * value for the 'customer' key is a map containing the keys 'email', 'firstName', and 'lastName'.
 */
public class GatewayMap extends LinkedHashMap<String, Object> {
    private static final Pattern arrayIndexPattern = Pattern.compile("(.*)\\[(.*)\\]");

    /**
     * Constructs an empty map with the specified capacity and load factor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     */
    public GatewayMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructs an empty map with the specified capacity and default load factor.
     *
     * @param initialCapacity the initial capacity
     */
    public GatewayMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty map with the default capacity and load factor.
     */
    public GatewayMap() {
        super();
    }

    /**
     * Constructs a map with the same mappings as in the specifed map.
     *
     * @param map the map whose mappings are to be placed in this map
     */
    public GatewayMap(Map<String, Object> map) {
        super(map);
    }

    /**
     * Constructs a map based of the speficied JSON string.
     *
     * @param jsonMapString the JSON string used to construct the map
     */
    public GatewayMap(String jsonMapString) {
        super();

        Map<? extends String, ? extends Object> map = new Gson().fromJson(jsonMapString, new TypeToken<Map<? extends String, ? extends Object>>() {
        }.getType());

        putAll(map);
    }


    /**
     * Constructs a map with an initial mapping of keyPath to value.
     *
     * @param keyPath key path with which the specified value is to be associated.
     * @param value   value to be associated with the specified key path.
     */
    public GatewayMap(String keyPath, Object value) {
        put(keyPath, value);
    }

    /**
     * Associates the specified value to the specified key path.
     *
     * @param keyPath key path to which the specified value is to be associated.
     * @param value   the value which is to be associated with the specified key path.
     * @throws IllegalArgumentException  if part of the key path does not match the expected type.
     * @throws IndexOutOfBoundsException if using an array index in the key path is out of bounds.
     */
    @Override
    public Object put(String keyPath, Object value) {
        String[] properties = keyPath.split("\\.");
        Map<String, Object> destinationObject = this;

        if (properties.length > 1) {
            for (int i = 0; i < (properties.length - 1); i++) {
                String property = properties[i];
                if (property.contains("[")) {
                    destinationObject = getDestinationMap(property, destinationObject, i == properties.length - 1);
                } else {
                    destinationObject = getPropertyMapFrom(property, destinationObject);
                }
            }
        } else if (keyPath.contains("[")) {
            destinationObject = getDestinationMap(keyPath, this, true);
        }

        // TODO: need to take care of the case where we are inserting a value into an array rather than
        // map ( eg map.put("a[2]", 123);

        if (destinationObject == this) {
            return super.put(keyPath, value);
        } else if (value instanceof Map) {     // if putting a map, call put all
            destinationObject.clear();
            GatewayMap m = new GatewayMap();
            m.putAll((Map<? extends String, ? extends Object>) value);
            destinationObject.put(properties[properties.length - 1], m);
            return destinationObject;
        } else {
            return destinationObject.put(properties[properties.length - 1], value);
        }
    }


    /**
     * Associates the specified value to the specified key path and returns a reference to
     * this map.
     *
     * @param keyPath key path to which the specified value is to be associated.
     * @param value   the value which is to be associated with the specified key path.
     * @return this map
     * @throws IllegalArgumentException  if part of the key path does not match the expected type.
     * @throws IndexOutOfBoundsException if using an array index in the key path is out of bounds.
     */
    public GatewayMap set(String keyPath, Object value) {
        put(keyPath, value);
        return this;
    }


    /**
     * Returns the value associated with the specified key path or null if there is no associated value.
     *
     * @param keyPath key path whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws IllegalArgumentException  if part of the key path does not match the expected type.
     * @throws IndexOutOfBoundsException if using an array index in the key path is out of bounds.
     */
    @Override
    public Object get(Object keyPath) {
        String[] keys = ((String) keyPath).split("\\.");

        if (keys.length <= 1) {
            Matcher m = arrayIndexPattern.matcher(keys[0]);
            if (!m.matches()) {             // handles keyPath: "x"
                return super.get(keys[0]);
            } else {                                                                                        // handle the keyPath: "x[]"
                String key = m.group(1);                                                                    // gets the key to retrieve from the matcher
                Object o = super.get(key);  // get the list from the map
                if (!(o instanceof List)) {
                    throw new IllegalArgumentException("Property '" + key + "' is not an array");
                }
                List<Map<String, Object>> l = (List<Map<String, Object>>) o;  // get the list from the map

                Integer index = l.size() - 1;                                        //get last item if none specified
                if (!"".equals(m.group(2))) {
                    index = Integer.parseInt(m.group(2));
                }
                return l.get(index);        // retrieve the map from the list
            }
        }

        Map<String, Object> map = findLastMapInKeyPath((String) keyPath);     // handles keyPaths beyond 'root' keyPath. i.e. "x.y OR x.y[].z, etc."

        // retrieve the value at the end of the object path i.e. x.y.z, this retrieves whatever is in 'z'
        return map.get(keys[keys.length - 1]);
    }

    /**
     * Returns true if there is a value associated with the specified key path.
     *
     * @param keyPath key path whose associated value is to be tested
     * @return true if this map contains an value associated with the specified key path
     * @throws IllegalArgumentException  if part of the key path does not match the expected type.
     * @throws IndexOutOfBoundsException if using an array index in the key path is out of bounds.
     */
    @Override
    public boolean containsKey(Object keyPath) {
        String[] keys = ((String) keyPath).split("\\.");

        if (keys.length <= 1) {
            Matcher m = arrayIndexPattern.matcher(keys[0]);
            if (!m.matches()) {             // handles keyPath: "x"
                return super.containsKey(keys[0]);
            } else {                                                                                        // handle the keyPath: "x[]"
                String key = m.group(1);
                Object o = super.get(key);  // get the list from the map
                if (!(o instanceof List)) {
                    throw new IllegalArgumentException("Property '" + key + "' is not an array");
                }
                List<Map<String, Object>> l = (List<Map<String, Object>>) o;  // get the list from the map

                Integer index = l.size() - 1;
                if (!"".equals(m.group(2))) {
                    index = Integer.parseInt(m.group(2));
                }
                return index >= 0 && index < l.size();
            }
        }

        Map<String, Object> map = findLastMapInKeyPath((String) keyPath);
        if (map == null) {
            return false;
        }
        return map.containsKey(keys[keys.length - 1]);
    }


    /**
     * Removes the value associated with the specified key path from the map.
     *
     * @param keyPath key path whose associated value is to be removed
     * @throws IllegalArgumentException  if part of the key path does not match the expected type.
     * @throws IndexOutOfBoundsException if using an array index in the key path is out of bounds.
     */
    @Override
    public Object remove(Object keyPath) {

        String[] keys = ((String) keyPath).split("\\.");

        if (keys.length <= 1) {
            Matcher m = arrayIndexPattern.matcher(keys[0]);
            if (!m.matches()) {
                return super.remove(keys[0]);
            } else {                                                                                        // handle the keyPath: "x[]"
                String key = m.group(1);                                                                    // gets the key to retrieve from the matcher
                Object o = super.get(key);  // get the list from the map
                if (!(o instanceof List)) {
                    throw new IllegalArgumentException("Property '" + key + "' is not an array");
                }
                List<Map<String, Object>> l = (List<Map<String, Object>>) o;  // get the list from the map

                Integer index = l.size() - 1;                                        //get last item if none specified
                if (!"".equals(m.group(2))) {
                    index = Integer.parseInt(m.group(2));
                }
                return l.remove(index.intValue());
            }
        }

        Map<String, Object> map = findLastMapInKeyPath((String) keyPath);

        return map.remove(keys[keys.length - 1]);
    }


    private Map<String, Object> findLastMapInKeyPath(String keyPath) {
        String[] keys = ((String) keyPath).split("\\.");

        Map<String, Object> map = null;
        for (int i = 0; i <= (keys.length - 2); i++) {
            Matcher m = arrayIndexPattern.matcher(keys[i]);
            String thisKey = keys[i];
            if (m.matches()) {
                thisKey = m.group(1);

                Object o = null;
                if (null == map) {    // if we are at the "root" of the object path
                    o = super.get(thisKey);
                } else {
                    o = map.get(thisKey);
                }

                if (!(o instanceof List)) {
                    throw new IllegalArgumentException("Property '" + thisKey + "' is not an array");
                }
                List<Map<String, Object>> l = (List<Map<String, Object>>) o;

                Integer index = l.size() - 1;                                        //get last item if none specified

                if (!"".equals(m.group(2))) {
                    index = Integer.parseInt(m.group(2));
                }

                map = (Map<String, Object>) l.get(index);

            } else {
                if (null == map) {
                    map = (Map<String, Object>) super.get(thisKey);
                } else {
                    map = (Map<String, Object>) map.get(thisKey);
                }

            }

        }

        return map;
    }


    private Map<String, Object> getDestinationMap(String property, Map<String, Object> destinationObject, boolean createMap) {

        Matcher m = arrayIndexPattern.matcher(property);
        if (m.matches()) {
            String propName = m.group(1);
            Integer index = null;
            if (!"".equals(m.group(2))) {
                index = Integer.parseInt(m.group(2));
            }
            return findOrAddToList(destinationObject, propName, index, createMap);
        }

        return destinationObject;

    }

    private Map<String, Object> findOrAddToList(Map<String, Object> destinationObject, String propName, Integer index, boolean createMap) {
        //

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        // find existing list or put the new list
        if (destinationObject.containsKey(propName)) {
            Object o = destinationObject.get(propName);
            if (!(o instanceof List)) {
                throw new IllegalArgumentException("Property '" + propName + "' is not an array");
            }
            list = (List<Map<String, Object>>) o;
        } else {
            destinationObject.put(propName, list);
        }

        // get the existing object in the list at the index
        Map<String, Object> propertyValue = null;
        if (index != null && list.size() > index) {
            propertyValue = list.get(index);
        }

        // no object at the index, create a new map and add it
        if (null == propertyValue) {
            propertyValue = new LinkedHashMap<String, Object>();
            if (null == index) {
                list.add(propertyValue);
            } else {
                list.add(index, propertyValue);
            }
        }

        // return the map retrieved from or added to the list
        destinationObject = propertyValue;

        return destinationObject;
    }

    private Map<String, Object> getPropertyMapFrom(String property, Map<String, Object> object) {
        // create a new map at the key specified if it doesnt already exist
        if (!object.containsKey(property)) {
            Map<String, Object> val = new LinkedHashMap<String, Object>();
            object.put(property, val);
        }

        Object o = object.get(property);
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        } else {
            throw new IllegalArgumentException("cannot change nested property to map");
        }
    }

    /**
     * Returns an identical copy of the map
     *
     * @param m The map to copy
     * @return A copy of the original map
     */
    public static Map<String, Object> normalize(Map<String, Object> m) {

        GatewayMap pm = new GatewayMap();

        for (String k : m.keySet()) {
            Object v = m.get(k);

            if (v == null) {
                pm.set(k, v);

            } else if (v instanceof List) {
                pm.set(k, normalize((List<Object>) v));

            } else if (v instanceof Map) {
                pm.set(k, normalize((Map<String, Object>) v));

            } else if (v instanceof String ||
                    v instanceof Double ||
                    v instanceof Float ||
                    v instanceof Number ||
                    v instanceof Boolean) {
                pm.set(k, v);

            } else {
                pm.set(k, v.toString());
            }
        }

        return pm;
    }

    private static List<Object> normalize(List<Object> l) {
        List<Object> pl = new ArrayList<Object>();

        for (Object v : l) {
            if (v == null) {
                pl.add(v);

            } else if (v instanceof List) {
                pl.add(normalize((List<Object>) v));

            } else if (v instanceof Map) {
                pl.add(normalize((Map<String, Object>) v));

            } else if (v instanceof String ||
                    v instanceof Double ||
                    v instanceof Float ||
                    v instanceof Number ||
                    v instanceof Boolean) {
                pl.add(v);

            } else {
                pl.add(v.toString());
            }
        }
        return pl;
    }
}
