package com.boomi.connector.caching;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;

public class CacheConnector extends BaseConnector {

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new CacheBrowser(createConnection(context));
    }    

    @Override
    protected Operation createGetOperation(OperationContext context) {
        return new CacheGetOperation(createConnection(context));
    }

    @Override
    protected Operation createUpsertOperation(OperationContext context) {
        return new CacheUpsertOperation(createConnection(context));
    }

    @Override
    protected Operation createDeleteOperation(OperationContext context) {
        return new CacheDeleteOperation(createConnection(context));
    }
   
    private CacheConnection createConnection(BrowseContext context) {
        return new CacheConnection(context);
    }
}