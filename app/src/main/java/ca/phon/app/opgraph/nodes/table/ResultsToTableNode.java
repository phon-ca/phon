/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.text.ParseException;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTitledSeparator;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.ReportHelper;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultValue;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.session.Group;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

@OpNodeInfo(name="Results To Table",
	description="Convert a set of result to a table",
	category="Table")
public class ResultsToTableNode extends OpNode implements NodeSettings {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ResultsToTableNode.class.getName());

	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);

	private final InputField resultSetsInput = new InputField("results", "Query results", false, true, ResultSet[].class);

	private final OutputField tableOutput = new OutputField("table", "Result sets as table", true, TableDataSource.class);

	/* Settings */
	private boolean includeSessionInfo;

	private boolean includeSpeakerInfo;

	private boolean includeTierInfo;

	/** Include metadata columns */
	private boolean includeMetadata;

	/* UI */
	private JPanel settingsPanel;
	private JCheckBox includeSessionInfoBox;
	private JCheckBox includeSpeakerInfoBox;
	private JCheckBox includeTierInfoBox;
	private JCheckBox includeMetadataBox;

	public ResultsToTableNode() {
		super();

		putField(projectInput);
		putField(resultSetsInput);
		putField(tableOutput);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsInput);
		final Project project = (Project)context.get(projectInput);

		context.put(tableOutput, resultsToTable(project, resultSets));
	}

	@SuppressWarnings("unchecked")
	private TableDataSource resultsToTable(Project project, ResultSet[] results) {
		final DefaultTableDataSource retVal = new DefaultTableDataSource();

		List<String> columnNames = new ArrayList<>();

		if(isIncludeSessionInfo()) {
			columnNames.add("Session");
			columnNames.add("Date");
		}

		if(isIncludeSpeakerInfo()) {
			columnNames.add("Speaker");
			columnNames.add("Age");
		}

		if(isIncludeTierInfo()) {
			columnNames.add("Record #");
			columnNames.add("Group #");
			columnNames.add("Tier");
			columnNames.add("Range");
		}

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
					tierNames.add(rv.getName());
				}
				columnNames.addAll(tierNames);
			});

		Set<String> metadataKeys = new LinkedHashSet<>();
		if(isIncludeMetadata()) {
			for(ResultSet rs:results) {
				metadataKeys.addAll(Arrays.asList(rs.getMetadataKeys()));
			}
			columnNames.addAll(metadataKeys);
		}

		for(ResultSet rs:results) {
			try {
				final Session session = project.openSession(rs.getCorpus(), rs.getSession());
				for(Result result:rs) {
					List<Object> rowData = new ArrayList<>();
					final Record record = session.getRecord(result.getRecordIndex());

					if(isIncludeSessionInfo()) {
						rowData.add(new SessionPath(rs.getCorpus(), rs.getSession()));
						rowData.add(session.getDate());
					}

					if(isIncludeSpeakerInfo()) {
						final Participant speaker = record.getSpeaker();
						if(speaker != null) {
							rowData.add(speaker);

							final Period age = speaker.getAge(session.getDate());
							if(age != null) {
								rowData.add(age);
							} else {
								rowData.add("");
							}
						} else {
							rowData.add("");
							rowData.add("");
						}

					}

					if(isIncludeTierInfo()) {
						rowData.add(result.getRecordIndex()+1);
						rowData.add(result.getResultValue(0).getGroupIndex()+1);
						rowData.add(ReportHelper.createReportString(tierNames.toArray(new String[0]), result.getSchema()));
						final String[] rvVals = new String[result.getNumberOfResultValues()];
						for(int i = 0; i < result.getNumberOfResultValues(); i++) {
							final ResultValue rv = result.getResultValue(i);
							rvVals[i] = rv.getRange().toString();
						}
						rowData.add(ReportHelper.createReportString(rvVals, result.getSchema()));
					}

					rowData.add(result);

					// add result objects from record
					for(String tierName:tierNames) {
						final List<ResultValue> resultValues = StreamSupport.stream(result.spliterator(), false)
							.filter( (rv) -> rv.getName().equals(tierName) )
							.collect(Collectors.toList());

						Object resultVal = new String();
						Formatter<Object> formatter = null;
						StringBuffer buffer = new StringBuffer();

						for(ResultValue rv:resultValues) {
							final Group group = record.getGroup(rv.getGroupIndex());
							Object tierValue = group.getTier(rv.getTierName());
							if(tierValue == null) tierValue = "";

							// attempt to find a formatter
							if(formatter == null) {
								formatter = (Formatter<Object>)FormatterFactory.createFormatter(tierValue.getClass());
							}
							final String tierTxt =
									(formatter != null ? formatter.format(tierValue) : tierValue.toString());

							String resultTxt =
									(rv.getRange().getFirst() >= 0 && rv.getRange().getLast() >= rv.getRange().getFirst() ?
									tierTxt.substring(
											Math.max(0, rv.getRange().getFirst()),
											Math.max(0, Math.min(rv.getRange().getLast(), tierTxt.length()))) : "");

							if(result.getSchema().equals("DETECTOR") && resultTxt.length() == 0) {
								resultTxt = "\u2205";
							}

							if(buffer.length() > 0) buffer.append("..");
							buffer.append(resultTxt);
						}
						resultVal = buffer.toString();
						if(formatter != null) {
							try {
								resultVal = formatter.parse(buffer.toString());
							} catch (ParseException e) {
								LOGGER.info( e.getLocalizedMessage(), e);
							}
						}
						rowData.add(resultVal);
					}

					if(isIncludeMetadata()) {
						for(String metakey:metadataKeys) {
							rowData.add(result.getMetadata().get(metakey));
						}
					}

					retVal.addRow(rowData.toArray());
				}
			} catch (IOException e) {
				throw new ProcessingException(null, e);
			}
		}

		for(int i = 0; i < columnNames.size(); i++) {
			retVal.setColumnTitle(i, columnNames.get(i));
		}

		return retVal;
	}

	public boolean isIncludeSessionInfo() {
		return (includeSessionInfoBox != null ? includeSessionInfoBox.isSelected() : this.includeSessionInfo);
	}

	public void setIncludeSessionInfo(boolean includeSessionInfo) {
		this.includeSessionInfo = includeSessionInfo;
		if(this.includeSessionInfoBox != null)
			this.includeSessionInfoBox.setSelected(includeSessionInfo);
	}

	public boolean isIncludeSpeakerInfo() {
		return (includeSpeakerInfoBox != null ? includeSpeakerInfoBox.isSelected() : this.includeSpeakerInfo);
	}

	public void setIncludeSpeakerInfo(boolean includeSpeakerInfo) {
		this.includeSpeakerInfo = includeSpeakerInfo;
		if(this.includeSpeakerInfoBox != null)
			this.includeSpeakerInfoBox.setSelected(includeSpeakerInfo);
	}

	public boolean isIncludeTierInfo() {
		return (includeTierInfoBox != null ? includeTierInfoBox.isSelected() : this.includeTierInfo);
	}

	public void setIncludeTierInfo(boolean includeTierInfo) {
		this.includeTierInfo = includeTierInfo;
		if(this.includeTierInfoBox != null)
			this.includeTierInfoBox.setSelected(includeTierInfo);
	}

	public boolean isIncludeMetadata() {
		return (includeMetadataBox != null ? includeMetadataBox.isSelected() : this.includeMetadata);
	}

	public void setIncludeMetadata(boolean includeMetadata) {
		this.includeMetadata = includeMetadata;
		if(this.includeMetadataBox != null)
			this.includeMetadataBox.setSelected(includeMetadata);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}

	private JPanel createSettingsPanel() {
		JPanel retVal = new JPanel();

		includeSessionInfoBox = new JCheckBox("Include session name and date", true);
		includeSpeakerInfoBox = new JCheckBox("Include speaker name and age", true);
		includeTierInfoBox = new JCheckBox("Include record number, tier, group and text range", true);
		includeMetadataBox = new JCheckBox("Include result metadata columns", true);

		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 5, 2);

		retVal.setLayout(layout);

		retVal.add(new JXTitledSeparator("Column options"), gbc);
		++gbc.gridy;
		retVal.add(includeSessionInfoBox, gbc);
		++gbc.gridy;
		retVal.add(includeSpeakerInfoBox, gbc);
		++gbc.gridy;
		retVal.add(includeTierInfoBox, gbc);
		++gbc.gridy;
		retVal.add(includeMetadataBox, gbc);
		++gbc.gridy;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		retVal.add(Box.createVerticalGlue(), gbc);

		return retVal;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();

		retVal.put("includeSessionInfo", isIncludeSessionInfo());
		retVal.put("includeSpeakerInfo", isIncludeSpeakerInfo());
		retVal.put("includeTierInfo", isIncludeTierInfo());
		retVal.put("includeMetadata", isIncludeSessionInfo());

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIncludeSessionInfo(
				Boolean.parseBoolean(properties.getProperty("includeSessionInfo", "true")));
		setIncludeSpeakerInfo(
				Boolean.parseBoolean(properties.getProperty("includeSpeakerInfo", "true")));
		setIncludeTierInfo(
				Boolean.parseBoolean(properties.getProperty("includeTierInfo", "true")));
		setIncludeMetadata(
				Boolean.parseBoolean(properties.getProperty("includeMetadata", "true")));
	}

}
