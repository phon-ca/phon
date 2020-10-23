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
package ca.phon.fontconverter;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.bind.*;

import ca.phon.fontconverter.io.*;
import ca.phon.util.resources.*;
import de.susebox.jtopas.*;

/**
 * Handles conversion of one transcription method to UTF-8 IPA
 * encoding.  Conversion files are stored in the data/conv directory
 * and must conform to the converter.xsd schema.
 */
public class TranscriptConverter {
	
	private final String name;
	
	/** The tokenizer */
	private final Tokenizer tokenizer;
	
	private final static String CONV_LIST = "data/conv/conv.list";
	private static ResourceLoader<TranscriptConverter> loader;
	static {
		loader = new ResourceLoader<>();
		final TranscriptConverterHandler handler = new TranscriptConverterHandler();
		handler.loadResourceFile(CONV_LIST);
		loader.addHandler(handler);
	}
	
	/**
	 * Get an instance of a converter given the font name
	 */
	public static TranscriptConverter getInstanceOf(String name) {
		TranscriptConverter retVal = null;
		for(TranscriptConverter tc:loader) {
			if(tc.getName().equals(name)) {
				retVal = tc;
				break;
			}
		}
		return retVal;
	}
	
	public static Collection<String> getAvailableConverterNames() {
		final List<String> retVal = new ArrayList<>();
		for(TranscriptConverter converter:loader) {
			retVal.add(converter.getName());
		}
		return retVal;
	}
	
	private static TranscriptConverter loadConverterFromURL(URL url) throws IOException {
		TokenizerProperties props = new StandardTokenizerProperties();
		props.setSeparators("#");
		props.removeWhitespaces(" ");
		
		try(InputStream is = url.openStream()) {
			// create the unmarshaller
			JAXBContext jaxbContext = JAXBContext.newInstance("ca.phon.fontconverter.io");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			ConversionTable convTbl = (ConversionTable)unmarshaller.unmarshal(is);
			
			for(MappingType mapping:convTbl.getMapping()) {
				TokenType from = mapping.getFrom();
				TokenType to = mapping.getTo();
				
				String tokenString = stringFromUnicodeSequence(from.getValue());
				String toString = stringFromUnicodeSequence(to.getValue());
				
				props.addSpecialSequence(tokenString, toString);
			}
			final StandardTokenizer tokenizer = new StandardTokenizer(props);
			return new TranscriptConverter(convTbl.getFontName(), tokenizer);
		} catch (JAXBException jaxbEx) {
			org.apache.logging.log4j.LogManager.getLogger(TranscriptConverter.class.getName()).warn(jaxbEx.getMessage());
			throw new IOException(jaxbEx);
		}
	}
	
	private static String stringFromUnicodeSequence(UnicodeSequenceType seq) {
		String retVal = "";
		
		for(String unicodeVal:seq.getCodepoint()) {
			int charValue = Integer.decode(unicodeVal);
			retVal += (char)charValue;
		}
		
		return retVal;
	}
	
	/** Constructor - hidden */
	protected TranscriptConverter(String name, Tokenizer tokenizer) {
		super();
		this.name = name;
		this.tokenizer = tokenizer;
	}
	
	public String getName() {
		return name;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	/**
	 * Convert the transcript
	 */
	public String convert(String trans) {
		StringSource source = new StringSource(trans);
		tokenizer.setSource(source);
		
		String retVal = "";
		
		try {
			while(tokenizer.hasMoreToken()) {
				Token token = tokenizer.nextToken();
				if(token.getCompanion() != null)
					retVal += token.getCompanion().toString();
				else
					retVal += token.getImage();
			}
		} catch (TokenizerException e) {
			org.apache.logging.log4j.LogManager.getLogger(getClass().getName()).warn(e.getMessage());
		}
		
		return retVal;
	}
	
	/**
	 * Handler for loading transcript converters
	 */
	private static class TranscriptConverterHandler extends ClassLoaderHandler<TranscriptConverter> {
		
		public TranscriptConverterHandler() {
		}

		@Override
		public TranscriptConverter loadFromURL(URL url) throws IOException {
			return loadConverterFromURL(url);
		}
		
	}
}
