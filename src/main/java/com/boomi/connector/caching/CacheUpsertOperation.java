package com.boomi.connector.caching;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.boomi.connector.api.ObjectData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.api.UpdateRequest;
import com.boomi.connector.util.BaseUpdateOperation;
import com.boomi.proserv.caching.CacheInstance;
import com.boomi.proserv.caching.CacheUtils;


public class CacheUpsertOperation extends BaseUpdateOperation {

	protected CacheUpsertOperation(CacheConnection conn) {
		super(conn);
	}

	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
		Logger logger = response.getLogger();
		logger.fine("ARA: executeUpdate received");
		
		String cacheName = getContext().getOperationProperties().getProperty("cache_name");
		logger.fine("ARA: CacheName " + cacheName);
		
		boolean autoKey = getContext().getOperationProperties().getBooleanProperty("auto_key");
		logger.fine("ARA: AutoKey " + autoKey);
		
		//String objectType = getContext().getObjectTypeId();
		
		int i=0;
		for (ObjectData input : request) {
			logger.fine("ARA: Processing input " + i++);
			try {
				String inputStr = CacheUtils.inputStreamToString(input.getData());
				Document doc = CacheUtils.parse(CacheUtils.stringToInputStream(inputStr));
				//logger.info("ARA: Content " + CacheUtils.toString(doc));
				String objectId = CacheUtils.getFirstNodeTextContent(doc, "//Upsert/ID");
				if(autoKey) {
					objectId = getConnection().computeKey(CacheInstance.getContextProperties());
				}
				getConnection().upsert(cacheName, objectId, CacheUtils.getFirstNodeTextContent(doc, "//Upsert/Value"));

				response.addResult(input, OperationStatus.SUCCESS, "200", "OK", ResponseUtil.toPayload(CacheUtils.stringToInputStream(inputStr)));
			} catch (Exception e) {
				// make best effort to process every input
				logger.log(Level.SEVERE, "Details of Exception:", e);
				ResponseUtil.addExceptionFailure(response, input, e);
			}
		}
		logger.fine("ARA: End of processing");
	}

	@Override
	public CacheConnection getConnection() {
		return (CacheConnection) super.getConnection();
	}
}