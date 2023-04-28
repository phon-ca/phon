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
package ca.phon.orthography;

import ca.phon.extensions.*;
import ca.phon.orthography.parser.*;
import ca.phon.orthography.parser.exceptions.OrthoParserException;
import ca.phon.visitor.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;

import java.text.ParseException;
import java.util.*;

/**
 * Container for orthographic transcriptions.  This class is immutable after being created.
 * To append/modify the data in the Orthography, use the {@link OrthographyBuilder} class to create
 * a modified {@link Orthography} instance.
 */
public final class Orthography implements Iterable<OrthographyElement>, Visitable<OrthographyElement>, IExtendable {
	
	private static final long serialVersionUID = 7468757586738978448L;
	private final ExtensionSupport extSupport = 
			new ExtensionSupport(Orthography.class, this);
	
	private final OrthographyElement[] elements;
	
	/**
	 * Parse the given text into a new {@link Orthography} object.
	 * 
	 * @text
	 *
	 * @throws ParseException
	 */
	public static Orthography parseOrthography(String text) 
		throws ParseException {
		org.antlr.v4.runtime.CharStream charStream = CharStreams.fromString(text);
		UnicodeOrthographyLexer lexer = new UnicodeOrthographyLexer(charStream);
		org.antlr.v4.runtime.TokenStream tokenStream = new org.antlr.v4.runtime.CommonTokenStream(lexer);

		UnicodeOrthographyBuilder orthoBuilder = new UnicodeOrthographyBuilder();
		UnicodeOrthographyParser parser = new UnicodeOrthographyParser(tokenStream);
		parser.addParseListener(orthoBuilder);

		try {
			parser.start();
		} catch (RecognitionException e) {
			throw new ParseException(text, e.getOffendingToken().getCharPositionInLine());
		} catch (OrthoParserException pe) {
			if(pe.getCause() instanceof RecognitionException) {
				RecognitionException re = (RecognitionException)pe.getCause();
				throw new ParseException(text, re.getOffendingToken().getCharPositionInLine());
			} else {
				throw new ParseException(text, -1);
			}
		}

		Orthography retVal = orthoBuilder.getOrthography();
		return retVal;
	}
	
	public Orthography() {
		super();
		
		elements = new OrthographyElement[0];
		
		extSupport.initExtensions();
	}
	
	public Orthography(Collection<? extends OrthographyElement> elements) {
		super();
		this.elements = elements.toArray(new OrthographyElement[0]);
		extSupport.initExtensions();
	}
	
	public Orthography(OrthographyElement[] eles) {
		super();
		this.elements = eles;
		extSupport.initExtensions();
	}
	
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public void accept(Visitor<OrthographyElement> visitor) {
		for(OrthographyElement ele:this) {
			visitor.visit(ele);
		}
	}
	
	public Orthography subsection(int start, int end) {
		int len = end - start;
		if(len > 0) {
			OrthographyElement[] subeles = Arrays.copyOfRange(elements, start, end);
			return new Orthography(subeles);
		}
		return new Orthography();
	}
	
	public int length() {
		return elements.length;
	}
	
	public OrthographyElement elementAt(int idx) {
		return elements[idx];
	}
	
	public int indexOf(OrthographyElement ele) {
		for(int i = 0; i < length(); i++) {
			final OrthographyElement e = elementAt(i);
			if(e == ele) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		for(OrthographyElement ele:this) {
			buffer.append(
					(buffer.length() > 0 ? " " : "") + ele.text());
		}
		
		return buffer.toString();
	}
	
	public List<OrthographyElement> toList() {
		return Collections.unmodifiableList(Arrays.asList(elements));
	}
	
	@Override
	public Iterator<OrthographyElement> iterator() {
		return toList().iterator();
	}
	
}
