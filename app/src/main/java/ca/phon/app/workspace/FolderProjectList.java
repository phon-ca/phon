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
package ca.phon.app.workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.border.MatteBorder;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXRadioGroup;
import org.joda.time.DateTime;

import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.session.DateFormatter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.workspace.Workspace;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * List for displaying project in a given directory.
 * @author ghedlund
 *
 */
public class FolderProjectList extends JPanel {
	
	private final static Logger LOGGER = Logger.getLogger(FolderProjectList.class.getName());
	
	private enum SortBy {
		NAME,
		MOD_DATE,
		SIZE;
		
		private String[] displayNames = {
				"Name", "Modified", "Size"
		};
		
		public String getDisplayName() {
			return displayNames[ordinal()];
		}
		
		@Override
		public String toString() {
			return getDisplayName();
		}
	};
	
	/**
	 * The folder we are displaying project from
	 */
	private File projectFolder;
	
	/**
	 * Place to put project buttons
	 */
	private JPanel listPanel;
	private JScrollPane listScroller;
	
	private SortPanel sortBar;
	
	/**
	 * List of project files
	 */
	private List<MultiActionButton> projectButtons =
		new ArrayList<MultiActionButton>();
	
	/**
	 * Constructor
	 */
	public FolderProjectList() {
		this(Workspace.userWorkspaceFolder());
	}
	
	public FolderProjectList(File f) {
		this.projectFolder = f;
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		setBackground(Color.white);
		setOpaque(true);
		
		listPanel = new ButtonPanel();
		BoxLayout bl = new BoxLayout(listPanel, BoxLayout.Y_AXIS);
		listPanel.setLayout(bl);
		
		listScroller = new JScrollPane(listPanel);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		add(listScroller, BorderLayout.CENTER);
		
		sortBar = new SortPanel();
		add(sortBar, BorderLayout.NORTH);
		
		
		
		scanDirectory();
		updateProjectList();
	}
	
	public void setFolder(File f) {
		this.projectFolder = f;
		scanDirectory();
		updateProjectList();
	}
	
	public File getFolder() {
		return this.projectFolder;
	}

	
	private void updateProjectList() {
		listPanel.removeAll();
		listPanel.revalidate();
		listPanel.repaint();
		
		boolean stripRow = false;
		for(MultiActionButton btn:projectButtons) {
			listPanel.add(btn);
			
			if(stripRow) {
				btn.setBackground(PhonGuiConstants.PHON_UI_STRIP_COLOR);
				stripRow = false;
			} else {
				btn.setBackground(Color.white);
				stripRow = true;
			}
		}
		
		revalidate();
		listPanel.revalidate();
		listPanel.repaint();
	}
	
	private void scanDirectory() {
		projectButtons.clear();
		if(projectFolder == null || projectFolder.listFiles() == null) return;
		for(File f:projectFolder.listFiles()) {
			if(f.isDirectory()) {
				// check for a project.xml file
				File projXML = new File(f, "project.xml");
				if(projXML.exists()) {
					projectButtons.add(getProjectButton(f));
				}
			} else if(f.isFile()) {
				if(f.getName().endsWith(".phon") || f.getName().endsWith(".zip")) {
					projectButtons.add(getProjectButton(f));
				}
			}
		}
		
		Collections.sort(projectButtons, new ProjectComparator(sortBar.sortByGrp.getSelectedValue()));
	}

	private LocalProjectButton getProjectButton(File f) {
		LocalProjectButton retVal = new LocalProjectButton(f);
		
		// if we have a file, we need to add the import action
		if(f.isFile()) {
			PhonUIAction extractAction = new PhonUIAction(this, "onExtractProject", retVal);
			ImageIcon importIcn = 
				IconManager.getInstance().getIcon("actions/archive-extract", IconSize.SMALL);
			ImageIcon importIcnL = 
				IconManager.getInstance().getIcon("actions/archive-extract", IconSize.MEDIUM);
			extractAction.putValue(Action.NAME, "Extract project");
			extractAction.putValue(Action.SHORT_DESCRIPTION, "Extract: " +  f.getAbsolutePath());
			extractAction.putValue(Action.SMALL_ICON, importIcn);
			extractAction.putValue(Action.LARGE_ICON_KEY, importIcnL);
			retVal.setDefaultAction(extractAction);
			
			retVal.getTopLabel().setIcon(importIcn);
			retVal.getTopLabel().setForeground(PhonGuiConstants.PHON_ORANGE);
		} else {
			PhonUIAction openAction = new PhonUIAction(this, "onOpenProject", retVal);
			ImageIcon openIcn = 
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
			ImageIcon openIcnL =
				IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM);
			
			
			
			openAction.putValue(Action.NAME, "Open project");
			openAction.putValue(Action.SHORT_DESCRIPTION, "Open: " + f.getAbsolutePath());
			openAction.putValue(Action.SMALL_ICON, openIcn);
			openAction.putValue(Action.LARGE_ICON_KEY, openIcnL);
			retVal.setDefaultAction(openAction);
			
			PhonUIAction showAction = new PhonUIAction(this, "onShowProject", retVal);
			showAction.putValue(Action.NAME, "Show project");
			showAction.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("apps/system-file-manager", IconSize.SMALL));
			showAction.putValue(Action.LARGE_ICON_KEY, IconManager.getInstance().getIcon("apps/system-file-manager", IconSize.MEDIUM));
			showAction.putValue(Action.SHORT_DESCRIPTION, "Show project in file system viewer");
			retVal.addAction(showAction);
			
			ImageIcon archiveIcn = IconManager.getInstance().getIcon("actions/archive-insert", IconSize.SMALL);
			ImageIcon archiveIcnL = IconManager.getInstance().getIcon("actions/archive-insert", IconSize.MEDIUM);
			
			PhonUIAction archiveAction = new PhonUIAction(this, "onArchiveProject", retVal);
			archiveAction.putValue(Action.NAME, "Archive project");
			archiveAction.putValue(Action.SHORT_DESCRIPTION, "Create .zip archive of phon project...");
			archiveAction.putValue(Action.SMALL_ICON, archiveIcn);
			archiveAction.putValue(Action.LARGE_ICON_KEY, archiveIcnL);
			retVal.addAction(archiveAction);
		
			retVal.getTopLabel().setIcon(openIcn);
		}
		
		
		return retVal;
	}
	
	public void refresh() {
		scanDirectory();
		updateProjectList();
	}
	
	private class ProjectComparator implements Comparator<MultiActionButton> {

		private SortBy sortBy = SortBy.NAME;
		
		public ProjectComparator() {
			this(SortBy.NAME);
		}
		
		public ProjectComparator(SortBy by) {
			sortBy = by;
		}
		
		@Override
		public int compare(MultiActionButton mo1, MultiActionButton mo2) {
			int retVal = 0;
			
			LocalProjectButton o1 = (LocalProjectButton)mo1;
			LocalProjectButton o2 = (LocalProjectButton)mo2;
			
			if(sortBy == SortBy.NAME) {
				String o1Name = o1.getProjectFile().getName();
				String o2Name = o2.getProjectFile().getName();
				
				retVal = o1Name.compareTo(o2Name);
			} else if(sortBy == SortBy.MOD_DATE) {
				Long o1Mod = o1.getProjectFile().lastModified();
				Long o2Mod = o2.getProjectFile().lastModified();
				
				retVal = o2Mod.compareTo(o1Mod);
			} else if(sortBy == SortBy.SIZE) {
				Long o1Size = o1.getProjectSize();
				Long o2Size = o2.getProjectSize();
				
				retVal = o1Size.compareTo(o2Size);
			}
			
			return retVal;
		}
		
	}
	
	private class SortPanel extends JPanel {
		
		private JXRadioGroup<SortBy> sortByGrp
			= new JXRadioGroup<SortBy>();
		
		public SortPanel() {
			init();
		}
		
		private void init() {
			// create border
			MatteBorder lineBorder = 
				BorderFactory.createMatteBorder(0, 1, 1, 1, Color.lightGray);
			setBorder(lineBorder);
			
			FormLayout layout = new FormLayout(
					"2dlu, left:pref, fill:pref:grow, right:pref, 2dlu", "pref");
			CellConstraints cc = new CellConstraints();
//			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
//			fl.setVgap(0);
			
			setLayout(layout);
			
			JXLabel titleLabel = new JXLabel("Sort by:");
			add(titleLabel, cc.xy(2,1));
			
			for(SortBy sortBy:SortBy.values()) {
				sortByGrp.add(sortBy);
			}
			
			sortByGrp.setOpaque(false);
			sortByGrp.setSelectedValue(SortBy.NAME);
			
			for(int compIdx = 0; compIdx < sortByGrp.getChildButtonCount(); compIdx++) {
				AbstractButton btn = sortByGrp.getChildButton(compIdx);
				btn.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JRadioButton btn = (JRadioButton)e.getSource();
						if(btn.isSelected()) {
							Collections.sort(projectButtons, new ProjectComparator(sortByGrp.getSelectedValue()));
							updateProjectList();
						}
					}
				});
			}
				
			add(sortByGrp, cc.xy(3,1));
			
//			PhonUIAction refreshAction = 
//				new PhonUIAction(FolderProjectList.this, "onRefresh");
			ImageIcon icn = IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
//			refreshAction.putValue(PhonUIAction.SMALL_ICON, icn);
//			refreshAction.putValue(PhonUIAction.NAME, "Refresh");
//			refreshAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Refresh project list");
//			
//			JButton refreshButton = new JButton(refreshAction);
//			refreshButton.setBorderPainted(false);
			
			JLabel refreshLabel = new JLabel("<html><u style='color: blue;'>Refresh</u></html>");
			refreshLabel.setIcon(icn);
			refreshLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			refreshLabel.setToolTipText("Refresh project list");
			refreshLabel.addMouseListener(new MouseInputAdapter() {
				
				@Override
				public void mouseClicked(MouseEvent me) {
					refresh();
				}
				
			});
			
			
			add(refreshLabel, cc.xy(4,1));
		}
		
		
	}
	
	/*
	 * UI Actions
	 */
	public void onArchiveProject(PhonActionEvent pae) {
		final LocalProjectButton btn = (LocalProjectButton)pae.getData();
		final ProjectFactory factory = new ProjectFactory();
		try {
			final Project project = factory.openProject(btn.getProjectFile());
			final String today = DateFormatter.dateTimeToString(DateTime.now());
			
			File backupsDir = new File(Workspace.userWorkspaceFolder(), "backups");
			if(!backupsDir.exists()) {
				backupsDir.mkdirs();
			}
			File destFile = new File(backupsDir, project.getName() + "-" + today + ".zip");
			
			int fIdx = 1;
			while(destFile.exists()) {
				destFile = 
					new File(Workspace.userWorkspaceFolder(), 
							"backups" + File.separator + project.getName() + "-" + today + "(" + (fIdx++) + ").zip");
			}
			
			ProjectArchiveTask task = new ProjectArchiveTask(project, destFile, true, false);
			task.setName("Archiving: " + project.getName());
			task.addTaskListener(new PhonTaskListener() {
				
				@Override
				public void statusChanged(PhonTask task, TaskStatus oldStatus,
						TaskStatus newStatus) {
					if(newStatus == TaskStatus.FINISHED) {
						long curTime = System.currentTimeMillis();
						long totalTime = task.getStartTime() - curTime;
						
						if(totalTime < 500) {
							try {
								Thread.sleep(500 - totalTime);
							} catch (InterruptedException e) {}
						}
						
						// refresh
						refresh();
					} else if(newStatus == TaskStatus.ERROR) {
						// display dialog
						NativeDialogs.showMessageDialog(CommonModuleFrame.getCurrentFrame(), new NativeDialogListener() {
							@Override
							public void nativeDialogEvent(NativeDialogEvent event) {
							}
						}, null, "Error archiving project", "Reason: " + 
							(task.getException() != null ? task.getException().getMessage() : "no reason given"));
						
						refresh();
					}
				}
				
				@Override
				public void propertyChanged(PhonTask task, String property,
						Object oldValue, Object newValue) {
				}
			});
			
			PhonTaskButton newBtn = new PhonTaskButton(task);
			
			int idx = projectButtons.indexOf(btn);
			projectButtons.remove(idx);
			projectButtons.add(idx, newBtn);
			updateProjectList();
			
			PhonWorker.getInstance().invokeLater(task);
					
//			HashMap<String, Object> initInfo = new HashMap<String, Object>();
//			initInfo.put("project", project);
//			ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction("ca.phon.modules.project.ProjectArchiveController");
//			LoadModule lm = new LoadModule(mi, initInfo);
//			lm.start();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		} catch (ProjectConfigurationException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	public void onExtractProject(PhonActionEvent pae) {
		LocalProjectButton btn = (LocalProjectButton)pae.getData();
		
		ExtractProjectArchiveTask task = new ExtractProjectArchiveTask(btn.getProjectFile());
		File destDir = task.getDestDir();
		if(destDir == null) {
			NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null, 
					"Not a project archive", "'" + btn.getProjectFile() + "' does not contain a phon project");
			return;
		}
		task.setName("Extracting: " + btn.getProjectFile().getName());
		task.addTaskListener(new PhonTaskListener() {
			
			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus,
					TaskStatus newStatus) {
				if(newStatus == TaskStatus.FINISHED) {
					long curTime = System.currentTimeMillis();
					long totalTime = task.getStartTime() - curTime;
					
					if(totalTime < 500) {
						try {
							Thread.sleep(500 - totalTime);
						} catch (InterruptedException e) {}
					}
					
					// refresh
					refresh();
				} else if(newStatus == TaskStatus.ERROR) {
					// display dialog
					NativeDialogs.showMessageDialog(CommonModuleFrame.getCurrentFrame(), new NativeDialogListener() {
						@Override
						public void nativeDialogEvent(NativeDialogEvent event) {
						}
					}, null, "Error extracting project", "Reason: " + 
						(task.getException() != null ? task.getException().getMessage() : "no reason given"));
					
					refresh();
				}
			}
			
			@Override
			public void propertyChanged(PhonTask task, String property,
					Object oldValue, Object newValue) {
			}
		});
		
		PhonTaskButton newBtn = new PhonTaskButton(task);
		
		int idx = projectButtons.indexOf(btn);
		projectButtons.remove(idx);
		projectButtons.add(idx, newBtn);
		updateProjectList();
		
		PhonWorker.getInstance().invokeLater(task);
	}
	
	public void onOpenProject(PhonActionEvent pae) {
		LocalProjectButton btn = (LocalProjectButton)pae.getData();
		
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("ca.phon.modules.core.OpenProjectController.projectpath", btn.getProjectFile().getAbsolutePath());
		
		PluginEntryPointRunner.executePluginInBackground("OpenProject", initInfo);
		
//		ModuleInformation mi = 
//			ResourceLocator.getInstance().getModuleInformationByAction(
//					"ca.phon.modules.core.OpenProjectController");
//		LoadModule lm = new LoadModule(mi, initInfo, true);
//		lm.start();
	}

	public void onShowProject(PhonActionEvent pae) {
		LocalProjectButton btn = (LocalProjectButton)pae.getData();
		try {
			OpenFileLauncher.openURL(
					btn.getProjectFile().toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private class ButtonPanel extends JPanel implements Scrollable {

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return null;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle arg0, int arg1,
				int arg2) {
			return 10;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
			return 10;
		}
		
	}
}
