package ca.phon.query.pql;

import ca.phon.project.Project;
import ca.phon.session.filter.RecordFilter;
import ca.phon.util.Range;

import java.util.*;

public class PQLSessionCollector extends PQLBaseVisitor<Void> {

	private Project project;

	private Map<String, RecordFilter> recordFilterMap;

	private List<Range> recordRanges = new ArrayList<>();

	public PQLSessionCollector() {
		this(null);
	}

	public PQLSessionCollector(Project project) {
		this.project = project;
		this.recordFilterMap = new LinkedHashMap<>();
	}

	@Override
	public Void visitSession_name(PQL.Session_nameContext ctx) {
		recordRanges.clear();
		return super.visitSession_name(ctx);
	}



	@Override
	public Void visitRecordList(PQL.RecordListContext ctx) {
		System.out.println(ctx.record_or_range(0));
		return super.visitRecordList(ctx);
	}

	@Override
	public Void visitRecordNumber(PQL.RecordNumberContext ctx) {
		int recordNum = Integer.parseInt(ctx.integer().getText());
		if(recordNum > 0)
			recordRanges.add(new Range(recordNum - 1, recordNum, true));
		return super.visitRecordNumber(ctx);
	}

	@Override
	public Void visitRecordRange(PQL.RecordRangeContext ctx) {
		int startNum = Integer.parseInt(ctx.integer(0).getText());
		int endNum = Integer.parseInt(ctx.integer(1).getText());

		if(startNum > 0 && endNum >= startNum) {
			Range recordRange = new Range(startNum - 1, endNum, true);
			recordRanges.add(recordRange);
		}
		return super.visitRecordRange(ctx);
	}

}
