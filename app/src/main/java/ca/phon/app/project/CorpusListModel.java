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
package ca.phon.app.project;

import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;

import javax.swing.*;
import javax.swing.event.*;
import java.text.Collator;
import java.util.*;

/**
 * Handles the data model for the corpus list in the
 * project window.
 *
 */
public class CorpusListModel implements ListModel<String> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(CorpusListModel.class.getName());
	
	/** The project */
	private Project project;
	
	/** Data listeners */
	private ArrayList<ListDataListener> dataListeners;
	
	/** Cached corpora */
	private List<String> cachedCorpora = null;
	private Object cachedMutex = new String();
	
	/** Constructor */
	public CorpusListModel(Project project) {
		super();
		
		this.dataListeners = new ArrayList<ListDataListener>();
		
		this.project = project;
	}
	
	public List<String> getCorpora() {
		if(cachedCorpora == null) {
			synchronized(cachedMutex) {
				cachedCorpora = project.getCorpora();
			}
		}
		return cachedCorpora;
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

	@Override
	public String getElementAt(int index) {
		try {
//			ArrayList<String> corpora = project.getCorpora();
			Collator collator = CollatorFactory.defaultCollator();
			List<String> corpora = new ArrayList<String>(getCorpora());
			Collections.sort(corpora, collator);
			return corpora.get(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			LOGGER.warn("Array index out of bounds: " + index);
		}
		return null;
	}

	@Override
	public int getSize() {
		return getCorpora().size();
	}
	
	public void fireDataChange() {
		ListDataListener listeners[] = 
			dataListeners.toArray(new ListDataListener[0]);
		
		ListDataEvent lde = new ListDataEvent(this,
				ListDataEvent.CONTENTS_CHANGED, 0, this.getSize());
		
		for(ListDataListener l:listeners) {
			l.contentsChanged(lde);
		}
	}
	
	public void refresh() {
		synchronized(cachedMutex) {
			cachedCorpora = project.getCorpora();
		}
		fireDataChange();
	}
	
}
