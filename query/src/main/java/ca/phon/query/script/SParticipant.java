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
package ca.phon.query.script;

import java.util.Calendar;

import ca.phon.application.transcript.IParticipant;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.Sex;
import ca.phon.util.PhonDateFormat;
import ca.phon.util.PhonDuration;
import ca.phon.util.PhonDurationFormat;

public class SParticipant {
	
	/** The participant object */
	private IParticipant participant;
	
	private ITranscript transcript;
	
	public SParticipant(IParticipant part, ITranscript t) {
		super();
		
		this.participant = part;
		this.transcript = t;
	}
	
	/**
	 * Return the name of the participant.
	 * 
	 * @return the participant name
	 */
	public String getName() {
		if(this.participant == null) return "";
		return this.participant.getName();
	}
	
	/**
	 * Return the birthday of the participant in the format
	 * YYYY-MM-DD.  This format allows for standard
	 * comparaison operations to function normally.
	 * 
	 * e.g. <CODE>date1 < date2</CODE>
	 * 
	 * @return the participant's birthday
	 */
	public String getBirthday() {
		if(this.participant == null) return "0000-00-00";
		Calendar bday = this.participant.getBirthDate();
		
		PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
		return pdf.format(bday);
	}
	
	/**
	 * Return the age of the participant in the format
	 * Y;M.D.
	 * 
	 * @return the age of the participant
	 */
	public String getAge() {
		if(this.participant == null) return "00;00.00";
		
		PhonDuration age = this.participant.getAge(transcript.getDate());
		
		PhonDurationFormat pdf = 
			new PhonDurationFormat(PhonDurationFormat.PHON_FORMAT);
		return pdf.format(age);
		
	}
	
	/**
	 * Return the gender of the particpant
	 * 
	 * @return either M or F
	 */
	public Sex getSex() {
		if(this.participant == null) return Sex.MALE;
		
		return this.participant.getSex();
	}
	
	@Override
	public String toString() {
		String retVal = "";
		if(this.participant != null)
			retVal = this.participant.getName();
		return retVal;
	}
}
