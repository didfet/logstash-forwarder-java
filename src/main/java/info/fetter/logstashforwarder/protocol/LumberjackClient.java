package info.fetter.logstashforwarder.protocol;

/*
 * Copyright 2015 Didier Fetter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import info.fetter.logstashforwarder.Event;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class LumberjackClient {
	private final static byte PROTOCOL_VERSION = 0x31;
	private final static byte FRAME_ACK = 0x41;
	private final static byte FRAME_WINDOW_SIZE = 0x57;
	private final static byte FRAME_DATA = 0x44;
	private final static byte FRAME_COMPRESSED = 0x43;

	private SSLSocket socket;
	private KeyStore keyStore;
	private String server;
	private int port;
	private DataOutputStream output;
	private DataInputStream input;
	private int sequence = 1;

	public LumberjackClient(String keyStorePath, String server, int port) throws IOException {
		this.server = server;
		this.port = port;

		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream(keyStorePath), null);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			tmf.init(keyStore);

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);

			SocketFactory socketFactory = context.getSocketFactory();
			socket = (SSLSocket)socketFactory.createSocket(this.server, this.port);
			socket.setUseClientMode(true);
			socket.startHandshake();

			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			input = new DataInputStream(socket.getInputStream());
		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int sendWindowSizeFrame(int size) throws IOException {
		output.writeByte(PROTOCOL_VERSION);
		output.writeByte(FRAME_WINDOW_SIZE);
		output.writeInt(size);
		output.flush();
		return 6;
	}

	private int sendDataFrame(DataOutputStream output, Map<String,byte[]> keyValues) throws IOException {
		output.writeByte(PROTOCOL_VERSION);
		output.writeByte(FRAME_DATA);
		output.writeInt(sequence++);
		output.writeInt(keyValues.size());
		int bytesSent = 10;
		for(String key : keyValues.keySet()) {
			int keyLength = key.length();
			output.writeInt(keyLength);
			bytesSent += 4;
			output.write(key.getBytes());
			bytesSent += keyLength;
			byte[] value = keyValues.get(key);
			output.writeInt(value.length);
			bytesSent += 4;
			output.write(value);
			bytesSent += value.length;
		}
		output.flush();
		return bytesSent;
	}

	public int sendDataFrameInSocket(Map<String,byte[]> keyValues) throws IOException {
		return sendDataFrame(output, keyValues);
	}

	public int sendCompressedFrame(List<Map<String,byte[]>> keyValuesList) throws IOException {
		output.writeByte(PROTOCOL_VERSION);
		output.writeByte(FRAME_COMPRESSED);

		ByteArrayOutputStream uncompressedBytes = new ByteArrayOutputStream();
		DataOutputStream uncompressedOutput = new DataOutputStream(uncompressedBytes);
		for(Map<String,byte[]> keyValues : keyValuesList) {
			sendDataFrame(uncompressedOutput, keyValues);
		}
		uncompressedOutput.close();
		Deflater compressor = new Deflater();
		compressor.setInput(uncompressedBytes.toByteArray());
		compressor.finish();

		ByteArrayOutputStream compressedBytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while(!compressor.finished()) {
			int count = compressor.deflate(buffer);
			compressedBytes.write(buffer, 0, count);
		}
		compressedBytes.close();

		output.writeInt(compressor.getTotalOut());
		output.write(compressedBytes.toByteArray());

		return 6 + compressor.getTotalOut();
	}

	public int readAckFrame() throws ProtocolException, IOException {
		byte protocolVersion = input.readByte();
		if(protocolVersion != PROTOCOL_VERSION) {
			throw new ProtocolException("Protocol version should be 1, received " + protocolVersion);
		}
		byte frameType = input.readByte();
		if(frameType != FRAME_ACK) {
			throw new ProtocolException("Frame type should be Ack, received " + frameType);
		}
		int sequenceNumber = input.readInt();
		return sequenceNumber;
	}

	public int sendEvents(List<Event> eventList) throws IOException {
		int beginSequence = sequence;
		int numberOfEvents = eventList.size();
		List<Map<String,byte[]>> keyValuesList = new ArrayList<Map<String,byte[]>>(numberOfEvents);
		for(Event event : eventList) {
			keyValuesList.add(event.getKeyValues());
		}
		sendCompressedFrame(keyValuesList);
		while(readAckFrame() < sequence) {}
		return sequence - beginSequence;
	}

	public void close() throws IOException {
		socket.close();
	}
}
