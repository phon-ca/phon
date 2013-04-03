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

import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;

import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;

/**
 * A session in a project.
 *
 */
public interface Session extends IExtendable {

	/** Get the corpus */
	public String getCorpus();
	
	/** Get the transcript name */
	public String getName();

	/** Get the transcript date */
	public DateTime getDate();
	
	/** Get the language */
	public String getLanguage();
	
	
	/** Get/Set the media file location */
	public String getMediaLocation();
	
	
	/** Get/Set the tier view */
	public List<TierOrderItem> getTierView();
	
	
	/** Get the list of transcribers */
	
	public Transcriber getTranscriber(String username);
	
	/**
	 * Get the metadata
	 * 
	 * @return Metadata
	 */
	public SessionMetadata getMetadata();
	
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
	 * Get the participant at the given index
	 * 
	 * @param idx
	 * @return the specified participant
	 */
	public Participant getParticipant(int idx);
	
	/** Set the corpus */
	public void setCorpus(String corpus);
	
	/** Set the transcript name */
	public void setName(String name);
	
	/** Get the transcript date */
	public void setDate(DateTime date);
	
	/** Set the language */
	public void setLanguage(String language);
	
	/** Media location */
	public void setMediaLocation(String mediaLocation);
	
	/** Tier view */
	public void setTierView(List<TierOrderItem> view);
	
	/**
	 * Add a new transcriber
	 */
	public void addTranscriber(Transcriber t);
	
	/**
	 * Remove a transcriber
	 */
	public void removeTranscriber(Transcriber t);
	public void removeTranscriber(String username);
	
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
	 * Sort records using the given comparator.
	 * 
	 * @param comp 
	 * @since Phon 1.5.0
	 */
	public void sortRecords(Comparator<Record> comp);
	
}
