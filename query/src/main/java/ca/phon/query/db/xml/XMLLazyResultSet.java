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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import ca.phon.query.db.Result;
import ca.phon.query.db.xml.io.resultset.ResultSetType;

/**
 * Similar to {@link XMLResultSet}, except loading of result set data is lazy
 * (i.e., delayed as long as possible).
 */
public class XMLLazyResultSet extends XMLResultSet {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(XMLLazyResultSet.class.getName());
	
	/** The file where data is located */
	private File resultSetFile;
	
	/** The result set manager to use when loading is necessary */
	private XMLResultSetManager manager;
	
	private boolean  isLoaded = false;
	
	/**
	 * Constructs a lazy result set loader from the given file. 
	 * 
	 * @param resultSetFile  the file containing the result set XML data
	 */
	XMLLazyResultSet(XMLResultSetManager manager, File resultSetFile) {
		this.manager = manager;
		this.resultSetFile = resultSetFile;
		
		String [] info = resultSetFile.getName().split("\\.");
		super.setSessionPath(info[0], info[1]);
	}
	
	/**
	 * Lazy loading of XML data.
	 */
	protected void loadData() {
		if(!isLoaded) {
			try {
				if(manager != null) {
					resultSet = manager.loadResultSet(resultSetFile);
					manager = null; // make null so it can be garbage collected
					isLoaded = true;
				}
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}
	
	@Override
	public ResultSetType getXMLObject() {
		loadData();
		return super.getXMLObject();
	}

	@Override
	public int size() {
		loadData();
		return super.size();
	}

	@Override
	public Result removeResult(int idx) {
		loadData();
		return super.removeResult(idx);
	}

	@Override
	public Result getResult(int idx) {
		loadData();
		return super.getResult(idx);
	}

	@Override
	public void addResult(Result res) {
		loadData();
		super.addResult(res);
	}

	@Override
	public int numberOfResults(boolean includeExcluded) {
		loadData();
		return super.numberOfResults(includeExcluded);
	}

	@Override
	public Iterator<Result> iterator(boolean includeExcluded) {
		loadData();
		return super.iterator(includeExcluded);
	}

	@Override
	public Iterator<Result> iterator() {
		loadData();
		return super.iterator();
	}
	
}
