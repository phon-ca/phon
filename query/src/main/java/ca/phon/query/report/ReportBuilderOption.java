/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.query.report;

/**
 * Represents a single option for a report builder.
 * Each option has a type, message, name, and property.
 * 
 * @deprecated
 */
@Deprecated
public interface ReportBuilderOption {
	
	/**
	 * Type of possible report options
	 */
	public static enum OptionType {
		BOOLEAN,
		STRING,
		NUMBER
	};
	
	/**
	 * Get the type of this option.
	 * 
	 */
	public OptionType getType();
	
	/**
	 * Get the name of this option
	 */
	public String getName();
	
	/**
	 * Get the UI message to be displayed
	 * with the option
	 */
	public String getMessage();
	
	/**
	 * The name of the property to store
	 * the value of this option
	 */
	public String getPropertyName();

}
