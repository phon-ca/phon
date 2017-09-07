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
package ca.phon.orthography;

import java.text.ParseException;
import java.util.*;

import org.antlr.runtime.*;

import ca.phon.extensions.*;
import ca.phon.orthography.parser.*;
import ca.phon.orthography.parser.exceptions.OrthoParserException;
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
