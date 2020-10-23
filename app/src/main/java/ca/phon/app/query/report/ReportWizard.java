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
package ca.phon.app.query.report;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.app.query.*;
import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.query.report.*;
import ca.phon.query.report.csv.*;
import ca.phon.query.report.io.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.wizard.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.*;

/**
 * 1-step wizard for creating reports.
 */
public class ReportWizard extends WizardFrame {
	
	private static final long serialVersionUID = -334722251289455999L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ReportWizard.class.getName());
	
	private final static String AUTOSAVE_FILENAME = "lastreport.xml";
	
	/*
	 * UI elements
	 */
	/*
	 * Report editor
	 */
	private ReportEditor reportEditor;
	
	/*
	 * Console 
	 */
	private BufferPanel console;
	
	/*
	 * Session selection
	 */
	private ResultSetSelector resultSetSelector;
	
	/*
	 * Query
	 */
	private Query query;
	
	/*
	 * Wizard steps 
	 */
	private WizardStep editorStep;
	private WizardStep reportStep;
	
	private PhonTask currentTask = null;
	
	private Project tempProject = null;
	
	public ReportWizard(Project project, Query q) {
		this(project, null, q);
	}
	
	/**
	 * Constructor
	 */
	public ReportWizard(Project project, Project tempProject, Query q) {
		super("Phon : " + project.getName() + " : Report");
		this.tempProject = tempProject;
		
		putExtension(Project.class, project);

		super.setWindowName("Report");
//		super.setProject(project);
		this.query = q;
		
//		super.setResizable(false);
		
		init();
	}
	
	private Project getProject() {
		return getExtension(Project.class);
	}
	
	private void init() {
		// add steps
		editorStep = createEditorStep();
		reportStep = createReportStep();
		
		editorStep.setNextStep(1);
		reportStep.setPrevStep(0);
		
		// setup navigation buttons
		super.btnBack.setVisible(false);
		
		super.btnNext.setText("Generate report");
		super.btnNext.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		super.getRootPane().setDefaultButton(btnNext);
		
		super.btnFinish.setVisible(false);
		
		final PhonUIAction saveAct = new PhonUIAction(console, "onSaveBuffer");
		saveAct.putValue(PhonUIAction.NAME, "Save Report");
		saveAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));
		super.btnFinish.setAction(saveAct);
		
		super.btnCancel.setText("Close");
	}
	
	/**
	 * Create step one of the wizard
	 */
	private WizardStep createEditorStep() {
		JPanel reportPanel = new JPanel(new BorderLayout());
		
		DialogHeader header = new DialogHeader("Report", "Set up report format and select result sets.");
		reportPanel.add(header, BorderLayout.NORTH);
		
		JTabbedPane tabs = new JTabbedPane();
		
		ReportDesign design = getReportDesign();
		
		final String queryName = query.getName();
		String reportName = queryName;
		if(queryName.indexOf('.') > 0) {
			reportName = queryName.substring(0, queryName.lastIndexOf('.'));
		}
		
		design.setName(reportName);
		reportEditor = new ReportEditor(design);
		
		tabs.addTab("Report Format", reportEditor);
		
		resultSetSelector = new ResultSetSelector((tempProject == null ? getProject() : tempProject), query);
		resultSetSelector.selectAll();
		tabs.addTab("Result Sets", resultSetSelector);
		
		reportPanel.add(tabs, BorderLayout.CENTER);
		
		return super.addWizardStep(reportPanel);
	}
	
	/**
	 * Create report gen step
	 */
	private WizardStep createReportStep() {
		JPanel importPanel = new JPanel(new BorderLayout());
		
		DialogHeader importHeader = new DialogHeader("Report", "Generating report, this may take some time.");
		importPanel.add(importHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
		
		console = new BufferPanel(getReportDesign().getName());
		consolePanel.add(console, BorderLayout.CENTER);
		
		importPanel.add(consolePanel, BorderLayout.CENTER);
		
		return super.addWizardStep(importPanel);
	}

	/**
	 * Returns last report design that was open or a new empty report
	 * design.
	 * @return
	 */
	private ReportDesign getReportDesign() {
		ObjectFactory factory = new ObjectFactory();
		ReportDesign retVal = factory.createReportDesign();
		
		File lastDesignFile = 
			new File(PrefHelper.getUserDataFolder(), AUTOSAVE_FILENAME);
		if(lastDesignFile.exists()) {
			try {
				retVal = ReportIO.readDesign(lastDesignFile);
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
	private void writeAutosaveReport() {
		File designFile = new File(PrefHelper.getUserDataFolder(), AUTOSAVE_FILENAME);
		try {
			ReportIO.writeDesign(reportEditor.getReportDesign(), designFile);
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public void prev() {
		super.prev();
		btnBack.setVisible(false);
		btnFinish.setVisible(false);
	}
	
	@Override
	public void next() {
		super.next();
		
		if(super.getCurrentStep() == reportStep) {
			try {
				generateReport();
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}
	
	
	@Override
	public void finish() {
		
	}
	
	@Override
	public void cancel() {
		if(currentTask != null
				&& currentTask.getStatus() == TaskStatus.RUNNING) {
			currentTask.shutdown();
		} else {
			super.cancel();
		}
	}
	
	private void generateReport() throws IOException {
		// save last design
		writeAutosaveReport();
		
		CSVReportBuilder builder = new CSVReportBuilder();
		builder.putProperty(CSVReportBuilder.INDENT_CONTENT, Boolean.TRUE);
		builder.putProperty(ReportBuilder.TEMP_PROJECT, tempProject);
		
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("Report");
		worker.setFinishWhenQueueEmpty(true);
		
		final OutputStreamWriter out = new OutputStreamWriter(console.getLogBuffer().getStdOutStream(), "UTF-8");
		out.flush();
		out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
		out.flush();
		
		Runnable onStart = new Runnable() {
			@Override
			public void run() {
				Runnable turnOffBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(false);
						btnCancel.setText("Cancel");
						if(!console.isShowingBuffer()) {
							console.clear();
						}
					}
				};
				
				SwingUtilities.invokeLater(turnOffBack);
			}
		};
		Runnable onEnd = new Runnable() {
			@Override
			public void run() {
				Runnable turnOnBack = new Runnable() {
					@Override
					public void run() {
						btnBack.setEnabled(true);
						btnCancel.setText("Close");
						console.showTable();
						try {
							out.flush();
							out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
							out.flush();
						} catch (IOException e) {
							LOGGER.error(
									e.getLocalizedMessage(), e);
						}
						console.setFirstRowIsHeader(false);
						btnBack.setVisible(true);
						btnFinish.setVisible(true);
						currentTask = null;
					}
				};
				SwingUtilities.invokeLater(turnOnBack);
			}
		};
		GenerateReportTask task = new GenerateReportTask(reportEditor.getReportDesign(), getProject(), query, 
				resultSetSelector.getSelectedSearches(), console.getLogBuffer().getStdOutStream(), builder);
		
		worker.invokeLater(onStart);
		worker.invokeLater(task);
		worker.setFinalTask(onEnd);
		currentTask = task;
		
		worker.start();
	}
	
	/**
	 * Generate report task
	 * 
	 */
	private class GenerateReportTask extends PhonTask implements PhonTaskListener {
		
		private ReportDesign design;
		private Project project;
		private Query query;
		private ResultSet[] resultSets;
		private OutputStream output;
		
		private ReportBuilder builder = null;
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			try {
				builder.buildReport(design, project, query, resultSets, output);
				
				super.setStatus(TaskStatus.FINISHED);
			} catch (ReportBuilderException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null, "Report Build Failed", "Error: " + e.getMessage());
				super.setStatus(TaskStatus.ERROR);
				super.err = e;
			}
			
		}

		public GenerateReportTask(ReportDesign design,
				Project project,
				Query query,
				ResultSet[] search,
				OutputStream output,
				ReportBuilder builder) {
			this.design = design;
			this.project = project;
			this.query = query;
			this.resultSets = search;
			this.output = output;
			this.builder = builder;
			
			addTaskListener(this);
		}

		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus,
				TaskStatus newStatus) {
			if(newStatus == TaskStatus.TERMINATED
					&& super.isShutdown()) {
				if(builder != null) {
					builder.putProperty(ReportBuilder.CANCEL_BUILD, Boolean.TRUE);
				}
			}
		}

		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			
		}
		
	}
	
}
