//package com.boomi.proserv.caching.test;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Properties;
//import java.util.logging.Logger;
//
//import com.boomi.connector.api.Payload;
//import com.boomi.document.api.InboundDocument;
//import com.boomi.document.api.InboundDocumentGroup;
//import com.boomi.document.api.OutboundDocument;
//import com.boomi.document.api.OutboundDocumentGroup;
//import com.boomi.document.scripting.DataContextImpl;
//
///**
// * Tester class, please run it and provide the properties using -Dproperty=value
// * @author anthony.rabiaza@gmail.com
// *
// */
//class StringOutputStream extends OutputStream {
//
//	StringBuilder mBuf = new StringBuilder();
//
//	public String getString() {
//		return mBuf.toString();
//	}
//
//	@Override
//	public void write(int i) throws IOException {
//		mBuf.append(i);
//	}
//}
//
///**
// * Tester class, please run it and provide the properties using -Dproperty=value <br/>
// * You can add:
// * -Dcom.boomi.proserv.caching.Cache.dynamic_process_properties_filter="param_.*" to take only the HTTP Param or
// * -Dcom.boomi.proserv.caching.Cache.dynamic_process_properties_filter="(query_.*)|(param_.*)"
// * @author anthony.rabiaza@gmail.com
// *
// */
//public class CacheTester {
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		DataContextImpl dataContext = new DataContextImpl(new InboundDocumentGroup() {
//			@Override
//			public Iterator<InboundDocument> iterator() {
//				return Collections.<InboundDocument>emptyList().iterator();
//			}
//		}, new OutboundDocumentGroup() {
//
//			@Override
//			public OutputStream getOutputStream() {
//				return new StringOutputStream();
//			}
//
//			@Override
//			public OutboundDocument generateDocument() {
//				return new OutboundDocument() {
//					
//					@Override
//					public void close() throws IOException {}
//					
//					@Override
//					public void setProperty(String paramString1, String paramString2) {}
//					
//					@Override
//					public void writeOutputStream() throws IOException {}
//					
//					@Override
//					public void write(Payload paramPayload) {}
//					
//					@Override
//					public OutputStream getOutputStream() {
//						return new StringOutputStream();
//					}
//					
//					@Override
//					public Logger getLogger() {
//						return null;
//					}
//				};
//			}
//
//			@Override
//			public void addInputs(Iterable<InboundDocument> arg0) {}
//
//			@Override
//			public void addInputs(InboundDocument... arg0) {}
//		}, true);
//		Properties properties = new Properties();
//		try {
//			properties.put("param_1", "value1");
//			properties.put("param_2", "value2");
//			
//			//dataContext.storeStream(new ByteArrayInputStream("You should store this".getBytes()), properties);
//			
//			com.boomi.proserv.caching.Cache.set("javaCache", "hello", "value");
//			
//			for(int i=0;i<50;i++) {
//				com.boomi.proserv.caching.Cache.set("javaCache", "hello" + i, i + "_" + Integer.toString(Integer.valueOf(String.valueOf(i), 16)));
//				Thread.sleep(10);
//			}
//			
//			System.out.println("Sleeping for 10 secs");
//			Thread.sleep(10000);
//			Map<String,String> map = com.boomi.proserv.caching.Cache.getAll("javaCache");
//			
//			for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
//				String key = iterator.next();
//				String value = map.get(key);
//				System.out.println("K:" + key + ",V:" + value);
//			}
//			
//			System.out.println("Sleeping for 5 minutes");
//			System.out.println("Bye");
//			System.exit(0);
//			
//			
//			for(int i=0;i<50;i++) {
//				System.out.println(
//						"Interation " + i + ", " +
//						"Output: "+ com.boomi.proserv.caching.Cache.get("javaCache", "hello" + i)
//						);
//				Thread.sleep(10);
//			}
//			
//			System.out.println("Sleeping for 5 minutes");
//			Thread.sleep(5 * 60 * 1000);
//			
//			
////			com.boomi.proserv.caching.Cache.set(dataContext, "javaCache");
////
////			System.out.println(
////					"Output: "+ com.boomi.proserv.caching.Cache.get(dataContext, "javaCache")
////					);
////			
////			System.out.println(
////					"Output: "+ com.boomi.proserv.caching.Cache.get("javaCache", "hello")
////					);
////			System.out.println(
////					com.boomi.proserv.caching.Cache.getInstance().computeKey(properties)
////					);
////			properties.put("inuser", "value3");
////			System.out.println(
////					com.boomi.proserv.caching.Cache.getInstance().computeKey(properties)
////					);
////			properties.remove("inuser");
////			System.out.println(
////					com.boomi.proserv.caching.Cache.getInstance().computeKey(properties)
////					);
//			
//			com.boomi.proserv.caching.Cache.getInstance().delete("javaCache", "hello");
//			com.boomi.proserv.caching.Cache.getInstance().delete("javaCache");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//}
