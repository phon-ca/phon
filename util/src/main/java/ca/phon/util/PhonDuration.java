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

package ca.phon.util;

import java.util.Calendar;

/**
 * Class to represent the a duration of time.  Usually
 * used for calculating the age of Participants.
 */
public class PhonDuration {
	/** Years */
	private int years = 0;
	/** Months */
	private int months = 0;
	/** Days */
	private int days = 0;
	/** Hours */
	private int hours = 0;
	/** Minutes */
	private int minutes = 0;
	/** Seconds */
	private int seconds = 0;
	/** Is is a negative duration */
	private boolean negative = false;
	
	/**
	 * Constructor
	 * @param years
	 * @param months
	 * @param days
	 * @param hours
	 * @param minutes
	 * @param seconds
	 */
	public PhonDuration(int years, int months, int days, int hours, int minutes,
			int seconds) {
		super();
		this.years = years;
		this.months = months;
		this.days = days;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}
	
	/**
	 * Constructor
	 * @param years
	 * @param months
	 * @param days
	 */
	public PhonDuration(int years, int months, int days) {
		super();
		this.years = years;
		this.months = months;
		this.days = days;
	}
	
	/**
	 * Constructor
	 */
	public PhonDuration() {
		super();
	}
	
	
	/**
	 * @return Returns the days.
	 */
	public int getDays() {
		return days;
	}
	/**
	 * @param days The days to set.
	 */
	public void setDays(int days) {
		this.days = days;
	}
	/**
	 * @return Returns the hours.
	 */
	public int getHours() {
		return hours;
	}
	/**
	 * @param hours The hours to set.
	 */
	public void setHours(int hours) {
		this.hours = hours;
	}
	/**
	 * @return Returns the minutes.
	 */
	public int getMinutes() {
		return minutes;
	}
	/**
	 * @param minutes The minutes to set.
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	/**
	 * @return Returns the months.
	 */
	public int getMonths() {
		return months;
	}
	/**
	 * @param months The months to set.
	 */
	public void setMonths(int months) {
		this.months = months;
	}
	/**
	 * @return Returns the seconds.
	 */
	public int getSeconds() {
		return seconds;
	}
	/**
	 * @param seconds The seconds to set.
	 */
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	/**
	 * @return Returns the years.
	 */
	public int getYears() {
		return years;
	}
	/**
	 * @param years The years to set.
	 */
	public void setYears(int years) {
		
		this.years = years;
	}
	
	/**
	 * Determines the time difference between the given dates.
	 *  
	 * @return the calculated age
	 */
	public static PhonDuration getDuration(Calendar before, Calendar after) {
		PhonDuration age = new PhonDuration();
		
		if(before == null) {
			return null;
		}
		
		if(after == null) {
			return null;
		}
		
		// calculate years
		int day = before.get(Calendar.DATE);
		int month = before.get(Calendar.MONTH)+1; // zero-based
		int year = before.get(Calendar.YEAR);
		
		int thisDay = after.get(Calendar.DATE);
		int thisMonth = after.get(Calendar.MONTH)+1; // zero-based
		int thisYear = after.get(Calendar.YEAR);
		
		int years, months, days = 0;
		
		years = thisYear - year;
		if(thisMonth >= month) {
			months = thisMonth - month;
		} else {
			years--;
			months = thisMonth + 12 - month;
		}
		
		if(thisDay >= day) {
			days = thisDay - day;
		} else {
			months--;
			days = thisDay + 30 - day;
		}
		
		if(months < 0) {
			years--;
			months = months+12;
		}
		
		age.setYears(years);
		age.setMonths(months);
		age.setDays(days);
				
		return age;
	}
	
	public static Calendar getBeforeDate(Calendar afterDate, PhonDuration duration) {
		Calendar retVal = Calendar.getInstance();
		
		retVal.set(
				afterDate.get(Calendar.YEAR) - duration.years, // years
				afterDate.get(Calendar.MONTH) - duration.months, // months
				afterDate.get(Calendar.DATE) - duration.days, // days
				0, 0, 0);   // hours, minutes, seconds
		
		return retVal;
	}
	
	public static Calendar getAfterDate(Calendar beforeDate, PhonDuration duration) {
		Calendar retVal = Calendar.getInstance();
		
		retVal.set(
				beforeDate.get(Calendar.YEAR) + duration.years, // years
				beforeDate.get(Calendar.MONTH) + duration.months, // months
				beforeDate.get(Calendar.DATE) + duration.days, // days
				0, 0, 0);   // hours, minutes, seconds
		
		return retVal;
	}
	
	public boolean valid() {
		return (
				years >= 0 &&
				months >= 0 &&
				days >= 0 &&
				hours >= 0 &&
				minutes >= 0 &&
				seconds >=  0);
	}
	/**
	 * @return Returns the negative.
	 */
	public boolean isNegative() {
		return negative;
	}
	/**
	 * @param negative The negative to set.
	 */
	public void setNegative(boolean negative) {
		this.negative = negative;
	}
	
	@Override
	public String toString() {
		String retVal = new String();
		
		retVal += (negative ? "negative\n" : "");
		retVal += (years == 0 ? "" : years + " years\n");
		retVal += (months == 0 ? "" : months + " months\n");
		retVal += (days == 0 ? "" : days + " days\n");
		retVal += (hours == 0 ? "" : hours + " hours\n");
		retVal += (minutes == 0 ? "" : minutes + " minutes\n");
		retVal += (seconds == 0 ? "" : seconds + " seconds\n");
		
		return retVal;
	}

	public boolean isZero() {
		boolean retVal = 
			(years == 0 
			 && months == 0
			 && days == 0
			 && hours == 0
			 && minutes == 0
			 && seconds == 0);
		return retVal;
	}

}
