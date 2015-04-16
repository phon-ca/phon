/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
import java.util.Map;

import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultValue;
import ca.phon.query.db.xml.io.resultset.MetaKeyList;
import ca.phon.query.db.xml.io.resultset.ObjectFactory;
import ca.phon.query.db.xml.io.resultset.ResultSetType;
import ca.phon.query.db.xml.io.resultset.ResultType;

/**
 * XML-based implementation of {@link ResultSet}. 
 */
public class XMLResultSet implements ResultSet, JAXBWrapper<ResultSetType> {
	/** JAXB object */ 
	protected ResultSetType resultSet;
	
	/**
	 * Default constructor.
	 */
	XMLResultSet() {
		this(new ResultSetType());
	}
	
	/**
	 * Constructs result set from a JAXB result set object.
	 * @param xmlResult
	 */
	XMLResultSet(ResultSetType xmlResult) {
		this.resultSet = xmlResult;
	}
	
	/**
	 * Get the JAXB element associated with this object.
	 * @return
	 */
	@Override
	public ResultSetType getXMLObject() {
		return resultSet;
	}
	
	@Override
	public String getCorpus() {
		return resultSet.getSessionPath().split("\\.")[0];
	}

	@Override
	public String getSession() {
		return resultSet.getSessionPath().split("\\.")[1];
	}

	@Override
	public String getSessionPath() {
		return resultSet.getSessionPath();
	}

	@Override
	public void setSessionPath(String sessionPath) {
		resultSet.setSessionPath(sessionPath);
	}
	
	@Override
	public void setSessionPath(String corpus, String session) {
		resultSet.setSessionPath(corpus + "." + session);
	};
	
	@Override
	public int size() {
		return resultSet.getResult().size();
	}
	
	@Override
	public Result removeResult(int idx) {
		Result retVal = getResult(idx);
		resultSet.getResult().remove(idx);
		return retVal;
	}
	
	@Override
	public Result getResult(int idx) {
		if(idx < 0 || idx >= size())
			throw new ArrayIndexOutOfBoundsException(idx);
		Result retVal = new XMLResult(resultSet.getResult().get(idx));
		return retVal;
	}
	
	@Override
	public void addResult(Result res) {
		if(res instanceof XMLResult) {
			resultSet.getResult().add( ((XMLResult)res).getXMLObject() );
		} else {
			final XMLResult result = new XMLResult();
			result.setExcluded(res.isExcluded());
			result.setRecordIndex(res.getRecordIndex());
			result.setSchema(res.getSchema());
			for(ResultValue rv:res) {
				result.addResultValue(rv);
			}
			addResult(result);
		}
	}
	
	private int numberOfExcludedResults() {
		int retVal = 0;
		for(ResultType r:resultSet.getResult()) {
			if(r.isExcluded()) retVal++;
		}
		return retVal;
	}
	
	@Override
	public int numberOfResults(boolean includeExcluded) {
		int retVal = resultSet.getResult().size();
		if(!includeExcluded) {
			retVal -= numberOfExcludedResults();
		}
		return retVal;
	}

	@Override
	public String[] getMetadataKeys() {
		if(!resultSet.isSetMetaKeys()) {
			setupMetadataKeys();
		}
		return resultSet.getMetaKeys().getMetaKey().toArray(new String[0]);
	}
	
	private void setupMetadataKeys() {
		final ObjectFactory factory = new ObjectFactory();
		final MetaKeyList keyList = factory.createMetaKeyList();
		resultSet.setMetaKeys(keyList);
		
		for(final Result r:this) {
			final Map<String, String> metadata = r.getMetadata();
			for(String metakey:metadata.keySet()) {
				if(!resultSet.getMetaKeys().getMetaKey().contains(metakey)) 
					resultSet.getMetaKeys().getMetaKey().add(metakey);
			}
		}
	}

	@Override
	public Iterator<Result> iterator(boolean includeExcluded) {
		return new ResultIterator(includeExcluded);
	}

	@Override
	public Iterator<Result> iterator() {
		return new ResultIterator();
	}
	
	private final class ResultIterator implements Iterator<Result> {
		
		private final boolean includeExcluded;
		
		private volatile int idx = 0;
		
		public ResultIterator() {
			this(true);
		}
		
		public ResultIterator(boolean includeExcluded) {
			super();
			this.includeExcluded = includeExcluded;
		}

		@Override
		public boolean hasNext() {
			if(idx < numberOfResults(true)) {
				if(includeExcluded) {
					return true;
				} else {
					final ResultType rt = resultSet.getResult().get(idx);
					if(rt.isExcluded()) {
						++idx; return hasNext();
					} else {
						return true;
					}
				}
			} else {
				return false;
			}
		}

		@Override
		public Result next() {
			return new XMLResult(resultSet.getResult().get(idx++));
		}

		@Override
		public void remove() {
			resultSet.getResult().remove(idx);
		}
		
	}
}
