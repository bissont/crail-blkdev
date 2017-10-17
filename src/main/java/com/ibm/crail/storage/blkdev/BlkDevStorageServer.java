/*
 * Crail: A Multi-tiered Distributed Direct Access File System
 *
 * Author:
 * Jonas Pfefferle <jpf@zurich.ibm.com>
 *
 * Copyright (C) 2016, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ibm.crail.storage.blkdev;


import com.ibm.crail.conf.CrailConfiguration;
import com.ibm.crail.storage.StorageServer;
import com.ibm.crail.storage.StorageResource;
import com.ibm.crail.storage.blkdev.client.BlkDevStorageEndpoint;
import com.ibm.crail.utils.CrailUtils;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlkDevStorageServer implements StorageServer {
	private static final Logger LOG = CrailUtils.getLogger();

	private InetSocketAddress storageAddr;
	private Path path;
	private boolean isAlive;
	private boolean initialized = false;
	private long addr;
	private long alignedSize;
	private BlkDevStorageEndpoint endPoint;

	public BlkDevStorageServer() throws Exception {

	}
	public void init(CrailConfiguration crailConfiguration, String[] args) throws Exception {
		if (initialized)	 {
			throw new IOException("BlkDevStorageTier already initialized");
		}
		initialized = true;
		BlkDevStorageConstants.init(crailConfiguration, args);
		LOG.info("initalizing block device datanode");
		// We do not support multiple block devices yet
		InetAddress address = InetAddress.getLoopbackAddress();
		int port = 12345;

		storageAddr = new InetSocketAddress(address, port);
		String directory = BlkDevStorageConstants.DATA_PATH;
		path = FileSystems.getDefault().getPath(directory);
		if (!Files.exists(path)) {
			throw new IllegalArgumentException("BlkDev path does not exists!");
		}
		isAlive = true;
		alignedSize = BlkDevStorageConstants.STORAGE_LIMIT - (BlkDevStorageConstants.STORAGE_LIMIT % BlkDevStorageConstants.ALLOCATION_SIZE);
		alignedSize = BlkDevStorageConstants.STORAGE_LIMIT;
		this.addr = 0;
		endPoint = new BlkDevStorageEndpoint();
	}

	@Override
	public void printConf(Logger log) {
		BlkDevStorageConstants.printConf(log);
	}

	@Override
	public StorageResource allocateResource() throws Exception {
		StorageResource resource = null;

		if (alignedSize > 0) {
			LOG.info("new block, length " + BlkDevStorageConstants.ALLOCATION_SIZE);
			LOG.debug("block stag 0, addr 0, length " + BlkDevStorageConstants.ALLOCATION_SIZE);
			alignedSize -= BlkDevStorageConstants.ALLOCATION_SIZE;
			resource = StorageResource.createResource(addr, (int) BlkDevStorageConstants.ALLOCATION_SIZE, 0);
			addr += BlkDevStorageConstants.ALLOCATION_SIZE;
		}
		return resource;
	}

	public void run() {
		LOG.info("BlkDataNodeSErver started");
	}

	public boolean isAlive() {
		return isAlive;
	}

	public InetSocketAddress getAddress() {
		return storageAddr;
	}
}
