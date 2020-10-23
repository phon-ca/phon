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

import java.io.*;
import java.time.*;
import java.util.*;

import ca.phon.query.db.*;

/**
 * Similar to {@link XMLQuery}, except loading of result set data is lazy
 * (i.e., delayed as long as possible).
 */
public class XMLLazyQuery extends XMLQuery {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(XMLLazyQuery.class.getName());
	
	/** The file where data is located */
	private File queryFile;
	
	/** The result set manager to use when loading is necessary */
	private XMLResultSetManager manager;
	
	private boolean  isLoaded = false;
	
	/**
	 * Constructs a lazy result set loader from the given file.
	 *  
	 * @param queryFile  the file containing the query XML data
	 */
	XMLLazyQuery(XMLResultSetManager manager, File queryFile) {
		this.manager = manager;
		this.queryFile = queryFile;
		super.setName(queryFile.getParentFile().getName());
	}
	
	/**
	 * Lazy loading of XML data.
	 */
	private void loadData() {
		if(!isLoaded) {
			try {
				if(manager != null) {
					query = manager.loadQuery(queryFile);
					manager = null; // make null so it can be garbage collected
					isLoaded = true;
				}
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public UUID getUUID() {
		loadData();
		return super.getUUID();
	}

	@Override
	public void setUUID(UUID uuid) {
		loadData();
		super.setUUID(uuid);
	}

	@Override
	public LocalDateTime getDate() {
		loadData();
		return super.getDate();
	}

	@Override
	public void setDate(LocalDateTime date) {
		loadData();
		super.setDate(date);
	}

	@Override
	public boolean isStarred() {
		loadData();
		return super.isStarred();
	}

	@Override
	public void setStarred(boolean starred) {
		loadData();
		super.setStarred(starred);
	}
	
	@Override
	public Script getScript() {
		loadData();
		return super.getScript();
	}
	
	@Override
	public void setScript(Script script) {
		loadData();
		super.setScript(script);
	}
	
	@Override
	public List<String> getTags() {
		loadData();
		return super.getTags();
	}

	@Override
	public String getComments() {
		loadData();
		return super.getComments();
	}

	@Override
	public void setComments(String comments) {
		loadData();
		super.setComments(comments);
	}

	
}
