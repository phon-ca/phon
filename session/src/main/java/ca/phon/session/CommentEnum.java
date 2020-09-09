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

package ca.phon.session;

public enum CommentEnum {
	// from https://talkbank.org/software/talkbank.xsd
	// see commentTypeType
	Activities,
	Bck,
	Date,
	InteractionType,
	Number,
	RecordingQuality,
	Transcription,
	Types,
	Blank,
	T,
	Generic,
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
	Exceptions,
	@Deprecated
	NewLanguage,
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
		"Interaction Type",
		"Number",
		"Recording Quality",
		"Transcription",
		"Blank",
		"T",
		"Generic",
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
		"Exceptions",
		"New Language",
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
			
			if(str.equalsIgnoreCase(ce.toString()) || str.equalsIgnoreCase(names[ce.ordinal()])) {
				retVal = ce;
				break;
			}	
		}
		
		return retVal;
	}
}
