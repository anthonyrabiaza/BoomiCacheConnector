package com.boomi.connector.caching;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.PropertyMap;
import com.boomi.connector.util.BaseConnection;
import com.boomi.proserv.caching.CacheInstance;

/**
 * 
 * @author anthony.rabiaza@gmail.com
 *
 */
public class CacheConnection extends BaseConnection {

	private static Map<String, CacheInstance> s_cacheInstance;//TODO old instance will still be in the Map

	PropertyMap propertiesMap;

	public CacheConnection(BrowseContext context) {
		super(context);
		propertiesMap = context.getConnectionProperties();
	}

	private CacheInstance getInstance() throws Exception {
		CacheInstance cacheInstance = null;

		try {
			String propertiesHash = DigestUtils.sha256Hex(propertiesMap.toString());

			if(s_cacheInstance!=null) {
				cacheInstance = s_cacheInstance.get(propertiesHash);
			} else {
				synchronized(this){
					s_cacheInstance = Collections.synchronizedMap(new HashMap<String, CacheInstance>());
				}
			}

			if(cacheInstance == null || !cacheInstance.isValid()){
				synchronized(this){
					Properties properties = new Properties();

					cacheInstance = new CacheInstance();
					cacheInstance.setStandalone(false);
					cacheInstance.setDynamicProcessPropertiesFilter(propertiesMap.getProperty("properties_filter"));
					cacheInstance.setHashing(propertiesMap.getBooleanProperty("hashing"));

					String type = propertiesMap.getProperty("type");
					cacheInstance.setType(type);

					properties.put(type + "." + "hosts", 		propertiesMap.getProperty("hosts"));
					properties.put(type + "." + "useSSL", 		propertiesMap.getBooleanProperty("useSSL").toString());
					properties.put(type + "." + "user", 		propertiesMap.getProperty("user"));
					properties.put(type + "." + "password", 	propertiesMap.getProperty("password"));
					cacheInstance.setProperties(properties);
					cacheInstance.init();

					s_cacheInstance.put(propertiesHash, cacheInstance);
				}
			}

			return cacheInstance;
		} catch (Exception e) {
			throw new Exception("Error when getting the Cache. Please Retry. Error: " + e.getMessage(), e);
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