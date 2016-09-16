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
package ca.phon.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents alignments between two arrays of similar objects.
 * 
 *
 */
public class AlignmentMap<T> {
	/** The value for an indel */
	public static final int INDEL_VALUE = -1;
	
	/** The value for a spacer */
	public static final int SPACER_VALUE = -2;
	
	/** The 'top' list of elements to align */
	protected T[] topElements;
	
	/** The 'bottom' list of elements to align */
	protected T[] bottomElements;
	
	/** The 'top' alignment values */
	protected Integer[] topAlignment;
	
	/** The 'bottom' alignment values */
	protected Integer[] bottomAlignment;
	
	/** Constructor */
	public AlignmentMap() {
		super();
		
		this.topAlignment = null;
		this.bottomAlignment = null;
		this.topAlignment = new Integer[0];
		this.bottomAlignment = new Integer[0];		
	}

	/** Get the value of the bottom alignment */
	public Integer[] getBottomAlignment() {
		return bottomAlignment;
	}

	/** Set the bottom alignment */
	public void setBottomAlignment(Integer[] bottomAlignment) {
		this.bottomAlignment = bottomAlignment;
	}

	/** Get the bottom elements */
	public T[] getBottomElements() {
		return bottomElements;
	}

	/** Set the bottom elements */
	public void setBottomElements(T[] bottomElements) {
		this.bottomElements = bottomElements;
	}

	/** Get the top alignment */
	public Integer[] getTopAlignment() {
		return topAlignment;
	}

	/** Set the top alignment */
	public void setTopAlignment(Integer[] topAlignment) {
		this.topAlignment = topAlignment;
	}

	/** Get the top elements */
	public T[] getTopElements() {
		return topElements;
	}

	/** Set the top elements */
	public void setTopElements(T[] topElements) {
		this.topElements = topElements;
	}
	
	public T[] getAligned(T[] eles) {
		return getAligned(Arrays.asList(eles));
	}
	
	/**
	 * Return the list of object that are aligned
	 * with the given iterable list of object.
	 * 
	 * @param eles
	 * @return aligned lements
	 */
	public T[] getAligned(Iterable<T> iterable) {
		final Iterator<T> itr = iterable.iterator();
		final List<T> list = new ArrayList<T>();
		while(itr.hasNext()) list.add(itr.next());
		return getAligned(list);
	}
	
	/**
	 * Return the list of objects that are aligned
	 * with the given list of objects.
	 * 
	 * This method does <b>not</b> return
	 * <code>null</code> values for indel/spacers.
	 * 
	 * @param eles
	 * @return aligned elements
	 */
	@SuppressWarnings("unchecked")
	public T[] getAligned(List<T> eles) {
		final List<T> retVal = new ArrayList<T>();		
		if(eles.size() == 0) return null;
		final List<T> topElements = getTopAlignmentElements();
		final List<T> btmElements = getBottomAlignmentElements();
		
		final boolean isTop = topElements.containsAll(eles);
		
		final int startIdx = (isTop ? topElements.indexOf(eles.get(0)) : btmElements.indexOf(eles.get(0)));
		final int endIdx = (isTop ? topElements.indexOf(eles.get(eles.size()-1)) : btmElements.indexOf(eles.get(eles.size()-1)));
		
		if(startIdx < 0 || endIdx < 0) return null;
		
		for(int i = startIdx; i <= endIdx; i++) {
			final T ele = (isTop ? btmElements.get(i) : topElements.get(i));
			if(ele != null)
				retVal.add(ele);
		}
		return (T[])retVal.toArray();
	}
	
	/** Get the top alignment as an array of elements */
	public List<T> getTopAlignmentElements() {
//		if(this.topAlignment == null)
//			return null;
		
		List<T> retVal = new ArrayList<T>();
//		if(topElements.length == 0) return null;
		
//		ArrayList<T> retVal = new ArrayList<T>();
//		T[] retVal = (T[])Array.newInstance(
//				topElements[0].getClass(), topAlignment.length);
		
		for(int i = 0; i < topAlignment.length; i++) {
			if(topAlignment[i] == INDEL_VALUE || topAlignment[i] == SPACER_VALUE) {
				retVal.add(null);
			} else {
				int eleIdx = topAlignment[i];
				if(eleIdx >= 0 && eleIdx < topElements.length)
					retVal.add(topElements[topAlignment[i]]);
				else
					retVal.add(null);
			}
		}
					
		return retVal;
	}
	
	/** Get the bottom alignment as an array of elements */
	public List<T> getBottomAlignmentElements() {
//		if(this.bottomAlignment == null)
//			return null;
		
//		if(bottomElements.length == 0) return null;
		
		ArrayList<T> retVal = new ArrayList<T>();
//		T[] retVal = (T[]) Array.newInstance(
//				bottomElements[0].getClass(), bottomAlignment.length);
		
		for(int i = 0; i < bottomAlignment.length; i++) {
			if( (bottomAlignment[i] == INDEL_VALUE || bottomAlignment[i] == SPACER_VALUE)
				|| bottomAlignment[i] >= bottomElements.length	) {
				retVal.add(null);
			} else {
				retVal.add(bottomElements[bottomAlignment[i]]);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns the length of the alignment.
	 * 
	 * @return int
	 */
	public int getAlignmentLength() {
		if(this.topAlignment == null)
			return 0;
		
		return this.topAlignment.length;
	}
	
	/**
	 * Returns the set of aligned elements at the
	 * given alignment position.
	 * 
	 * @param alignmentIndex
	 * @return T[0] the top alignment element, T[1] the bottom alignment element
	 */
	public List<T> getAlignedElements(int alignmentIndex) {
		if(alignmentIndex < 0
				|| alignmentIndex >= getAlignmentLength())
			throw new ArrayIndexOutOfBoundsException();
		
		Integer topValue = topAlignment[alignmentIndex];
		Integer bottomValue = bottomAlignment[alignmentIndex];
		
		List<T> retVal = new ArrayList<T>();
		
//		Class theClass = null;
//		if(topElements.length > 0 && topElements[0] != null) {
//			theClass = topElements[0].getClass();
//		} else if (bottomElements.length > 0 && bottomElements[0] != null) {
//			theClass = bottomElements[0].getClass();
//		}
//		
//		if(theClass == null) theClass = Object.class;
//		
//		T[] retVal = (T[]) Array.newInstance(
//				theClass, 2);
		
		if(topValue >= topElements.length)
//			retVal[0] = null;
			retVal.add(null);
		else 
			retVal.add( 
				(topValue == INDEL_VALUE ? null : topElements[topValue]));
		
		if(bottomValue >= bottomElements.length)
			retVal.add(null);
		else
			retVal.add(
				(bottomValue == INDEL_VALUE ? null : bottomElements[bottomValue]));
		
		return retVal;
	}
	
}
