package com.boomi.connector.caching;

import java.util.logging.Logger;

import com.boomi.connector.api.GetRequest;
import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.util.BaseGetOperation;
import com.boomi.proserv.caching.CacheInstance;

public class CacheGetOperation extends BaseGetOperation {

	protected CacheGetOperation(CacheConnection conn) {
		super(conn);
	}

	@Override
	protected void executeGet(GetRequest request, OperationResponse response) {
		
		Logger logger = response.getLogger();
		logger.fine("ARA: executeGet received");
		
		String cacheName = getContext().getOperationProperties().getProperty("cache_name");
		logger.fine("ARA: CacheName " + cacheName);
		
		boolean autoKey = getContext().getOperationProperties().getBooleanProperty("auto_key");
		logger.fine("ARA: AutoKey " + autoKey);
		
		boolean throwException = getContext().getOperationProperties().getBooleanProperty("throw_exception");
		logger.fine("ARA: ThrowException " + throwException);
		
		boolean wrapInProfile = getContext().getOperationProperties().getBooleanProperty("wrap_inprofile");
		logger.fine("ARA: WrapInProfile " + wrapInProfile);
		
		ObjectIdData input = request.getObjectId();
		
		try {
			String objectId = input.getObjectId();
			
			if(autoKey) {
				objectId = getConnection().computeKey(CacheInstance.getContextProperties());
			}
			logger.info("ARA: ID " + objectId);
			
			String cachedValue = getConnection().get(cacheName, objectId);
			
			if(throwException && cachedValue==null) {
				ResponseUtil.addExceptionFailure(response, input, new Exception("Value not found in the Cache"));
			}

			logger.fine("ARA: CacheValue received:" + cachedValue);

			if(cachedValue != null) {
				logger.fine("ARA: Cache Hit");
				if(wrapInProfile) {
					response.addResult(input, OperationStatus.SUCCESS, "200", "OK", ResponseUtil.toPayload("<Get><ID>"+objectId+"</ID><Value>"+cachedValue+"</Value></Get>"));
				} else {
					response.addResult(input, OperationStatus.SUCCESS, "200", "OK", ResponseUtil.toPayload(cachedValue));
				}
			} else {
				//Return null
				logger.fine("ARA: Cache Empty");
				response.addEmptyResult(input, OperationStatus.SUCCESS, "200", "OK");
			}

		}catch (Exception e) {
			ResponseUtil.addExceptionFailure(response, input, e);
		}
	}

	@Override
	public CacheConnection getConnection() {
		return (CacheConnection) super.getConnection();
	}
}