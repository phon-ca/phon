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
package ca.phon.opgraph;

import java.io.*;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.io.*;

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
