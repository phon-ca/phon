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

package ca.phon.query.db.xml;

import ca.phon.engines.search.db.ResultValue;
import ca.phon.engines.search.db.xml.io.resultset.RangeType;
import ca.phon.engines.search.db.xml.io.resultset.ResultValueType;
import ca.phon.util.Range;

/**
 * A result value used in the {@link XMLResult}.  
 */
public class XMLResultValue implements ResultValue, JAXBWrapper<ResultValueType> {
	/** JAXB object */
	private ResultValueType resultValue;
	
	/**
	 * Default constructor.
	 */
	XMLResultValue() {
		this(new ResultValueType());
	}
	
	/**
	 * Constructs a result value from a JAXB result value object. 
	 * @param resultValue
	 */
	XMLResultValue(ResultValueType resultValue) {
		this.resultValue = resultValue;
	}
	
	/**
	 * Get the JAXB element associated with this object.
	 * @return
	 */
	@Override
	public ResultValueType getXMLObject() {
		return resultValue;
	}
	
	@Override
	public String getTierName() {
		return resultValue.getTierName();
	}

	@Override
	public void setTierName(String tierName) {
		resultValue.setTierName(tierName);
	}

	@Override
	public Range getRange() {
		RangeType r = resultValue.getRange();
		return new Range(r.getStartIndex(), r.getEndIndex(), r.isExcludesEnd());
	}

	@Override
	public void setRange(Range range) {
		RangeType r = new RangeType();
		r.setStartIndex(range.getStart());
		r.setEndIndex(range.getEnd());
		r.setExcludesEnd(range.isExcludesEnd());
		resultValue.setRange(r);
	}

	@Override
	public int getGroupIndex() {
		return resultValue.getGroupIndex();
	}

	@Override
	public void setGroupIndex(int groupIndex) {
		resultValue.setGroupIndex(groupIndex);
	}

	@Override
	public String getData() {
		return resultValue.getData();
	}

	@Override
	public void setData(String data) {
		resultValue.setData(data);
	}
	
}