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
package ca.phon.app.session.editor;

import java.lang.ref.*;
import java.util.concurrent.atomic.*;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.*;

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
