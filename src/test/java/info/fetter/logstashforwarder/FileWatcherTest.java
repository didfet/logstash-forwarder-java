package info.fetter.logstashforwarder;

import static org.apache.log4j.Level.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileWatcherTest {
	Logger logger = Logger.getLogger(FileWatcherTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
		RootLogger.getRootLogger().setLevel(TRACE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BasicConfigurator.resetConfiguration();
	}

	//@Test
	public void testFileWatch() throws InterruptedException, IOException {
		FileWatcher watcher = new FileWatcher();
		watcher.addFilesToWatch("./test.txt");
		for(int i = 0; i < 100; i++) {
			Thread.sleep(1000);
			watcher.checkFiles();
		}
	}

	@Test
	public void testWildcardWatch() throws InterruptedException, IOException {
		FileWatcher watcher = new FileWatcher();
		watcher.addFilesToWatch("./test*.txt");

		File file1 = new File("test1.txt");
		File file2 = new File("test2.txt");
		File file3 = new File("test3.txt");
		File file4 = new File("test4.txt");
		
		File testDir = new File("test");
		//FileUtils.forceMkdir(new File("test"));
		
		watcher.checkFiles();
		Thread.sleep(100);
		FileUtils.write(file1, "file 1 line 1\n", true);
		Thread.sleep(100);
		watcher.checkFiles();
		FileUtils.write(file1, "file 1 line 2\n", true);
		FileUtils.write(file2, "file 2 line 1\n", true);
		Thread.sleep(1000);
		watcher.checkFiles();
		FileUtils.moveFileToDirectory(file1, testDir, true);
		FileUtils.write(file2, "file 2 line 2\n", true);
		FileUtils.moveFile(file2, file1);
		FileUtils.write(file2, "file 3 line 1\n", true);
//		FileUtils.touch(file3);
//		FileUtils.forceDelete(file1);
//		FileUtils.forceDelete(file2);
		Thread.sleep(1000);
		watcher.checkFiles();
		
		
		watcher.close();
		FileUtils.forceDelete(file1);
		FileUtils.forceDelete(file2);
		FileUtils.forceDelete(testDir);
		
		
//		FileUtils.moveFile(file3, file4);
//		FileUtils.touch(file3);
//		Thread.sleep(500);
//		watcher.checkFiles();
//		FileUtils.forceDelete(file3);
//		FileUtils.forceDelete(file4);
	}
}
