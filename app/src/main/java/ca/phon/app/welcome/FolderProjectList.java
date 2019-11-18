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
package ca.phon.app.welcome;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.DesktopProjectFactory;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.session.util.DateFormatter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

/**
 * List for displaying project in a given directory.
 * @author ghedlund
 *
 */
public class FolderProjectList extends JPanel {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(FolderProjectList.class.getName());
	
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
		
		final String defaultIconName = 
				(f.isFile() ? "actions/archive-extract" : "actions/document-open");
		ImageIcon icon = IconManager.getInstance().getSystemIconForPath(f.getAbsolutePath(), defaultIconName, IconSize.SMALL);
		ImageIcon iconL = IconManager.getInstance().getSystemIconForPath(f.getAbsolutePath(), defaultIconName, IconSize.MEDIUM);
		
		// if we have a file, we need to add the import action
		if(f.isFile()) {
			PhonUIAction extractAction = new PhonUIAction(this, "onExtractProject", retVal);
			
			extractAction.putValue(Action.NAME, "Extract project");
			extractAction.putValue(Action.SHORT_DESCRIPTION, "Extract: " +  f.getAbsolutePath());
			extractAction.putValue(Action.SMALL_ICON, icon);
			extractAction.putValue(Action.LARGE_ICON_KEY, iconL);
			retVal.setDefaultAction(extractAction);
			
			retVal.getTopLabel().setForeground(PhonGuiConstants.PHON_ORANGE);
		} else {
			PhonUIAction openAction = new PhonUIAction(this, "onOpenProject", retVal);
			
			openAction.putValue(Action.NAME, "Open project");
			openAction.putValue(Action.SHORT_DESCRIPTION, "Open: " + f.getAbsolutePath());
			openAction.putValue(Action.SMALL_ICON, icon);
			openAction.putValue(Action.LARGE_ICON_KEY, iconL);
			retVal.setDefaultAction(openAction);
			
			String fsIconName = "apps/system-file-manager";
			String fsName = "file system viewer";
			
			ImageIcon fsIcon = IconManager.getInstance().getIcon(fsIconName, IconSize.SMALL);
			ImageIcon fsIconL = IconManager.getInstance().getIcon(fsIconName, IconSize.MEDIUM);
			
			if(OSInfo.isWindows()) {
				fsName = "File Explorer";
				
				final String explorerPath = "C:\\Windows\\explorer.exe";
				ImageIcon explorerIcon = IconManager.getInstance().getSystemIconForPath(explorerPath, IconSize.SMALL);
				ImageIcon explorerIconL = IconManager.getInstance().getSystemIconForPath(explorerPath, IconSize.MEDIUM);
				
				if(explorerIcon != null)
					fsIcon = explorerIcon;
				if(explorerIconL != null)
					fsIconL = explorerIconL;
			} else if(OSInfo.isMacOs()) {
				fsName = "Finder";
				
				ImageIcon finderIcon = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.FinderIcon, IconSize.SMALL);
				ImageIcon finderIconL = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.FinderIcon, IconSize.MEDIUM);
				
				if(finderIcon != null)
					fsIcon = finderIcon;
				if(finderIconL != null)
					fsIconL = finderIconL;
			}
			
			PhonUIAction showAction = new PhonUIAction(this, "onShowProject", retVal);
			showAction.putValue(Action.NAME, "Show project");
			showAction.putValue(Action.SMALL_ICON, fsIcon);
			showAction.putValue(Action.LARGE_ICON_KEY, fsIconL);
			showAction.putValue(Action.SHORT_DESCRIPTION, "Show project in " + fsName);
			retVal.addAction(showAction);
			
			final String defaultArchiveIconName = "actions/archive-insert";
			ImageIcon archiveIcn = IconManager.getInstance().getSystemIconForFileType("zip", defaultArchiveIconName, IconSize.SMALL);
			ImageIcon archiveIcnL = IconManager.getInstance().getSystemIconForFileType("zip", defaultArchiveIconName, IconSize.MEDIUM);
			
			PhonUIAction archiveAction = new PhonUIAction(this, "onArchiveProject", retVal);
			archiveAction.putValue(Action.NAME, "Archive project");
			archiveAction.putValue(Action.SHORT_DESCRIPTION, "Create .zip archive of phon project...");
			archiveAction.putValue(Action.SMALL_ICON, archiveIcn);
			archiveAction.putValue(Action.LARGE_ICON_KEY, archiveIcnL);
			retVal.addAction(archiveAction);
		}
		retVal.getTopLabel().setIcon(iconL);
		
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
				Long o1Mod = new File(o1.getProjectFile(), "project.xml").lastModified();
				Long o2Mod = new File(o2.getProjectFile(), "project.xml").lastModified();
				
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
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray);
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
			
			JLabel refreshLabel = new JLabel("<html><u style='color: rgb(0, 90, 140);'>Refresh</u></html>");
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
		final ProjectFactory factory = new DesktopProjectFactory();
		try {
			final Project project = factory.openProject(btn.getProjectFile());
			final String today = DateFormatter.dateTimeToString(LocalDate.now());
			
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
			LOGGER.error(e.getMessage());
		} catch (ProjectConfigurationException e) {
			LOGGER.error(e.getMessage());
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
		
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_LOCATION, btn.getProjectFile().getAbsolutePath());
		
		PluginEntryPointRunner.executePluginInBackground("OpenProject", args);
	}

	public void onShowProject(PhonActionEvent pae) {
		LocalProjectButton btn = (LocalProjectButton)pae.getData();
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(btn.getProjectFile());
			} catch (IOException e) {
				LogUtil.warning(e);
				Toolkit.getDefaultToolkit().beep();
			}
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
			return 20;
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
