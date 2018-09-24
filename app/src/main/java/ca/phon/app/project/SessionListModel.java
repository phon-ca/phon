/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.project;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;

/**
 * Handles the data model for the session list in the
 * project window.
 *
 */
public class SessionListModel implements ListModel<String> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SessionListModel.class.getName());

	/** Data listeners */
	private ArrayList<ListDataListener> dataListeners;
	
	/** The project */
	private Project project;
	
	/** The corpus */
	private String corpus;
	
	/** Cached session list */
	private List<String> cachedSessions = null;
	private Object cachedMutex = new String();
	
	/** Constructor */
	public SessionListModel(Project project) {
		super();
		
		this.dataListeners = new ArrayList<ListDataListener>();
		
		this.project = project;
		this.corpus = null;
	}
	
	public List<String> getSessions() {
		if(cachedSessions == null) {
			synchronized(cachedMutex) {
				cachedSessions = project.getCorpusSessions(corpus);
			}
		}
		return cachedSessions;
	}
	
	@Override
	public String getElementAt(int index) {
		if(corpus == null)
			return null;
		
		try {
			Collator collator = CollatorFactory.defaultCollator();
			List<String> transcripts = 
				getSessions();
			Collections.sort(transcripts, collator);
			return transcripts.get(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			LOGGER.warn("Array index out of bounds: " + index);
		}
		
		return null;
	}

	@Override
	public int getSize() {
		if(corpus == null) return 0;
		return getSessions().size();
	}
	
	public void setCorpus(String corpus) {
		this.corpus = corpus;
		refresh();
		fireDataChange();
	}
	
	public String getCorpus() {
		return this.corpus;
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public void fireDataChange() {
		ListDataEvent lde = 
			new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize());
		ListDataListener listeners[] = 
			dataListeners.toArray(new ListDataListener[0]);
		for(ListDataListener listener:listeners) {
			listener.contentsChanged(lde);
		}
	}
	
	public void refresh() {
		synchronized(cachedMutex) {
			cachedSessions = project.getCorpusSessions(corpus);
		}
		fireDataChange();
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		if(!dataListeners.contains(l))
			dataListeners.add(l);
	}
	
	@Override
	public void removeListDataListener(ListDataListener l) {
		if(dataListeners.contains(l))
			dataListeners.remove(l);
	}
}
