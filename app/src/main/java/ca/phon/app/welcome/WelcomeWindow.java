package ca.phon.app.welcome;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.app.VersionInfo;
import ca.phon.app.menu.edit.PreferencesCommand;
import ca.phon.app.menu.file.NewProjectCommand;
import ca.phon.app.menu.file.OpenProjectCommand;
import ca.phon.app.project.RecentProjectHistory;
import ca.phon.app.project.RecentProjectsList;
import ca.phon.app.workspace.WorkspaceProjectsPanel;
import ca.phon.app.workspace.WorkspaceTextStyler;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Entry window for the application.  This window provides access to
 * common project actions - create, browse, extract, preferences,
 * recent projects, and workspace setup.
 * 
 */
public class WelcomeWindow extends CommonModuleFrame implements WindowListener {
	
	private final static Logger LOGGER = Logger.getLogger(WelcomeWindow.class.getName());

	private static final long serialVersionUID = -8877301049044343272L;
	
	private DialogHeader header;
	
	private final static String HEADER_TITLE = "Welcome to Phon " + VersionInfo.getInstance().getShortVersion();
	private final static String HEADER_MESSAGE = "To begin, create a project or select a project from the workspace or recent projects.";
	
	// action panel
	private JXTitledPanel actionsContainer;
	private JXCollapsiblePane actionsPanel;
	
	// action buttons
	private MultiActionButton newProjectButton;
	private MultiActionButton browseProjectButton;
	private MultiActionButton openPrefsButton;
	private MultiActionButton mediaPrefsButton;
	
	// recent projects
	private JXTitledPanel recentProjectsContainer;
	private JXCollapsiblePane recentProjectsPanel;
	private RecentProjectsList recentProjectsList;
	
	// workspace projects
	private JXTitledPanel workspaceContainer;
	private WorkspaceProjectsPanel workspaceProjectsPanel;
	
	public WelcomeWindow() {
		super();
		
		setWindowName("Welcome");
		addWindowListener(this);
		init();
	}
	
	private void init() {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		setLayout(layout);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		header = new DialogHeader(HEADER_TITLE, HEADER_MESSAGE);
		add(header, gbc);
		
		// setup actions
		actionsPanel = new JXCollapsiblePane(Direction.DOWN);

		newProjectButton = createNewButton();
		browseProjectButton = createBrowseButton();
		openPrefsButton = createPrefsButton();
		mediaPrefsButton = createMediaButton();
		
		actionsPanel.setLayout(new VerticalLayout());
		actionsPanel.add(newProjectButton);
		actionsPanel.add(browseProjectButton);
		actionsPanel.add(new JXTitledSeparator("Settings"));
		actionsPanel.add(openPrefsButton);
		actionsPanel.add(mediaPrefsButton);
		
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
		
		actionsContainer = new JXTitledPanel("Actions", actionsPanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 2;
		gbc.gridwidth = 1;
		add(actionsContainer, gbc);
		
		recentProjectsPanel = new JXCollapsiblePane(Direction.DOWN);
		recentProjectsPanel.setLayout(new BorderLayout());
		recentProjectsList = new RecentProjectsList();
		final JScrollPane recentProjectsScroller = new JScrollPane(recentProjectsList);
		recentProjectsPanel.add(recentProjectsScroller, BorderLayout.CENTER);
		recentProjectsContainer = new JXTitledPanel("Recent Projects", recentProjectsPanel);
		gbc.gridx++;
		gbc.gridheight = 1;
		add(recentProjectsContainer, gbc);
		
		PrefHelper.getUserPreferences().addPreferenceChangeListener( (p) -> {
			if(p.getKey().equals(RecentProjectHistory.PROJECT_HISTORY_PROP)) {
				SwingUtilities.invokeLater( () -> recentProjectsList.updateProjectList() );
			}
		});
		
		workspaceProjectsPanel = new WorkspaceProjectsPanel();
		workspaceContainer = new JXTitledPanel("Workspace", workspaceProjectsPanel);
		
		gbc.gridy++;
		add(workspaceContainer, gbc);
	}
	
	private MultiActionButton createNewButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon newIcn = IconManager.getInstance().getIcon("actions/folder_new", IconSize.SMALL);
		ImageIcon newIcnL = IconManager.getInstance().getIcon("actions/folder_new", IconSize.MEDIUM);
		
		String s1 = "Create Project";
		String s2 = "Create a new project...";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setIcon(newIcn);
		retVal.getBottomLabel().setText(WorkspaceTextStyler.toDescText(s2));
		retVal.setOpaque(false);
		
		final NewProjectCommand newProjectCmd = new NewProjectCommand();
		newProjectCmd.putValue(NewProjectCommand.SMALL_ICON, newIcn);
		newProjectCmd.putValue(NewProjectCommand.LARGE_ICON_KEY, newIcnL);
		retVal.setDefaultAction(newProjectCmd);
		
		BtnBgPainter bgPainter = new BtnBgPainter();
		retVal.setBackgroundPainter(bgPainter);
		retVal.addMouseListener(bgPainter);
		
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return retVal;
	}
	
	private MultiActionButton createBrowseButton() {
		MultiActionButton retVal = new MultiActionButton();
		
		ImageIcon browseIcn = IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		ImageIcon browseIcnL = IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM);
		
		String s1 = "Browse for Project";
		String s2 = "Browse for project folder on disk...";
		
		retVal.getTopLabel().setText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(browseIcn);
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
		
		ImageIcon prefsIcn = IconManager.getInstance().getIcon("categories/preferences", IconSize.SMALL);
		ImageIcon prefsIcnL = IconManager.getInstance().getIcon("categories/preferences", IconSize.MEDIUM);
		
		String s1 = "Edit Preferences";
		String s2 = "Modify application settings";
		
		retVal.setTopLabelText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(prefsIcn);
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
		
		ImageIcon videoFolderIcn = IconManager.getInstance().getIcon("places/folder-video", IconSize.SMALL);
		ImageIcon videoFolderIcnL = IconManager.getInstance().getIcon("places/folder-video", IconSize.MEDIUM);
		
		String s1 = "Select Media Folders";
		String s2 = "Set up a list of folders where media can be found";
		
		retVal.setTopLabelText(WorkspaceTextStyler.toHeaderText(s1));
		retVal.getTopLabel().setIcon(videoFolderIcn);
		retVal.getTopLabel().setFont(FontPreferences.getTitleFont());
		retVal.getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		retVal.setBottomLabelText(WorkspaceTextStyler.toDescText(s2));
		
		final PreferencesCommand prefsAct = new PreferencesCommand("Media");
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

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
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
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}
}
