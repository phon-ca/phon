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
package ca.phon.ipa.relations;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;

import java.util.*;

/**
* <link rel='stylesheet' type='text/css' href='../../../../../resources/ca/phon/query/detectors/detectors.css'/>
* 
* <p>This package provides detectors for Harmony and Metathesis in aligned 
* IPA Transcriptions.</p>
* 
* <div id='conventions'><h2>Conventions</h2>
* <p>
* <ul>
* <li>The set of all IPA characters is denoted by &#x2119;.  Likewise the mutually exclusive sets of all consonants and all vowels are represented by &#x2102; and &#x1d54d; respectively.</li>
* <li>T is the target IPA transcription. T = [t<sub><i>1</i></sub>, t<sub><i>2</i></sub>, &hellip; , t<sub><i>n</i></sub>], t<sub><i>i</i></sub> &#x2208; &#x2119;</li>
* <li>A is the actual IPA transcription. A = [a<sub><i>1</i></sub>, a<sub><i>2</i></sub>, &hellip; , a<sub><i>m</i></sub>], a<sub><i>i</i></sub> &#x2208; &#x2119;</li>
* <li>M is a 2 X <i>k</i> matrix containing pair-wise alignment of T and A.  <i>k</i> is the pre-calculated length of the alignment.  Elements from
* T are contained in the first row while elements from A are contained in the second row. There may be indels contained in the alignment (i.e., 
* deletions or epenthesis in A) which are indicated by <code>null</code> values.  Indels may occur on the first row, indicating epenthesis, or
* the second row for deletions.
* <div align='center'>
* <p align='left'>e.g.,</p> 
* <table class='matrix'>
* 	<tr>
* 		<td>t<sub><i>1</i></sub></td>
* 		<td>&#x2205;</td>
* 		<td>t<sub><i>2</i></sub></td>
* 		<td>&hellip;</td>
* 		<td>t<sub><i>n</i></sub></td>
* 	</tr>
* 	<tr>
* 		<td>a<sub><i>1</i></sub></td>
* 		<td>a<sub><i>2</i></sub></td>
* 		<td>&hellip;</td>
* 		<td>a<sub><i>m</i></sub></td>
* 		<td>&#x2205;</td>
* 	</tr>
* </table>
* </div>
* </li>
* <li>There exists a function <i>profile(p)</i>, &#x2200; <i>p</i> &#x2208; &#x2119;, which provides a set of values for the phonetic? dimensions 
* of <i>p</i>.  <i>profile(p)</i> is defined as:
* <div>
* 	<p><i>profile(p)</i> = </p>
* 	<table class="stepfunction">
* 		<tr>
* 			<td>{ <i>place(p)<i>, <i>manner(p)</i>, <i>voicing(p)</i> }</td>
* 			<td>if <i>p</i> &#x2208; &#x2102;</td>
* 		</tr>
* 		<tr>
* 			<td>{ <i>height(p)</i>, <i>backness(p)</i>, <i>tenseness(p)</i>, <i>rounding(p)</i> }</td>
* 			<td>if <i>p</i> &#x2208; &#x1d54d;</td>
* 		</tr>
* 		<tr>
* 			<td>&#x2205;</td>
* 			<td>Otherwise</td>
* 		</tr>
* 	</table>
* </p>
* <p>In most functions, dimensions are treated independently and the virtual function <i>dim(p)</i> is used to represent any
* dimension.</p>
* </div>
* </li>
* </ul>
* </p>
* </div>
* 
* 
* 
* </div>
*/
public class SegmentalRelation implements Comparable<SegmentalRelation> {
	
	public static enum Direction {
		Progressive,
		Regressive
	};
	
	/**
	 * Relation types.
	 * 
	 */
	public static enum Relation {
		Migration,
		Reduplication,
		Metathesis,
		Harmony,
		Assimilation
	};
	
	public static enum Locality {
		Nonlocal,
		Local
	};
	
	private PhoneMap phoneMap;
	
	private int position1;
	
	private int position2;
	
	private Relation relation;

	private PhoneticProfile profile1;
	
	private PhoneticProfile profile2;
	
	public SegmentalRelation(Relation relation, PhoneMap pm, int p1, int p2,
			PhoneticProfile profile1, PhoneticProfile profile2) {
		super();
		this.relation = relation;
		this.phoneMap = pm;
		this.position1 = p1;
		this.position2 = p2;
		this.profile1 = profile1;
		this.profile2 = profile2;
	}

	public PhoneMap getPhoneMap() {
		return phoneMap;
	}

	public void setPhoneMap(PhoneMap phoneMap) {
		this.phoneMap = phoneMap;
	}

	public int getPosition1() {
		return position1;
	}

	public void setPosition1(int position1) {
		this.position1 = position1;
	}

	public int getPosition2() {
		return position2;
	}

	public void setPosition2(int position2) {
		this.position2 = position2;
	}
	
	public void setProfile1(PhoneticProfile profile) {
		this.profile1 = profile;
	}
	
	public PhoneticProfile getProfile1() {
		return this.profile1;
	}
	
	public void setProfile2(PhoneticProfile profile) {
		this.profile2 = profile;
	}
	
	public PhoneticProfile getProfile2() {
		return this.profile2;
	}
	
	public Relation getRelation() {
		return this.relation;
	}
	
	public void setRelation(Relation relation) {
		this.relation = relation;
	}
	
	public Set<PhoneDimension> getDimensions() {
		return Collections.unmodifiableSet(getProfile1().getDimensions());
	}
	
	public Direction getDirection() {
		return (getPosition2() > getPosition1() ? Direction.Progressive : Direction.Regressive);
	}
	
	public Locality getLocality() {
		return (getPosition2() == getPosition1()+1 ? Locality.Local : Locality.Nonlocal);
	}
	
	public int getDistance() {
		return (int)Math.sqrt((getPosition1()*getPosition1()) + (getPosition2()*getPosition2()));
	}
	
	public String getName() {
		final StringBuffer buffer = new StringBuffer();
		
		if(getRelation() == Relation.Metathesis 
				|| getRelation() == Relation.Reduplication) {
			buffer.append(getLocality()).append(' ');
		}
		
		if(getRelation() != Relation.Metathesis)
			buffer.append(getDirection()).append(' ');
		
		buffer.append(getRelation());
		
		return buffer.toString();
	}
	
	public String getResultString() {
		final String ELLIPSIS = "\u2026";
		int pos1 = getPosition1();
		int pos2 = getPosition2();
		if(getDirection() == Direction.Regressive) {
			pos1 = getPosition2();
			pos2 = getPosition1();
		}
		
		List<IPAElement> elems1 = getPhoneMap().getAlignedElements(pos1);
    	List<IPAElement> elems2 = getPhoneMap().getAlignedElements(pos2);
    	
    	// Set up target/actual strings
    	String sTarget = (elems1.get(0) != null ? elems1.get(0).toString() : "\u2205");
    	String sActual = (elems1.get(1) != null ? elems1.get(1).toString() : "\u2205");
    	if(pos1 != pos2 - 1) {
    		sTarget += ELLIPSIS;
    		sActual += ELLIPSIS;
    	}
    	sTarget += (elems2.get(0) != null ? elems2.get(0).toString() : "\u2205");
    	sActual += (elems2.get(1) != null ? elems2.get(1).toString() : "\u2205");
    	if(pos1 > 0) {
    		sTarget = ELLIPSIS + sTarget;
    		sActual = ELLIPSIS + sActual;
    	}
    	if(pos2 < getPhoneMap().getAlignmentLength() - 1) {
    		sTarget = sTarget + ELLIPSIS;
    		sActual = sActual + ELLIPSIS;
    	}
    	
    	return sTarget + " \u2192 " + sActual;
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
    	buffer.append(getResultString());
		buffer.append(' ').append(getName());
		getDimensions().stream().forEach( (dim) -> buffer.append(' ').append(dim) );
		
		return buffer.toString();
	}

	@Override
	public int compareTo(SegmentalRelation o) {
		return (new SegmentalRelationComparator()).compare(this, o);
	}
	
}
