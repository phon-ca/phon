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

public class PMLUAction extends AssessmentAction {

	private static final long serialVersionUID = -2704840851738827770L;
	
	private final static String QUERY_SCRIPT = "ca/phon/query/script/PMLU.js";
	
	private final static String REPORT_SCRIPT = "ca/phon/query/analysis/pmlu.js";
	
	private final static String TXT = "PMLU...";
	
	private final static String DESC = "Phonological Mean Length of Utterance";
	
	public PMLUAction(CommonModuleFrame projectFrame) {
		super(projectFrame, QUERY_SCRIPT, REPORT_SCRIPT);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
}
