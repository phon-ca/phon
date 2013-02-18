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

import java.util.List;

import ca.phon.ipa.Phone;


public interface IPhoneticRep {
	
	/**
	 * Get the list of phones.  The objects
	 * are references to current phone objects.  However,
	 * changes to the list will not be saved.  Use,
	 * setPhones to add/remove phones.
	 * 
	 * @return ArrayList&lt;IPhone&gt;
	 */
	public List<Phone> getPhones();
	
	/**
	 * Get the list of phones.  This list will not
	 * contain SyllableStress or SyllableBoundary phones.
	 * 
	 * @return ArrayList&lt;IPhone&gt;
	 */
	public List<Phone> getSoundPhones();
	
	/**
	 * Set the list of phones
	 * @param phones
	 */
	public void setPhones(List<Phone> phones);
	
	/**
	 * Get the transcription
	 * @return String
	 */
	public String getTranscription();
	
	/**
	 * Get the type
	 * @return Form
	 */
	public Form getForm();
	
	/**
	 * Set the form
	 * @param form
	 */
	public void setForm(Form form);
	
	/**
	 * Get the id
	 * @return String
	 */
	public String getID();
	
	/**
	 * Set the id
	 * @param id
	 */
	public void setID(String id);

}
