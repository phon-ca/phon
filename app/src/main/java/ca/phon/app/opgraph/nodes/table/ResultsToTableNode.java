/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.app.opgraph.GlobalParameter;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.*;
import ca.phon.ipa.*;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.report.datasource.*;
import ca.phon.query.script.params.*;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
import ca.phon.session.Record;
import ca.phon.session.*;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.time.Period;
import java.util.List;
import java.util.*;
import java.util.stream.*;

@OpNodeInfo(name="Results To Table",
	description="Convert a set of result to a table",
	category="Table")
public class ResultsToTableNode extends OpNode implements NodeSettings {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ResultsToTableNode.class.getName());

	// required inputs
	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);

	private final InputField resultSetsInput = new InputField("results", "Query results", false, true, ResultSet[].class);

	// optional inputs
	private final InputField includeSessionInfoInput = new InputField("includeSessionInfo", "Include session info columns: Name, Date", true, true, Boolean.class);
	
	private final InputField includeSpeakerInfoInput = new InputField("includeSpeakerInfo", "Include speaker info columns: Speaker, Age",  true, true, Boolean.class);
	
	private final InputField includeTierInfoInput = new InputField("includeTierInfo", "Include tier info columns: Record #, Group #, Tier, Range",  true, true, Boolean.class);
	
	private final InputField includeMetadataInput = new InputField("includeMetadata", "Include metadata columns such as aligned group and word tiers",  true, true, Boolean.class);
	
	private final InputField ignoreDiacriticsInput = new InputField("ignoreDiacritics", "Ignore diacritics",  true, true, Boolean.class);
	
	private final InputField onlyOrExceptInput = new InputField("onlyOrExcept", "If true (only) selected diacritics will be ignored, if false operation will be 'except'",  true, true, Boolean.class);
	
	private final InputField selectedDiacriticsInput = new InputField("selectedDiacritics", "Selected diacriitcs to ignore",  true, true, Collection.class);
	
	private final OutputField tableOutput = new OutputField("table", "Result sets as table", true, TableDataSource.class);

	/* Settings */
	private boolean includeSessionInfo;

	private boolean includeSpeakerInfo;

	private boolean includeTierInfo;

	/** Include metadata columns */
	private boolean includeMetadata;
	
	/** Diacritic options */
	private boolean isIgnoreDiacritics = false;
	
	private boolean isOnlyOrExcept = false;
	
	private Collection<Diacritic> selectedDiacritics = new ArrayList<>();

	/* UI */
	private JPanel settingsPanel;
	private DiacriticOptionsPanel diacriticOptionsPanel;
	private JCheckBox includeSessionInfoBox;
	private JCheckBox includeSpeakerInfoBox;
	private JCheckBox includeTierInfoBox;
	private JCheckBox includeMetadataBox;

	public ResultsToTableNode() {
		super();

		putField(projectInput);
		putField(resultSetsInput);
		
		putField(includeSessionInfoInput);
		putField(includeSpeakerInfoInput);
		putField(includeTierInfoInput);
		putField(includeMetadataInput);
		putField(ignoreDiacriticsInput);
		putField(onlyOrExceptInput);
		putField(selectedDiacriticsInput);
		
		putField(tableOutput);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsInput);
		final Project project = (Project)context.get(projectInput);
		
		boolean includeSessionInfo = context.get(includeSessionInfoInput) != null 
				?	(boolean)context.get(includeSessionInfoInput)
				:	isIncludeSessionInfo();
				
		boolean includeSpeakerInfo = context.get(includeSpeakerInfoInput) != null
				?	(boolean)context.get(includeSpeakerInfoInput)
				:	isIncludeSpeakerInfo();
				
		boolean includeTierInfo = context.get(includeTierInfoInput) != null
				?	(boolean)context.get(includeTierInfoInput)
				:	isIncludeTierInfo();
				
		boolean includeMetadata = context.get(includeMetadataInput) != null
				?	(boolean)context.get(includeMetadataInput)
				:	isIncludeMetadata();
				
		boolean ignoreDiacritics = context.get(GlobalParameter.IGNORE_DIACRITICS.getParamId()) != null
				?	(boolean)context.get(GlobalParameter.IGNORE_DIACRITICS.getParamId())
				:	context.get(ignoreDiacriticsInput) != null
					?	(boolean)context.get(ignoreDiacriticsInput)
					:	isIgnoreDiacritics();
		
		boolean onlyOrExcept = context.get(GlobalParameter.ONLY_OR_EXCEPT.getParamId()) != null
				?	(boolean)context.get(GlobalParameter.ONLY_OR_EXCEPT.getParamId())
				:	context.get(onlyOrExceptInput) != null
					?	(boolean)context.get(onlyOrExceptInput)
					:	isOnlyOrExcept();
					
		@SuppressWarnings("unchecked")
		Collection<Diacritic> selectedDiacritics = context.get(GlobalParameter.SELECTED_DIACRITICS.getParamId()) != null
				?	(Collection<Diacritic>)context.get(GlobalParameter.SELECTED_DIACRITICS.getParamId())
				:	context.get(selectedDiacriticsInput) != null
					?	(Collection<Diacritic>)context.get(selectedDiacriticsInput)
					:	getSelectedDiacritics();
		
		TableDataSource table = resultsToTable(project, resultSets, includeSessionInfo, includeSpeakerInfo, includeTierInfo, 
				includeMetadata, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
		context.put(tableOutput, table);
	}

	@SuppressWarnings("unchecked")
	private TableDataSource resultsToTable(Project project, ResultSet[] results, boolean includeSessionInfo,
			boolean includeSpeakerInfo, boolean includeTierInfo, boolean includeMetadata, 
			boolean ignoreDiacritics, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
		final DefaultTableDataSource retVal = new DefaultTableDataSource();

		List<String> columnNames = new ArrayList<>();
		
		if(includeSessionInfo) {
			columnNames.add("Session");
			columnNames.add("Date");
		}

		if(includeSpeakerInfo) {
			columnNames.add("Speaker");
			columnNames.add("Age");
		}

		if(includeTierInfo) {
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
		if(includeMetadata) {
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

					if(includeSessionInfo) {
						rowData.add(new SessionPath(rs.getCorpus(), rs.getSession()));
						rowData.add(session.getDate());
					}

					if(includeSpeakerInfo) {
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

					if(includeTierInfo) {
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
							
							boolean defaultOutput = true;
							// attempt to carry over as much data as possible from
							// the IPA transcript. 
							if(tierValue instanceof IPATranscript) {
								IPATranscript origIPA = (IPATranscript)tierValue;
								
								// attempt to use result value to find phone indicies
								IPATranscriptBuilder builder = new IPATranscriptBuilder();
								int startidx = -1;
								int endidx = -1;
								for(int pidx = 0; pidx < origIPA.length(); pidx++) {
									IPAElement ele = origIPA.elementAt(pidx);
									int stringIdx = origIPA.stringIndexOfElement(pidx);
									int endEleIdx = stringIdx + ele.toString().length();
								
									if(rv.getRange().getStart() >= stringIdx) {
										startidx = pidx;
									} 
									if(rv.getRange().getEnd() == endEleIdx) {
										endidx = pidx;
									}
								}
								
								// take only whole elements
								if(startidx >= 0 && endidx >= startidx) {
									IPATranscript subVal = origIPA.subsection(startidx, endidx+1);
									
									if(ignoreDiacritics) {
										if(onlyOrExcept)
											subVal = subVal.stripDiacritics(selectedDiacritics);
										else
											subVal = subVal.stripDiacriticsExcept(selectedDiacritics);
									}
									
									buffer.append(subVal.toString(true));
									defaultOutput = false;
								}
							}

							if(defaultOutput) {
								final String tierTxt =
										(formatter != null ? formatter.format(tierValue) : tierValue.toString());
	
								String resultTxt =
										(rv.getRange().getStart() >= 0 && rv.getRange().getEnd() >= rv.getRange().getFirst() ?
										tierTxt.substring( rv.getRange().getStart(), rv.getRange().getEnd() ) : "");
	
								if(result.getSchema().equals("DETECTOR") && resultTxt.length() == 0) {
									resultTxt = "\u2205";
								}
								
								if(ignoreDiacritics) {
									resultTxt = stripDiacriticsFromText(resultTxt, onlyOrExcept, selectedDiacritics);
								}
								
								if(buffer.length() > 0) buffer.append("..");
								buffer.append(resultTxt);
							}
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

					if(includeMetadata) {
						for(String metakey:metadataKeys) {
							String metaValue = result.getMetadata().get(metakey);
							if(ignoreDiacritics) {
								metaValue = stripDiacriticsFromText(metaValue, onlyOrExcept, selectedDiacritics);
							}
							rowData.add(metaValue);
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
	
	private String stripDiacriticsFromText(String text, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if(keepCharacter(ch, onlyOrExcept, selectedDiacritics)) {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}
	
	private boolean keepCharacter(char ch, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
		FeatureMatrix fm = FeatureMatrix.getInstance();
		Collection<Character> dias = fm.getCharactersWithFeature("diacritic");

		// don't strip ligatures
		dias.remove(Character.valueOf('\u035c'));
		dias.remove(Character.valueOf('\u0361'));
		dias.remove(Character.valueOf('\u0362'));

		if(dias.contains(ch)) {
			boolean inSet = selectedDiacritics.stream().filter( d -> d.getCharacter() == ch ).findFirst().isPresent();
			
			if(onlyOrExcept) {
				return !inSet;
			} else {
				return inSet;
			}
		} else {
			return true;
		}
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
	
	public boolean isIgnoreDiacritics() {
		if(diacriticOptionsPanel != null) {
			return diacriticOptionsPanel.getDiacriticOptions().isIgnoreDiacritics();
		} else {
			return isIgnoreDiacritics;
		}
	}
	
	public boolean isOnlyOrExcept() {
		if(diacriticOptionsPanel != null) {
			boolean retVal = diacriticOptionsPanel.getDiacriticOptions().getSelectionMode() == SelectionMode.ONLY ? 
					true : false;
			return retVal;
		} else {
			return isOnlyOrExcept;
		}
	}
	
	public Collection<Diacritic> getSelectedDiacritics() {
		if(diacriticOptionsPanel != null) {
			return diacriticOptionsPanel.getDiacriticOptions().getSelectedDiacritics();
		} else {
			return selectedDiacritics;
		}
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

		DiacriticOptionsScriptParam param = new DiacriticOptionsScriptParam("", "", isIgnoreDiacritics, selectedDiacritics);
		param.setIgnoreDiacritics(isIgnoreDiacritics);
		param.setSelectionMode(isOnlyOrExcept ? SelectionMode.ONLY : SelectionMode.EXCEPT);
		param.setSelectedDiacritics(selectedDiacritics);
		
		diacriticOptionsPanel = new DiacriticOptionsPanel(param);
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
		retVal.add(diacriticOptionsPanel, gbc);
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

//		retVal.put("stripDiacritics", diacriticOptionsPanel.getIgnoreDiacriticsBox().isSelected());
		
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
