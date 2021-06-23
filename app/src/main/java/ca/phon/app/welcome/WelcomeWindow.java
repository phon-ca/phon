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
import ca.hedlund.desktopicons.NativeUtilities;
import ca.hedlund.desktopicons.StockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.VersionInfo;
import ca.phon.app.menu.edit.PreferencesCommand;
import ca.phon.app.menu.file.NewProjectCommand;
import ca.phon.app.menu.file.OpenProjectCommand;
import ca.phon.app.project.RecentProjects;
import ca.phon.app.project.RecentProjectsList;
import ca.phon.app.workspace.Workspace;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * Entry window for the application.  This window provides access to
 * common project actions - create, browse, extract, preferences,
 * recent projects, and workspace setup.
 *
 */
public class WelcomeWindow extends CommonModuleFrame implements IExtendable {

	public final static String WORKSPACE_PROJECTS_COLLAPSED = WelcomeWindow.class.getName() + ".workspaceProjectCollapsed";
	public final static boolean DEFAULT_COLLAPSE_WORKSPACE_PROJECTS =
			(!Workspace.userWorkspaceFolder().exists() || Workspace.userWorkspace().getProjects().size() == 0);
	private boolean showWorkspaceProjects = PrefHelper.getBoolean(WORKSPACE_PROJECTS_COLLAPSED, DEFAULT_COLLAPSE_WORKSPACE_PROJECTS);

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(WelcomeWindow.class.getName());

	private static final long serialVersionUID = -8877301049044343272L;

	private DialogHeader header;

	private final static String HEADER_TITLE = "Welcome to Phon " + VersionInfo.getInstance().getVersionNoBuild();
	private final static String HEADER_MESSAGE = "";

	// action panel
	private TitledPanel actionsContainer;
	private JPanel actionsPanel;

	// action buttons
	private MultiActionButton newProjectButton;
	private MultiActionButton browseProjectButton;
	private MultiActionButton openPrefsButton;
	private MultiActionButton mediaPrefsButton;

	// recent projects
	private TitledPanel recentProjectsContainer;
	private JPanel recentProjectsPanel;
	private RecentProjectsList recentProjectsList;

	// workspace projects
	private TitledPanel workspaceContainer;
	private WorkspaceProjectsPanel workspaceProjectsPanel;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(WelcomeWindow.class, this);

	public WelcomeWindow() {
		super();

		setWindowName("Welcome");
		init();
		
		extSupport.initExtensions();
	}

	private void init() {
		setLayout(new BorderLayout());

		header = new DialogHeader(HEADER_TITLE, HEADER_MESSAGE);
		add(header, BorderLayout.NORTH);

		// setup actions
		actionsPanel = new JPanel();

		newProjectButton = createNewButton();
		browseProjectButton = createBrowseButton();
		openPrefsButton = createPrefsButton();
		mediaPrefsButton = createMediaButton();

		actionsPanel.setLayout(new VerticalLayout());
		actionsPanel.setPreferredSize(new Dimension(250, 0));
		actionsPanel.add(newProjectButton);
		actionsPanel.add(browseProjectButton);
		actionsPanel.add(openPrefsButton);

		// add plug-in actions
		int pluginActions = 0;
		for(IPluginExtensionPoint<WelcomeWindowAction> extPt:PluginManager.getInstance().getExtensionPoints(WelcomeWindowAction.class)) {
			final IPluginExtensionFactory<WelcomeWindowAction> factory = extPt.getFactory();
			final WelcomeWindowAction actionExtension = factory.createObject();
			final MultiActionButton btn = actionExtension.createButton(this);
			if(pluginActions++ == 0) {
				actionsPanel.add(new JXTitledSeparator("Other"));
			}
			actionsPanel.add(btn);
		}

		actionsContainer = new TitledPanel("Actions", actionsPanel);
		add(actionsContainer, BorderLayout.WEST);
		
		if(OSInfo.isMacOs()) {
			final ImageIcon actionsIcn = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.ToolbarUtilitiesFolderIcon, IconSize.SMALL);
			actionsContainer.setLeftDecoration(new JLabel(actionsIcn));
		}

		workspaceProjectsPanel = new WorkspaceProjectsPanel();
		final JXCollapsiblePane cpane = new JXCollapsiblePane(JXCollapsiblePane.Direction.UP);
		cpane.setLayout(new BorderLayout());
		cpane.add(workspaceProjectsPanel, BorderLayout.CENTER);
		cpane.setCollapsed(PrefHelper.getBoolean(WORKSPACE_PROJECTS_COLLAPSED, DEFAULT_COLLAPSE_WORKSPACE_PROJECTS));
		cpane.addPropertyChangeListener("collapsed", (e) -> {
			updateWorkspaceDecoration(cpane);
			PrefHelper.getUserPreferences().putBoolean(WORKSPACE_PROJECTS_COLLAPSED, cpane.isCollapsed());
		});
		workspaceContainer = new TitledPanel("Workspace", cpane);
		workspaceProjectsPanel.setPreferredSize(new Dimension(0, 300));
		workspaceContainer.getTitleLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		workspaceContainer.getTitleLabel().setToolTipText("Click to collapse/show workspace projects");
		workspaceContainer.getTitleLabel().addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				cpane.setCollapsed(!cpane.isCollapsed());
			}
		});
		updateWorkspaceDecoration(cpane);

		recentProjectsPanel = new JPanel();
		recentProjectsPanel.setLayout(new BorderLayout());
		recentProjectsList = new RecentProjectsList();
		final JScrollPane recentProjectsScroller = new JScrollPane(recentProjectsList);
		recentProjectsPanel.add(recentProjectsScroller, BorderLayout.CENTER);
		recentProjectsContainer = new TitledPanel("Recent Projects", recentProjectsPanel);
		
		if(OSInfo.isMacOs()) {
			final ImageIcon recentsIcn = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.ToolbarSitesFolderIcon, IconSize.SMALL);
			recentProjectsContainer.setLeftDecoration(new JLabel(recentsIcn));
		}

		PrefHelper.getUserPreferences().addPreferenceChangeListener( (p) -> {
			if(p.getKey().equals(RecentProjects.PROJECT_HISTORY_PROP)) {
				SwingUtilities.invokeLater( () -> recentProjectsList.updateProjectList() );
			}
		});

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(workspaceContainer, BorderLayout.NORTH);
		rightPanel.add(recentProjectsContainer, BorderLayout.CENTER);

		add(rightPanel, BorderLayout.CENTER);
	}

	private void updateWorkspaceDecoration(JXCollapsiblePane cpane) {
		Icon icn =
				(cpane.isCollapsed() ? UIManager.getIcon("Tree.collapsedIcon") : UIManager.getIcon("Tree.expandedIcon"));

		if(OSInfo.isMacOs()) {
			final ImageIcon workspaceIcn = IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.ToolbarDocumentsFolderIcon, IconSize.SMALL);
			icn = IconManager.getInstance().createIconStrip(new Icon[]{workspaceIcn, icn});
		}

		workspaceContainer.setLeftDecoration(new JLabel(icn));
	}

	private MultiActionButton createNewButton() {
		MultiActionButton retVal = new MultiActionButton();

		final String folderIconName = "actions/folder_new";
		final StockIcon stockIcon =
				(NativeUtilities.isMacOs() ? MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER);
		final ImageIcon folderIcon =
				IconManager.getInstance().getSystemStockIcon(stockIcon, folderIconName, IconSize.MEDIUM);
		final ImageIcon addIcon =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);

		final BufferedImage newIcnImg =
				new BufferedImage(IconSize.MEDIUM.getHeight(), IconSize.MEDIUM.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
		final Graphics g = newIcnImg.createGraphics();
		folderIcon.paintIcon(null, g, 0, 0);
		g.drawImage(addIcon.getImage(), IconSize.MEDIUM.getWidth() - IconSize.XSMALL.getWidth(),
				IconSize.MEDIUM.getHeight() - IconSize.XSMALL.getHeight(), this);
		final ImageIcon newIcn = new ImageIcon(newIcnImg);

		String s1 = "Create Project";
		String s2 = "Create a new project";

		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		retVal.setOpaque(false);

		final NewProjectCommand newProjectCmd = new NewProjectCommand();
		newProjectCmd.putValue(NewProjectCommand.SMALL_ICON, newIcn);
		newProjectCmd.putValue(NewProjectCommand.LARGE_ICON_KEY, newIcn);
		retVal.setDefaultAction(newProjectCmd);

		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);

		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		return retVal;
	}

	private MultiActionButton createBrowseButton() {
		MultiActionButton retVal = new MultiActionButton();

		final String defaultFolderIconName = "actions/document-open";
		final StockIcon stockIcon =
				(NativeUtilities.isMacOs() ? MacOSStockIcon.OpenFolderIcon :
					NativeUtilities.isWindows() ? WindowsStockIcon.FOLDEROPEN : null);

		final ImageIcon browseIcn =
				IconManager.getInstance().getSystemStockIcon(stockIcon, defaultFolderIconName, IconSize.SMALL);
		final ImageIcon browseIcnL =
				IconManager.getInstance().getSystemStockIcon(stockIcon, defaultFolderIconName, IconSize.MEDIUM);

		String s1 = "Browse for Project";
		String s2 = "Browse for project folder on disk";

		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(browseIcnL);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));

		final OpenProjectCommand browseAct = new OpenProjectCommand();
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

	private MultiActionButton createPrefsButton() {
		MultiActionButton retVal = new MultiActionButton();

		final StockIcon prefIcon =
				OSInfo.isMacOs() ? MacOSStockIcon.ToolbarCustomizeIcon
						: OSInfo.isWindows() ?  WindowsStockIcon.APPLICATION : null;
		final String defIcn = "categories/preferences";
		ImageIcon prefsIcn = IconManager.getInstance().getSystemStockIcon(prefIcon, defIcn, IconSize.SMALL);
		ImageIcon prefsIcnL = IconManager.getInstance().getSystemStockIcon(prefIcon, defIcn, IconSize.MEDIUM);

		String s1 = "Edit Preferences";
		String s2 = "Modify application settings";

		retVal.setTopLabelText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(prefsIcnL);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));

		final PreferencesCommand prefsAct = new PreferencesCommand();
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

		final String folderIconName = "places/folder-video";
		final StockIcon stockIcon =
				(NativeUtilities.isMacOs() ? MacOSStockIcon.VoicesFolderIcon
						: NativeUtilities.isWindows() ? WindowsStockIcon.VIDEOFILES : null);
		final ImageIcon folderIcon =
				IconManager.getInstance().getSystemStockIcon(stockIcon, folderIconName, IconSize.MEDIUM);
		final ImageIcon addIcon =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);

		final BufferedImage newIcnImg =
				new BufferedImage(IconSize.MEDIUM.getHeight(), IconSize.MEDIUM.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
		final Graphics g = newIcnImg.createGraphics();
		folderIcon.paintIcon(null, g, 0, 0);
		g.drawImage(addIcon.getImage(), IconSize.MEDIUM.getWidth() - IconSize.XSMALL.getWidth(),
				IconSize.MEDIUM.getHeight() - IconSize.XSMALL.getHeight(), this);
		final ImageIcon newIcn = new ImageIcon(newIcnImg);

		String s1 = "Select Media Folders";
		String s2 = "Set up a list of folders where media can be found";

		retVal.setTopLabelText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(newIcn);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));

		final PreferencesCommand prefsAct = new PreferencesCommand("Media");
		prefsAct.putValue(PhonUIAction.NAME, "Select media folders...");
		prefsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Choose where Phon looks for media files...");
		prefsAct.putValue(PhonUIAction.SMALL_ICON, newIcn);
		prefsAct.putValue(PhonUIAction.LARGE_ICON_KEY, newIcn);

		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		retVal.setOpaque(false);
		retVal.setDefaultAction(prefsAct);

		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return retVal;
	}

	public static class BtnBgPainter extends MouseInputAdapter implements Painter<MultiActionButton> {

		private Color selectedColor = new Color(0, 100, 200, 100);

		private boolean useSelected = false;
		
		@Override
		public void paint(Graphics2D g, MultiActionButton obj, int width,
				int height) {
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

	public void refreshWorkspaceProjects() {
		workspaceProjectsPanel.refresh();
	}

	public Container getActionList() {
		return actionsPanel;
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
	
}
