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
import com.ibm.crail.conf.CrailConstants;
import org.slf4j.Logger;

import java.io.IOException;

public class BlkDevStorageConstants {

	private final static String PREFIX = "crail.storage.blkdev";

	// target:interface to access storage device
	public static final String STORAGE_BLKDEV_INTERFACE_KEY = "interface";
	public static String STORAGE_BLKDEV_INTERFACE = "eth5";

	// target port to access storage device
	public static final String STORAGE_BLKDEV_PORT_KEY = "port";
	public static int STORAGE_BLKDEV_PORT  = 12345;

	// target: size of storage path
	public static final String STORAGE_SIZE_KEY = "storagesize";
	public static long STORAGE_SIZE = 1073741824; /* 1GB */

	// target allocation unit size
	public static final String ALLOCATION_SIZE_KEY = "allocationsize";
	public static long ALLOCATION_SIZE = 1048576; /* 1MB */

	// client Blk-Dev datanode list (device path)
	public static final String DATA_PATH_KEY = "datapath";
	public static String DATA_PATH;

	// client: Blk-dev datanode list (ip/port)
	public static final String DATA_IP_PORT_KEY = "dataipport";
	public static String DATA_IP_PORT;

	// client
	public static final String QUEUE_DEPTH_KEY = "queuedepth";
	public static int QUEUE_DEPTH = 16;

	// client: total size of all Block Devices
	public static final String STORAGE_LIMIT_KEY = "storagelimit";
	public static long STORAGE_LIMIT = 1073741824; /* 1GB */


	private static String fullKey(String key) {
		return PREFIX + "." + key;
	}

	private static String get(CrailConfiguration conf, String key) {
		return conf.get(fullKey(key));
	}

	/*
	 * Called only from the client side to map device names to block
	 * device servers.
	 */
	public static void updateClientConstants(CrailConfiguration conf) {

		String arg = get(conf, DATA_PATH_KEY);
		if (arg != null) {
			DATA_PATH = arg;
		}

		arg = get(conf, DATA_IP_PORT_KEY);
		if (arg != null) {
			DATA_IP_PORT= arg;
		}

		arg = get(conf, QUEUE_DEPTH_KEY);
		if (arg != null) {
			QUEUE_DEPTH = Integer.parseInt(arg);
		}

		arg = get(conf, STORAGE_LIMIT_KEY);
		if (arg != null) {
			STORAGE_LIMIT = Long.parseLong(arg);
		}
	}

	/*
	 * Called only from the target size 
	 */
	public static void updateTargetConstants(CrailConfiguration conf) {
		
		String arg = get(conf, STORAGE_BLKDEV_INTERFACE_KEY);
		if (arg != null) {
			STORAGE_BLKDEV_INTERFACE = arg;
		}
		
		arg = get(conf, STORAGE_BLKDEV_PORT_KEY);
		if (arg != null) {
			STORAGE_BLKDEV_PORT = Integer.parseInt(arg);
		}

		arg = get(conf, STORAGE_SIZE_KEY);
		if (arg != null) {
			STORAGE_SIZE = Long.parseLong(arg);
		}

		arg = get(conf, ALLOCATION_SIZE_KEY);
		if (arg != null) {
			ALLOCATION_SIZE = Long.parseLong(arg);
		}
		
	}

	public static void printClientConf(Logger logger) {
		logger.info(fullKey(DATA_PATH_KEY) + " " + DATA_PATH);
		logger.info(fullKey(DATA_IP_PORT_KEY) + " " + DATA_IP_PORT);
		logger.info(fullKey(QUEUE_DEPTH_KEY) + " " + QUEUE_DEPTH);
		logger.info(fullKey(STORAGE_LIMIT_KEY) + " " + STORAGE_LIMIT);
	}
	
	public static void printTargetConf(Logger logger) {
		logger.info(fullKey(STORAGE_SIZE_KEY) + " " + STORAGE_SIZE);
		logger.info(fullKey(ALLOCATION_SIZE_KEY) + " " + ALLOCATION_SIZE);
		logger.info(fullKey(STORAGE_BLKDEV_INTERFACE_KEY) + " " + STORAGE_BLKDEV_INTERFACE);
		logger.info(fullKey(STORAGE_BLKDEV_PORT_KEY) + " " + STORAGE_BLKDEV_PORT);
	}

	public static void verify() throws IOException {
		if (ALLOCATION_SIZE % CrailConstants.BLOCK_SIZE != 0){
			throw new IOException("allocationsize must be multiple of crail.blocksize");
		}
		if (STORAGE_SIZE % ALLOCATION_SIZE != 0){
			throw new IOException("storageSize must be multiple of allocationSize");
		}
	}

	public static void init(CrailConfiguration conf, String[] args) throws Exception{
		BlkDevStorageConstants.updateTargetConstants(conf);
	}
}
