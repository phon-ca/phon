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

public enum CommentEnum {
	/*
	 * <xs:enumeration value="Activities"/>
          <xs:enumeration value="Bck"/>
          <xs:enumeration value="Coder"/>
          <xs:enumeration value="Coding"/>
          <xs:enumeration value="Date"/>
          <xs:enumeration value="Education"/>
          <xs:enumeration value="Exceptions"/>
          <xs:enumeration value="Generic"/>
          <!-- Added 3-31-05 -->
          <xs:enumeration value="Code"/>
          <xs:enumeration value="New Language"/>
          <xs:enumeration value="Location"/>
          <xs:enumeration value="New Episode"/>
          <xs:enumeration value="Page"/>
          <xs:enumeration value="Pause"/>
          <xs:enumeration value="Room Layout"/>
          <xs:enumeration value="Script"/>
          <xs:enumeration value="Situation"/>
          <!--
              <xs:enumeration value="Stim"/>
-->
          <xs:enumeration value="Tape Location"/>
          <xs:enumeration value="Time Duration"/>
          <xs:enumeration value="Time Start"/>
          <!--
              <xs:enumeration value="Transcriber"/>
-->
          <xs:enumeration value="Warning"/>
          <!-- MUN 7 Mar 2005: Added 'Media' as a comment type -->
          <xs:enumeration value="Media"/>
	 */
	Activities,
	Bck,
	Coder,
	Coding,
	Date,
	Education,
	Exceptions,
	Generic,
	Code,
	NewLanguage,
	Location,
	NewEpisode,
	Page,
	Pause,
	RoomLayout,
	Script,
	Situation,
	TapeLocation,
	TimeDuration,
	TimeStart,
	Warning,
	Media,

	// gems
	LazyGem
	;
	
	private static final String[] names = {
		"Activities",
		"Bck",
		"Coder",
		"Coding",
		"Date",
		"Education",
		"Exceptions",
		"Generic",
		"Code",
		"New Language",
		"Location",
		"New Episode",
		"Page",
		"Pause",
		"Room Layout",
		"Script",
		"Situation",
		"Tape Location",
		"Time Duration",
		"Time Start",
		"Warning",
		"Media",
		"LazyGem"
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
