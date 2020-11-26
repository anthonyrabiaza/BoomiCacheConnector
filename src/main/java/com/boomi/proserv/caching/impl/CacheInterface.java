package com.boomi.proserv.caching.impl;

import java.util.Map;
import java.util.Properties;

/**
 * CacheInterface, implement this class to create a new Cache implementation
 * @author anthony.rabiaza@gmail.com
 *
 */
public interface CacheInterface {
	public boolean isValid();
	public Map<String,String> get(String cacheName, Long ttl);
	public String get(String cacheName, String key, Long ttl);
	public void set(String cacheName, String key, String value, Long ttl);
	public void delete(String cacheName);
	public void delete(String cacheName, String key);
	
	public void setProperties(Properties properties);
	public Properties getProperties();
	public void init();
}
