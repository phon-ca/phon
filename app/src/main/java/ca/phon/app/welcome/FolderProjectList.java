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
package ca.phon.app.welcome;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.DesktopProjectFactory;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.*;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.session.format.DateFormatter;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;
import com.jgoodies.forms.layout.*;
import org.jdesktop.swingx.*;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

/**
 * List for displaying project in a given directory.
 * @author ghedlund
 *
 */
public class FolderProjectList extends JPanel {
	
	private enum SortBy {
		NAME,
		MOD_DATE;

		private String[] displayNames = {
				"Name", "Modified"
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
		
		listPanel = new ButtonPanel();
		BoxLayout bl = new BoxLayout(listPanel, BoxLayout.Y_AXIS);
		listPanel.setLayout(bl);
		
		listScroller = new JScrollPane(listPanel);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScroller.getViewport().setBackground(Color.white);
		
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

		PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);
		
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

		worker.start();
		revalidate();
		listPanel.revalidate();
		listPanel.repaint();
	}
	
	private void scanDirectory() {
		projectButtons.clear();
		if(projectFolder == null || projectFolder.listFiles() == null) return;
		for(File f:projectFolder.listFiles()) {
			if(f.isDirectory()
					&& !f.isHidden()
					&& !f.getName().startsWith("~")
					&& !f.getName().endsWith("~")
					&& !f.getName().startsWith("__")
					&& !f.getName().equals("backups")) {
				// check for a project.xml file
				projectButtons.add(getProjectButton(f));
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
		
		PhonUIAction<LocalProjectButton> openAction = PhonUIAction.eventConsumer(this::onOpenProject, retVal);
		
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
		
		PhonUIAction<LocalProjectButton> showAction = PhonUIAction.eventConsumer(this::onShowProject, retVal);
		showAction.putValue(Action.NAME, "Show project");
		showAction.putValue(Action.SMALL_ICON, fsIcon);
		showAction.putValue(Action.LARGE_ICON_KEY, fsIconL);
		showAction.putValue(Action.SHORT_DESCRIPTION, "Show project in " + fsName);
		retVal.addAction(showAction);
		
		final String defaultArchiveIconName = "actions/archive-insert";
		ImageIcon archiveIcn = IconManager.getInstance().getSystemIconForFileType("zip", defaultArchiveIconName, IconSize.SMALL);
		ImageIcon archiveIcnL = IconManager.getInstance().getSystemIconForFileType("zip", defaultArchiveIconName, IconSize.MEDIUM);
		
		PhonUIAction<LocalProjectButton> archiveAction = PhonUIAction.eventConsumer(this::onArchiveProject, retVal);
		archiveAction.putValue(Action.NAME, "Archive project");
		archiveAction.putValue(Action.SHORT_DESCRIPTION, "Create .zip archive of phon project...");
		archiveAction.putValue(Action.SMALL_ICON, archiveIcn);
		archiveAction.putValue(Action.LARGE_ICON_KEY, archiveIcnL);
		retVal.addAction(archiveAction);
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
			
			ImageIcon icn = IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
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
	public void onArchiveProject(PhonActionEvent<LocalProjectButton> pae) {
		final LocalProjectButton btn = pae.getData();
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
		} catch (IOException | ProjectConfigurationException e) {
			LogUtil.warning(e);
		}
	}
	
	public void onOpenProject(PhonActionEvent<LocalProjectButton> pae) {
		LocalProjectButton btn = pae.getData();
		
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_LOCATION, btn.getProjectFile().getAbsolutePath());
		
		PluginEntryPointRunner.executePluginInBackground("OpenProject", args);
	}

	public void onShowProject(PhonActionEvent<LocalProjectButton> pae) {
		LocalProjectButton btn = pae.getData();
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
