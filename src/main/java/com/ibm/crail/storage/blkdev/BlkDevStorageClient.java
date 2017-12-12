package com.ibm.crail.storage.blkdev;

import com.ibm.crail.conf.CrailConfiguration;
import com.ibm.crail.metadata.DataNodeInfo;
import com.ibm.crail.storage.StorageClient;
import com.ibm.crail.storage.StorageEndpoint;
import com.ibm.crail.utils.CrailUtils;
import com.ibm.crail.storage.blkdev.client.BlkDevStorageEndpoint;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;

public class BlkDevStorageClient implements StorageClient{
    private static final Logger LOG = CrailUtils.getLogger();
	private HashMap<Long, String> nodeMap;

	public void printConf(Logger logger) {
		BlkDevStorageConstants.printClientConf(logger);
	}

	private long convertIPPortToKey(String ipPort) throws IOException {
		//The first token is the ip and the second is the port
		StringTokenizer tokenizer = new StringTokenizer(ipPort, ":");
		String ip = tokenizer.nextToken();
		byte[] bytes = InetAddress.getByName(ip).getAddress();
		int port = Integer.parseInt(tokenizer.nextToken());

		return DataNodeInfo.calcKey(bytes, port);	
	}

	/*
	 * Read the comma seperated lists of virtual device paths and their corresponding datanodes
	 * (IP/ports) from the configuration file, creating a map from the key (IP/port) to virtual
	 * device path. This is map is then consulted to identify the virtual device to when an Endpoint
	 * is created.
	 */
	private void createHT() throws IOException {
		nodeMap  = new HashMap<Long, String>();
		
		StringTokenizer tokenizer = new StringTokenizer(BlkDevStorageConstants.DATA_PATH, ",");
		if (!tokenizer.hasMoreTokens()){
			throw new IOException("No data paths defined!");
		}

		// read the list of virtual device paths
		List<String> devList= new ArrayList();
		while (tokenizer.hasMoreTokens()) {
			String devName = tokenizer.nextToken();
			devList.add(devName);
		}

		// read the list of ip/ports
		tokenizer = new StringTokenizer(BlkDevStorageConstants.DATA_IP_PORT, ",");
		if (!tokenizer.hasMoreTokens()){
			throw new IOException("No IP/ports defined!");
		}

		List<Long> hashList = new ArrayList();
		while (tokenizer. hasMoreTokens()) {
			Long key = convertIPPortToKey(tokenizer.nextToken());
			hashList.add(key);
		}

		if (hashList.size() != devList.size()){
			throw new IOException("Device and ip/port list must be the same size" +
				hashList.size() + ":" + devList.size());
		}

		// Add the tuples into the hashMap
		for (int i = 0; i <devList.size(); i++) {
			Long hash = hashList.get(i);
			String devName = devList.get(i);
			nodeMap.put(hash,devName);
		}
	}


	public void init(CrailConfiguration crailConfiguration, String[] args) throws IOException {
		BlkDevStorageConstants.updateClientConstants(crailConfiguration);
		createHT();
		BlkDevStorageConstants.verify();
	}

	public StorageEndpoint createEndpoint(DataNodeInfo info) throws IOException {
		String vDevPath = nodeMap.get(info.key());
		return new BlkDevStorageEndpoint(vDevPath);
	}

	public void close() throws Exception {
	}

}
