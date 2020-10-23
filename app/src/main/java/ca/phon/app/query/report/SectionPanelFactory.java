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
