package info.fetter.logstashforwarder;

import java.io.IOException;
import java.util.zip.Adler32;

import info.fetter.logstashforwarder.util.LogFile;
import info.fetter.logstashforwarder.util.RandomAccessFile;


public class FileSigner {
	private static final Adler32 adler32 = new Adler32();

	public static long computeSignature(LogFile logFile, int signatureLength) throws IOException {
		if (!(logFile instanceof RandomAccessFile)) return 0;

		RandomAccessFile reader = (RandomAccessFile) logFile;
		adler32.reset();
		byte[] input = new byte[signatureLength];
		reader.seek(0);
		reader.read(input);
		adler32.update(input);
		return adler32.getValue();
	}
}
