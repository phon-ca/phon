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

import java.io.*;
import java.util.*;

import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.report.io.ReportDesign;

/**
 * An interface for building reports based on a report design
 * and given search data.
 * 
 * Each type of report output (CSV, excel, etc.) requires an 
 * implementation of this interface.
 * 
 * @deprecated
 */
@Deprecated
public abstract class ReportBuilder {
	
	/**
	 * Report properties.  Sub-classes can define their own set of
	 * configurable properties.
	 */
	private Map<String, Object> properties = 
		Collections.synchronizedMap(new TreeMap<String, Object>());
	
	/*
	 * Property methods
	 */
	public void putProperty(String key, Object val) {
		properties.put(key, val);
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	/**
	 * Returns a list of property names
	 * used for configuring the builder.
	 * The properties are presented as options
	 * in the report wizard.
	 * 
	 * @return propNames
	 */
	public String[] getPropertyNames() {
		return new String[] { TEMP_PROJECT, CANCEL_BUILD };
	}
	
	/**
	 * Returns the value type for the given
	 * property name.
	 * 
	 * NOTE: Only Boolean and String values are
	 * supported by the report wizard.
	 * 
	 * @param propName
	 * @return the class of the property value
	 */
	public Class<?> getPropertyClass(String propName) {
		Class<?> retVal = Object.class;
		
		if(TEMP_PROJECT.equals(propName)) {
			retVal = Project.class;
		} else if(CANCEL_BUILD.equals(propName)) {
			retVal = Boolean.class;
		}
		
		return retVal;
	}
	
	/**
	 * Returns the UI message associated with
	 * a property name returned by getPropertyNames()
	 * 
	 * @param propName
	 * @return the UI message
	 */
	public String getPropertyMessage(String propName) {
		return "";
	}
	
	/**
	 * Get the default value for the given property name.
	 * 
	 * @param propName
	 * @return the default property value.  This MUST
	 * be of the type returned by  getPropertyClass(propName)
	 */
	public Object getPropertyDefault(String propName) {
		Object retVal = null;
		
		if(CANCEL_BUILD.equals(propName)) {
			retVal = Boolean.FALSE;
		}
		
		return retVal;
	}
	
	/**
	 * Temporary project.  This is the project the report builder should
	 * read the result set files from.  If <code>null</code>, result sets will be read
	 * from default project. Type: IPhonProject, default: <code>null</code>
	 */
	public final static String TEMP_PROJECT = "temp_project";
	
	/**
	 * Property for halting the report building process. Useful
	 * if using this class in a thread with the ability to cancel the
	 * operation. (default:<code>false</code>)
	 */
	public final static String CANCEL_BUILD = "cancel_build";
	
	/**
	 * Get the mimetype created by this report builder.
	 * 
	 * @return report mimetype
	 */
	public abstract String getMimetype();
	
	/**
	 * Get the default extension for the report
	 * 
	 * @return default extension
	 */
	public abstract String getFileExtension();
	
	/**
	 * Get the display name for the builder.
	 * 
	 * @return display name
	 */
	public abstract String getDisplayName();
	
	/**
	 * Has this build been canceled by the user?
	 * This method should be called periodically during
	 * the build method.  If the build has been cancelled, the
	 * build method should throw a new ReportBuilderException.
	 */
	public boolean isBuildCanceled() {
		Object v = getProperty(CANCEL_BUILD);
		boolean retVal = false;
		if(v != null && (v instanceof Boolean))
			retVal = (Boolean)v;
		return retVal;
	}
	
	/**
	 * Build the report.
	 * 
	 * @param design the report design
	 * @param project the phon project
	 * @param query the query data
	 * @param resultSets the selected searches to report on
	 * @param file the output file
	 * 
	 * @throws ReportBuilderException
	 */
	public void buildReport(
			ReportDesign design,
			Project project,
			Query q,
			ResultSet[] resultSets,
			File file) throws ReportBuilderException {
		OutputStream out;
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new ReportBuilderException(e);
		}
		buildReport(design, project, q, resultSets, out);
	}
	
	/**
	 * Build the report.
	 * 
	 * @param design the report design
	 * @param project the phon project
	 * @param query the query data
	 * @param resultSets the selected searches to report on
	 * @param file the output file
	 * 
	 * @throws ReportBuilderException
	 */
	public abstract void buildReport(
			ReportDesign design,
			Project project,
			Query q,
			ResultSet[] resultSets,
			OutputStream file) throws ReportBuilderException;

}
