/* interface declaration for RPC */
package org.whispercomm.manes.client;

import org.whispercomm.manes.client.ErrorCode;

interface RemoteMac{

	void initiate(int appId);
	
	ErrorCode send(int appId, in byte[] packet);	
	
	byte[] receive(int appId, long timeout);
	
	void disconnect(int appId);

	boolean registered();

}