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

import ca.phon.query.report.io.AggregrateInventory;

public class AggregatedInventorySectionPanel extends InventorySectionPanel {
	
	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><body>" +
		"<i>Aggregated Inventory</i>" +
		"<p>Outputs a table of result values and the number of times they appear in each selected search.</p>" +
		"</body></html>";
	
	public AggregatedInventorySectionPanel(AggregrateInventory section) {
		super(section);
		
		init();
	}
	
	private void init() {
		super.setInformationText(
				getClass().getName()+".info", INFO_TEXT);
	}

}
