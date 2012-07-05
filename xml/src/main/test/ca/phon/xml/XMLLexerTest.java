package ca.phon.xml;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

import junit.framework.TestCase;

public class XMLLexerTest extends TestCase {

	public void testLexer() {
		final String xmlLocation = "xml/xsd/xml.xsd";
		final InputStream is = getClass().getClassLoader().getResourceAsStream(xmlLocation);
		try {
			final XMLLexer lexer = XMLLexer.fromStream(is, "UTF-8");
			Token token = CommonToken.EOF_TOKEN;
			while((token = lexer.nextToken()) != CommonToken.EOF_TOKEN) {
				System.out.println(token.getType());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
