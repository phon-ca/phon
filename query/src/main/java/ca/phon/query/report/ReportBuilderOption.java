/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
