package info.fetter.logstashforwarder.util;

/*
 * Copyright 2018 Alberto González Palomo https://sentido-labs.com
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class NamedPipe implements LogFile {
	private static Logger logger = Logger.getLogger(NamedPipe.class);

	protected File file;
	protected Pipe pipe;

	// We need to keep a cache of inputStreams because
	// the input stream of a pipe can not be opened more than once:
	// it will block and not return even after the previous stream
	// has been closed.
	protected static Map<File, Pipe> pipes = new HashMap<File, Pipe>();
	protected class Pipe {
		public int refCount;
		public FileInputStream inputStream;
		public long bytesRead;
		public Pipe()
		{
			refCount = 1;
			inputStream = null;
			bytesRead = 0;
		}
	}

	private NamedPipe() {}

	public NamedPipe(File file)
	{
		this.file = file;
		synchronized (pipes) {
			pipe = pipes.get(file);
			if (pipe == null) {
				pipe = new Pipe();
				pipes.put(file, pipe);
				(this.new OpenerThread()).start();
			} else {
				synchronized (pipe) {
					++pipe.refCount;
				}
			}
		}
	}

	protected class OpenerThread extends Thread
	{
		public void run()
		{
			try {
				pipe.inputStream = new FileInputStream(file);
			} catch (IOException e) {
				logger.error("Error opening named pipe: " + e.getMessage());
			}
		}
	}

	public boolean isEmpty() throws IOException { return false; }
	public void seek(long pos) throws IOException { }
	public long getFilePointer() throws IOException { return pipe.bytesRead; }

	public int read() throws IOException
	{
		synchronized (pipe) {
			if (pipe.inputStream != null && pipe.inputStream.available() > 0) {
				++pipe.bytesRead;
				return pipe.inputStream.read();
			} else {
				return -1;
			}
		}
	}

	public void close() throws IOException
	{
		synchronized (pipe) {
			synchronized (pipes) {
				--pipe.refCount;
				if (pipe.refCount == 0) {
					pipe.inputStream.close();
					pipe.inputStream = null;
					pipes.remove(file);
				}
			}
		}
	}

	@Override
	protected void finalize() throws IOException { close(); }
}
