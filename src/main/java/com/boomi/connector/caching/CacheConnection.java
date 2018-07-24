package com.boomi.connector.caching;

import java.util.Properties;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.PropertyMap;
import com.boomi.connector.util.BaseConnection;
import com.boomi.proserv.caching.CacheInstance;

public class CacheConnection extends BaseConnection {

	private static CacheInstance s_cacheInstance;

	PropertyMap propertiesMap;

	public CacheConnection(BrowseContext context) {
		super(context);
		propertiesMap = context.getConnectionProperties();
	}

	private CacheInstance getInstance() throws Exception {
		try {
			if(s_cacheInstance==null || !s_cacheInstance.isValid()) {
				
				synchronized(this){
					Properties properties = new Properties();
	
					s_cacheInstance = new CacheInstance();
					s_cacheInstance.setStandalone(false);
					s_cacheInstance.setDynamicProcessPropertiesFilter(propertiesMap.getProperty("properties_filter"));
					s_cacheInstance.setHashing(propertiesMap.getBooleanProperty("hashing"));
	
					String type = propertiesMap.getProperty("type");
					s_cacheInstance.setType(type);
	
					properties.put(type + "." + "hosts", 		propertiesMap.getProperty("hosts"));
					properties.put(type + "." + "useSSL", 		propertiesMap.getBooleanProperty("useSSL"));
					properties.put(type + "." + "user", 		propertiesMap.getProperty("user"));
					properties.put(type + "." + "password", 	propertiesMap.getProperty("password"));
					s_cacheInstance.setProperties(properties);
					s_cacheInstance.init();
				}
			}

			return s_cacheInstance;
		} catch (Exception e) {
			throw new Exception("Please click on Back and retry. Error: " + e.getMessage(), e);
		}
	}
	
	public String get(String cacheName, String key) throws Exception {
		return getInstance().get(cacheName, key);
	}

	public void upsert(String cacheName, String key, String value) throws Exception {
		getInstance().set(cacheName, key, value);
	}

	public void delete(String cacheName) throws Exception {
		getInstance().delete(cacheName);
	}

	public void delete(String cacheName, String key) throws Exception {
		getInstance().delete(cacheName, key);
	}
	
	public String computeKey(Properties properties) throws Exception {
		return getInstance().computeKey(properties);
	}

}