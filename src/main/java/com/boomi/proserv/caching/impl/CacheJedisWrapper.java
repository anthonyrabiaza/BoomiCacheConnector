package com.boomi.proserv.caching.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * Class Wrapper with Jedis Implementation
 * 
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
	
	public Map<String, String> getAll(String hashVal, Long ttl) {
		getLogger().fine("getAll with args hashVal, ttl : " + hashVal + ", " + ttl);
		HashMap<String, String> result = new HashMap<String, String>();
		Jedis jedis = getJedis();
		Map<String, Response<String>> responses = new HashMap<>();
		List<String> keys;
		Pipeline p;
		try {
			ScanParams scanParams = new ScanParams().count(100).match(hashVal);
			String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
			do {
				ScanResult<String> scanResult = jedis.scan(cur, scanParams);
				keys = scanResult.getResult();
				p = jedis.pipelined();
				// work with result
				for (String thisKey : keys) {
					responses.put(thisKey, p.get(thisKey));
				}
				p.sync();
				for (String thisKey : responses.keySet()) {
					Response<String> r = (Response<String>) responses.get(thisKey);
					result.put(thisKey, r.get());
					getLogger().finest("retrieved key: " + thisKey + " and value: " + r.get());
				}
				cur = scanResult.getCursor();
			} while (!cur.equals(redis.clients.jedis.ScanParams.SCAN_POINTER_START));
		} finally {
			releaseJedis(jedis);
		}

		return result;
	}

	public String get(String hashVal, Long ttl) {
		String result;
		Jedis jedis = getJedis();
		try {
			result = jedis.get(hashVal);
			if (ttl != -1) {
				jedis.pexpire(hashVal, ttl);
			}
			;
		} finally {
			releaseJedis(jedis);
		}

		return result;
	}

	public void set(String hashVal, String value, Long ttl) {
		Jedis jedis = getJedis();
		try {
			if (ttl != -1) {
				jedis.psetex(hashVal, ttl, value);
			} else {
				jedis.set(hashVal, value);
			}
		} finally {
			releaseJedis(jedis);
		}
	}

	public void del(String hashVal) {
		Jedis jedis = getJedis();
		try {
			jedis.del(hashVal);
		} finally {
			releaseJedis(jedis);
		}
	}

	public void delAll(String hashVal) {
		getLogger().fine("delAll with args hashVal: " + hashVal);
		Jedis jedis = getJedis();
		long numDel;
		try {
			ScanParams scanParams = new ScanParams().count(100).match(hashVal);
			String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
			do {
				ScanResult<String> scanResult = jedis.scan(cur, scanParams);
				String[] arrKeys = Arrays.copyOf(scanResult.getResult().toArray(), scanResult.getResult().size(),
						String[].class);
				if (arrKeys.length > 0) {
					getLogger().fine("Attempting to del " + arrKeys.length + " keys");
					numDel = jedis.del(arrKeys);
					if (numDel != arrKeys.length) {
						getLogger().warning("Could not delete " + (arrKeys.length - numDel) + " entries from Redis");
					}
				} else {
					getLogger().fine("Nothing to delete");
				}

				cur = scanResult.getCursor();
			} while (!cur.equals(redis.clients.jedis.ScanParams.SCAN_POINTER_START));
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