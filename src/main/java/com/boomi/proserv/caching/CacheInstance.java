package com.boomi.proserv.caching;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

import com.boomi.document.scripting.DataContextImpl;
import com.boomi.execution.ExecutionManager;
import com.boomi.execution.ExecutionUtil;
import com.boomi.launchutil.StreamUtil;
import com.boomi.proserv.caching.impl.CacheEHCache;
import com.boomi.proserv.caching.impl.CacheInterface;

/**
 * CacheInstance is the intermediate Class which will wrap the Implementation 
 * @author anthony.rabiaza@gmail.com
 *
 */
public class CacheInstance {
	
	private final static String PROPERTIES_SEPARATOR 	= "@@";
	private final static String CACHE_HIT 				= "cache_hit";
	
	private CacheInterface cache 					= null; 
	private String dynamicProcessPropertiesFilter 	= "(query_.*)|(param_.*)";
	private String type								= null;
	private boolean hashing 						= true;
	private boolean standalone 						= false;
	private Properties properties;
	private Date creationDate 						= null; 
	private Date lastUsedDate 						= null;
	
	public String getDynamicProcessPropertiesFilter() {
		return dynamicProcessPropertiesFilter;
	}

	public void setDynamicProcessPropertiesFilter(String dynamicProcessPropertiesFilter) {
		this.dynamicProcessPropertiesFilter = dynamicProcessPropertiesFilter;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isHashing() {
		return hashing;
	}

	public void setHashing(boolean hashing) {
		this.hashing = hashing;
	}

	public boolean isStandalone() {
		return standalone;
	}

	public void setStandalone(boolean standalone) {
		getLogger().info("Setting standalone configuration to " + standalone);
		this.standalone = standalone;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		getLogger().info("Setting properties");
		this.properties = properties;
	}

	public CacheInstance() {
		
	}
	
	/**
	 * This method can be call to reinit the Cache after a change in boomicache.properties to avoid a restart
	 */
	public void init() {
		if(type == null) {
			type = CacheEHCache.class.getName();
		}
		
		getLogger().info("Initialization of Cache Instance with type " + type + " and properties " + properties);
		if(type != null && type.length()>0) {
			getLogger().info("Trying to use " + type + " cache type");
			try {
				Class<?> c = Class.forName(type);
				cache = (CacheInterface)c.newInstance();
			} catch (Exception e) {
				getLogger().warning(e.getMessage());
				e.printStackTrace();
			}
		}

		if(cache == null) {
			getLogger().info("Use default cache type (EHCache)");
			type = CacheEHCache.class.getName();
			cache = new CacheEHCache();
		}
		
		cache.setProperties(properties);
		cache.init();
		
		creationDate = new Date();
		getLogger().info("End of Initialization of Cache Instance");
	}

	public boolean isValid() {
		try {
			return cache.isValid();
		} catch (Exception e) {
			return false;
		}
	}
	
	public CacheInterface getCache() {
		return cache;
	}

	/**
	 * Store the current document to cache using the auto-calculated key.
	 * @param dataContext
	 * @param cacheName
	 * @throws IOException
	 */
	public void set(DataContextImpl dataContext, String cacheName) throws IOException {
		set(dataContext, cacheName, computeKey(dataContext));
	}

	/**
	 * Get the current document to cache using the auto-calculated key.
	 * Also a Dynamic Process property call cache_hit will be set to true (if value is found) of false (otherwise)
	 * @param dataContext
	 * * @param cacheName
	 * @return
	 */
	public String get(DataContextImpl dataContext, String cacheName) {
		return get(dataContext, cacheName, computeKey(dataContext));
	}

	/*End*/

	/*Key is provided, value to store will be the full Document*/

	/**
	 * Store the current document to cache using the provided key
	 * @param dataContext
	 * * @param cacheName
	 * @param key
	 * @throws IOException
	 */
	public void set(DataContextImpl dataContext, String cacheName, String key) throws IOException {
		StringBuffer content = new StringBuffer();
		for(int i=0;i<dataContext.getDataCount();i++) {
			content.append(StreamUtil.toString(dataContext.getStream(i), "UTF-8"));
		}
		set(cacheName, key, content.toString());
	}

	/**
	 * Get the current document to cache using the provided key.
	 * Also a Dynamic Process property call cache_hit will be set to true (if value is found) of false (otherwise)
	 * @param dataContext
	 * @param cacheName
	 * @param key
	 * @return
	 */
	public String get(DataContextImpl dataContext, String cacheName, String key) {
		return get(cacheName, key);
	}

	/*End*/

	/*Provide the key and value*/

	/**
	 * Store the current value to cache using the provided key.
	 * @param cacheName
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void set(String cacheName, String key, String value) throws IOException {
		getLogger().info("Storing " + (value.length()>50?value.substring(0, 50)+"(truncated)":value) + " to " + cacheName + " with key " + key);
		cache.set(cacheName, key, value);
		lastUsedDate = new Date();
	}
	
	/**
	 * Get all the keys,valuse from cache
	 * @param cacheName
	 * @param key
	 * @return
	 */
	public Map<String,String> get(String cacheName) {
		getLogger().info("Getting all keys,values from " + cacheName);
		Map<String,String> keysValues = cache.get(cacheName);
		getLogger().info("Value returned");
		if(!standalone) {
			CacheInstance.addCacheHitToContext(keysValues!=null);
		}
		lastUsedDate = new Date();
		return keysValues;
	}

	/**
	 * Get the current value from cache using the provided key.
	 * @param cacheName
	 * @param key
	 * @return
	 */
	public String get(String cacheName, String key) {
		getLogger().info("Getting value from " + cacheName + " with key " + key);
		String value = cache.get(cacheName, key);
		getLogger().info("Value returned");
		if(!standalone) {
			CacheInstance.addCacheHitToContext(value!=null);
		}
		lastUsedDate = new Date();
		return value;
	}
	
	/**
	 * Delete all the content of a given cache
	 * @param cacheName
	 */
	public void delete(String cacheName) {
		getLogger().info("Deleting all values from " + cacheName);
		cache.delete(cacheName);
		lastUsedDate = new Date();
	}
	
	/**
	 * Delete a given pair based on the key
	 * @param cacheName
	 * @param key
	 */
	public void delete(String cacheName, String key) {
		getLogger().info("Deleting value from " + cacheName + " with key " + key);
		cache.delete(cacheName, key);
		lastUsedDate = new Date();
	}

	private Logger getLogger() {
		try {
			return ExecutionUtil.getBaseLogger();
		} catch (Exception e){
			return Logger.getLogger(CacheInstance.class.getName());
		}
	}

	/*End*/


	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastUsedDate() {
		return lastUsedDate;
	}

	private String computeKey(DataContextImpl dataContext) {
		if(!standalone) {
			return computeKey(CacheInstance.getContextProperties());
		} else {
			return "DEFAULT";
		}
	}

	/**
	 * Method used to compute the key (when the key is not provided). It using the Dynamic Process Properties
	 * You can setup the filter by providing the property com.boomi.proserv.caching.Cache.dynamic_process_properties_filter; Default value will be "(query_.*)|(param_.*)" to use HTTP Query elements and HTTP Parameters
	 * @param properties
	 * @return
	 */
	public String computeKey(Properties properties) {

		StringBuffer buffer = new StringBuffer();
		
		for (String key : properties.keySet().toArray(new String[0])) {
			boolean add = true;

			if(key.equals(CACHE_HIT)) {
				continue;
			}

			add = Pattern.matches(dynamicProcessPropertiesFilter, key);

			if(add) {
				buffer.append(key + "=" + properties.getProperty(key) + PROPERTIES_SEPARATOR);
			}
		}
		
		if(buffer.length()==0) {
			buffer.append("NO_KEY");
		}

		getLogger().info("Key before hashing " + buffer.toString());

		if(hashing) {
			return DigestUtils.sha256Hex(buffer.toString());
		} else {
			return buffer.toString();
		}
	}
	
	public void close() {
		getLogger().info("Closing CacheInstance with type " + type + " and properties " + properties);
	}
	
	static public void addCacheHitToContext(boolean hit) {
		ExecutionManager.getCurrent().setProperty(CACHE_HIT, String.valueOf(hit));
		
	}
	
	static public Properties getContextProperties() {
		return ExecutionManager.getCurrent().getProperties();
	}

}
