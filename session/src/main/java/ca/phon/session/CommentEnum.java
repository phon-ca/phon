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

package ca.phon.session;

public enum CommentEnum {
	// from https://talkbank.org/software/talkbank.xsd
	// see commentTypeType
	Activities,
	Bck,
	Date,
	Exceptions,
	InteractionType,
	Number,
	RecordingQuality,
	Transcription,
	Blank,
	T,
	Generic,
	NewLanguage,
	Location,
	NewEpisode,
	RoomLayout,
	Situation,
	TapeLocation,
	TimeDuration,
	TimeStart,
	Transcriber,
	Warning,
	Page,
	EndTurn,
	
	@Deprecated
	Coder,
	@Deprecated
	Coding,
	@Deprecated
	Education,
	@Deprecated
	Code,
	@Deprecated
	Pause,
	@Deprecated
	Script,
	@Deprecated
	Media,

	/** CHAT lazy-gem */
	LazyGem,
	
	/** CHAT begin gem */
	BeginGem,
	
	/** CHAT end gem */
	EndGem,
	
	/** CHAT Begin utterance group */
	BeginTcu,
	
	/** Chat End utternace group */
	EndTcu
	;
	
	private static final String[] names = {
		"Activities",
		"Bck",
		"Date",
		"Exceptions",
		"Interaction Type",
		"Number",
		"Recording Quality",
		"Transcription",
		"Blank",
		"T",
		"Generic",
		"New Language",
		"Location",
		"New Episode",
		"Room Layout",
		"Situation",
		"Tape Location",
		"Time Duration",
		"Time Start",
		"Transcriber",
		"Warning",
		"Page",
		"End Turn",
		
		// deprecated
		"Coder",
		"Coding",
		"Education",
		"Code",
		"Pause",
		"Script",
		"Media",
		
		// gems
		"LazyGem",
		"BeginGem",
		"EndGem",
		
		// record groups
		"BeginTcu",
		"EndTcu"
	};

	public String getTitle() {
		return names[ordinal()];
	}
	
	public static CommentEnum fromString(String str) {
		CommentEnum retVal = null;
		
		for(CommentEnum ce:values()) {
			
			if(str.equalsIgnoreCase(names[ce.ordinal()])) {
				retVal = ce;
				break;
			}	
		}
		
		return retVal;
	}
}
