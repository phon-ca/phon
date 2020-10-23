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
package ca.phon.ipa;

import java.text.*;
import java.util.*;

import org.apache.logging.log4j.*;

import ca.phon.extensions.*;

/**
 * Class for building {@link IPATranscript}s.
 */
public class IPATranscriptBuilder {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(IPATranscriptBuilder.class.getName());
	
	private final IPAElementFactory factory = new IPAElementFactory();

	/**
	 * Element buffer
	 */
	private final List<IPAElement> buffer = new ArrayList<IPAElement>();
	
	/**
	 * Cached UnvalidatedValue, if an error occurs while building the transcript
	 * this will be added to the returned (empty) transcript as an extension
	 */
	private UnvalidatedValue unvalidatedValue = null;
	
	public IPATranscriptBuilder() {
		
	}
	
	/**
	 * Append the given {@link IPAElement}
	 * 
	 * @param ipaElement
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(IPAElement ipaElement) {
		if(unvalidatedValue != null) {
			unvalidatedValue.setValue(unvalidatedValue.getValue() + ipaElement.toString());
		} else {
			buffer.add(ipaElement);
		}
		return this;
	}
	
	/**
	 * Make the last added element plus  this phone a new compound phone.  This
	 * will replace the last phone added.
	 * 
	 * @param phone
	 * @param lig
	 * @return
	 * 
	 * @throws IllegalStateException if previous phone is not present or is not an instance of
	 *  Phone
	 */
	public IPATranscriptBuilder makeCompoundPhone(IPAElement phone, Character lig) {
		if(size() == 0) {
			throw new IllegalStateException("No previous phone");
		}
		final IPAElement prevEle = buffer.get(buffer.size()-1);
		if(!(prevEle instanceof Phone)) {
			throw new IllegalStateException("Previous element not a phone");
		}
		if(!(phone instanceof Phone)) {
			throw new IllegalStateException("Element must be a phone.");
		}
		
		final Phone prevPhone = (Phone)prevEle;
		final CompoundPhone newPhone = factory.createCompoundPhone(prevPhone, (Phone)phone, lig);
		buffer.remove(prevPhone);
		buffer.add(newPhone);

		return this;
	}
	
	public IPATranscriptBuilder makeCompoundPhone(Character lig) {
		if(size() < 2) {
			throw new IllegalStateException("No previous phone");
		}
		final IPAElement ele1 = buffer.get(buffer.size()-2);
		if(!(ele1 instanceof Phone)) {
			throw new IllegalStateException("Previous element not a phone");
		}
		final IPAElement ele2 = buffer.get(buffer.size()-1);
		if(!(ele2 instanceof Phone)) {
			throw new IllegalStateException("Element must be a phone.");
		}
		
		final Phone p1 = (Phone)ele1;
		final Phone p2 = (Phone)ele2;
		final CompoundPhone newPhone = factory.createCompoundPhone(p1, p2, lig);
		buffer.remove(p1);
		buffer.remove(p2);
		buffer.add(newPhone);

		return this;
	}
	
	/**
	 * Append all elements
	 * 
	 * @param eleList
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(Collection<? extends IPAElement> eleList) {
		eleList.forEach( this::append );
		return this;
	}
	
	/**
	 * Append the given string to the transcript.
	 * This utilizes the {@link IPATranscript#parseIPATranscript(String)} method.
	 * 
	 * @param ipa
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(String ipa) {
		if(unvalidatedValue != null) {
			unvalidatedValue.setValue(unvalidatedValue.getValue() + ipa);
			return this;
		}
		try {
			final IPATranscript transcript = IPATranscript.parseIPATranscript(toIPATranscript().toString() + ipa);
			buffer.clear();
			append(transcript);
		} catch (ParseException e) {
			LOGGER.warn( e.getLocalizedMessage(), e);
			
			// keep as an unvalidated value
			final IPATranscript transcript = toIPATranscript();
			final ParseException pe = new ParseException(e.getMessage(), transcript.toList().size()+e.getErrorOffset());
			unvalidatedValue = new UnvalidatedValue(transcript.toString() + ipa, pe);
		}
		return this;
	}
	
	/**
	 * Append the given transcript
	 * 
	 * @param ipa
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder append(IPATranscript ipa) {
		if(ipa.getExtension(UnvalidatedValue.class) != null) {
			final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
			final String current = toIPATranscript().toString();
			final String unvalidated = current + uv.getValue();
			final int newIdx = current.length() + uv.getParseError().getErrorOffset();
			final ParseException pe = new ParseException(uv.getParseError().getMessage(), newIdx);
			unvalidatedValue = new UnvalidatedValue(unvalidated, pe);
		} else {
			for(IPAElement ele:ipa) {
				append(ele);
			}
		}
		return this;
	}
	
	/**
	 * Append a word boundary.
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendWordBoundary() {
		append(factory.createWordBoundary());
		return this;
	}
	
	/**
	 * Append a syllable boundary (e.g., '.')
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendSyllableBoundary() {
		append(factory.createSyllableBoundary());
		return this;
	}
	
	/**
	 * Append a compound word marker (e.g, '+')
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendCompoundWordMarker() {
		append(factory.createCompoundWordMarker());
		return this;
	}
	
	/**
	 * Append a linker (including liason)
	 * @return builder
	 */
	public IPATranscriptBuilder appendLinker() {
		append(factory.createLinker());
		return this;
	}
	
	/**
	 * Append contraction
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendContraction() {
		append(factory.createContraction());
		return this;
	}
	
	/**
	 * Append minor intonation group
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendMinorIntonationGroup() {
		append(factory.createMinorIntonationGroup());
		return this;
	}
	
	/**
	 * Append major intonation group
	 * 
	 * @return builder
	 */
	public IPATranscriptBuilder appendMajorIntonationGroup() {
		append(factory.createMajorIntonationGroup());
		return this;
	}
	
	/**
	 * Return the {@link IPATranscript} object
	 * 
	 * @return ipa
	 */
	public IPATranscript toIPATranscript() {
		if(unvalidatedValue != null) {
			final IPATranscript retVal = new IPATranscript();
			retVal.putExtension(UnvalidatedValue.class, unvalidatedValue);
			return retVal; 
		} else {
			return new IPATranscript(buffer);
		}
	}
	
	/**
	 * Return the last element in the buffer or <code>null</code> if
	 * buffer is empty.
	 * 
	 * @return last element in buffer
	 */
	public IPAElement last() {
		return (buffer.size() > 0 ? buffer.get(buffer.size()-1) : null);
	}
	
	/**
	 * Reverse elements in this builder.
	 * 
	 * 
	 */
	public IPATranscriptBuilder reverse() {
		Collections.reverse(buffer);
		return this;
	}
	
	/**
	 * Return the length of the current {@link IPATranscript} in the
	 * buffer.
	 * 
	 * @return size of buffer
	 */
	public int size() {
		return buffer.size();
	}
}
