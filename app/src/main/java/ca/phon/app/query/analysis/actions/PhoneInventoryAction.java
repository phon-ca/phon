/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.query.analysis.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
import ca.phon.ui.CommonModuleFrame;

public class PhoneInventoryAction extends AssessmentAction {

	private static final long serialVersionUID = 160534063316471612L;

	private final static String PHONES_SCRIPT = "ca/phon/query/script/Phones.js";
	
	private final static String REPORT_SCRIPT = "ca/phon/query/analysis/phone_inventory.js";
	
	private final static String TXT = "Phone Inventory...";
	
	private final static Logger LOGGER = Logger.getLogger(PhoneInventoryAction.class.getName());
	
	public PhoneInventoryAction(CommonModuleFrame projectFrame) {
		super(projectFrame, PHONES_SCRIPT, REPORT_SCRIPT);
		
		putValue(NAME, TXT);
		
		setupScriptParams();
	}
	
	private void setupScriptParams() {
		final QueryScript script = super.getQueryScript();
		try {
			final ScriptParameters params 
				= script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
			params.setParamValue("filters.primary.filter", "\\w");
			params.setParamValue("includeAligned", Boolean.FALSE);
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
}
