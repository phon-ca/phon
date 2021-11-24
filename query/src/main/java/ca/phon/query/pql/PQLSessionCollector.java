package ca.phon.query.pql;

import ca.phon.project.Project;
import ca.phon.session.filter.RecordFilter;
import org.antlr.v4.runtime.tree.*;

import java.util.*;

public class PQLSessionCollector extends PQLBaseVisitor<Void> {

	private Project project;

	private Map<String, RecordFilter> recordFilterMap;

	public PQLSessionCollector() {
		this(null);
	}

	public PQLSessionCollector(Project project) {
		this.project = project;
		this.recordFilterMap = new LinkedHashMap<>();
	}

	@Override
	public Void visitSessionList(PQL.SessionListContext ctx) {
		return super.visitSessionList(ctx);
	}

	@Override
	public Void visitSession_name(PQL.Session_nameContext ctx) {
		return super.visitSession_name(ctx);
	}

}
