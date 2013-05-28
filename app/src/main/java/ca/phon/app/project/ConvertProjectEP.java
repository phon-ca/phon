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
package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.application.IPhonFactory;
import ca.phon.application.PhonTask;
import ca.phon.application.PhonTask.TaskStatus;
import ca.phon.application.PhonTaskListener;
import ca.phon.application.PhonWorker;
import ca.phon.application.project.PhonProject;
import ca.phon.application.transcript.IDepTierDesc;
import ca.phon.application.transcript.IParticipant;
import ca.phon.application.transcript.ITranscriber;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.IUtterance;
import ca.phon.application.transcript.TranscriptUtils;
import ca.phon.gui.CommonModuleFrame;
import ca.phon.gui.DialogHeader;
import ca.phon.gui.components.PhonLoggerConsole;
import ca.phon.system.logger.PhonLogger;
import ca.phon.system.plugin.IPluginEntryPoint;
import ca.phon.system.plugin.PhonPlugin;
import ca.phon.system.plugin.PluginEntryPointRunner;
import ca.phon.system.plugin.PluginException;
import ca.phon.util.CollatorFactory;
import ca.phon.util.NativeDialogs;
import ca.phon.util.PhonUtilities;
import ca.phon.util.sysprops.SystemProperties;
import ca.phon.util.sysprops.io.Property;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * Converts ('old')  projects into new ones.  Old projects
 * are identified by project versions which are 'unk'.
 *
 */
@PhonPlugin(name="default")
public class ConvertProjectEP implements IPluginEntryPoint {
	
	/** The old project */
	private PhonProject oldProject;
	
	/** The new project */
	private PhonProject newProject;
	
	private String projectPath;
	
	private CopyTaskPanel panel;
	
	private boolean runSilent = false;
	
	private PhonWorker worker;
	
	private final static String EP_NAME = "ConvertProject";
	@Override
	public String getName() {
		return EP_NAME;
	}

	/**
	 * Module accepts 2 parameters:
	 * 
	 *  * oldProjectPath = path to project for upgrade: String
	 *  * silent = don't display UI: Boolean
	 */
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		panel = new CopyTaskPanel();
		
		if(initInfo.get("oldProjectPath") == null) {
			throw new IllegalArgumentException("No project given for upgrade");
		}
		
//		boolean silent = false;
		if(initInfo.get("silent") != null) {
			runSilent = (Boolean)initInfo.get("silent");
		}
		
		if(!runSilent) {
			CommonModuleFrame cmf = new CommonModuleFrame("Convert Project");
			cmf.setLayout(new BorderLayout());
			cmf.add(panel, BorderLayout.CENTER);
			cmf.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	//		cmf.pack();
			cmf.setSize(400, 600);
			cmf.setVisible(true);
		}
		
		// get the oldproject path
		final String oldProjectPath = initInfo.get("oldProjectPath").toString();
		projectPath = oldProjectPath;
		
		// we want to copy over the project with a new name
		final String newProjectPath = oldProjectPath;
		
		if(!runSilent) {
			worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			worker.setName("Convert project thread");
		}
		
		PhonTask startTask = new PhonTask() {
			@Override
			public void performTask() {
				NumberFormat twoFormat = NumberFormat.getIntegerInstance();
				twoFormat.setMinimumIntegerDigits(2);
				
				Calendar date = Calendar.getInstance();
				String dateSuffix = "-Phon-1_3-project-" +
					date.get(Calendar.YEAR) + 
					twoFormat.format(date.get(Calendar.MONTH)+1) + 
					twoFormat.format(date.get(Calendar.DATE));
//				String oldpp = oldProjectPath.replace(".phon", "-"+dateSuffix+".phon");
				File oldProjectFile = new File(oldProjectPath);
				File backupDir = new File(PhonUtilities.getPhonWorkspace(), "backups");
				File oldppFile = new File(backupDir, oldProjectFile.getName() + dateSuffix);
				int fIdx = 1;
				while(oldppFile.exists()) {
					oldppFile = new File(backupDir, oldProjectFile.getName() + dateSuffix + "(" + (fIdx++) + ")");
				}
				String oldpp = oldppFile.getAbsolutePath();
				
				PhonLogger.info(
						"Creating backup of project at " + oldpp);
				// copy the old project to the new project name
//				try {
					File oldP = new File(oldpp);
					File newP = new File(newProjectPath);

//					if(newP.isFile()) {
//						PhonUtilities.copyFile(new File(newProjectPath), new File(oldpp));
//					} else {
					if(!newP.renameTo(oldP)) {
						PhonLogger.severe("Could not rename project.");
						return;
					}
//					}
//				} catch (IOException e) {
//					PhonLogger.warning(e.toString());
//					return;
//				}
				
//				File oldFile = new File(newProjectPath);
//				boolean success = oldFile.delete();
//				if(!success) {
//					PhonLogger.warning("Could not delete old project.");
//					return;
//				}
				
				PhonLogger.info(
						"Creating new project at " + newProjectPath);
				// create the new project
				try {
					oldProject = (PhonProject) PhonProject.fromFile(oldpp);
					newProject = (PhonProject) PhonProject.newProject(newProjectPath);
					newProject.setProjectName(oldProject.getProjectName());
					newProject.save();
				} catch (IOException e) {
					PhonLogger.warning(e.toString());
					return;
				}
				
				PhonLogger.info(
						"Building session list...");
				
				Collator collator = CollatorFactory.defaultCollator();
				ArrayList<String> corpusList = new ArrayList<String>();
				corpusList.addAll(oldProject.getCorpora());
				Collections.sort(corpusList, collator);
				ArrayList<CopySessionTask> allTasks = new ArrayList<CopySessionTask>();
				for(String corpus:corpusList) {
					try {
						PhonLogger.info(
								"Adding corpus '" + corpus + "'");
						newProject.newCorpus(corpus, oldProject.getCorpusDescription(corpus));
					} catch (IOException e) {
						PhonLogger.warning(e.toString());
					}
					ArrayList<String> sessionList = new ArrayList<String>();
					sessionList.addAll(oldProject.getCorpusTranscripts(corpus));
					Collections.sort(sessionList, collator);
//					CopySessionTask[] csts = new CopySessionTask[sessionList.size()];
					int cstIdx = 0;
					for(String session:sessionList) {
						CopySessionTask cst = new CopySessionTask(corpus, session);
//						csts[cstIdx++] = cst;
						allTasks.add(cst);
						cst.addTaskListener(new PhonTaskListener() {

							@Override
							public void propertyChanged(PhonTask task, String property,
									Object oldValue, Object newValue) {
								if(!runSilent)
									panel.updateTable();
							}

							@Override
							public void statusChanged(PhonTask task,
									TaskStatus oldStatus, TaskStatus newStatus) {
								if(!runSilent)
									panel.updateTable();
								if(newStatus == TaskStatus.FINISHED) {
									PhonLogger.info("Converted session: " + task.getName());
								} else if(newStatus == TaskStatus.ERROR) {
									PhonLogger.info("Failed to convert: " + task.getName());
								}
							}
							
						});
						if(!runSilent)
							worker.invokeLater(cst);
						else
							cst.run();
					}
//					panel.setTasks(csts);
				}
				if(!runSilent)
					panel.setTasks(allTasks.toArray(new CopySessionTask[0]));
				
				if(!runSilent) {
					worker.setFinalTask(new Runnable() {
						@Override
						public void run() {
							
							panel.openButton.setEnabled(true);
							panel.console.stopLogging();
						}
					});
				}
				
			}
		};
		if(!runSilent)
			worker.invokeLater(startTask);
		
		// create the worker group
//		PhonWorkerGroup pwg = new PhonWorkerGroup(1);
		
//		worker.
		
		
//		LogListener listener = new LogListener() {
//
//			@Override
//			public void newLogEntry(LogEvent entry) {
//				PhonLogger.info(entry.getMessage());
//			}
//			
//		};
//		PhonLogger.addLogListener(listener);
		if(!runSilent) {
			panel.console.addReportThread(worker);
			panel.console.startLogging();
			worker.start();
		} else {
			startTask.run();
		}
	}
	
	private class CopyTaskPanel extends JComponent {
//		private PhonWorkerGroup workerGroup;
		
		private CopySessionTask tasks[];
		
		private JXTable taskTable;
		
		private DialogHeader header;
		
//		private JTextArea console;
		private PhonLoggerConsole console;
		
		private JButton closeButton;
		
		private JButton openButton;
		
		public CopyTaskPanel() {
			super();
			
			this.tasks = new CopySessionTask[0];
			
			init();
		}
		
		public void init() {
			setLayout(new BorderLayout());
			
			this.header = new DialogHeader("Convert Project", projectPath);
			
			this.console = new PhonLoggerConsole();
//			this.console.setMinimumSize(new Dimension(0, 100));
//			this.console.setEditable(false);
			this.console.setFont(new Font("Courier New", Font.PLAIN, 10));
//			JScrollPane consoleScroll = new JScrollPane(console);
//			consoleScroll.setMinimumSize(new Dimension(0, 100));
			
			taskTable = new JXTable(new CopyTaskTableModel(tasks));
			taskTable.setVisibleRowCount(10);
			taskTable.packAll();
			
			JSplitPane splitPane = new JSplitPane();
			splitPane.setOrientation(SwingConstants.HORIZONTAL);
			splitPane.setLeftComponent(new JScrollPane(taskTable));
			splitPane.setRightComponent(console);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerLocation(0.75);
			
			closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CommonModuleFrame parentFrame = 
						(CommonModuleFrame)SwingUtilities.getAncestorOfClass(CommonModuleFrame.class, closeButton);
					// shutdown worker
					worker.shutdown();
					if(parentFrame != null)
						parentFrame.setVisible(false);
				}
				
			});
			
			openButton = new JButton("Open Project");
			openButton.setEnabled(false);
			openButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CommonModuleFrame parentFrame = 
						(CommonModuleFrame)SwingUtilities.getAncestorOfClass(CommonModuleFrame.class, closeButton);
					if(parentFrame != null)
						parentFrame.setVisible(false);
					
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					initInfo.put("ca.phon.modules.core.OpenProjectController.projectpath", 
							projectPath);
					try {
						PluginEntryPointRunner.executePlugin("OpenProject", initInfo);
					} catch (PluginException ex) {
						PhonLogger.severe(ex.getMessage());
						ex.printStackTrace();
						
						NativeDialogs.showMessageDialogBlocking(null, "", "Open Project",
								"Could not open project. Reason: " + ex.getMessage());
					}
				}
				
			});
			
			add(ButtonBarFactory.buildOKCancelBar(openButton, closeButton),
					BorderLayout.SOUTH);
			
			add(this.header, BorderLayout.NORTH);
			add(splitPane, BorderLayout.CENTER);
		}
		
		public void setTasks(CopySessionTask[] tasks) {
			taskTable.setModel(new CopyTaskTableModel(tasks));
		}
		
		public void updateTable() {
			CopyTaskTableModel model = (CopyTaskTableModel)taskTable.getModel();
			model.fireTableDataChanged();
		}
		
	}
	
	private class CopyTaskTableModel extends AbstractTableModel implements PhonTaskListener {
		
		private CopySessionTask tasks[];
		
		public CopyTaskTableModel(CopySessionTask tasks[]) {
			super();
			
			this.tasks = tasks;
			for(CopySessionTask t:tasks) t.addTaskListener(this);
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return tasks.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Runnable runnable = 
				tasks[rowIndex];
			CopySessionTask cst = (CopySessionTask)runnable;
			if(columnIndex == 1) {
				String retVal = cst.getStatus().toString();
				if(cst.getStatus() == TaskStatus.RUNNING
						&& cst.getProperty(CopySessionTask.PR_PROP) != null) {
					Double d = (Double)cst.getProperty(CopySessionTask.PR_PROP);
					retVal = String.format("%.1f%% complete", d);
				}
				return retVal;
			} else {
				return cst.getName();
			}
		}
		
		@Override
		public String getColumnName(int col) {
			String retVal = "";
			
			if(col == 1)
				retVal = "Status";
			else if(col == 0)
				retVal = "Session";
			
			return retVal;
		}
		
		
		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			if(property.equals(CopySessionTask.PR_PROP))
				super.fireTableDataChanged();
		}

		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus,
				TaskStatus newStatus) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class CopySessionTask extends PhonTask {
		
		public static final String PR_PROP = "_progress_";
		
		/** The corpus and session names */
		private String corpus;
		private String session;
		
		public CopySessionTask(String corpus, String session) {
			super(corpus + "." + session);
			
			this.corpus = corpus;
			this.session = session;
		}

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			// open the old transcript with the tb2 factory
			IPhonFactory tb2Factory = IPhonFactory.getFactory("1.2");
			
			ITranscript oldSession = tb2Factory.createTranscript();
			try {
				oldSession = oldProject.getTranscript(corpus, session);
			} catch (IOException e) {
				PhonLogger.warning(e.toString());
				return;
			}
			
			if(!newProject.getCorpora().contains(corpus)) {
				try {
					newProject.newCorpus(corpus, "");
				} catch (IOException e) {
					PhonLogger.warning(e.toString());
				}
			}
			ITranscript newSession = null;
			try {
				newSession = newProject.newTranscript(corpus, session);
			} catch (IOException e) {
				PhonLogger.warning(e.toString());
				return;
			}
			
			// copy over header data
			if(oldSession.getDate() != null)
				newSession.setDate(oldSession.getDate());
			newSession.setCorpus(corpus);
			newSession.setID(session);
			if(oldSession.getLanguage() != null)
				newSession.setLanguage(oldSession.getLanguage());
			if(oldSession.getMediaLocation() != null)
				newSession.setMediaLocation(oldSession.getMediaLocation());
			
			// copy participants
			for(IParticipant p:oldSession.getParticipants()) {
				IParticipant newP = newSession.newParticipant();
				newP.setId(p.getId());
				if(p.getBirthDate() != null)
					newP.setBirthDate(p.getBirthDate());
				if(p.getEducation() != null)
					newP.setEducation(p.getEducation());
				if(p.getGroup() != null)
					newP.setGroup(p.getGroup());
				if(p.getLanguage() != null)
					newP.setLanguage(p.getLanguage());
				if(p.getName() != null)
					newP.setName(p.getName());
				if(p.getRole() != null)
					newP.setRole(p.getRole());
				if(p.getSex() != null)
					newP.setSex(p.getSex());
				if(p.getSES() != null)
					newP.setSES(p.getSES());
			}
			
			// setup tiers
			for(IDepTierDesc tierDesc:oldSession.getDependentTiers()) {
				IDepTierDesc newTier = newSession.newDependentTier();
				newTier.setTierName(tierDesc.getTierName());
				newTier.setTierFont(tierDesc.getTierFont());
				newTier.setIsGrouped(false);
			}
			
			for(IDepTierDesc tierDesc:oldSession.getWordAlignedTiers()) {
				IDepTierDesc newTier = newSession.newDependentTier();
				newTier.setTierName(tierDesc.getTierName());
				newTier.setTierFont(tierDesc.getTierFont());
				newTier.setIsGrouped(true);
			}
			
			SystemProperties transcriptDb = null;
			// copy blind transcriptions
			try {
				transcriptDb = 
					oldProject.getTranscriptDatabase(corpus, session);
				
				// create a list of transcribers
				String transcriberNameProp = "transcriber\\.([a-zA-Z0-9 ]+)\\.name";
				Pattern namePattern = Pattern.compile(transcriberNameProp);
				List<Property> props = transcriptDb.getProperties(transcriberNameProp);
				for(Property p:props) {
					Matcher m = namePattern.matcher(p.getName());
					String username = null;
					if(m.matches()) {
						username = m.group(1);
					}
					String name = p.getValue();
					
					ITranscriber newTranscriber =
						newSession.newTranscriber();
					newTranscriber.setUsername(username);
					newTranscriber.setRealName(name);
					
					// see if there is a password property
					String pwdPropS = "transcriber\\." + username + "\\.password";
					Object pwdObj = transcriptDb.getProperty(pwdPropS);
					if(pwdObj != null) {
						String pass = pwdObj.toString();
						newTranscriber.setUsePassword(true);
						newTranscriber.setPassword(pass);
					}
				}
				
			} catch (IOException e1) {
				PhonLogger.warning(e1.toString());
			}
			
			// copy utterances
			int uttIndex = 0;
			int uttCount = oldSession.getNumberOfUtterances();
			for(IUtterance utt:oldSession.getUtterances()) {
//				if(uttIndex++ == 145)
//					System.out.println(uttIndex);
				super.setProperty(PR_PROP, ((double)uttIndex++/(double)uttCount) * 100);
				TranscriptUtils.addRecordToTranscript(newSession, utt, transcriptDb);
			}
			
			// save new session
			int writeLock = newProject.getTranscriptWriteLock(corpus, session);
			try {
				newProject.saveTranscript(newSession, writeLock);
			} catch (IOException e) {
				PhonLogger.warning(e.toString());
			}
			newProject.releaseTranscriptWriteLock(corpus, session, writeLock);
			
			try {
				newProject.save();
			} catch (IOException e) {
				PhonLogger.warning(e.toString());
			}
			
			System.out.println("Finished session: " + super.getName());
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	}

}
