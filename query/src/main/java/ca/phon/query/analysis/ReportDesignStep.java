package ca.phon.query.analysis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

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
		q.setDate(DateTime.now());
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
