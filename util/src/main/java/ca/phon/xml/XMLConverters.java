/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

package ca.phon.xml;

import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Methods for converting from XML returned types to
 * objects that Phon understands.
 * 
 *
 */
public class XMLConverters {
//	/** 
//	 * Convert a javax.xml.datatype.Duration object to a PhonDuration
//	 * object.
//	 * 
//	 * @param xmlDuration
//	 * @return PhonDuration
//	 */
//	public static PhonDuration toPhonDuration(Duration xmlDuration) {
//		PhonDuration phonDuration = new PhonDuration();
//			phonDuration.setSeconds(xmlDuration.getSeconds());
//			phonDuration.setMinutes(xmlDuration.getMinutes());
//			phonDuration.setHours(xmlDuration.getHours());
//			phonDuration.setDays(xmlDuration.getDays());
//			phonDuration.setMonths(xmlDuration.getMonths());
//			phonDuration.setYears(xmlDuration.getYears());
//		return phonDuration;
//	}
	
//	/** 
//	 * Convert a PhonDuration from a given
//	 * javax.xml.datatype.Duration object;
//	 * 
//	 * @param phonDuration
//	 * @return Duration
//	 */
//	public static Duration toXMLDuration(PhonDuration phonDuration) {
//		int years = phonDuration.getYears();
//		int months = phonDuration.getMonths();
//		int days = phonDuration.getDays();
//		int hours = phonDuration.getHours();
//		int minutes = phonDuration.getMinutes();
//		int seconds = phonDuration.getSeconds();
//		
//		DatatypeFactory factory;
//		try {
//			factory = DatatypeFactory.newInstance();
//			return factory.newDuration(true, years, months, days, hours, minutes, seconds);
//		} catch (DatatypeConfigurationException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	/**
	 * Convert a Calendar object on XMLGregorianCalendar object
	 * 
	 * @param date
	 * @return XMLGregorianCalendar
	 */
	public static XMLGregorianCalendar toXMLCalendar(Calendar date) {
		DatatypeFactory factory = null;
		XMLGregorianCalendar retVal = null;
		try {
			factory = DatatypeFactory.newInstance();
			retVal = factory.newXMLGregorianCalendar();
			
			retVal.setYear(date.get(Calendar.YEAR));
			retVal.setMonth(date.get(Calendar.MONTH)+1);
			retVal.setDay(date.get(Calendar.DAY_OF_MONTH));
			
			retVal.setTimezone(date.getTimeZone().getRawOffset()/1000/60);
			
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
}
