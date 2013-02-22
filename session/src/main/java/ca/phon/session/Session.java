/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;

/**
 * A session in a project.
 *
 */
public interface Session extends IExtendable, Visitable<SessionElement> {

	/** Get the corpus */
	public String getCorpus();
	
	/** Set the corpus */
	public void setCorpus(String corpus);
	
	/** Get the transcript name */
	public String getName();
	
	/** Set the transcript name */
	public void setName(String name);
	
	/** Get the transcript date */
	public Calendar getDate();
	
	/** Get the transcript date */
	public void setDate(Calendar date);
	
	/** Get the language */
	public String getLanguage();
	
	/** Set the language */
	public void setLanguage(String language);
	
	/** Get/Set the media file location */
	public String getMediaLocation();
	public void setMediaLocation(String mediaLocation);
	
	/** Get/Set the tier view */
	public List<TierOrderItem> getTierView();
	
	public void setTierView(List<TierOrderItem> view);
	
	/** Get the list of transcribers */
	public List<Transcriber> getTranscribers();
	
	/**
	 * Add a new transcriber
	 */
	public Transcriber newTranscriber();
	
	/**
	 * Remove a transcriber
	 */
	public void removeTranscriber(Transcriber t);
	public void removeTranscriber(String username);
	
	public Transcriber getTranscriber(String username);
	
	/**
	 * Get the metadata
	 * 
	 * @return Metadata
	 */
	public SessionMetadata getMetainfo();
	
	/**
	 * Return the record at the given index.
	 * 
	 * @param pos
	 * @return the specified record
	 */
	public Record getRecord(int pos);
	
	/**
	 * Return the number of records.
	 * 
	 * @return the number of records
	 */
	public int getNumberOfRecords();
	
	/**
	 * Add a new record to the session
	 * 
	 * @param record
	 */
	public void addRecord(Record record);
	
	/**
	 * Add a new record to the list in the given position.
	 * 
	 * @param record
	 * @param idx
	 */
	public void addRecord(int pos, Record record);
	
	/**
	 * Remove a record from the session.
	 * 
	 * @param record
	 */
	public void removeRecord(Record record);
	
	/**
	 * Remove a record from the session
	 * 
	 * @param pos
	 */
	public void removeRecord(int pos);
	
	/**
	 * Get the position of the given record.
	 * 
	 * @param record
	 */
	public int getRecordPosition(Record record);
	
	/**
	 * Get the number of participants
	 * 
	 * @return the number of participants
	 */
	public int getNumberOfParticipants();

	/**
	 * Add a new participant
	 * 
	 * @param participant
	 */
	public void addParticipant(Participant participant);
	
	/**
	 * Remove a participant.
	 * 
	 * @param participant
	 */
	public void removeParticipant(Participant participant);
	
	/**
	 * Remove a participant
	 * 
	 * @param idx
	 */
	public void removeParticipant(int idx);
	
	/**
	 * Get the participant at the given index
	 * 
	 * @param idx
	 * @return the specified participant
	 */
	public Participant getParticipant(int idx);
	
	/**
	 * Sort records using the given comparator.
	 * 
	 * @param comp 
	 * @since Phon 1.5.0
	 */
	public void sortRecords(Comparator<Record> comp);
	
}
