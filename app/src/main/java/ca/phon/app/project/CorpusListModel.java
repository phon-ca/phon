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
import java.util.*;
import java.util.logging.Logger;

import javax.swing.ListModel;
import javax.swing.event.*;

import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;

/**
 * Handles the data model for the corpus list in the
 * project window.
 *
 */
public class CorpusListModel implements ListModel<String> {
	
	private final static Logger LOGGER = Logger.getLogger(CorpusListModel.class.getName());
	
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
			LOGGER.warning("Array index out of bounds: " + index);
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
