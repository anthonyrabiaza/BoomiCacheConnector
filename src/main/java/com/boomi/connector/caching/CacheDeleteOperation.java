package com.boomi.connector.caching;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.boomi.connector.api.DeleteRequest;
import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.util.BaseDeleteOperation;
import com.boomi.proserv.caching.CacheInstance;

/**
 * 
 * @author anthony.rabiaza@gmail.com
 *
 */
public class CacheDeleteOperation extends BaseDeleteOperation {

	protected CacheDeleteOperation(CacheConnection conn) {
		super(conn);
	}

	@Override
	protected void executeDelete(DeleteRequest request, OperationResponse response) {
		Logger logger = response.getLogger();
		logger.fine("ARA: executeDelete received");
		
		String cacheName = getContext().getOperationProperties().getProperty("cache_name");
		logger.fine("ARA: CacheName " + cacheName);
		
		boolean autoKey = getContext().getOperationProperties().getBooleanProperty("auto_key");
		logger.fine("ARA: AutoKey " + autoKey);
		
		int i=0;
		for (ObjectIdData input : request) {
			logger.fine("ARA: Processing input " + i++);
            try {
            	logger.info("ARA: Deleting " + cacheName);
            	String objectId = input.getObjectId();
            	if("*".equals(objectId)) {
            		logger.fine("Found wildcard objectId - executing batch delete");
            		getConnection().delete(cacheName);
            	} else {
            		if(autoKey) {
            			objectId = getConnection().computeKey(CacheInstance.getContextProperties());
            		}
            		getConnection().delete(cacheName, objectId);
            	}
                response.addEmptyResult(input, OperationStatus.SUCCESS, "200", "OK");
            }
            catch (Exception e) {
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