package info.fetter.logstashforwarder.protocol;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class LumberjackClient {
	private final static byte PROTOCOL_VERSION = 0x31;
	private final static byte FRAME_ACK = 0x41;
	private final static byte FRAME_WINDOW_SIZE = 0x57;
	private final static byte FRAME_DATA = 0x44;
	
	private SSLSocket socket;
	private KeyStore keyStore;
	private String server;
	private int port;
	private DataOutputStream output;
	private DataInputStream input;
	private int sequence = 1;
	
	public LumberjackClient(String keyStorePath, String server, int port) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException, KeyManagementException {
		this.server = server;
		this.port = port;
		
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
	
	public int readAckFrame() throws IOException {
		byte protocolVersion = input.readByte();
		if(protocolVersion != PROTOCOL_VERSION) {
			throw new IOException("Protocol version should be 1, received " + protocolVersion);
		}
		byte frameType = input.readByte();
		if(frameType != FRAME_ACK) {
			throw new IOException("Frame type should be Ack, received " + frameType);
		}
		int sequenceNumber = input.readInt();
		return sequenceNumber;
	}
	
	public void close() throws IOException {
		socket.close();
	}
}
