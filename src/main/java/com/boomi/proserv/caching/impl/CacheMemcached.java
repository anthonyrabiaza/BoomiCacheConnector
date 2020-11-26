package com.boomi.proserv.caching.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.boomi.execution.ExecutionUtil;

import net.spy.memcached.MemcachedClient;

/**
 * Cache implementing memcached, this class is the one implementation of CacheInterface <br/>
 * You can add the property com.boomi.proserv.caching.impl.CacheMemcached.hosts to set up the list of hosts
 * in boomi.properties (in conf folder)
 * @author anthony.rabiaza@gmail.com
 */
public class CacheMemcached implements CacheInterface {

	private static MemcachedClient s_client;
	private Properties properties;

	public CacheMemcached() {
	}

	@Override
	public boolean isValid() {
		return true;
	}


	private MemcachedClient getCache(String cacheName) {
		MemcachedClient client = null;

		if(s_client == null) {
			String hosts = getProperties().getProperty(CacheMemcached.class.getName() + ".hosts");
			try {
				if(hosts.contains(",")) {
					getLogger().info("Trying to create Memcached with hosts list " + hosts);
					String[] pairs = hosts.split(",");
					for(int i=0;i<pairs.length;i++) {
						String[] pair = pairs[i].split(":");
						List<InetSocketAddress> socketList = new ArrayList<InetSocketAddress>();
						socketList.add(new InetSocketAddress(pair[0], Integer.parseInt(pair[1])));

						client = new MemcachedClient(socketList);
					}
					getLogger().info("Done creating Memcached with hosts list");
				} else {
					getLogger().info("Trying to create Memcached with a single host " + hosts);
					String[] pair = hosts.split(":");
					client = new MemcachedClient(new InetSocketAddress(pair[0], Integer.parseInt(pair[1])));
					getLogger().info("Done creating Memcached with a single host");
				}   
			} catch (IOException e) {
				Utils.throwException(e);
			} 

			s_client = client;
		} else {
			client = s_client;
		}

		return client;
	}

	@Override
	public Map<String,String> get(String cacheName, Long ttl) {
		throw new RuntimeException("Memcached doesn't support getAll");
	}
	
	@Override
	public String get(String cacheName, String key, Long ttl) {
		return getCache(cacheName).get(cacheName + "_" + key).toString();
	}

	@Override
	public void set(String cacheName, String key, String value, Long ttl) {
		int ttlInt;
		if(ttl < 0) {
			ttlInt = 0;
		} else {
			ttlInt = ttl.intValue();
		}
			
		getCache(cacheName).set(cacheName + "_" + key, ttlInt, value);
	}

	@Override
	public void delete(String cacheName) {
		getCache(cacheName).flush();
	}

	@Override
	public void delete(String cacheName, String key) {
		getCache(cacheName).delete(cacheName + "_" + key);
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
