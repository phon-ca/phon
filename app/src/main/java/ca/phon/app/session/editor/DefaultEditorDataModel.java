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
package ca.phon.app.session.editor;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;

/**
 * Default data model for the {@link SessionEditor}
 * 
 */
public class DefaultEditorDataModel implements EditorDataModel {

	/**
	 * Reference to session
	 */
	private final WeakReference<Session> sessionRef;
	
	/**
	 * Transcriber
	 */
	private AtomicReference<Transcriber> transcriberRef = new AtomicReference<Transcriber>();
	
	/**
	 * Constructor
	 */
	public DefaultEditorDataModel(Session session) {
		super();
		this.sessionRef = new WeakReference<Session>(session);
	}

	@Override
	public Session getSession() {
		return sessionRef.get();
	}

	@Override
	public int getRecordCount() {
		int retVal = 0;
		
		final Session session = getSession();
		if(session != null) retVal = session.getRecordCount();
		
		return retVal;
	}

	@Override
	public Record getRecord(int idx) {
		Record retVal = null;
		
		final Session session = getSession();
		if(session != null) retVal = session.getRecord(idx);
		
		return retVal;
	}

	@Override
	public int getNextRecordIndex(int idx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPrevRecordIndex(int idx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RecordFilter getRecordFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRecordFilter(RecordFilter filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Transcriber getTranscriber() {
		return transcriberRef.get();
	}

	@Override
	public void setTranscriber(Transcriber transcriber) {
		transcriberRef.getAndSet(transcriber);
	}
	
}
