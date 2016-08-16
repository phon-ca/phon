/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.query.db.Script;

/**
 * Similar to {@link XMLQuery}, except loading of result set data is lazy
 * (i.e., delayed as long as possible).
 */
public class XMLLazyQuery extends XMLQuery {
	
	private final static Logger LOGGER = Logger.getLogger(XMLLazyQuery.class.getName());
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
