/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.query.report;

import ca.phon.query.report.io.*;

/**
 * Provide a utility method for generating 
 * report section panels.
 *
 */
public class SectionPanelFactory {
	
	public SectionPanelFactory() {
		super();
	}
	
	public SectionPanel<? extends Section> createSectionPanel(Section section) {
		SectionPanel<? extends Section> retVal = new SectionPanel<Section>(section);
		
		if(section instanceof ReportDesign) {
			ReportDesign design = (ReportDesign)section;
			retVal = new ReportSectionPanel(design);
		} else if(section instanceof SummarySection) {
			SummarySection sumSect = (SummarySection)section;
			retVal = new SummarySectionPanel(sumSect);
		} else if(section instanceof CommentSection) {
			CommentSection cSect = (CommentSection)section;
			retVal = new CommentSectionPanel(cSect);
		} else if(section instanceof Group) {
			Group gSect = (Group)section;
			retVal = new GroupSectionPanel(gSect); 
		} else if(section instanceof ParamSection) {
			ParamSection pSect = (ParamSection)section;
			retVal = new ParamSectionPanel(pSect);
		} else if(section instanceof InventorySection) {
			InventorySection iSect = (InventorySection)section;
			
			if(iSect instanceof AggregrateInventory) {
				AggregrateInventory agInfo = (AggregrateInventory)section;
				retVal = new AggregatedInventorySectionPanel(agInfo);
			} else {
				retVal = new InventorySectionPanel(iSect);
			}
		} else if(section instanceof ResultListing) {
			ResultListing lSect = (ResultListing)section;
			retVal = new ResultListingSectionPanel(lSect);
		}
		
		return retVal;
	}

}
