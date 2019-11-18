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

import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.session.filter.RecordFilter;

/**
 * Data model for the session editor.
 *
 */
public interface EditorDataModel {

	/**
	 * Get the session (if any) associated with the editor.
	 * 
	 * @return session
	 */
	public Session getSession();
	
	/**
	 * Get the number of records.
	 * 
	 * @return number of records
	 */
	public int getRecordCount();
	
	/**
	 * Get record at given index.
	 * 
	 * @param idx
	 * @return record
	 */
	public Record getRecord(int idx);
	
	/**
	 * Return the index of the next record from the
	 * given index.  This method uses the record
	 * filter if present to determine what the 'next'
	 * record would be in a list.
	 * 
	 * @param idx
	 * @return the index of the next record or < 0 if
	 *  at the end of the list or idx is out of bounds
	 */
	public int getNextRecordIndex(int idx);
	
	/**
	 * Return the index of the previous record from the
	 * given index.  This method uses the record filter
	 * if present to determine what the 'next' record
	 * would be in a list.
	 * 
	 * @param idx
	 * @return the index of the next record or < 0 if
	 *  at the beginning of the list or idx is out of bounds
	 */
	public int getPrevRecordIndex(int idx);
	
	/**
	 * Record filter associated with the model
	 * 
	 * @return the record filter, may be <code>null</code>
	 */
	public RecordFilter getRecordFilter();
	
	/**
	 * Set the record filter associated with the model.
	 * This may change the record count in the session.
	 * Use <code>null</code> to turn off filtering.
	 * 
	 * @param filter
	 */
	public void setRecordFilter(RecordFilter filter);
	
	/**
	 * If the session is in 'blind' mode then this
	 * method will return the transcriber that is currently
	 * making edits.
	 * 
	 * @return transcriber or <code>null</code> if not in
	 *  blind mode
	 */
	public Transcriber getTranscriber();
	
	/**
	 * Set the blind mode transcriber
	 * 
	 * @param transcriber
	 */
	public void setTranscriber(Transcriber transcriber);
	
}
