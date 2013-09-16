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
package ca.phon.query.report;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBElement;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.io.ObjectFactory;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.report.io.ResultListing;
import ca.phon.query.report.io.Section;

/**
 * Handles storage of result listing formats
 * for result sets.
 */
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
				project.getLocation();
		final String queryid = query.getUUID().toString();
		final String rsName = resultSet.getSessionPath();
		
		final String sc = File.separator;
		
		return projectLocation + sc + "__res" + sc + RESOURCE_FOLDER + sc 
				+ queryid + sc + rsName + ".xml";
	}
	
}
