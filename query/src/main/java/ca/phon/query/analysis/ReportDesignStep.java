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
package ca.phon.query.analysis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.functor.AnalysisStep;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.ReportBuilder;
import ca.phon.query.report.ReportBuilderException;
import ca.phon.query.report.ReportBuilderFactory;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.session.SessionPath;

public class ReportDesignStep implements AnalysisStep<String, QueryAnalysisResult> {

	private static final Logger LOGGER = Logger
			.getLogger(ReportDesignStep.class.getName());
	
	private final ReportDesign reportDesign;
	
	public ReportDesignStep(ReportDesign reportDesign) {
		this.reportDesign = reportDesign;
	}
	
	@Override
	public String op(QueryAnalysisResult obj) {
		final ReportBuilder builder = ReportBuilderFactory.getInstance().getBuilder("CSV");
		
		final QueryFactory qf = QueryManager.getSharedInstance().createQueryFactory();
		final Query q = qf.createQuery();
		q.setDate(LocalDateTime.now());
		q.setName("internal");
		
		final ResultSet[] resultSets = new ResultSet[obj.getQueryResults().keySet().size()];
		int idx = 0;
		for(SessionPath sp:obj.getQueryResults().keySet()) {
			resultSets[idx++] = obj.getQueryResults().get(sp);
		}
		
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		String retVal = "";
		try {
			builder.buildReport(
					reportDesign,
					obj.getInput().getProject(), 
					q, 
					resultSets, bout);
			
			retVal =  bout.toString("UTF-8");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (ReportBuilderException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return retVal;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public Class<QueryAnalysisResult> getParameterType() {
		return QueryAnalysisResult.class;
	}

}
