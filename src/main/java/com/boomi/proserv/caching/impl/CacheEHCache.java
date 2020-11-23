package com.boomi.proserv.caching.impl;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;

import com.boomi.execution.ExecutionUtil;

/**
 * Cache implementing EHCache, this class is the one implementation of CacheInterface <br/>
 * You can add the property com.boomi.proserv.caching.impl.CacheEHCache.heap to set up the number of max entries, by default 1024 <br/>
 * in boomi.properties (in conf folder)
 * @author anthony.rabiaza@gmail.com
 */
public class CacheEHCache implements CacheInterface {

	private static Map<String, Cache<String, String>> s_caches;
	private Properties properties;

	public CacheEHCache() {
	}

	@Override
	public boolean isValid() {
		return true;
	}


	private Cache<String, String> getCache(String cacheName) {
		Cache<String, String> cache;
		CacheManager cacheManager = null;

		if(s_caches == null) {
			Map<String, Cache<String, String>> map = new HashMap<String, Cache<String, String>>();
			s_caches = Collections.synchronizedMap(map);
		}

		cache = s_caches.get(cacheName);

		if(cache == null) {
			int heap = 1024;

			String heapStr = getProperties().getProperty(CacheEHCache.class.getName() + ".heap");

			if(heapStr!=null) {
				if(heapStr.contains(".")) {
					try {
						getLogger().info("Trying to create EHCache using configuration file " + heapStr);
						URL myUrl = new URL("file:///" + heapStr); 
						XmlConfiguration xmlConfig = new XmlConfiguration(myUrl); 
						cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
						cacheManager.init(); 
						cache = cacheManager.getCache(cacheName, String.class, String.class);
						getLogger().info("Succeed on creating EHCache using configuration file");
						if(cache == null) {
							//Cache not found in the configuration, forcing to use default cache
							getLogger().severe("Failed to create EHCache with the configuration");
							cacheManager = null;
						}
					} catch (Exception e1) {
						getLogger().severe("Failed to create EHCache with the configuration, error: "  + e1.getMessage());
						cacheManager = null;
					}
				} else {
					try {
						heap = Integer.valueOf(heapStr);
					}catch(Exception e) {
					}
				}
			}

			if(cacheManager==null) {
				
				getLogger().info("Trying to create default EHCache using heap size " + heapStr);
				
				CacheConfigurationBuilder<String, String> cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
						String.class, String.class, 
						ResourcePoolsBuilder.heap(heap)
						);

				cacheManager = CacheManagerBuilder.newCacheManagerBuilder().withCache(cacheName, cacheConfig).build();
				cacheManager.init(); 
				cache = cacheManager.getCache(cacheName, String.class, String.class);
				getLogger().info("Succeed on creating EHCache using default configuration (heap of " + heap + ")");
			}

			
			s_caches.put(cacheName, cache);
		}

		return cache;
	}

	@Override
	public Map<String,String> get(String cacheName) {
		Set<String> keys = new HashSet<>();
		getCache(cacheName).forEach(entry -> keys.add(entry.getKey()));
		return getCache(cacheName).getAll(keys);
	}
	
	@Override
	public String get(String cacheName, String key) {
		return getCache(cacheName).get(key);
	}

	@Override
	public void set(String cacheName, String key, String value) {
		getCache(cacheName).put(key, value);
	}

	@Override
	public void delete(String cacheName) {
		getCache(cacheName).clear();
	}

	@Override
	public void delete(String cacheName, String key) {
		getCache(cacheName).remove(key);
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void init() {
	}
	
	private Logger getLogger() {
		try {
			return ExecutionUtil.getBaseLogger();
		} catch (Exception e){
			return Logger.getLogger(this.getClass().getName());
		}
	}


}
