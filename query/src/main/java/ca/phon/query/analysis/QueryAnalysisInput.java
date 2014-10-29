package ca.phon.query.analysis;

import ca.phon.session.SessionLocation;
import ca.phon.session.SessionPath;

import java.util.List;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.script.QueryScript;

public class QueryAnalysisInput {
	
	private Project project;

	private List<SessionPath> sessions;

	private QueryScript queryScript;
	
	private ReportDesign reportDesign;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<SessionPath> getSessions() {
		return sessions;
	}

	public void setSessions(List<SessionPath> sessions) {
		this.sessions = sessions;
	}

	public QueryScript getQueryScript() {
		return queryScript;
	}

	public void setQuery(QueryScript queryScript) {
		this.queryScript = queryScript;
	}

	public ReportDesign getReportDesign() {
		return reportDesign;
	}

	public void setReportDesign(ReportDesign reportDesign) {
		this.reportDesign = reportDesign;
	}
	
}
