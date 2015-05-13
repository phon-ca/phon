/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MenuManager;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.workspace.Workspace;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class WorkspaceDialog extends CommonModuleFrame implements WindowListener {
	
	private static final long serialVersionUID = -5830638846526625648L;

	private final static Logger LOGGER = Logger.getLogger(WorkspaceDialog.class.getName());
	
	/** New project button */
	private MultiActionButton newProjectButton;
	
	private MultiActionButton createProjectButton;
	
	private MultiActionButton browseProjectButton;
	
	private MultiActionButton extractProjectButton;
	
	private MultiActionButton openPrefsButton;
	
	private MultiActionButton mediaPrefsButton;
	
	private WorkspaceProjectsPanel workspacePanel;
	
	private JXPanel workspaceActionsContainer;
	
	private JXPanel otherActionsContainer;
	
	public WorkspaceDialog() {
		super("Phon : Workspace");
		
		super.setWindowName("Workspace");
		
		init();
		super.addWindowListener(this);
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		JXPanel mainPanel = new JXPanel(new BorderLayout());
		mainPanel.setBackgroundPainter(new PanelBgPainter());
		
		DialogHeader header = new DialogHeader("Workspace", "Manage your project files.");
		mainPanel.add(header, BorderLayout.NORTH);
		
		newProjectButton = createNewButton();
		createProjectButton = createCreateButton();
		browseProjectButton = createBrowseButton();
		extractProjectButton = createExtractButton();
		openPrefsButton = createPrefsButton();
		mediaPrefsButton = createMediaButton();
		
		workspaceActionsContainer = new JXPanel(new FormLayout("fill:default:grow", "pref,pref"));
		CellConstraints cc = new CellConstraints();
		workspaceActionsContainer.add(newProjectButton, cc.xy(1, 1));
		workspaceActionsContainer.add(extractProjectButton, cc.xy(1,2));
		workspaceActionsContainer.setBackground(Color.white);
		workspaceActionsContainer.setOpaque(true);
		workspaceActionsContainer.setBackgroundPainter(new PanelBgPainter());

		otherActionsContainer = new JXPanel(new FormLayout("fill:default:grow", "pref,pref,pref"));
		otherActionsContainer.add(browseProjectButton, cc.xy(1,1));
		otherActionsContainer.add(openPrefsButton, cc.xy(1,3));
		otherActionsContainer.add(mediaPrefsButton, cc.xy(1,2));
		otherActionsContainer.setBackground(Color.white);
		otherActionsContainer.setOpaque(true);
		otherActionsContainer.setBackgroundPainter(new PanelBgPainter());
		
		JXTitledPanel actionPanel = new JXTitledPanel("Workspace Actions",workspaceActionsContainer);
		JXTitledPanel otherActionPanel = new JXTitledPanel("Other Actions", otherActionsContainer);
		
		// set a default size that does not change
//		Dimension prefSize = new Dimension(250, 0);
//		actionPanel.setPreferredSize(prefSize);
//		Dimension maxSize = new Dimension(250, Short.MAX_VALUE);
//		actionPanel.setMaximumSize(maxSize);
//		Dimension minSize = new Dimension(250, 40);
//		actionPanel.setMinimumSize(minSize);
		
//		actionPanel.setTitleFont(actionPanel.getTitleFont().deriveFont(Font.BOLD, 14.0f));
		
//		tabPanel = new TabbedPanel();
		workspacePanel = new WorkspaceProjectsPanel();
		
//		ShapedGradientTheme gradientTheme = new ShapedGradientTheme();
		
//		JTabbedPane tabbedPane = new JTabbedPane();
//		tabbedPane.addTab("Workspace", workspacePanel);
//		TitledTab workspaceTab = new TitledTab("Workspace", null, workspacePanel, null);
//		workspaceTab.getProperties().addSuperObject(gradientTheme.getTitledTabProperties());
////		workspaceTab.getProp
//		tabPanel.addTab(workspaceTab);
//		tabPanel.getProperties().
//		  addSuperObject(gradientTheme.getTabbedPanelProperties());
//		tabPanel.getProperties().setTabAreaOrientation(Direction.DOWN);
		
		JPanel leftPanel = new JPanel(new FormLayout("250", "pref,fill:pref:grow"));
		leftPanel.add(actionPanel, cc.xy(1,1));
		leftPanel.add(otherActionPanel, cc.xy(1,2));
		  
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(workspacePanel, BorderLayout.CENTER);
		
		add(mainPanel, BorderLayout.CENTER);
	}
	
	private MultiActionButton createNewButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon newIcn = IconManager.getInstance().getIcon("actions/folder_new", IconSize.SMALL);
		ImageIcon newIcnL = IconManager.getInstance().getIcon("actions/folder_new", IconSize.MEDIUM);
		
		String s1 = "Create Project";
		String s2 = "Create a new project in the workspace folder";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		retVal.setOpaque(false);
		
		PhonUIAction newAct = new PhonUIAction(this, "onNewProject");
		newAct.putValue(Action.LARGE_ICON_KEY, newIcnL);
		newAct.putValue(Action.SMALL_ICON, newIcn);
		newAct.putValue(Action.NAME, "New project");
		newAct.putValue(Action.SHORT_DESCRIPTION, "Create a new project in the workspace folder");
		retVal.setDefaultAction(newAct);
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return retVal;
	}
	
	private MultiActionButton createCreateButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon newIcn = IconManager.getInstance().getIcon("actions/folder_new", IconSize.SMALL);
		ImageIcon newIcnL = IconManager.getInstance().getIcon("actions/folder_new", IconSize.MEDIUM);
		
		String s1 = "Create Project";
		String s2 = "Enter project name and press enter.  Press escape to cancel.";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
		retVal.setAlwaysDisplayActions(true);
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.setOpaque(false);
		
		ImageIcon cancelIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		ImageIcon cancelIcnL = 
				new ImageIcon(cancelIcn.getImage().getScaledInstance(IconSize.MEDIUM.getWidth(), IconSize.MEDIUM.getHeight(), Image.SCALE_SMOOTH));
		
		PhonUIAction btnSwapAct = new PhonUIAction(this, "onSwapNewAndCreate", retVal);
		btnSwapAct.putValue(Action.ACTION_COMMAND_KEY, "CANCEL_CREATE_PROJECT");
		btnSwapAct.putValue(Action.NAME, "Cancel create project");
		btnSwapAct.putValue(Action.SHORT_DESCRIPTION, "Cancel create new project");
		btnSwapAct.putValue(Action.SMALL_ICON, cancelIcn);
		btnSwapAct.putValue(Action.LARGE_ICON_KEY, cancelIcnL);
		retVal.addAction(btnSwapAct);
		
		JPanel projectNamePanel = new JPanel(new BorderLayout());
		projectNamePanel.setOpaque(false);
		
		final JTextField projectNameField = new JTextField();
		projectNameField.setDocument(new ProjectNameDocument());
		projectNameField.setText("Project Name");
//		projectNameField.setColumns(10);
		projectNamePanel.add(projectNameField, BorderLayout.CENTER);
		
		ActionMap actionMap = retVal.getActionMap();
		actionMap.put(btnSwapAct.getValue(Action.ACTION_COMMAND_KEY), btnSwapAct);
		InputMap inputMap = retVal.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		
		inputMap.put(ks, btnSwapAct.getValue(Action.ACTION_COMMAND_KEY));
		
		retVal.setActionMap(actionMap);
		retVal.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
		
		PhonUIAction createNewProjectAct = 
			new PhonUIAction(this, "onCreateProject", projectNameField);
//		createNewProjectAct.putValue(PhonUIAction.NAME, "Create Project");
		createNewProjectAct.putValue(Action.SHORT_DESCRIPTION, "Create new project in workspace");
		createNewProjectAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));
		
		JButton createBtn = new JButton(createNewProjectAct);
		projectNamePanel.add(createBtn, BorderLayout.EAST);
		
		projectNameField.setAction(createNewProjectAct);
		
		// swap bottom component in new project button
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));
		retVal.add(projectNamePanel, BorderLayout.CENTER);
//		newProjectButton.revalidate();
		
		retVal.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				projectNameField.requestFocus();
			}
		});
		
		return retVal;
	}
	
	private MultiActionButton createBrowseButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon browseIcn = IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		ImageIcon browseIcnL = IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM);
		
		String s1 = "Browse for Project";
		String s2 = "Open a project located outside the workspace folder";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(browseIcn);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		
		PhonUIAction browseAct = new PhonUIAction(this, "onBrowse");
		browseAct.putValue(Action.SMALL_ICON, browseIcn);
		browseAct.putValue(PhonUIAction.LARGE_ICON_KEY, browseIcnL);
		browseAct.putValue(PhonUIAction.NAME, "Browse...");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for project folder on disk...");
		retVal.setOpaque(false);
		
		retVal.setDefaultAction(browseAct);
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return retVal;
	}
	
	private MultiActionButton createExtractButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon importIcn = IconManager.getInstance().getIcon("actions/archive-extract", IconSize.SMALL);
		ImageIcon importIcnL = IconManager.getInstance().getIcon("actions/archive-extract", IconSize.MEDIUM);
		
		String s1 = "Extract Project";
		String s2 = "Extract project from .phon/.zip archive into the workspace folder";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(importIcn);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		
		PhonUIAction extractAct = new PhonUIAction(this, "onExtract");
		extractAct.putValue(PhonUIAction.SMALL_ICON, importIcn);
		extractAct.putValue(PhonUIAction.LARGE_ICON_KEY, importIcnL);
		extractAct.putValue(PhonUIAction.NAME, "Extract project...");
		extractAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Extract project from .zip archive into current workspace...");
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		retVal.setDefaultAction(extractAct);
		retVal.setOpaque(false);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return retVal;
	}
	
	private MultiActionButton createPrefsButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon prefsIcn = IconManager.getInstance().getIcon("categories/preferences", IconSize.SMALL);
		ImageIcon prefsIcnL = IconManager.getInstance().getIcon("categories/preferences", IconSize.MEDIUM);
		
		String s1 = "Edit Preferences";
		String s2 = "Modify application settings";
		
		retVal.setTopLabelText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(prefsIcn);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));
		
		PhonUIAction prefsAct = new PhonUIAction(this, "onOpenPrefs");
		prefsAct.putValue(PhonUIAction.NAME, "Open preferences...");
		prefsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Modify application settings...");
		prefsAct.putValue(PhonUIAction.SMALL_ICON, prefsIcn);
		prefsAct.putValue(PhonUIAction.LARGE_ICON_KEY, prefsIcnL);
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		retVal.setDefaultAction(prefsAct);
		retVal.setOpaque(false);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return retVal;
	}
	
	private MultiActionButton createMediaButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon videoFolderIcn = IconManager.getInstance().getIcon("places/folder-video", IconSize.SMALL);
		ImageIcon videoFolderIcnL = IconManager.getInstance().getIcon("places/folder-video", IconSize.MEDIUM);
		
		String s1 = "Select Media Folders";
		String s2 = "Set up a list of folders where media can be found";
		
		retVal.setTopLabelText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(videoFolderIcn);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));
		
		PhonUIAction prefsAct = new PhonUIAction(this, "onOpenPrefs", "Media");
		prefsAct.putValue(PhonUIAction.NAME, "Select media folders...");
		prefsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Choose where Phon looks for media files...");
		prefsAct.putValue(PhonUIAction.SMALL_ICON, videoFolderIcn);
		prefsAct.putValue(PhonUIAction.LARGE_ICON_KEY, videoFolderIcnL);
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		retVal.setOpaque(false);
		retVal.setDefaultAction(prefsAct);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return retVal;
	}
	
	/*
	 * UI Actions
	 */
	public void onNewProject(PhonActionEvent pae) {
		pae.setData(newProjectButton);
		onSwapNewAndCreate(pae);
	}
	
	public void onCreateProject(PhonActionEvent pae) {
		JTextField textField = (JTextField)pae.getData();
		
		String projectName = textField.getText();
		if(projectName.length() == 0) return;
		
		File projectFile = new File(Workspace.userWorkspaceFolder(), projectName);
		if(projectFile.exists()) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setHeader("Unable to create project");
			props.setMessage("Folder already exsists " + projectFile.getAbsolutePath());
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setRunAsync(false);
			NativeDialogs.showDialog(props);
			return;
		}
		
		// create a new project at the given location
		try {
			final ProjectFactory factory = new ProjectFactory();
			Project newProject = factory.createProject(projectFile);
			newProject.setName(projectName);
			
			// refresh project list
			workspacePanel.refresh();
			
			// open project window
			ProjectWindow pw = new ProjectWindow(newProject, "");
			pw.pack();
			pw.setVisible(true);
			
			// swap buttons
			pae.setData(createProjectButton);
			onSwapNewAndCreate(pae);
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setHeader("Unable to create project");
			props.setMessage(e.getMessage());
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setRunAsync(false);
			NativeDialogs.showDialog(props);
			return;
		}
		
	}
	
	public void onSwapNewAndCreate(PhonActionEvent pae) {
		boolean swapNew = (pae.getData() == newProjectButton);
		CellConstraints cc = new CellConstraints();
		if(swapNew) {
			workspaceActionsContainer.remove(newProjectButton);
			workspaceActionsContainer.add(createProjectButton, cc.xy(1,1));
			workspaceActionsContainer.revalidate();
			createProjectButton.repaint();
			createProjectButton.requestFocus();
		} else {
			workspaceActionsContainer.remove(createProjectButton);
			workspaceActionsContainer.add(newProjectButton, cc.xy(1,1));
			workspaceActionsContainer.revalidate();
			newProjectButton.repaint();
		}
	}
	
	private final NativeDialogListener browseListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			if(event.getDialogResult() == NativeDialogEvent.OK_OPTION) {
				final String selectedDir = event.getDialogData().toString();
				File selectedFile = new File(selectedDir);
				File projectFile = new File(selectedFile, "project.xml");
				if(!projectFile.exists()) {
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(CommonModuleFrame.getCurrentFrame());
					props.setHeader("Unable to open project");
					props.setMessage(selectedDir + File.separator + "project.xml not found");
					NativeDialogs.showDialog(props);
					
					LOGGER.severe("Failed to open project: '" + selectedDir + "'  Reason: project.xml not found.  Not a project.");
					return;
				}
				HashMap<String, Object> initInfo = new HashMap<String, Object>();
				initInfo.put("ca.phon.modules.core.OpenProjectController.projectpath", selectedDir);
				
				PluginEntryPointRunner.executePluginInBackground("OpenProject", initInfo);
			}
		}
	};
	public void onBrowse(PhonActionEvent pae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setListener(browseListener);
		props.setTitle("Browse for Project");
		props.setCanCreateDirectories(true);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		NativeDialogs.showDialog(props);
	}
	
	public void onExtract(PhonActionEvent pae) {
		FileFilter archiveFilter = FileFilter.zipFilter;
		FileFilter[] filters = new FileFilter[] { archiveFilter };
		String selectedFile = 
			NativeDialogs.browseForFileBlocking(CommonModuleFrame.getCurrentFrame(), null, ".phon", 
					filters, "Open project archive");
		if(selectedFile != null) {
			ExtractProjectArchiveTask task = new ExtractProjectArchiveTask(new File(selectedFile));
			File destDir = task.getDestDir();
			if(destDir == null) {
				NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null, 
						"Not a project archive", "'" + selectedFile + "' does not contain a phon project");
				return;
			}
			
			final PhonTaskButton taskBtn = new PhonTaskButton(task);
			taskBtn.setSize(extractProjectButton.getSize());
			taskBtn.setPreferredSize(extractProjectButton.getPreferredSize());
			CellConstraints cc = new CellConstraints();
			workspaceActionsContainer.remove(extractProjectButton);
			workspaceActionsContainer.add(taskBtn, cc.xy(1,2));
			workspaceActionsContainer.revalidate();
			workspaceActionsContainer.repaint();
			
			task.addTaskListener(new PhonTaskListener() {
				
				@Override
				public void statusChanged(PhonTask task, TaskStatus oldStatus,
						TaskStatus newStatus) {
					if(newStatus == TaskStatus.FINISHED) {

						
						long startTime = task.getStartTime();
						long curTime = System.currentTimeMillis();
						long totalTime = curTime - startTime;
						
						if(totalTime < 500) {
							try {
								Thread.sleep(500-totalTime);
							} catch (InterruptedException e) {}
						}
						
						// swap buttons back
						final Runnable onEDT = new Runnable() {
							public void run() {
								workspacePanel.refresh();
								CellConstraints cc = new CellConstraints();
								workspaceActionsContainer.remove(taskBtn);
								workspaceActionsContainer.add(extractProjectButton, cc.xy(1,2));
								workspaceActionsContainer.revalidate();
								extractProjectButton.repaint();
							}
						};
						SwingUtilities.invokeLater(onEDT);
					}
				}
				
				@Override
				public void propertyChanged(PhonTask task, String property,
						Object oldValue, Object newValue) {
					// TODO Auto-generated method stub
					
				}
			});
			
			PhonWorker.getInstance().invokeLater(task);
		}
		
		
		
	}
	
	public void onOpenPrefs(PhonActionEvent pae) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		
		if(pae.getData() != null) {
			initInfo.put("prefpanel", pae.getData().toString());
		}
		
		PluginEntryPointRunner.executePluginInBackground("Preferences", initInfo);
	}
	
	private class BtnBgPainter extends MouseInputAdapter implements Painter<MultiActionButton> {
		
		private Color selectedColor = new Color(0, 100, 200, 100);
		
		private boolean useSelected = false;

		@Override
		public void paint(Graphics2D g, MultiActionButton obj, int width,
				int height) {
			
			if(obj.isOpaque()) {
				GradientPaint gp = new GradientPaint(new Point(0,0), Color.white, 
						new Point(width, height), PhonGuiConstants.PHON_UI_STRIP_COLOR);
				MattePainter gpPainter = new MattePainter(gp);
	
				gpPainter.paint(g, obj, width, height);
			}
			
			if(useSelected) {
				GlowPathEffect effect = new GlowPathEffect();
				effect.setRenderInsideShape(true);
				effect.setBrushColor(selectedColor);
				
				// get rectangle
				Rectangle2D.Double boundRect = 
					new Rectangle2D.Double(0.0f, 0.0f, (double)width, (double)height);
				
				effect.apply(g, boundRect, 0, 0);
			}
			
		}
		
		@Override
		public void mouseEntered(MouseEvent me) {
			useSelected = true;
		}
		
		@Override
		public void mouseExited(MouseEvent me) {
			useSelected = false;
		}
		
	}
	
	private class PanelBgPainter implements Painter<JXPanel> {
		
		@Override
		public void paint(Graphics2D arg0, JXPanel arg1, int arg2, int arg3) {

			
			GradientPaint gp = new GradientPaint(new Point(0,0), Color.white, 
					new Point(arg2, arg3), UIManager.getColor("control"));
			MattePainter gpPainter = new MattePainter(gp);
			
			gpPainter.paint(arg0, arg1, arg2, arg3);
		}
		
	}
	
	private class ProjectNameDocument extends PlainDocument {
		
		/**
		 * Ensure proper project names.
		 * 
		 * Project name must start with a letter, and can be followed
		 * by at most 30 letters, numbers, underscores, dashes.
		 */
		private String projectRegex = "[a-zA-Z0-9][- a-zA-Z_0-9]{0,29}";

		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			// only allow valid inputs
			String p1 = super.getText(0, offs);
			String p2 = super.getText(offs, getLength()-offs);
			String val = p1 + str + p2;
			
			if(val.matches(projectRegex)) {
				super.insertString(offs, str, a);
			}
		}
		
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// if we are the only window open exit the application
		if(CommonModuleFrame.getOpenWindows().size() == 0)
		{
			try {
				PluginEntryPointRunner.executePlugin("Exit");
			} catch (PluginException e1) {
				LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				System.exit(1);
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}
