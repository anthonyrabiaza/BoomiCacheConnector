package com.boomi.proserv.caching.impl;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.boomi.execution.ExecutionUtil;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterConnectionHandler;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;

/**
 * Class Wrapper with Jedis Implementation
 * @author anthony.rabiaza@gmail.com
 *
 */
public class CacheJedisWrapper {

	private static int S_TIMEOUT = 0;
	private static int S_ATTEMPS = 3;

	Jedis jedis;
	JedisPool jedisPool;
	JedisClusterConnectionHandler jedisConnHandler;
	
	boolean noPool;

	public CacheJedisWrapper(String hosts, String password, boolean useSSL, String parameters) {
		if(hosts != null && hosts.length()>0) {
			//Cluster
			if(hosts.contains(",")) {
				getLogger().info("Trying to create Redis with hosts list " + hosts);
				JedisCluster jedisCluster;
				String[] pairs = hosts.split(",");
				Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
				for(int i=0;i<pairs.length;i++) {
					String[] pair = pairs[i].split(":");
					jedisClusterNodes.add(new HostAndPort(pair[0], Integer.parseInt(pair[1])));
				}

				if(password != null && password.length()>0) {
					jedisCluster = new JedisCluster(jedisClusterNodes, S_TIMEOUT, S_TIMEOUT, S_ATTEMPS, password, new GenericObjectPoolConfig());
				} else {
					jedisCluster = new JedisCluster(jedisClusterNodes);
				}

				//Creation of Pool
				Field connectionHandlerField;
				try {
					connectionHandlerField = JedisCluster.class.getDeclaredField("connectionHandler");
					connectionHandlerField.setAccessible(true);
					jedisConnHandler = (JedisClusterConnectionHandler)connectionHandlerField.get(jedisCluster);
				} catch (Exception e) {
					Utils.throwException(e);
				}
				getLogger().info("Done creating Redis with hosts list");

			} 
			//Single Node
			else {
				getLogger().info("Trying to create Redis with single host " + hosts);
				noPool = (parameters != null && parameters.contains("nopool"));
				String[] pair = hosts.split(":");
		        
		        if(!noPool) {
		        	getLogger().info("Creating Redis with single host and pool");
		        	JedisPoolConfig poolConfig = new JedisPoolConfig();
			        poolConfig.setMaxWaitMillis(S_TIMEOUT);
					if(password != null && password.length()>0) {
						jedisPool = new JedisPool(poolConfig, pair[0], Integer.parseInt(pair[1]), S_TIMEOUT, password, useSSL);
					} else {
						jedisPool = new JedisPool(poolConfig, pair[0], Integer.parseInt(pair[1]), S_TIMEOUT, useSSL);
					}
		        } else {
		        	getLogger().info("Creating Redis with single host with pool disabled");
		        	if(password != null && password.length()>0) {
						jedis = new Jedis(pair[0], Integer.parseInt(pair[1]), S_TIMEOUT, useSSL);
						jedis.auth(password);
					} else {
						jedis = new Jedis(pair[0], Integer.parseInt(pair[1]), S_TIMEOUT, useSSL);
					}
		        }
				getLogger().info("Done creating Redis with single host");
			}
		} else {
			Utils.throwException(new Exception("Cache Host is empty"));
		}
	}

	public boolean isValid() {
		try {
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	private Jedis getJedis() {
		if(jedisConnHandler != null) {
			return ((JedisSlotBasedConnectionHandler)jedisConnHandler).getConnection();
		} else {
			if(!noPool) {
				return jedisPool.getResource();
			} else {
				return jedis;
			}
		}
	}
	
	private void releaseJedis(Jedis jedis) {
		jedis.disconnect();
		jedis.close();
	}
	
	public Map<String,String> hgetAll(String cacheName) {
		Map<String,String> result;
		Jedis jedis = getJedis();
		try {
			result = jedis.hgetAll(cacheName);
		} finally {
			releaseJedis(jedis);
		}

		return result;
	}

	public String hget(String cacheName, String key) {
		String result;
		Jedis jedis = getJedis();
		try {
			result = jedis.hget(cacheName, key);
		} finally {
			releaseJedis(jedis);
		}

		return result;
	}

	public void hset(String cacheName, String key, String value) {
		Jedis jedis = getJedis();
		try {
			jedis.hset(cacheName, key, value);
		} finally {
			releaseJedis(jedis);
		}
	}

	public void hdel(String cacheName) {
		Jedis jedis = getJedis();
		try {
			jedis.del(cacheName);
		} finally {
			releaseJedis(jedis);
		}
	}

	public void hdel(String cacheName, String key) {
		Jedis jedis = getJedis();
		try {
			jedis.hdel(cacheName, key);
		} finally {
			releaseJedis(jedis);
		}
	}
	
	private Logger getLogger() {
		try {
			return ExecutionUtil.getBaseLogger();
		} catch (Exception e){
			return Logger.getLogger(this.getClass().getName());
		}
	}
}

class Utils
{
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void throwException(Throwable exception, Object dummy) throws T
	{
		throw (T) exception;
	}

	public static void throwException(Throwable exception)
	{
		Utils.<RuntimeException>throwException(exception, null);
	}
}