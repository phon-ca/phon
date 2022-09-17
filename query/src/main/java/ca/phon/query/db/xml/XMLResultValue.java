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

package ca.phon.query.db.xml;

import ca.phon.query.db.ResultValue;
import ca.phon.query.db.xml.io.resultset.*;
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

	@Override
	public int getMatcherGroupCount() {
		return resultValue.getMatcherGroup().size();
	}

	@Override
	public String getMatcherGroup(int index) {
		return resultValue.getMatcherGroup().get(index);
	}

	@Override
	public String getName() {
		if(resultValue.getName() != null)
			return resultValue.getName();
		else
			return getTierName();
	}

	@Override
	public void setName(String name) {
		resultValue.setName(name);
	}

}
