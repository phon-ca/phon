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

import java.util.List;
import java.util.Map;

import ca.phon.engines.search.db.Result;
import ca.phon.engines.search.db.ResultSet;
import ca.phon.engines.search.db.xml.JAXBArrayList.Mapper;
import ca.phon.engines.search.db.xml.io.resultset.MetaKeyList;
import ca.phon.engines.search.db.xml.io.resultset.ObjectFactory;
import ca.phon.engines.search.db.xml.io.resultset.ResultSetType;
import ca.phon.engines.search.db.xml.io.resultset.ResultType;

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
	public List<Result> getResults() {
		Mapper<Result, ResultType> mapper = new Mapper<Result, ResultType>() {
			@Override
			public ResultType map(Result x) {
				if(x instanceof XMLResult)
					return ((XMLResult)x).getXMLObject();
				return null;
			}

			@Override
			public Result create(ResultType x) {
				return new XMLResult(x);
			}
		};
		
		return new JAXBArrayList<Result, ResultType>(resultSet.getResult(), mapper);
	}
	
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
		getResults().add(res);
	}
	
	private int numberOfExcludedResults() {
		int retVal = 0;
		for(Result r:getResults()) {
			if(r.isExcluded()) retVal++;
		}
		return retVal;
	}
	
	@Override
	public int numberOfResults(boolean includeExcluded) {
		int retVal = getResults().size();
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
		
		for(final Result r:getResults()) {
			final Map<String, String> metadata = r.getMetadata();
			for(String metakey:metadata.keySet()) {
				if(!resultSet.getMetaKeys().getMetaKey().contains(metakey)) 
					resultSet.getMetaKeys().getMetaKey().add(metakey);
			}
		}
	}
}
