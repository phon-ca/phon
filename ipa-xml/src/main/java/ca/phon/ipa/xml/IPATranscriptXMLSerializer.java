package ca.phon.ipa.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ca.phon.ipa.IPATranscript;
import ca.phon.xml.XMLLexer;
import ca.phon.xml.XMLSerializer;

/**
 * Serialization for IPATranscript objects.
 */
public class IPATranscriptXMLSerializer implements XMLSerializer {

	@Override
	public <T> T read(Class<T> type, InputStream input) throws IOException {
		if(type != declaredType())
			throw new IOException("Invalid type specified.");
		
		final IPATranscript transcript = new IPATranscript();
		
		final Properties tokenProps = new Properties();
		final InputStream propsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("antlr3/tokens/Pho.tokens");
		tokenProps.load(propsStream);
		
		final XMLLexer lexer = XMLLexer.fromStream(input, "UTF-8", tokenProps);
		final TokenStream tokenStream = new CommonTokenStream(lexer);
		final PhoParser phoParser = new PhoParser(tokenStream);
		
		try {
			final PhoParser.pho_return phoRet = phoParser.pho();
			final CommonTree tree = phoRet.tree;
			final CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(tree);
			final Xml2IPATranscript treeParser = new Xml2IPATranscript(nodeStream);
			treeParser.setTranscript(transcript);
			treeParser.pho();
		} catch (RecognitionException e) {
			throw new IOException(e);
		}
		
		return type.cast(transcript);
	}

	@Override
	public <T> void write(Class<T> type, T obj, OutputStream output) throws IOException {
		if(type != declaredType())
			throw new IOException("Invalid type specified.");
		
		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(output));
		
		final IPATranscript transcript = IPATranscript.class.cast(obj);
		final PhoCommonTreeBuilder treeBuilder = new PhoCommonTreeBuilder();
		transcript.accept(treeBuilder);
		
		final CommonTree tree = treeBuilder.getTree();
		final CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(tree);
		final IPATranscript2Xml xmlTemplate = new IPATranscript2Xml(nodeStream);
		
		IPATranscript2Xml.pho_return phoRet;
		try {
			phoRet = xmlTemplate.pho();

			final String xmlData = phoRet.st.toString();
			out.write(xmlData);
			out.flush();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Class<?> declaredType() {
		return IPATranscript.class;
	}
}
