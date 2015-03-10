package info.fetter.logstashforwarder.util;

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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class LastModifiedFileFilter extends AbstractFileFilter {
	private boolean after;
	private long cutoff;
	
	public LastModifiedFileFilter(long cutoff, boolean after) {
		this.after = after;
		this.cutoff = cutoff;
	}
	
	public LastModifiedFileFilter(long cutoff) {
		this(cutoff, true);
	}

	@Override
	public boolean accept(File file) {
		long timeMillis = System.currentTimeMillis() - cutoff;
		if(after) {
			return FileUtils.isFileNewer(file, timeMillis);
		} else {
			return FileUtils.isFileOlder(file, timeMillis);
		}
	}

}
