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
package ca.phon.app.session.editor;

import ca.phon.session.*;

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
