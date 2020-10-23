/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
		if(resultSet.getMetaKeys() == null) {
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
