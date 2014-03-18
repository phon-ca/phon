/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.query.report;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ca.phon.app.query.ResultSetSelector;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.ReportBuilder;
import ca.phon.query.report.ReportBuilderException;
import ca.phon.query.report.ReportIO;
import ca.phon.query.report.csv.CSVReportBuilder;
import ca.phon.query.report.io.ObjectFactory;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

/**
 * 1-step wizard for creating reports.
 */
public class ReportWizard extends WizardFrame {
	
	private final static Logger LOGGER = Logger.getLogger(ReportWizard.class.getName());
	
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
	private PhonLoggerConsole console;
	
	/*
	 * Session selection
	 */
	private ResultSetSelector resultSetSelector;
	
	/*
	 * Open after gen button 
	 */
	private JCheckBox openReportBox;
	private final String OPEN_REPORT_PROP = getClass().getName() + ".openReportWhenDone";
	
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
		
		final ImageIcon saveIcon = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		super.btnNext.setIcon(saveIcon);
		super.btnNext.setText("Save Report");
		super.btnNext.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		super.getRootPane().setDefaultButton(btnNext);
		
		super.btnFinish.setVisible(false);
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
		
		DialogHeader importHeader = new DialogHeader("Report", "Writing report to file, this may take some time.");
		importPanel.add(importHeader, BorderLayout.NORTH);
		
		JPanel consolePanel = new JPanel(new BorderLayout());
		
		console = new PhonLoggerConsole();
		consolePanel.add(console, BorderLayout.CENTER);
		
		importPanel.add(consolePanel, BorderLayout.CENTER);
		
		openReportBox = new JCheckBox("Open report when done");
		boolean openReportWhenDone = PrefHelper.getBoolean(OPEN_REPORT_PROP, true);
		openReportBox.setSelected(openReportWhenDone);
		openReportBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				PrefHelper.getUserPreferences().putBoolean(OPEN_REPORT_PROP, openReportBox.isSelected());
			}
		});
		FlowLayout optLayout = new FlowLayout(FlowLayout.RIGHT);
		optLayout.setVgap(0);
		JPanel optPanel = new JPanel(optLayout);
		optPanel.add(openReportBox);
		consolePanel.add(optPanel, BorderLayout.NORTH);
		
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
	private void writeAutosaveReport() {
		File designFile = new File(PrefHelper.getUserDataFolder(), AUTOSAVE_FILENAME);
		try {
			ReportIO.writeDesign(reportEditor.getReportDesign(), designFile);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public void next() {
		String saveAs = null;
		if(super.getCurrentStep() == editorStep) {
//			File reportDir = 
//				new File(PhonUtilities.getPhonWorkspace(), "reports");
//			if(!reportDir.exists()) {
//				reportDir.mkdirs();
//			}
			
			String reportBaseName = 
				reportEditor.getReportDesign().getName();
			// remove extension
//			if(reportBaseName.indexOf('.') > 0) {
//				reportBaseName = reportBaseName.substring(0, reportBaseName.lastIndexOf('.'));
//			}
			String reportName = reportBaseName;
			String defExt = ".csv";
			
//			String rptFullName = reportBaseName + defExt;
//			File rptFile = new File(reportDir, rptFullName);
//			int fIdx = 0;
//			while(rptFile.exists()) {
//				reportName = reportBaseName + " (" + (++fIdx) + ")";
//				rptFile = new File(reportDir, reportName + defExt);
//			}
			
			// generate report
			FileFilter filters[] = { FileFilter.csvFilter };
			saveAs = NativeDialogs.showSaveFileDialogBlocking(this, null, reportName, null, filters, "Save report");
			if(saveAs == null) return;
		}
		
		super.next();
		
		if(super.getCurrentStep() == reportStep) {
			generateReport(saveAs);
		}
	}
	
	@Override
	public void cancel() {
		if(currentTask != null
				&& currentTask.getStatus() == TaskStatus.RUNNING) {
//			console.appendWarning("Stopping report generation...");
			currentTask.shutdown();
		} else {
			super.cancel();
		}
	}
	
	private void generateReport(String saveAs) {
		if(saveAs != null) {

			// save last design
			writeAutosaveReport();
			
//			PathExpander pe = new PathExpander();
//			busyMsgLabel.setText("Saving report to file '" +
//					pe.compressPath(saveAs) + "'...");
			
			CSVReportBuilder builder = new CSVReportBuilder();
			builder.putProperty(CSVReportBuilder.INDENT_CONTENT, Boolean.TRUE);
			builder.putProperty(ReportBuilder.TEMP_PROJECT, tempProject);
			
			PhonWorker worker = PhonWorker.createWorker();
			worker.setName("Report");
			worker.setFinishWhenQueueEmpty(true);
			
			Runnable onStart = new Runnable() {
				@Override
				public void run() {
					Runnable turnOffBack = new Runnable() {
						@Override
						public void run() {
							btnBack.setEnabled(false);
							btnCancel.setText("Cancel");
							showBusyLabel(console);
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
							console.removeLogger(LOGGER);
							btnBack.setEnabled(true);
							btnCancel.setText("Close");
							stopBusyLabel();
							currentTask = null;
						}
					};
					SwingUtilities.invokeLater(turnOnBack);
				}
			};
			GenerateReportTask task = new GenerateReportTask(reportEditor.getReportDesign(), getProject(), query, 
					resultSetSelector.getSelectedSearches(), new File(saveAs), builder);
			
			worker.invokeLater(onStart);
			worker.invokeLater(task);
			worker.setFinalTask(onEnd);
			currentTask = task;
			
			console.addLogger(LOGGER);
			
			worker.start();
		}
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
		private File output;
		
		private ReportBuilder builder = null;
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			LOGGER.info("Writing report to file: " + output.getAbsolutePath());
			
			try {
				builder.buildReport(design, project, query, resultSets, output);
				Boolean openWhenDone = openReportBox.isSelected();
				if(openWhenDone) {
					try {
						LOGGER.info("Opening report in default editor...");
						OpenFileLauncher.openURL(new URL("file://" + output.getAbsolutePath()));
					} catch (MalformedURLException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
						
						NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null,
								"Failed to open report", "Message: " + e.getMessage());
					}
				}
				
				LOGGER.info("Report generation finished.");
				
				super.setStatus(TaskStatus.FINISHED);
			} catch (ReportBuilderException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null, "Report Build Failed", "Error: " + e.getMessage());
				super.setStatus(TaskStatus.ERROR);
				super.err = e;
			}
			
		}

		public GenerateReportTask(ReportDesign design,
				Project project,
				Query query,
				ResultSet[] search,
				File output,
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
