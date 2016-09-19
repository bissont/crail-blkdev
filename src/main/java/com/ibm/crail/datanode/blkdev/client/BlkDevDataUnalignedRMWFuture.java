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

package com.ibm.crail.datanode.blkdev.client;

import com.ibm.crail.datanode.DataResult;
import com.ibm.crail.namenode.protocol.BlockInfo;
import com.ibm.crail.utils.CrailUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class BlkDevDataUnalignedRMWFuture extends BlkDevDataUnalignedFuture {

	private static final Logger LOG = CrailUtils.getLogger();

	private Future<DataResult> future;

	private volatile boolean done;

	public BlkDevDataUnalignedRMWFuture(BlkDevDataNodeEndpoint endpoint, ByteBuffer buffer, BlockInfo remoteMr, long remoteOffset,
	                                    ByteBuffer stagingBuffer) throws NoSuchFieldException, IllegalAccessException {
		super(endpoint, buffer, remoteMr, remoteOffset, stagingBuffer);
		future = null;
		done = false;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void signal(int result) throws IOException, InterruptedException {
		if (result >= 0) {
			long srcAddr = BlkDevDataNodeUtils.getAddress(buffer) + localOffset;
			long dstAddr = BlkDevDataNodeUtils.getAddress(stagingBuffer) + BlkDevDataNodeUtils.fileBlockOffset(remoteOffset);
			unsafe.copyMemory(srcAddr, dstAddr, len);

			stagingBuffer.clear();
			int alignedLen = (int) BlkDevDataNodeUtils.alignLength(remoteOffset, len);
			stagingBuffer.limit(alignedLen);
			future = endpoint.write(stagingBuffer, null, remoteMr, BlkDevDataNodeUtils.alignOffset(remoteOffset));
		}
		super.signal(result);
	}

	@Override
	public DataResult get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (!done) {
			super.get(timeout, unit);
			future.get(timeout, unit);
			try {
				endpoint.putBuffer(stagingBuffer);
			} catch (IOException e) {
				throw new ExecutionException(e);
			}
			done = true;
		}
		return this;
	}
}
