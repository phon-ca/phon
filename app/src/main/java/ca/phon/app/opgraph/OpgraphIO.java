/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.io.OpGraphSerializer;
import ca.phon.opgraph.io.OpGraphSerializerFactory;

/**
 * Utility class providing easy access to open/save methods for opgraph
 * files.
 * 
 */
public class OpgraphIO {

	public static OpGraph read(File file) throws IOException {
		return read(new FileInputStream(file));
	}
	
	public static OpGraph read(InputStream in) throws IOException {
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		return serializer.read(in);
	}
	
	public static void write(OpGraph graph, File file) throws IOException {
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		serializer.write(graph, new FileOutputStream(file));
	}
	
}
