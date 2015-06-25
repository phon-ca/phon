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

import ca.phon.ui.CommonModuleFrame;

public class WordMatchAction extends AssessmentWizardAction {

	private static final long serialVersionUID = -5997676379072566324L;

	private static final String TXT = "Word Match...";
	
	private static final String DESC = "Whole word comparasons of IPA Actual vs. IPA Target forms";
	
	private static final String WORDMATCH_SCRIPT = 
			"ca/phon/query/script/Word Match.js";
	
	private static final String REPORT_SCRIPT =
			"ca/phon/query/analysis/simple_report.js";
	
	public WordMatchAction(CommonModuleFrame projectFrame) {
		super(projectFrame, WORDMATCH_SCRIPT, REPORT_SCRIPT);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

}
