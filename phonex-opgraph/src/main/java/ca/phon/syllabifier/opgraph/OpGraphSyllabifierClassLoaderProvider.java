/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.syllabifier.opgraph;

import ca.phon.syllabifier.*;
import ca.phon.util.resources.ClassLoaderHandler;

import java.io.*;
import java.net.URL;

/**
 * Load opgraph syllabifiers listed in the syllabifier/opgraph.list file.
 * 
 */
public class OpGraphSyllabifierClassLoaderProvider extends ClassLoaderHandler<Syllabifier> implements SyllabifierProvider {
	
	private final static String LIST = "syllabifier/opgraph.list";
	
	/**
	 * Constructor
	 */
	public OpGraphSyllabifierClassLoaderProvider() {
		super();
		super.loadResourceFile(LIST);
	}

	@Override
	public Syllabifier loadFromURL(URL url) throws IOException {
		final InputStream is = url.openStream();
		final OpGraphSyllabifier syllabifier = OpGraphSyllabifier.createSyllabifier(is);
		return syllabifier;
	}

}
