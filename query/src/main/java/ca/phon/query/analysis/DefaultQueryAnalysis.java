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
package ca.phon.query.analysis;

import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScript;

public class DefaultQueryAnalysis extends QueryAnalysis {

	public DefaultQueryAnalysis(QueryScript queryScript, ReportDesign reportDesign) {
		super(new QueryStep(queryScript), new ReportDesignStep(reportDesign));
	}
	
	public DefaultQueryAnalysis(QueryScript queryScript, PhonScript reportScript) {
		super(new QueryStep(queryScript), new ScriptReportStep(reportScript));
	}

	public DefaultQueryAnalysis(QueryScript queryScript, String reportScript) {
		super(new QueryStep(queryScript), new ScriptReportStep(reportScript));
	}
	
}
