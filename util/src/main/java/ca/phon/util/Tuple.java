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
package ca.phon.util;

/**
 * Tuple of elements.
 *
 */
public class Tuple<T, S> implements Comparable<Tuple<? extends T, ? extends S>> {
	
	/**
	 * T obj
	 */
	private T obj1 = null;
	
	/**
	 * S obj
	 */
	private S obj2 = null;
	
	/**
	 * Constructor
	 */
	public Tuple() {
		
	}
	
	/**
	 * Constructor with objects
	 */
	public Tuple(T obj1, S obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	
	/*
	 * Getters/setters
	 */
	
	public T getObj1() {
		return this.obj1;
	}
	
	public void setObj1(T obj1) {
		this.obj1 = obj1;
	}
	
	public S getObj2() {
		return this.obj2;
	}
	
	public void setObj2(S obj2) {
		this.obj2 = obj2;
	}

	@Override
	public boolean equals(Object obj) {
		
		if(obj == null) return false;
		
		/*
		 * Do some type-checking
		 */
		if(!(obj instanceof Tuple)) return false;
		
		Tuple<Object, Object> bObj = (Tuple)obj;
		
		boolean obj1Match = false;
		if(getObj1() == null && bObj.getObj1() == null)
			obj1Match = true;
		else if(getObj1() != null && bObj.getObj1() != null && getObj1().equals(bObj.getObj1()))
			obj1Match = true;
		
		boolean obj2Match = false;
		if(getObj2() == null && bObj.getObj2() == null)
			obj2Match = true;
		else if(getObj2() != null && bObj.getObj2() != null && getObj2().equals(bObj.getObj2()))
			obj2Match = true;
		
		return obj1Match && obj2Match;
	}

	@Override
	public String toString() {
		String retVal = new String();
		
		retVal += "<";
		retVal += (getObj1() == null ? "null" : getObj1().toString());
		retVal += ",";
		retVal += (getObj2() == null ? "null" : getObj2().toString());
		retVal += ">";
		
		return retVal;
	}

	@Override
	public int compareTo(Tuple<? extends T, ? extends S> o) {
		return toString().compareTo(o.toString());
	}

//	@Override
//	public int hashCode() {
//		int hash = 0;
//		int nullHash = super.hashCode();
//		
//		if(getObj1() != null) {
//			hash = getObj1().hashCode();
//		} else {
//			hash = nullHash;
//		}
//		
//		if(getObj2() != null) {
//			hash |= getObj2().hashCode();
//		} else {
//			hash |= nullHash;
//		}
//		
//		return hash;
//	}
	
	
	
}
