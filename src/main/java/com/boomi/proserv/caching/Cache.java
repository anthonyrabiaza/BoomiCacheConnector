//package com.boomi.proserv.caching;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Map;
//import java.util.Properties;
//import java.util.logging.Logger;
//
//import com.boomi.document.scripting.DataContextImpl;
//import com.boomi.execution.ExecutionUtil;
//
///**
// * Cache Class, this class is the only element used in the Groovy Script of Boomi.
// * To run locally (outside a Boomi context) add -Dcom.boomi.proserv.caching.Cache.standalone=true
// * By default the Cache will be an embedded EHCache to change the cache implementation, update boomicache.properties to set the type:
// * com.boomi.proserv.caching.Cache.type=com.boomi.proserv.caching.impl.CacheEHCache (default)
// * OR
// * com.boomi.proserv.caching.Cache.type=com.boomi.proserv.caching.impl.CacheRedis
// * @author anthony.rabiaza@gmail.com
// *
// */
//public class Cache {
//
//	private static CacheInstance s_cache 				= null; 
//
//	private static String s_dynamic_process_properties_filter;
//	private static String s_type;
//	private static boolean s_standalone;
//	private static boolean s_hashing = true;
//	
//	static {
//		init();
//	}
//	
//	/**
//	 * This method can be call to reinit the Cache after a change in boomicache.properties to avoid a restart
//	 */
//	public static void init() {
//		
//		s_standalone 	= Boolean.valueOf(System.getProperty(Cache.class.getName() + ".standalone"));
//		s_hashing 		= Boolean.valueOf(getProperties().getProperty(Cache.class.getName() + ".hashing"));
//		s_dynamic_process_properties_filter = getProperties().getProperty(Cache.class.getName() + ".dynamic_process_properties_filter");
//		if(s_dynamic_process_properties_filter == null || (s_dynamic_process_properties_filter != null && s_dynamic_process_properties_filter.length()==0)) {
//			s_dynamic_process_properties_filter = "(query_.*)|(param_.*)";
//		}
//
//		s_type = getProperties().getProperty(Cache.class.getName() + ".type");
//
//		s_cache = new CacheInstance();
//		s_cache.setStandalone(s_standalone);
//		s_cache.setType(s_type);
//		s_cache.setDynamicProcessPropertiesFilter(s_dynamic_process_properties_filter);
//		s_cache.setHashing(s_hashing);
//		s_cache.setProperties(getProperties());
//		s_cache.init();
//	}
//
//	private static Properties getProperties() {
//		Properties properties = new Properties();
//		try {
//			String location = new File("").getAbsoluteFile().toString() + "/conf/";
//			properties.load(new FileInputStream(location+ "boomicache.properties"));
//		} catch (IOException e) {
//			getLogger().warning(e.getMessage());
//		}
//		return properties;
//	}
//	
//	public static CacheInstance getInstance() {
//		if(s_cache.isValid()) {
//			return s_cache;
//		} else {
//			init();
//			return s_cache;
//		}
//	}
//
//	/*Automatically calculate the key (based on Dynamic Process Properties), value to store will be the full Document*/
//
//	/**
//	 * Store the current document to cache using the auto-calculated key.
//	 * @param dataContext
//	 * @param cacheName
//	 * @throws IOException
//	 */
//	public static void set(DataContextImpl dataContext, String cacheName) throws IOException {
//		getInstance().set(dataContext, cacheName);
//	}
//
//	/**
//	 * Get the current document to cache using the auto-calculated key.
//	 * Also a Dynamic Process property call cache_hit will be set to true (if value is found) of false (otherwise)
//	 * @param dataContext
//	 * * @param cacheName
//	 * @return
//	 */
//	public static String get(DataContextImpl dataContext, String cacheName) {
//		return getInstance().get(dataContext, cacheName);
//	}
//
//	/*End*/
//
//	/*Key is provided, value to store will be the full Document*/
//
//	/**
//	 * Store the current document to cache using the provided key
//	 * @param dataContext
//	 * * @param cacheName
//	 * @param key
//	 * @throws IOException
//	 */
//	public static void set(DataContextImpl dataContext, String cacheName, String key) throws IOException {
//		getInstance().set(dataContext, cacheName, key);
//	}
//
//	/**
//	 * Get the current document to cache using the provided key.
//	 * Also a Dynamic Process property call cache_hit will be set to true (if value is found) of false (otherwise)
//	 * @param dataContext
//	 * @param cacheName
//	 * @param key
//	 * @return
//	 */
//	public static String get(DataContextImpl dataContext, String cacheName, String key) {
//		return getInstance().get(dataContext, cacheName);
//	}
//
//	/*End*/
//
//	/*Provide the key and value*/
//
//	/**
//	 * Store the current value to cache using the provided key.
//	 * @param cacheName
//	 * @param key
//	 * @param value
//	 * @throws IOException
//	 */
//	public static void set(String cacheName, String key, String value) throws IOException {
//		getInstance().set(cacheName, key, value);
//	}
//
//	/**
//	 * Get the current value from cache using the provided key.
//	 * @param cacheName
//	 * @param key
//	 * @return
//	 */
//	public static String get(String cacheName, String key) {
//		return getInstance().get(cacheName, key);
//	}
//	
//	/**
//	 * Get all the keys,values from cach.
//	 * @param cacheName
//	 * @return
//	 */
//	public static Map<String,String> getAll(String cacheName) {
//		return getInstance().get(cacheName);
//	}
//	
//	/**
//	 * Delete the full content of the Cache
//	 * @param cacheName
//	 */
//	public static void delete(String cacheName) {
//		getInstance().delete(cacheName);
//	}
//
//	private static Logger getLogger() {
//		try {
//			return ExecutionUtil.getBaseLogger();
//		} catch (Exception e){
//			return Logger.getLogger(Cache.class.getName());
//		}
//	}
//
//	/*End*/
//
//	/**
//	 * Utility to convert InputStream to String
//	 * @param is
//	 * @return
//	 * @throws IOException
//	 */
//	public static String inputStreamToString(InputStream is) throws IOException {
//		return CacheUtils.inputStreamToString(is);
//	}
//
//	/**
//	 * Utility to convert String to InputStream
//	 * @param str
//	 * @return
//	 * @throws IOException
//	 */
//	public static InputStream stringToInputStream(String str) throws IOException {
//		return CacheUtils.stringToInputStream(str);
//	}
//	
//	/*
//	 * Dynamic Document Properties
//	 * ["document.dynamic.userdefined.inheader_Accept-Encoding":"gzip,deflate", "document.dynamic.userdefined.sourceType":"document", "document.dynamic.userdefined.dataType":"dynamic", "document.dynamic.userdefined.type":"userdefined"]
//	 * Dynamic Process Properties
//	 * ["query_param2":"value2", "query_param1":"value1", "inpath":"/mobilecatalog/Mobile", "inmethod":"GET", "inuser":"boomi_anthonyrabiaza-3IG5LK"]
//	 */
//
//}
