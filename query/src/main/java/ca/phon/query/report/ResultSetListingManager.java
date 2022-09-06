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

import java.io.*;

import jakarta.xml.bind.*;

import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.query.report.io.*;

/**
 * Handles storage of result listing formats
 * for result sets.
 * 
 * @deprecated
 */
@Deprecated
public class ResultSetListingManager {
	
	/**
	 * Folder for storage of result listing files.
	 * This folder lives inside the '__res' folder
	 * of the project.
	 */
	private final String RESOURCE_FOLDER = "result_tables";
	
	/**
	 * Section name of result listing
	 */
	private final String LISTING_NAME = "resultset_editor";

	/**
	 * Get the result listing (if any)
	 * for the given result set.
	 * 
	 * @param project
	 * @param query
	 * @param resultset
	 * 
	 * @return the result list format for the 
	 *  result set or <code>null</code> if not found.
	 *  
	 * @throws IOException
	 */
	public ResultListing getResultListing(Project project, Query query, ResultSet resultSet)
		throws IOException {
		final String designPath = getPathForResultListing(project, query, resultSet);
		ResultListing retVal = null;
		final ReportDesign design = ReportIO.readDesign(designPath);
		
		// design should consist of a single result listing section
		for(JAXBElement<? extends Section> reportSectionEle:design.getReportSection()) {
			final Section val = reportSectionEle.getValue();
			// make sure it's a report listing section
			if(val.getName().equals(LISTING_NAME)) {
				if(val instanceof ResultListing) {
					retVal = ResultListing.class.cast(val);
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Save result listing for a given result set
	 * 
	 * @param project
	 * @param query
	 * @param resultset
	 * @param listing
	 * 
	 * @throws IOException
	 */
	public void saveResultListing(Project project, Query query, ResultSet resultSet, ResultListing listing) 
		throws IOException {
		final String designPath = getPathForResultListing(project, query, resultSet);
		final ObjectFactory factory = new ObjectFactory();
		final ReportDesign design = factory.createReportDesign();
		
		listing.setName(LISTING_NAME);
		final JAXBElement<Section> listingEle = factory.createReportSection(listing);
		design.getReportSection().add(listingEle);
		
		// attempt to create the parent folder if it does not exist
		final File parentFolder = (new File(designPath)).getParentFile();
		if(!parentFolder.exists()) {
			parentFolder.mkdirs();
		}
		
		ReportIO.writeDesign(design, designPath);
	}
	
	
	/**
	 * Get path for the result listing xml file.
	 * 
	 * @param project
	 * @param query
	 * @param resultset
	 * 
	 * @return the path to the result listing
	 */
	public String getPathForResultListing(Project project, Query query, ResultSet resultSet) {
		final String projectLocation = 
				project.getResourceLocation();
		final String queryid = query.getUUID().toString();
		final String rsName = resultSet.getSessionPath();
		
		final String sc = File.separator;
		
		return projectLocation + sc + RESOURCE_FOLDER + sc 
				+ queryid + sc + rsName + ".xml";
	}
	
}
