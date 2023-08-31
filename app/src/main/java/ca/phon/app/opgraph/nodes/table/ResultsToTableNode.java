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

import ca.phon.app.log.LogUtil;
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
import ca.phon.query.report.ResultsToTable;
import ca.phon.query.report.datasource.*;
import ca.phon.query.script.params.*;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.worker.PhonWorkerGroup;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.time.Period;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.*;

@OpNodeInfo(name="Results To Table",
	description="Convert a set of result to a table",
	category="Table")
public class ResultsToTableNode extends OpNode implements NodeSettings {

	// global context key for results cache
	private final static String RESULT_CACHE = "__resultTableCache";

	// required inputs
	private final InputField projectInput = new InputField("project", "Project", false, true, Project.class);

	private final InputField resultSetsInput = new InputField("results", "Query results", false, true, ResultSet[].class);

	// optional inputs
	private final InputField cacheIdInput = new InputField("cacheId", "Result cache id (optional)", true, true, UUID.class);

	private final InputField includeSessionInfoInput = new InputField("includeSessionInfo", "Include session info columns: Name, Date", true, true, Boolean.class);
	
	private final InputField includeSpeakerInfoInput = new InputField("includeSpeakerInfo", "Include speaker info columns: Speaker, Age",  true, true, Boolean.class);
	
	private final InputField includeTierInfoInput = new InputField("includeTierInfo", "Include tier info columns: Record #, Group #, Tier, Range",  true, true, Boolean.class);
	
	private final InputField includeMetadataInput = new InputField("includeMetadata", "Include metadata columns such as aligned group and word tiers",  true, true, Boolean.class);
	
	private final InputField ignoreDiacriticsInput = new InputField("ignoreDiacritics", "Ignore diacritics",  true, true, Boolean.class);
	
	private final InputField onlyOrExceptInput = new InputField("onlyOrExcept", "If true (only) selected diacritics will be ignored, if false operation will be 'except'",  true, true, Boolean.class);
	
	private final InputField selectedDiacriticsInput = new InputField("selectedDiacritics", "Selected diacritics to ignore",  true, true, Collection.class);
	
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
		putField(cacheIdInput);

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

	private OpContext getGlobalContext(OpContext context) {
		OpContext retVal = context;
		while(retVal.getParent() != null) {
			retVal = retVal.getParent();
		}
		return retVal;
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final OpContext globalContext = getGlobalContext(context);
		if(!globalContext.containsKey(RESULT_CACHE)) {
			globalContext.put(RESULT_CACHE, Collections.synchronizedMap(new HashMap<>()));
		}
		final Map<ResultTableKey, DefaultTableDataSource> resultTableCache = (Map<ResultTableKey, DefaultTableDataSource>)globalContext.get(RESULT_CACHE);

		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsInput);
		final Project project = (Project)context.get(projectInput);

		final UUID cacheId = (UUID) context.get(cacheIdInput);
		
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

		int numProcessors = Runtime.getRuntime().availableProcessors();
		int numThreads = (int)Math.ceil((float)numProcessors / 4.0);
		int numSessions = resultSets.length;
		final PhonWorkerGroup workerGroup = new PhonWorkerGroup(Math.min(numThreads, numSessions));
		int serial = 0;
		final Map<ResultSet, DefaultTableDataSource> resultTables = Collections.synchronizedMap(new HashMap<>());
		final AtomicReference<CountDownLatch> latchRef = new AtomicReference<>();
		for(final ResultSet rs:resultSets) {
			workerGroup.queueTask(() -> {
				DefaultTableDataSource resultTable = null;
				if(cacheId != null) {
					resultTable = resultTableCache.get(new ResultTableKey(cacheId, project.getUUID(), rs.getSessionPath()));
				}
				if(resultTable == null)
					resultTable = ResultsToTable.createResultTable(project, new ResultSet[]{rs}, includeSessionInfo, includeSpeakerInfo, includeTierInfo, includeMetadata, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
				resultTables.put(rs, resultTable);
				latchRef.get().countDown();

				if(cacheId != null) {
					resultTableCache.put(new ResultTableKey(cacheId, project.getUUID(), rs.getSessionPath()), resultTable);
				}
			});
			++serial;
		}
		final CountDownLatch cdLatch = new CountDownLatch(serial);
		latchRef.set(cdLatch);
		final DefaultTableDataSource table = ResultsToTable.setupTable(resultSets, includeSessionInfo, includeSpeakerInfo, includeTierInfo, includeMetadata);

		workerGroup.begin();

		try {
			cdLatch.await();
			workerGroup.shutdown();

			for(ResultSet rs:resultSets) {
				DefaultTableDataSource tbl = resultTables.get(rs);
				if(tbl != null)
					table.append(tbl);
			}
		} catch (InterruptedException e) {
			throw new ProcessingException(null, e);
		}

		context.put(tableOutput, table);
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

	// key used for result table cache
	private class ResultTableKey {
		UUID queryId;
		UUID projectId;
		String sessionPath;

		public ResultTableKey(UUID queryId, UUID projectId, String sessionPath) {
			this.queryId = queryId;
			this.projectId = projectId;
			this.sessionPath = sessionPath;
		}

		@Override
		public int hashCode() {
			return Objects.hash(queryId, projectId, sessionPath);
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof ResultTableKey)) return false;
			ResultTableKey otherKey = (ResultTableKey) o;
			return queryId.equals(otherKey.queryId) &&
					projectId.equals(otherKey.projectId) &&
					sessionPath.equals(otherKey.sessionPath);
		}
	}

}
