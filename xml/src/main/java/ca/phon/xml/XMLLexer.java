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
package ca.phon.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import ca.phon.util.EmptyQueueException;
import ca.phon.util.Queue;
import ca.phon.visitor.annotation.Visits;

/**
 * <p>ANTLR3 lexer for XML files using the
 * StAX API ({@link XMLEventReader}) to
 * pull tokens from the xml stream.</p>
 * 
 * <p>Tokens are emitted on the following xml events:
 * <ul>
 * <li/>
 * </ul>
 * </p>
 * 
 * <p>Token numbers can be assigned using a properties
 * file.  If no file is given or the generated token name
 * is not found, the token ID is given ....</p>
 * 
 * TODO Handle namespaces
 */
public class XMLLexer implements TokenSource {
	
	/** StAX XML event reader */
	private final XMLEventReader reader;
	
	/** Queue of tokens to emit */
	private final Queue<Token> tokenQueue = new Queue<Token>();
	
	/** Token property map */
	private final Properties tokenMap = new Properties();
	
	/**
	 * Create a new lexer for the given stream.
	 * 
	 * @param source
	 * 
	 * @throws IOException
	 */
	public static XMLLexer fromStream(InputStream source, String encoding) 
		throws IOException {
		return fromStream(source, encoding, null);
	}
	
	/**
	 * Create a new lexer for the given stream.
	 * Also provide a token map, can be <code>null</code>.
	 * 
	 * @param stream
	 * @param tokenMap
	 * 
	 * @throws IOException
	 */
	public static XMLLexer fromStream(InputStream source, String encoding, Properties tokenMap) 
		throws IOException {
		final XMLInputFactory factory = XMLUtil.newInputFactory();
		if(factory.isPropertySupported(XMLInputFactory.IS_NAMESPACE_AWARE))
			factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		try {
			final String charset = (encoding == null ? Charset.defaultCharset().name() : encoding);
			final XMLEventReader xmlReader = factory.createXMLEventReader(source, charset);
			
			final XMLLexer lexer = new XMLLexer(xmlReader);
			lexer.tokenMap.putAll(tokenMap);
			return lexer;
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param reader
	 */
	public XMLLexer(XMLEventReader reader) {
		this.reader = reader;
	}
	
	/**
	 * Turn the given {@link QName} into a ANTLR token
	 * name.
	 * 
	 * @param qname
	 * @return the generated token name
	 */
	protected String tokenName(QName qname) {
		String retVal = new String();
		
		retVal = tokenizeString(qname.getLocalPart());
		
		return retVal;
	}
	
	/**
	 * Get the token ID for the given token name.
	 * 
	 * @param tokenName
	 * @return the token ID, if not found in the tokenMap it will 
	 *  be generated.
	 */
	protected int tokenID(String tokenName) {
		if(!tokenMap.containsKey(tokenName)) {
			int tokenID = generateTokenID(tokenName);
			tokenMap.setProperty(tokenName, tokenID + "");
		}
		try {
			final Integer retVal = Integer.parseInt(tokenMap.getProperty(tokenName));
			return retVal;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	private int generateTokenID(String tokenName) {
		int tokenID = Math.abs(tokenName.hashCode());
		while(tokenMap.containsValue(tokenID + "")) {
			tokenID++;
		}
		return tokenID;
	}
	
	
	/**
	 * 
	 */
	private String tokenizeString(String value) {
		String retVal = new String(value);
		
		// convert whitespace to '_'
		retVal = retVal.replaceAll("\\p{Space}", "_");
		
		// convert any punctuation into '_'
		retVal = retVal.replaceAll("[\\p{Punct}&&[^_]]", "_");
		
		// convert to upper-case
		retVal = retVal.toUpperCase();
		
		return retVal;
	}
	
	/**
	 * Utility methods for creating common tokens
	 */
	private CommonToken createToken(String tokenName, Location location) {
		return createToken(tokenID(tokenName), location);
	}
	
	private CommonToken createToken(int tokenID, Location location) {
		final CommonToken retVal = new CommonToken(tokenID);
		retVal.setLine(location.getLineNumber());
		retVal.setCharPositionInLine(location.getColumnNumber());
		return retVal;
	}
	
	@Override
	public Token nextToken() {
		Token retVal = null;
		
		if(!tokenQueue.isEmpty()) {
			try {
				retVal = tokenQueue.dequeue();
			} catch (EmptyQueueException e) {
				retVal = new CommonToken(CommonToken.EOF);
			}
		} else {
			pullTokens();
			if(tokenQueue.isEmpty()) {
				// at end of document
				retVal = new CommonToken(CommonToken.EOF);
			} else {
				retVal = nextToken();
			}
		}
		return retVal;
	}

	@Override
	public String getSourceName() {
		return "XML";
	}
	
	/**
	 * Queue some tokens.
	 */
	private void pullTokens() {
		final XMLEventQueuer queuer = new XMLEventQueuer();
		while(tokenQueue.isEmpty() && reader.hasNext()) {
			try {
				final XMLEvent evt = reader.nextEvent();
				queuer.visit(evt);
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * XML event visitor
	 */
	public class XMLEventQueuer extends XMLEventVisitor {
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitProcessingInstruction(javax.xml.stream.events.ProcessingInstruction)
		 */
		@Override
		public void visitProcessingInstruction(ProcessingInstruction pi) {
			
		}

		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitStartElement(javax.xml.stream.events.StartElement)
		 */
		@Override
		@Visits
		@SuppressWarnings("unchecked")
		public void visitStartElement(StartElement startEle) {
			// queue element start token
			final String tokenName = tokenName(startEle.getName());
			final CommonToken startToken = createToken(tokenName, startEle.getLocation());
			startToken.setText(tokenName);
			tokenQueue.queue(startToken);
			
			// add tokens for attributes
			final Iterator<Attribute> attributes = startEle.getAttributes();
			while(attributes.hasNext()) {
				final Attribute attr = attributes.next();
				final String attrTokenName = tokenizeString(startEle.getName().getLocalPart()) + "_" + tokenName(attr.getName());
				final CommonToken attrToken = createToken(attrTokenName, attr.getLocation());
				attrToken.setText(attr.getValue());
				tokenQueue.queue(attrToken);
			}
		}
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitEndElement(javax.xml.stream.events.EndElement)
		 */
		@Override
		@Visits
		public void visitEndElement(EndElement endEle) {
			// queue element start token
			final String tokenName = tokenName(endEle.getName()) + "_END";
			final CommonToken endToken = createToken(tokenName, endEle.getLocation());
			endToken.setText(tokenName);
			tokenQueue.queue(endToken);
		}
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitStartDocument(javax.xml.stream.events.StartDocument)
		 */
		@Override
		public void visitStartDocument(StartDocument startDoc) {
			
		}
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitEndDocument(javax.xml.stream.events.EndDocument)
		 */
		@Override
		public void visitEndDocument(EndDocument endDoc) {
			
		}
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitEntityReference(javax.xml.stream.events.EntityReference)
		 */
		@Override
		public void visitEntityReference(EntityReference entityRef) {
			
		}
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitEntityDeclaration(javax.xml.stream.events.EntityDeclaration)
		 */
		@Override
		public void visitEntityDeclaration(EntityDeclaration entityDec) {
			
		}
		
		/* (non-Javadoc)
		 * @see ca.phon.xml.XMLEventVisitor#visitNotationDeclaration(javax.xml.stream.events.NotationDeclaration)
		 */
		@Override
		public void visitNotationDeclaration(NotationDeclaration notDec) {
			
		}

		@Override
		@Visits
		public void visitCharacters(Characters chars) {
			final String tokenName = "TEXT";
			final CommonToken txtToken = createToken(tokenName, chars.getLocation());
			final String charData = chars.getData();
			if(charData.trim().length() > 0) {
				txtToken.setText(chars.getData());
				tokenQueue.queue(txtToken);
			}
		}
	}
}
