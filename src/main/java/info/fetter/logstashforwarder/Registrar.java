package info.fetter.logstashforwarder;

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
import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Registrar {
	
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static FileState[] readStateFromJson(File file) throws JsonParseException, JsonMappingException, IOException {
		FileState[] stateArray = mapper.readValue(file, FileState[].class);
		return stateArray;
	}
	
	public static FileState[] readStateFromJson(String file) throws JsonParseException, JsonMappingException, IOException {
		return readStateFromJson(new File(file));
	}
	
	public static void writeStateToJson(File file, Collection<FileState> stateList) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(file, stateList);
	}
	
	public static void writeStateToJson(String file, Collection<FileState> stateList) throws JsonGenerationException, JsonMappingException, IOException {
		writeStateToJson(new File(file), stateList);
	}
	
}
