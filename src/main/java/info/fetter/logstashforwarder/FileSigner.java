package info.fetter.logstashforwarder;

import info.fetter.logstashforwarder.util.RandomAccessFile;

import java.io.IOException;
//import java.io.RandomAccessFile;
import java.util.zip.Adler32;


public class FileSigner {
	private static final Adler32 adler32 = new Adler32();
	
	public static long computeSignature(RandomAccessFile file, int signatureLength) throws IOException {
		adler32.reset();
		byte[] input = new byte[signatureLength];
		file.seek(0);
		file.read(input);
		adler32.update(input);
		return adler32.getValue();
	}
}
