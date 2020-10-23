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

import java.text.*;
import java.util.*;

import org.antlr.runtime.*;

import ca.phon.extensions.*;
import ca.phon.orthography.parser.*;
import ca.phon.orthography.parser.exceptions.*;
import ca.phon.visitor.*;

/**
 * Container for orthographic transcriptions.  This class is immutable after being created.
 * To append/modify the data in the Orthography, use the {@link OrthographyBuilder} class to create
 * a modified {@link Orthography} instance.
 */
public class Orthography implements Iterable<OrthoElement>, Visitable<OrthoElement>, IExtendable {
	
	private static final long serialVersionUID = 7468757586738978448L;
	private final ExtensionSupport extSupport = 
			new ExtensionSupport(Orthography.class, this);
	
	private final OrthoElement[] elements;
	
	/**
	 * Parse the given text into a new {@link Orthography} object.
	 * 
	 * @text
	 *
	 * @throws ParseException
	 */
	public static Orthography parseOrthography(String text) 
		throws ParseException {
		final OrthoTokenSource tokenSource = new OrthoTokenSource(text);
		final TokenStream tokenStream = new CommonTokenStream(tokenSource);
		final OrthographyParser parser = new OrthographyParser(tokenStream);
		try {
			return parser.orthography().ortho;
		} catch (RecognitionException e) {
			throw new ParseException(text, e.charPositionInLine);
		} catch (OrthoParserException pe) {
			if(pe.getCause() instanceof RecognitionException) {
				RecognitionException re = (RecognitionException)pe.getCause();
				throw new ParseException(text, re.charPositionInLine);
			} else {
				throw new ParseException(text, -1);
			}
		}
	}
	
	public Orthography() {
		super();
		
		elements = new OrthoElement[0];
		
		extSupport.initExtensions();
	}
	
	public Orthography(Collection<? extends OrthoElement> elements) {
		super();
		this.elements = elements.toArray(new OrthoElement[0]);
		extSupport.initExtensions();
	}
	
	public Orthography(OrthoElement[] eles) {
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
	public void accept(Visitor<OrthoElement> visitor) {
		for(OrthoElement ele:this) {
			visitor.visit(ele);
		}
	}
	
	public Orthography subsection(int start, int end) {
		int len = end - start;
		if(len > 0) {
			OrthoElement[] subeles = Arrays.copyOfRange(elements, start, end);
			return new Orthography(subeles);
		}
		return new Orthography();
	}
	
	public int length() {
		return elements.length;
	}
	
	public OrthoElement elementAt(int idx) {
		return elements[idx];
	}
	
	public int indexOf(OrthoElement ele) {
		for(int i = 0; i < length(); i++) {
			final OrthoElement e = elementAt(i);
			if(e == ele) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		for(OrthoElement ele:this) {
			buffer.append(
					(buffer.length() > 0 ? " " : "") + ele.text());
		}
		
		return buffer.toString();
	}
	
	public List<OrthoElement> toList() {
		return Collections.unmodifiableList(Arrays.asList(elements));
	}
	
	@Override
	public Iterator<OrthoElement> iterator() {
		return toList().iterator();
	}
	
}
