/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.script;

import ca.phon.util.Range;

/**
 * An interface defining an object as search-able.  If an
 * object is search-able, it should be able to be added to
 * a result set.
 */
public interface ResultValue {
	
	/** Return the record index this object's data belongs to */
	public int getRecordIndex();
	
	/** Return the tier this object's data belongs to */
	public String getTier();
	
	/** 
	 * If the tier is group-aligned, this will
	 * return the group index the data belongs to.
	 * Otherwise it should return <CODE>-1</CODE>.
	 */
	public int getGroupIndex();
	
	/**
	 * Return the range of characters (within the group)
	 * to which this object's data represents.
	 */
	public Range getDataRange();
}
