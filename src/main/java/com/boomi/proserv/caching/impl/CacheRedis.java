package com.boomi.proserv.caching.impl;

import java.util.Map;
import java.util.Properties;

/**
 * Cache implementing Redis (Standalone and Cluster), this class is the one implementation of CacheInterface
 * You need to update boomicache.properties to add <br/>:
 * com.boomi.proserv.caching.Cache.type=com.boomi.proserv.caching.impl.CacheRedis
 * By default, it will use a localhost configuration, but you can add the following properties to set the host and port: 
 * com.boomi.proserv.caching.impl.CacheRedis.hosts=host1:6379:6379 <br/>
 * For a cluster configuration, please separated the hosts configuration wit a coma:
 * com.boomi.proserv.caching.impl.CacheRedis.hosts=host1:6379,host2:6379
 * @author anthony.rabiaza@gmail.com
 */
public class CacheRedis implements CacheInterface {

	private CacheJedisWrapper cache = null;
	
	private String hosts;
	private String password;
	private boolean useSSL;
	private String parameters;
	private Properties properties;
	
	public CacheRedis() {
	}
	
	@Override
	public boolean isValid() {
		return getCache().isValid();
	}

	private CacheJedisWrapper getCache() {
		return cache;
	}
	
	@Override
	public Map<String,String> get(String cacheName) {
		return getCache().hgetAll(cacheName);
	}

	@Override
	public String get(String cacheName, String key) {
		return getCache().hget(cacheName, key);
	}

	@Override
	public void set(String cacheName, String key, String value) {
		getCache().hset(cacheName, key, value);
	}
	
	@Override
	public void delete(String cacheName) {
		getCache().hdel(cacheName);
	}
	
	@Override
	public void delete(String cacheName, String key) {
		getCache().hdel(cacheName, key);
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
		hosts 		= 					getProperties().getProperty(CacheRedis.class.getName() + ".hosts");
		useSSL 		= Boolean.valueOf(	getProperties().getProperty(CacheRedis.class.getName() + ".useSSL"));
		password 	= 					getProperties().getProperty(CacheRedis.class.getName() + ".password");
		parameters 	= 					getProperties().getProperty(CacheRedis.class.getName() + ".heap");
		cache 		= new CacheJedisWrapper(hosts, password, useSSL, parameters);
	}
}
