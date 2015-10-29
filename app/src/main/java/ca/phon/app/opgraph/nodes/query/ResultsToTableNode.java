package ca.phon.app.opgraph.nodes.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.project.Project;
import ca.phon.query.db.ReportHelper;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultValue;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.session.Tier;

@OpNodeInfo(name="Results To Table",
	description="Convert a set of result to a table",
	category="Report")
public class ResultsToTableNode extends OpNode {
	
	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);
	
	private final InputField resultSetsInput = new InputField("result set", "Query results", false, true, ResultSet[].class);
	
	private final OutputField tableOutput = new OutputField("table", "Result sets as table", true, TableDataSource.class);

	public ResultsToTableNode() {
		super();
		
		putField(projectInput);
		putField(resultSetsInput);
		putField(tableOutput);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsInput);
		final Project project = (Project)context.get(projectInput);
		
		context.put(tableOutput, resultsToTable(project, resultSets));
	}
	
	private TableDataSource resultsToTable(Project project, ResultSet[] results) {
		final DefaultTableDataSource retVal = new DefaultTableDataSource();
		
		List<String> columnNames = new ArrayList<>();
		columnNames.add("Session");
		columnNames.add("Record #");
		columnNames.add("Group #");
		columnNames.add("Result");
		
		// collect all result value tier names
		final Set<String> tierNames = new LinkedHashSet<>();
		// assuming all results come from the same query, the tiers should be the
		// same in every result value
		Arrays.asList(results).stream()
			.filter((rs) -> rs.numberOfResults(true) > 0)
			.findFirst()
			.ifPresent( firstNonEmptyResultSet -> {
				final Result firstResult = firstNonEmptyResultSet.getResult(0);
				for(ResultValue rv:firstResult) {
					tierNames.add(rv.getTierName());
				}
				columnNames.addAll(tierNames);
			});
		Set<String> metadataKeys = new LinkedHashSet<>();
		for(ResultSet rs:results) {
			metadataKeys.addAll(Arrays.asList(rs.getMetadataKeys()));
		}
		columnNames.addAll(metadataKeys);
		
		for(ResultSet rs:results) {
			try {
				final Session session = project.openSession(rs.getCorpus(), rs.getSession());
				for(Result result:rs) {
					List<Object> rowData = new ArrayList<>();
					rowData.add(new SessionPath(rs.getCorpus(), rs.getSession()));
					rowData.add(result.getRecordIndex()+1);
					rowData.add(result.getResultValue(0).getGroupIndex()+1);
					rowData.add(ReportHelper.createResultString(result));
					
					// add result objects from record
					final Record record = session.getRecord(result.getRecordIndex());
					for(ResultValue rv:result) {
						final Tier<?> tier = record.getTier(rv.getTierName());
						final Object tierValue = tier.getGroup(rv.getGroupIndex());
						
						// attempt to find a formatter
						@SuppressWarnings("unchecked")
						final Formatter<Object> formatter =
								(Formatter<Object>)FormatterFactory.createFormatter(tierValue.getClass());
						final String tierTxt = 
								(formatter != null ? formatter.format(tierValue) : tierValue.toString());
						
						final String resultTxt = 
								(rv.getRange().getFirst() >= 0 && rv.getRange().getLast() >= rv.getRange().getFirst() ?
								tierTxt.substring(rv.getRange().getFirst(), rv.getRange().getLast()) : "");
						Object resultVal = resultTxt;
						if(formatter != null) {
							try {
								resultVal = formatter.parse(resultTxt);
							} catch (ParseException e) {
								// TODO
							}
						}
						rowData.add(resultVal);
					}
					
					for(String metakey:metadataKeys) {
						rowData.add(result.getMetadata().get(metakey));
					}
					
					retVal.addRow(rowData.toArray());
				}
			} catch (IOException e) {
				throw new ProcessingException(null, e);
			}
		}
		
		for(int i = 0; i < columnNames.size(); i++) {
			retVal.setColumntTitle(i, columnNames.get(i));
		}
		
		return retVal;
	}
	
}
