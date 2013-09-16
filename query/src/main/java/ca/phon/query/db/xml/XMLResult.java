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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.phon.query.db.ReportHelper;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultValue;
import ca.phon.query.db.xml.JAXBArrayList.Mapper;
import ca.phon.query.db.xml.io.resultset.MetaType;
import ca.phon.query.db.xml.io.resultset.ResultType;
import ca.phon.query.db.xml.io.resultset.ResultValueType;

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
	public List<ResultValue> getResultValues() {
		Mapper<ResultValue, ResultValueType> mapper = new Mapper<ResultValue, ResultValueType>() {
			@Override
			public ResultValueType map(ResultValue x) {
				if(x instanceof XMLResultValue)
					return ((XMLResultValue)x).getXMLObject();
				return null;
			}

			@Override
			public ResultValue create(ResultValueType x) {
				return new XMLResultValue(x);
			}
		};
		
		return new JAXBArrayList<ResultValue, ResultValueType>(result.getResultValue(), mapper);
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
}