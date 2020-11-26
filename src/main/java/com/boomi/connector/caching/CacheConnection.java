package com.boomi.connector.caching;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

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

	private static Map<String, CacheInstance> s_cacheInstance;
	private static Timer s_timer;

	//Every 1 minute
	private static long DELAY 				= 1 * 60000L;
	private static long PERIOD 				= 1 * 60000L;

	//Default Inactive Period: 1 hour
	private static long INACTIVE_PERIOD 	= 3600 * 60000L;

	PropertyMap propertiesMap;

	static {
		TimerTask repeatedTask = new TimerTask() {
			public void run() {
				System.out.println("Cleaning  " + new Date());
				if(s_cacheInstance != null) {
					for (Iterator<String> iterator = s_cacheInstance.keySet().iterator(); iterator.hasNext();) {
						String currentInstanceName = iterator.next();
						CacheInstance currentInstance = s_cacheInstance.get(currentInstanceName);
						if(currentInstance != null && currentInstance.getLastUsedDate() != null) {
							if((currentInstance.getLastUsedDate().getTime()-new Date().getTime())>=INACTIVE_PERIOD) {
								currentInstance.close();
								s_cacheInstance.remove(currentInstanceName);
							}
						}
					}
				}
			}
		};
		s_timer = new Timer("CacheConnection_cleanup");


		s_timer.scheduleAtFixedRate(repeatedTask, DELAY, PERIOD);
	}

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

					properties.put(type + "." + "heap", 		propertiesMap.getProperty("heap"));
					properties.put(type + "." + "hosts", 		propertiesMap.getProperty("hosts"));
					properties.put(type + "." + "useSSL", 		propertiesMap.getBooleanProperty("useSSL").toString());
					properties.put(type + "." + "user", 		propertiesMap.getProperty("user"));
					properties.put(type + "." + "password", 	propertiesMap.getProperty("password"));
					cacheInstance.setProperties(properties);
					cacheInstance.init();

					s_cacheInstance.put(propertiesHash, cacheInstance);
				}
			}

			//TODO, put a timestamp of last used on cacheInstance

			return cacheInstance;
		} catch (Exception e) {
			throw new Exception("Error when getting the Cache. Please Retry. Error: " + e.getMessage(), e);
		}
	}

	public Map<String,String> get(String cacheName, Long ttl) throws Exception {
		return getInstance().get(cacheName, ttl);
	}
	
	public String get(String cacheName, String key, Long ttl) throws Exception {
		return getInstance().get(cacheName, key, ttl);
	}

	public void upsert(String cacheName, String key, String value, Long ttl) throws Exception {
		getInstance().set(cacheName, key, value, ttl);
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