/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.fontconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ca.phon.fontconverter.io.ConversionTable;
import ca.phon.fontconverter.io.MappingType;
import ca.phon.fontconverter.io.TokenType;
import ca.phon.fontconverter.io.UnicodeSequenceType;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.StringSource;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;

/**
 * Handles conversion of one transcription method to UTF-8 IPA
 * encoding.  Conversion files are stored in the data/conv directory
 * and must conform to the converter.xsd schema.
 */
public class TranscriptConverter {
	
	/** The tokenizer */
	private final Tokenizer tokenizer;
	
	/** The static instances */
	private static Hashtable<String, TranscriptConverter> _instances = 
		new Hashtable<String, TranscriptConverter>();
	
	private final static File convDir = new File("data/conv");
	
	private static Hashtable<String, File> converters = null;
	
	/** 
	 * Get an instance of a converter given the file path.
	 */
	public static TranscriptConverter getInstanceOf(File file) {
		if(_instances.get(file) == null) {
			Tokenizer tokenizer = getTokenizerFromFile(file);
			_instances.put(file.getAbsolutePath(), new TranscriptConverter(tokenizer));
		}
		
		return _instances.get(file.getAbsolutePath());
	}
	
	/**
	 * Get an instance of a converter given the font name
	 */
	public static TranscriptConverter getInstanceOf(String name) {
		if(converters == null) {
			// create list of converters
			getAvailableConverterNames();
		}
		
		if(converters.get(name) != null) {
			return getInstanceOf(converters.get(name));
		} else {
			return null;
		}
	}
	
	public static Collection<String> getAvailableConverterNames() {
		if(converters == null) {
			converters = new Hashtable<String, File>();
		}
		converters.clear();
		
		if(!convDir.isDirectory())
			return new ArrayList<String>();
		
		Unmarshaller unmarshaller = null;
		try {
			JAXBContext context = JAXBContext.newInstance("ca.phon.util.transconv.io");
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException jaxbEx) {
			Logger.getLogger(TranscriptConverter.class.getName()).warning(jaxbEx.getMessage());
		}
		
		for(String f:convDir.list()) {
			if(f.endsWith(".xml")) {
				try {
					ConversionTable convTbl = 
						(ConversionTable)unmarshaller.unmarshal(new File(convDir.getAbsolutePath() + File.separator + f));
					converters.put(convTbl.getFontName(), new File(convDir.getAbsolutePath() + File.separator + f));
				} catch (JAXBException jaxbEx) {
					Logger.getLogger(TranscriptConverter.class.getName()).warning(jaxbEx.getMessage());
				}
			}
		}
		return converters.keySet();
	}
	
	private static Tokenizer getTokenizerFromFile(File file) {
		
		TokenizerProperties props = new StandardTokenizerProperties();
		props.setSeparators("#");
		props.removeWhitespaces(" ");
		
		try {
			// create the unmarshaller
			JAXBContext jaxbContext = JAXBContext.newInstance("ca.phon.util.transconv.io");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			ConversionTable convTbl = (ConversionTable)unmarshaller.unmarshal(file);
			
			for(MappingType mapping:convTbl.getMapping()) {
				TokenType from = mapping.getFrom();
				TokenType to = mapping.getTo();
				
				String tokenString = stringFromUnicodeSequence(from.getValue());
				String toString = stringFromUnicodeSequence(to.getValue());
				
				props.addSpecialSequence(tokenString, toString);
			}
		} catch (JAXBException jaxbEx) {
			Logger.getLogger(TranscriptConverter.class.getName()).warning(jaxbEx.getMessage());
		}
		
		return new StandardTokenizer(props);
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
	protected TranscriptConverter(Tokenizer tokenizer) {
		super();
		this.tokenizer = tokenizer;
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
			Logger.getLogger(getClass().getName()).warning(e.getMessage());
		}
		
		return retVal;
	}
}
