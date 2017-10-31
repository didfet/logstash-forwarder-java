package info.fetter.logstashforwarder;

import info.fetter.logstashforwarder.util.RandomAccessFile;

import java.io.IOException;
//import java.io.RandomAccessFile;
import java.util.zip.Adler32;


public class FileSigner {
	private static final Adler32 adler32 = new Adler32();
	private static long fakeSignatureForPipes = System.currentTimeMillis();

	public static long computeSignature(RandomAccessFile file, int signatureLength) throws IOException {
		// If the file is not seekable, a pipe for instance,
		// we report an ever-changing fake signature to keep
		// FileWatcher trying to read again as it would with
		// a normal file that had changed.
		if (!file.canSeek()) return ++fakeSignatureForPipes;

		adler32.reset();
		byte[] input = new byte[signatureLength];
		file.seek(0);
		file.read(input);
		adler32.update(input);
		return adler32.getValue();
	}
}
