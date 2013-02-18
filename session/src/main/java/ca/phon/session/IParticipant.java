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

import java.util.Calendar;

import ca.phon.util.PhonDuration;

public interface IParticipant {

	/**
	 * @return Returns the age.
	 */
	public abstract PhonDuration getAge(Calendar fromDate);

	/**
	 * @param age The age to set.
	 */
	public abstract void setAge(PhonDuration age);

	/**
	 * @return Returns the ageTo.
	 */
	public abstract PhonDuration getAgeTo();

	/**
	 * @param ageTo The ageTo to set.
	 */
	public abstract void setAgeTo(PhonDuration ageTo);

	/**
	 * @return Returns the birthDate.
	 */
	public abstract Calendar getBirthDate();

	/**
	 * @param birthDate The birthDate to set.
	 */
	public abstract void setBirthDate(Calendar birthDate);

	/**
	 * @return Returns the education.
	 */
	public abstract String getEducation();

	/**
	 * @param education The education to set.
	 */
	public abstract void setEducation(String education);

	/**
	 * @return Returns the group.
	 */
	public abstract String getGroup();

	/**
	 * @param group The group to set.
	 */
	public abstract void setGroup(String group);

	/**
	 * @return Returns the id.
	 */
	public abstract String getId();

	/**
	 * @param id The id to set.
	 */
	public abstract void setId(String id);

	/**
	 * @return Returns the language.
	 */
	public abstract String getLanguage();

	/**
	 * @param language The language to set.
	 */
	public abstract void setLanguage(String language);

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	/**
	 * @param name The name to set.
	 */
	public abstract void setName(String name);

	/**
	 * @return Returns the role.
	 */
	public abstract String getRole();

	/**
	 * @param role The role to set.
	 */
	public abstract void setRole(String role);

	/**
	 * @return Returns the sES.
	 */
	public abstract String getSES();

	/**
	 * @param ses The sES to set.
	 */
	public abstract void setSES(String ses);

	/**
	 * @return Returns the sex.
	 */
	public abstract Sex getSex();

	/**
	 * @param sex The sex to set.
	 */
	public abstract void setSex(Sex sex);
	
	/**
	 * @return The syllabifier to use for this participant.
	 * Returns 'default' if not specified
	 */
	public abstract String getSyllabifier();
	
	/**
	 * @param the syllabifier to use
	 */
	public abstract void setSyllabifier(String syllabifier);
	
	/**
	 * @return The dictionary to use for this participant.
	 * Returns 'default' if not specified.
	 */
	public abstract String getDictionary();
	
	/**
	 * @param the dictionary to use for this participant.
	 */
	public abstract void setDictionary(String dict);

}