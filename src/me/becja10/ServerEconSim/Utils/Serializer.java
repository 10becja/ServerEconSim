package me.becja10.ServerEconSim.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Serializer {

    public static Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> map = new HashMap<>();
        for (Object key : object.keySet()) {
            map.put(key.toString(), fromJson(object.get(key)));
        }
        return map;
    }

    private static Object fromJson(Object json) {
        if (json == null) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    public static List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (Object value : array) {
            list.add(fromJson(value));
        }
        return list;
    }

    public static List<String> toStringList(Inventory inv) {
        List<String> result = new ArrayList<>();
        List<ConfigurationSerializable> items = new ArrayList<>();
        Collections.addAll(items, inv.getContents());
        for (ConfigurationSerializable cs : items) {
            if (cs == null) {
                result.add("null");
            } else {
                result.add(toString(cs));
            }
        }
        return result;
    }
    
    public static String toString(ConfigurationSerializable cs)
    {
    	return new JSONObject(serialize(cs)).toString();
    }

    public static Inventory toInventory(List<String> stringItems, int number, int size, String title) {
        Inventory inv = Bukkit.createInventory(null, size, title);
        List<ItemStack> contents = new ArrayList<>();
        for (String piece : stringItems) {
            if (piece.equalsIgnoreCase("null")) {
                contents.add(null);
            } else {
                ItemStack item = toItemStack(piece);
                contents.add(item);
            }
        }
        ItemStack[] items = new ItemStack[contents.size()];
        for (int x = 0; x < contents.size(); x++) {
            items[x] = contents.get(x);
        }
        inv.setContents(items);
        return inv;
    }
    
    public static ItemStack toItemStack(String str)
    {
    	if(str == "") return null;
    	ItemStack item = (ItemStack) deserialize(toMap((JSONObject) JSONValue.parse(str)));
    	return item;
    }
    
    public static Location toLocation(String str)
    {
    	if(str == "") return null;
    	Location loc = (Location) deserialize(toMap((JSONObject) JSONValue.parse(str)));
    	return loc;
    }

    public static Map<String, Object> serialize(ConfigurationSerializable cs) {
        Map<String, Object> returnVal = handleSerialization(cs.serialize());
        returnVal.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(cs.getClass()));
        return returnVal;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Map<String, Object> handleSerialization(Map<String, Object> map) {
        Map<String, Object> serialized = recreateMap(map);
        for (Entry<String, Object> entry : serialized.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSerializable) {
                entry.setValue(serialize((ConfigurationSerializable) entry.getValue()));
            } else if (entry.getValue() instanceof Iterable<?>) {
                List<Object> newList = new ArrayList<>();
                for (Object object : ((Iterable) entry.getValue())) {
                    if (object instanceof ConfigurationSerializable) {
                        object = serialize((ConfigurationSerializable) object);
                    }
                    newList.add(object);
                }
                entry.setValue(newList);
            } else if (entry.getValue() instanceof Map<?, ?>) {
                // unchecked cast here.  If you're serializing to a non-standard Map you deserve ClassCastExceptions
                entry.setValue(handleSerialization((Map<String, Object>) entry.getValue()));
            }
        }
        return serialized;
    }

    public static Map<String, Object> recreateMap(Map<String, Object> original) {
        Map<String, Object> map = new HashMap<>();
        map.putAll(original);
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object deserialize(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                entry.setValue(deserialize((Map) entry.getValue()));
            } else if (entry.getValue() instanceof Iterable) {
                entry.setValue(convertIterable((Iterable) entry.getValue()));
            } else if (entry.getValue() instanceof Number) {
                entry.setValue(convertNumber((Number) entry.getValue()));
            }
        }
        return map.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY) ? ConfigurationSerialization.deserializeObject(map) : map;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<?> convertIterable(Iterable<?> iterable) {
        List<Object> newList = new ArrayList<>();
        for (Object object : iterable) {
            if (object instanceof Map) {
                object = deserialize((Map<String, Object>) object);
            } else if (object instanceof List) {
                object = convertIterable((Iterable) object);
            } else if (object instanceof Number) {
                object = convertNumber((Number) object);
            }
            newList.add(object);
        }
        return newList;
    }

    private static Number convertNumber(Number number) {
        if (number instanceof Long) {
            Long longObj = (Long) number;
            if (longObj.longValue() == longObj.intValue()) {
                return new Integer(longObj.intValue());
            }
        }
        return number;
    }
}