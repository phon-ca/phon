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

/**
 * Interface for transcript objects.
 * 
 * 
 *
 */
public interface ISession {

	/** Get the transcript ID */
	public String getID();
	
	/** Set the transcript ID */
	public void setID(String id);
	
	/** Get the transcript Version */
	public String getVersion();
	
	/** Set the Version */
	public void setVersion(String version);
	
	/** Get the transcript date */
	public Calendar getDate();
	
	/** Get the transcript date */
	public void setDate(Calendar date);
	
	/** Get the corpus */
	public String getCorpus();
	
	/** Set the corpus */
	public void setCorpus(String corpus);
	
	/** Get the language */
	public String getLanguage();
	
	/** Set the language */
	public void setLanguage(String language);
	
	/** Get/Set the media file location */
	public String getMediaLocation();
	public void setMediaLocation(String mediaLocation);
	
	/** Get/Set the tier view */
	public List<ITierOrderItem> getTierView();
	
	public void setTierView(List<ITierOrderItem> view);
	
	/** Get the list of transcribers */
	public List<ITranscriber> getTranscribers();
	
	/**
	 * Add a new transcriber
	 */
	public ITranscriber newTranscriber();
	
	/**
	 * Remove a transcriber
	 */
	public void removeTranscriber(ITranscriber t);
	public void removeTranscriber(String username);
	
	public ITranscriber getTranscriber(String username);
	
	/**
	 * Get the metadata
	 * 
	 * @return Metadata
	 */
	public IMetadata getMetainfo();
	
	/**
	 * Get the transcript data.
	 * 
	 * @return <CODE>java.util.ArrayList&lt;TranscriptElement&lt;Object&gtl;&gt;</CODE>
	 * @deprecated use getUtterance(int) instead
	 */
	public List<IUtterance> getUtterances();
	
	/**
	 * Return the utterance at the given index.
	 * 
	 * @param uttindex
	 * @return the utterance
	 */
	public IUtterance getUtterance(int uttindex);
	
	/**
	 * Return the number of utterances.
	 * 
	 * @return the number of utterances
	 */
	public int getNumberOfUtterances();
	
	/**
	 * Add a new utterance to the data and return the empty object.
	 * 
	 * @return <CODE>IUtterance</CODE>
	 */
	public IUtterance newUtterance();
	
	/**
	 * Add a new utterance to the list in the given position.
	 * 
	 * @param pos if <=0, a new record will be inserted at the beginning of the
	 * list, if pos >utts.size() a new record will be inserted at the end of the
	 * list.
	 * @return IUtterance
	 */
	public IUtterance newUtterance(int pos);
	
	/**
	 * Remove an utterance from the data.
	 * 
	 * @param utt
	 */
	public void removeUtterance(IUtterance utt);
	
	/**
	 * Get the utterance index
	 * @param utt
	 * @return int the index of the given utterance or <CODE>-1</CODE> if not
	 * found.
	 */
	public int getUtteranceIndex(IUtterance utt);
	
	/**
	 * Get the participants.
	 * 
	 * @return <CODE>java.util.ArrayList&lt;IParticipant&gt;</CODE>
	 */
	public List<IParticipant> getParticipants();
	
	/**
	 * Add a new participant and return the empty object.
	 * 
	 * @return IParticipant
	 */
	public IParticipant newParticipant();
	
	/**
	 * Remove a participant.
	 * 
	 * @param participant
	 */
	public void removeParticipant(IParticipant participant);
	
	/**
	 * Return a list of all dependant tiers found.
	 * @return ArrayList<String>
	 */
	public List<IDepTierDesc> getDependentTiers();
	
	/**
	 * Add a new dependent tier
	 */
	public IDepTierDesc newDependentTier();
	
	/**
	 * Remove a dependent tier
	 */
	public void removeDependentTier(String tierName);
	
	/**
	 * Return the list of word-aligned dependent
	 * tiers.
	 * 
	 * @return ArrayList<String>
	 */
	public List<IDepTierDesc> getWordAlignedTiers();
	
	/**
	 * Return the contents of the transcript as a list of
	 * TranscriptElements.  
	 * 
	 * 
	 */
	public List<TranscriptElement<Object>> getTranscriptElements();
	
	/**
	 * Sort records using the given comparator.
	 * 
	 * @param comp
	 * @since Phon 1.5.0
	 */
	public void sortRecords(Comparator<IUtterance> comp);
	
	/**
	 * Add a new comment to the transcript.  The comment will be added
	 * after the last utterance.
	 * 
	 * @return the comment object
	 */
	public IComment newComment();
	
//	/**
//	 * Get the XML formatted data
//	 */
//	public StringBuffer getTranscriptFormattedData()
//		throws IOException;
	
//	/**
//	 * Get the XML data as a StringBuffer
//	 */
//	public StringBuffer getTranscriptRawData()
//		throws IOException;
	
	/**
	 * Load transcript from file.  The file is retained
	 * and subsequent calls to saveTranscript() will
	 * save to this file.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void loadTranscriptFile(File f) 
		throws IOException;
	
	/**
	 * Save the transcript to the file given
	 * by the last call to loadTranscriptFile(File f)
	 * 
	 * @throws IOException
	 */
	public void saveTranscript()
		throws IOException;
	
	/**
	 * Load the transcript data from the given stream.
	 * 
	 * @param in
	 * @throws IOException
	 * @deprecated use loadTranscriptFile(File f) instead
	 */
	public void loadTranscriptData(InputStream in)
		throws IOException;
	
	/**
	 * Save the transcript data to the given stream.
	 * 
	 * @deprecated use loadTranscriptFile(File f) and saveTranscript() instead
	 */
	public void saveTranscriptData(OutputStream out)
		throws IOException;
}
