package com.boomi.connector.demo;
import com.boomi.connector.api.OperationType;
import com.boomi.connector.caching.CacheConnector;
import com.boomi.connector.testutil.ConnectorTester;

public class CacheConnectorTest {
	
	public void testGetOperation() throws Exception
	{
		CacheConnector connector = new CacheConnector();
		ConnectorTester tester = new ConnectorTester(connector);

		// setup the operation context for a GET operation on an object with type "SomeType"
		tester.setOperationContext(OperationType.GET, null, null, "SomeType", null);

		// ... setup the expected output ...
		//List<SimpleOperationResult> expectedResults = ...;

		//tester.testExecuteGetOperation("37", expectedResults);
	}
	public static void main(String[] args) {
		try {
			new CacheConnectorTest().testGetOperation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
