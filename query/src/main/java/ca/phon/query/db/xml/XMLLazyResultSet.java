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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.query.db.Result;
import ca.phon.query.db.xml.io.resultset.ResultSetType;

/**
 * Similar to {@link XMLResultSet}, except loading of result set data is lazy
 * (i.e., delayed as long as possible).
 */
public class XMLLazyResultSet extends XMLResultSet {
	
	private final static Logger LOGGER = Logger.getLogger(XMLLazyResultSet.class.getName());
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public List<Result> getResults() {
		loadData();
		return super.getResults();
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
	
	
}
