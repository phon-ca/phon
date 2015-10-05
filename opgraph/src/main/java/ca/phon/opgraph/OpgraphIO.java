package ca.phon.opgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerFactory;

/**
 * Utility class providing easy access to open/save methods for opgraph
 * files.
 * 
 */
public class OpgraphIO {

	public OpGraph read(File file) throws IOException {
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		return serializer.read(new FileInputStream(file));
	}
	
	public void write(OpGraph graph, File file) throws IOException {
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		serializer.write(graph, new FileOutputStream(file));
	}
	
}
