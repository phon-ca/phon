/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.syllabifier.opgraph;

import java.io.*;
import java.net.URL;

import ca.phon.syllabifier.*;
import ca.phon.util.resources.ClassLoaderHandler;

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
