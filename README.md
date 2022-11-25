# Boomi Cache Connector v2 (with TTL)

This repository is deprecated, please visit [Boomi Bitbucket](https://bitbucket.org/officialboomi/boomicacheconnector/src/master/)

I wanted to share a solution I recently developed to have high-throughput in the Dell Boomi AtomSphere Platform: use of Cache Pattern with In-Memory Data Grid or In-Memory Database (cf [IMDG/IMDB](https://en.wikipedia.org/wiki/List_of_in-memory_databases). 
The Connector is designed to allow the use of a Caching/In-Memory Data Grid/Database in Boomi and thus, provide high-throughput APIs and Processes which stored information in Memory and avoid unnecessary calls to the backend system for read/query scenarios.

Boomi Cache Connector is a Generic Connector which will allows you to connect to any Cache system. Initially, it is supporting **Ehcache**,  **Redis** (Standalone or Clustered) and **memcached**.

![Alt text](resources/BoomiCache_Connector.png?raw=true "BoomiCache")

The Boomi Cache Connector can accelerate: 

- ![Alt text](resources/API.png?raw=true "BoomiCache") **RESTFul APIs**, the Connector can **automatically calculate** the caching key based on the HTTP queries and parameters provided by the API Consumer.
- ![Alt text](resources/Process.png?raw=true "BoomiCache") **Integration Processes**, you will provide the key and the value to store.


The Cache Connector will access the Cache system, the **Cache** system will contains several **CacheObjects**. And each **CacheObject** contains a **list of key-value Pair**. The key is a String and the Value can be any Object (String, JSON, XML...)


## Getting Started

Please download the library [connector-archive](BoomiCacheConnector-0.60.zip?raw=true) and the connector descriptor [connector-descriptor](connector-descriptor.xml?raw=true)

### Prerequisites in Boomi

#### Setup of the connector

Please go to Setup>Account>Publisher and fill out the information.

![Alt text](resources/Publisher.png?raw=true "BoomiCache")

And then, go to Setup>Development Resources>Developer and create a new Group by clicking on ![Alt text](resources/Boomi_Developer_Connector_Init.png?raw=true "BoomiCache"). On Initial Connector Display Name, you can put Cache Connector or Cache Connector (Beta). The two files to upload are the files you previous downloaded. For the Vendor Product Version, please mentioned the version of the Zip Archive.

![Alt text](resources/Boomi_Developer_Connector.png?raw=true "BoomiCache")

The result should like that:

![Alt text](resources/Boomi_Developer_Connector_Done.png?raw=true "BoomiCache")


#### Use of the Cache Connector

The configuration of the Cache is done in the Connector:

![Alt text](resources/BoomiCache_Connector_Config.png?raw=true "BoomiCache")

**Three operations** are provided

- Get: Get information from the Cache system based on a key (can be calculated for APIs)
- Upsert: Create or Update information in the Cache system, input is key-value pair
- Delete: Delete a key-value pair based on a key or delete the full Cache Object

Please use the "Browse" Option in the Connector to Generate the Request/Response Profile. You can use Test Atom Cloud or your On-Prem Atom. 

#### Example of RESTFul API 
The Following Process is a Web Services Server process getting information from a connector

![Alt text](resources/Boomi_Process_NoCache.png?raw=true "BoomiCache")

We will update this process to add **Caching Shapes** with the Caching logics:

![Alt text](resources/Boomi_Process_BoomiCacheConnector.png?raw=true "BoomiCache")

In the try path, we are using **Get** operation, the **key is automatically computed** and the **value** returned will be the data in the cache.

![Alt text](resources/Boomi_Op_Get.png?raw=true "BoomiCache")

In case of cache miss, an exception with be thrown and the process will go to the Catch Path.

We are calling the back-end system and store (**Upsert**) the value to the Cache, again the **key is automatically computed** 

![Alt text](resources/Boomi_Op_Upsert.png?raw=true "BoomiCache")

We are not using the **Delete** operation here but it is very similar to **Get** operation and is taking a Key, it you put * as key, it will delete the full Cache object.

### Automatic key calculation
When using RESTFul APIs, you can enable the **Automatic Key Computation** in the operation and avoid providing the key (just put 'auto' in the ID). The connector will use the HTTP queries and params.
By default all dynamic process properties starting with *query_* and *param_* will be used, to change it, set the property in the operation to set the regular expression to filter the dynamic process properties:


## Example of APIs call

### Call of API with JSON results

The following API calls will store some values in Redis using the automatic key generation.
![Alt text](resources/Boomi_API_Call.png?raw=true "BoomiCache")

![Alt text](resources/Boomi_API_Call_2.png?raw=true "BoomiCache")

![Alt text](resources/Boomi_API_Call_3.png?raw=true "BoomiCache")

![Alt text](resources/Boomi_API_Call_4.png?raw=true "BoomiCache")

### Data stored in Redis

You can see the **keys** generated when we are using HTTP Parameters:

![Alt text](resources/Boomi_API_Redis.png?raw=true "BoomiCache")


## Use of Azure Redis Caches

You can use Boomi Connector to connect to an On-Prem Redis and also to Secured Cloud one. On Azure, please select "Redis Caches", select the option of your instance (the Cache Region should be the same as the VM hosting Boomi to minimize latency.
Once all the options are selected, click on "Create Redis Cache"

![Alt text](resources/Azure_Redis_0.png?raw=true "BoomiCache")

Copy the Host name:

![Alt text](resources/Azure_Redis_1.png?raw=true "BoomiCache")

Copy the primary key:

![Alt text](resources/Azure_Redis_2.png?raw=true "BoomiCache")

Paste the Hostname followed by :6380 in the Connector Configuration and paste the Key to the Password Value, please don't forget to check "Use SSL".

For additional security, you can also update the Redis firewall to allow only your VMs (and your local network) to access the instance. 

## Use of Redis on Atom Cloud

Pooling need to be disable to make the Redis connector working on Test Atom Cloud or Atom Cloud. Please set the Maximum Heap or Configuration file to "nopool"

![Alt text](resources/Azure_Redis_Atom_Cloud.png?raw=true "BoomiCache")


## Use of ehcache configuration file

You have to place the following content in a file (for instance /opt/Dell_Boomi/Atom/conf/ehcache.xml), go the connector configuration and set the value of **Maximum Heap or Configuration file (EHCache)** property to the full path of the previous file.

```
<?xml version="1.0" encoding="UTF-8"?>

<ehcache:config xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
	xmlns:ehcache='http://www.ehcache.org/v3'
	xsi:schemaLocation="http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.1.xsd">

	<ehcache:cache alias="BoomiCache">
		<ehcache:key-type>java.lang.String</ehcache:key-type>
		<ehcache:value-type>java.lang.String</ehcache:value-type>
		<ehcache:expiry>
			<ehcache:ttl unit="seconds">10</ehcache:ttl>
		</ehcache:expiry>
		<ehcache:resources>
			<ehcache:heap unit="entries">100</ehcache:heap>
			<ehcache:offheap unit="MB">1</ehcache:offheap>
		</ehcache:resources>
	</ehcache:cache>
	
</ehcache:config>
```

# Update of libraries

To update the internal libraries, open the Archive Zip with your favorite Archive Management tool (7Zip, Winzip, etc.). Go to the lib folder and update the jars:

![Archive](./resources/Archive.png?raw=true)

**List of libraries**

EHCache:

- ehcache-X.Y.Z.jar (https://github.com/ehcache/ehcache3)

MemCached:

- spymemcached-X.Y.Z.jar (https://github.com/couchbase/spymemcachedd)

Redis:

- jedis-X.Y.Z.jar (https://github.com/xetorthio/jedis)
- commons-pool2-X.Y.Z.jar
