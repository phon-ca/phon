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

package ca.phon.csv2phon.wizard;

import java.util.Calendar;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;

import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.csv2phon.io.SexType;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.xml.XMLConverters;

public class CSVImportParticipant implements Participant {
	
	/**
	 * The participant element
	 */
	public ParticipantType participantElement;
	
	@Override
	public String toString() {
		return getName();
	}
	
	public CSVImportParticipant() {
		this((new ObjectFactory()).createParticipantType());
	}
	
	public CSVImportParticipant(ParticipantType participantElement) {
//		super(participantElement);
		
		this.participantElement = participantElement;
	}

	

	@Override
	public Calendar getBirthDate() {
//		if(participantElement.isSetBirthday())
		Calendar retVal = Calendar.getInstance();
		if(participantElement.getBirthday() != null)
			retVal = participantElement.getBirthday().toGregorianCalendar();
//		else
//			return null;
		return retVal;
	}

	@Override
	public void setBirthDate(Calendar birthDate) {
		participantElement.setBirthday(XMLConverters.toXMLCalendar(birthDate));
	}

	@Override
	public String getEducation() {
		return participantElement.getEducation();
	}

	@Override
	public void setEducation(String education) {
		participantElement.setEducation(education);
	}

	@Override
	public String getGroup() {
		return participantElement.getGroup();
	}

	@Override
	public void setGroup(String group) {
		participantElement.setGroup(group);
	}

	@Override
	public String getId() {
		return participantElement.getId();
	}

	@Override
	public void setId(String id) {
		participantElement.setId(id);
	}

	@Override
	public String getLanguage() {
		String retVal = "";
		
		for(String l:participantElement.getLanguage()) {
			retVal += (retVal.length() > 0 ? ", " : "") + l;
		}
		
		return retVal;
	}

	@Override
	public void setLanguage(String language) {
		// split languages by ','
		participantElement.getLanguage().clear();
		String[] splitLangs = language.split(",");
		for(String lang:splitLangs) {
			participantElement.getLanguage().add(StringUtils.strip(lang));
		}
	}

	@Override
	public String getName() {
		return participantElement.getName();
	}

	@Override
	public void setName(String name) {
		participantElement.setName(name);
	}

	@Override
	public String getRole() {
		return participantElement.getRole();
	}

	@Override
	public void setRole(String role) {
		participantElement.setRole(role);
	}

	@Override
	public String getSES() {
		return participantElement.getSES();
	}

	@Override
	public void setSES(String ses) {
		participantElement.setSES(ses);
	}

	@Override
	public Sex getSex() {
//		if(participantElement.) {
			SexType sexType = participantElement.getSex();
			if(sexType == SexType.MALE)
				return Sex.MALE;
			else if(sexType == SexType.FEMALE)
				return Sex.FEMALE;
			else
				return null;
//		}
		
//		return null;
	}

	@Override
	public void setSex(Sex sex) {
		if(sex == Sex.MALE)
			participantElement.setSex(SexType.MALE);
		else if(sex == Sex.FEMALE)
			participantElement.setSex(SexType.FEMALE);
	}

	public Object getXMLData() {
		return participantElement;
	}

	@Override
	public int hashCode() {
//		MD5 md5 = new MD5();
//		try {
//			md5.Update(getXMLData().toString(), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//		}
//		return md5.asHex().hashCode();
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object b) {
		if(b == null) return false;
		return hashCode() == b.hashCode();
	}

	@Override
	public PhonDuration getAge(Calendar fromDate) {
		if(fromDate == null || getBirthDate() == null) 
			return null;
		
		PhonDuration duration =
			PhonDuration.getDuration(getBirthDate(), fromDate);
		return duration;
	}

	@Override
	public PhonDuration getAgeTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAge(PhonDuration age) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAgeTo(PhonDuration ageTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDictionary() {
		String retVal = "default";
		
		if(participantElement.getDictionary() != null) 
			retVal = participantElement.getDictionary();
		
		return retVal;
	}

	@Override
	public String getSyllabifier() {
		String retVal = "default";
		
		if(participantElement.getSyllabifier() != null) 
			retVal = participantElement.getSyllabifier();
		
		return retVal;
	}

	@Override
	public void setDictionary(String dict) {
		participantElement.setDictionary(dict);
	}

	@Override
	public void setSyllabifier(String syllabifier) {
		participantElement.setSyllabifier(syllabifier);
	}

	@Override
	public Set<Class<?>> getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Period getAge(DateTime fromDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAge(Period age) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAgeTo(Period ageTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBirthDate(DateTime birthDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRole(ParticipantRole role) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSex(ca.phon.session.Sex sex) {
		// TODO Auto-generated method stub
		
	}
}
