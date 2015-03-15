package info.fetter.logstashforwarder;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class FileModificationListener implements FileAlterationListener {
	private Event fields;
	private FileWatcher watcher;
	
	public FileModificationListener(FileWatcher watcher, Event fields) {
		this.watcher = watcher;
		this.fields = fields;
	}

	public void onDirectoryChange(File file) {
		// Not implemented
	}

	public void onDirectoryCreate(File file) {
		// Not implemented
	}

	public void onDirectoryDelete(File file) {
		// Not implemented
	}

	public void onFileChange(File file) {
		watcher.onFileChange(file, fields);
	}

	public void onFileCreate(File file) {
		watcher.onFileCreate(file, fields);
	}

	public void onFileDelete(File file) {
		watcher.onFileDelete(file);
	}

	public void onStart(FileAlterationObserver file) {
		// Not implemented
	}

	public void onStop(FileAlterationObserver file) {
		// Not implemented
	}

}
