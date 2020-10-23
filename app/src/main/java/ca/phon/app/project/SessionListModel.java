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
package ca.phon.app.project;

import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.project.*;
import ca.phon.util.*;

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
