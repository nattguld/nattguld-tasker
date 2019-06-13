package com.nattguld.tasker.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author randqm
 *
 */

public class Attributes {
	
	/**
	 * The map containing the key-value pairs.
	 */
	private final Map<String, Object> kvps = new HashMap<>();
	
	
	/**
	 * Sets a new key-value pair.
	 * 
	 * @param key The key.
	 * 
	 * @param value The value.
	 */
	public void put(String key, Object value) {
		kvps.put(key, value);
	}
	
	/**
	 * Retrieves whether a key exists or not.
	 * 
	 * @param key The key.
	 * 
	 * @return The result.
	 */
	public boolean has(String key) {
		return kvps.containsKey(key);
	}
	
	/**
	 * Retrieves a value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The value.
	 */
	public Object get(String key, Object defaultValue) {
		if (!has(key)) {
			put(key, defaultValue);
			return get(key, defaultValue);
		}
		return kvps.get(key);
	}
	
	/**
	 * Retrieves a value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The value.
	 */
	public Object get(String key) {
		return get(key, null);
	}
	
	/**
	 * Retrieves a char value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The char value.
	 */
	public char getAsChar(String key, char defaultValue) {
		return (char)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a char value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The char value.
	 */
	public char getAsChar(String key) {
		return getAsChar(key, Character.MIN_VALUE);
	}
	
	/**
	 * Retrieves a string value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The string value.
	 */
	public String getAsString(String key, String defaultValue) {
		return (String)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a string value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The string value.
	 */
	public String getAsString(String key) {
		return getAsString(key, "");
	}
	
	/**
	 * Retrieves a byte value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The byte value.
	 */
	public byte getAsByte(String key, byte defaultValue) {
		return (byte)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a byte value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The byte value.
	 */
	public byte getAsByte(String key) {
		return getAsByte(key, (byte)0);
	}
	
	/**
	 * Retrieves a float value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The float value.
	 */
	public float getAsFloat(String key, float defaultValue) {
		return (float)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a float value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The float value.
	 */
	public float getAsFloat(String key) {
		return getAsFloat(key, 0f);
	}
	
	/**
	 * Retrieves a double value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The double value.
	 */
	public double getAsDouble(String key, double defaultValue) {
		return (double)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a double value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The double value.
	 */
	public double getAsDouble(String key) {
		return getAsDouble(key, 0d);
	}
	
	/**
	 * Retrieves an integer value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The integer value.
	 */
	public int getAsInt(String key, int defaultValue) {
		return (int)get(key, defaultValue);
	}
	
	/**
	 * Retrieves an integer value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The integer value.
	 */
	public int getAsInt(String key) {
		return getAsInt(key, 0);
	}
	
	/**
	 * Retrieves a long value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The long value.
	 */
	public long getAsLong(String key, long defaultValue) {
		return (long)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a long value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The long value.
	 */
	public long getAsLong(String key) {
		return getAsLong(key, 0l);
	}
	
	/**
	 * Retrieves a boolean value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @param defaultValue The default value.
	 * 
	 * @return The boolean value.
	 */
	public boolean getAsBool(String key, boolean defaultValue) {
		return (boolean)get(key, defaultValue);
	}
	
	/**
	 * Retrieves a boolean value for a given key.
	 * 
	 * @param key The key.
	 * 
	 * @return The boolean value.
	 */
	public boolean getAsBool(String key) {
		return getAsBool(key, false);
	}
	
	/**
	 * Increments a value with another byte value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The increment value.
	 * 
	 * @return The new value.
	 */
	public byte increment(String key, byte value) {
		byte result = (byte) (getAsByte(key) + value);
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Decrements a value with another byte value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The decrement value.
	 * 
	 * @return The new value.
	 */
	public byte decrement(String key, byte value) {
		byte result = (byte) (getAsByte(key) - value);
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Increments a value with another integer value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The increment value.
	 * 
	 * @return The new value.
	 */
	public int increment(String key, int value) {
		int result = getAsInt(key) + value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Decrements a value with another integer value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The decrement value.
	 * 
	 * @return The new value.
	 */
	public int decrement(String key, int value) {
		int result = getAsInt(key) - value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Increments a value with another float value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The increment value.
	 * 
	 * @return The new value.
	 */
	public float increment(String key, float value) {
		float result = getAsFloat(key) + value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Decrements a value with another float value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The decrement value.
	 * 
	 * @return The new value.
	 */
	public float decrement(String key, float value) {
		float result = getAsFloat(key) - value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Increments a value with another double value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The increment value.
	 * 
	 * @return The new value.
	 */
	public double increment(String key, double value) {
		double result = getAsDouble(key) + value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Decrements a value with another double value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The decrement value.
	 * 
	 * @return The new value.
	 */
	public double decrement(String key, double value) {
		double result = getAsDouble(key) - value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Increments a value with another long value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The increment value.
	 * 
	 * @return The new value.
	 */
	public long increment(String key, long value) {
		long result = getAsLong(key) + value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Decrements a value with another long value.
	 * 
	 * @param key The key.
	 * 
	 * @param value The decrement value.
	 * 
	 * @return The new value.
	 */
	public long decrement(String key, long value) {
		long result = getAsLong(key) - value;
		
		put(key, result);
		
		return result;
	}
	
	/**
	 * Clears the attributes.
	 */
	public void clear() {
		kvps.clear();
	}
	
	/**
	 * Retrieves the key-value pairs.
	 * 
	 * @return The key-value pairs.
	 */
	public Map<String, Object> getKvps() {
		return kvps;
	}

}
