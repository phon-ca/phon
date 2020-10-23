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

import java.util.*;

import ca.phon.query.db.*;
import ca.phon.query.db.xml.io.resultset.*;

/**
 * XML-based implementation of {@link Result}.
 */
public class XMLResult implements Result, JAXBWrapper<ResultType> {
	/** JAXB object */
	private ResultType result;
	
	/**
	 * Default constructor.
	 */
	XMLResult() {
		this(new ResultType());
	}
	
	/**
	 * Constructs result from a JAXB result object.
	 * @param result
	 */
	XMLResult(ResultType result) {
		this.result = result;
	}
	
	/**
	 * Get the JAXB element associated with this object.
	 * @return
	 */
	@Override
	public ResultType getXMLObject() {
		return result;
	}

	@Override
	public int getRecordIndex() {
		return result.getRecordIndex();
	}

	@Override
	public void setRecordIndex(int index) {
		result.setRecordIndex(index);
	}

	@Override
	@SuppressWarnings("serial")
	public Map<String, String> getMetadata() {
		return new LinkedHashMap<String, String>() {
			// Instance initializer
			{
				for(MetaType meta : result.getMeta())
					super.put(meta.getKey(), meta.getValue());
			}
			
			@Override
			public void clear() {
				super.clear();
				result.getMeta().clear();
			}

			@Override
			public String put(String key, String value) {
				// See if there exists a MetaType object in the list already,
				// and if not, add one
				boolean found = false;
				for(MetaType meta : result.getMeta()) {
					if(meta.getKey().equals(key)) {
						found = true;
						meta.setValue(value);
					}
				}
				
				if(!found) {
					MetaType newMeta = new MetaType();
					newMeta.setKey(key);
					newMeta.setValue(value);
					result.getMeta().add(newMeta);
				}
				
				//
				return super.put(key, value);
			}

			@Override
			public void putAll(Map<? extends String, ? extends String> m) {
				for(Map.Entry<? extends String, ? extends String> entry : m.entrySet())
					put(entry.getKey(), entry.getValue());
			}

			@Override
			public String remove(Object key) {
				String ret = super.remove(key);
				if(ret != null) {
					Iterator<MetaType> iter = result.getMeta().iterator();
					while(iter.hasNext()) {
						MetaType meta = iter.next();
						if(meta.getKey().equals(key)) {
							iter.remove();
							break;
						}
					}
				}
				return ret;
			}
		};
	}
	
	@Override
	public String getSchema() {
		return result.getSchema();
	}

	@Override
	public void setSchema(String format) {
		result.setSchema(format == null ? "" : format);
	}
	
	@Override
	public String toString() {
		return ReportHelper.createResultString(this);
	}

	@Override
	public boolean isExcluded() {
		final ResultType xmlObj = getXMLObject();
		return (xmlObj != null ? xmlObj.isExcluded() : false);
	}

	@Override
	public void setExcluded(boolean excluded) {
		final ResultType xmlObj = getXMLObject();
		if(xmlObj != null) {
			xmlObj.setExcluded(excluded);
		}
	}

	@Override
	public Iterator<ResultValue> iterator() {
		return new ResultValueIterator();
	}

	@Override
	public int getNumberOfResultValues() {
		return result.getResultValue().size();
	}

	@Override
	public ResultValue getResultValue(int idx) {
		return new XMLResultValue(result.getResultValue().get(idx));
	}

	@Override
	public ResultValue removeResultValue(int idx) {
		final ResultValue retVal = getResultValue(idx);
		result.getResultValue().remove(idx);
		return retVal;
	}

	@Override
	public int addResultValue(ResultValue resultValue) {
		if(resultValue instanceof XMLResultValue) {
			result.getResultValue().add(((XMLResultValue)resultValue).getXMLObject());
			return result.getResultValue().size() - 1;
		} else {
			// convert type
			final XMLResultValue xmlResultVal = new XMLResultValue();
			xmlResultVal.setData(resultValue.getData());
			xmlResultVal.setGroupIndex(resultValue.getGroupIndex());
			xmlResultVal.setTierName(resultValue.getTierName());
			xmlResultVal.setRange(resultValue.getRange());
			return addResultValue(xmlResultVal);
		}
	}
	
	private final class ResultValueIterator implements Iterator<ResultValue> {
		
		private volatile int idx = 0;

		@Override
		public boolean hasNext() {
			return idx < getNumberOfResultValues();
		}

		@Override
		public ResultValue next() {
			return new XMLResultValue(result.getResultValue().get(idx++));
		}

		@Override
		public void remove() {
			result.getResultValue().remove(idx);
		}
		
	}

	@Override
	public Optional<ResultValue> getResultValue(String name) {
		for(int i = 0; i < getNumberOfResultValues(); i++) {
			var rv = getResultValue(i);
			if(rv.getName().equals(name)) {
				return Optional.of(rv);
			}
		}
		return Optional.empty();
	}
}