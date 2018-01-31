/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.welcome;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.hedlund.desktopicons.*;
import ca.phon.app.VersionInfo;
import ca.phon.app.menu.edit.PreferencesCommand;
import ca.phon.app.menu.file.*;
import ca.phon.app.project.*;
import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Entry window for the application.  This window provides access to
 * common project actions - create, browse, extract, preferences,
 * recent projects, and workspace setup.
 *
 */
public class WelcomeWindow extends CommonModuleFrame {

	private final static Logger LOGGER = Logger.getLogger(WelcomeWindow.class.getName());

	private static final long serialVersionUID = -8877301049044343272L;

	private DialogHeader header;

	private final static String HEADER_TITLE = "Welcome to Phon " + VersionInfo.getInstance().getShortVersion();
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

	public WelcomeWindow() {
		super();

		setWindowName("Welcome");
		addWindowListener(windowListener);
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
		actionsPanel = new JPanel();

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

		actionsContainer = new TitledPanel("Actions", actionsPanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 2;
		gbc.gridwidth = 1;
		add(actionsContainer, gbc);

		workspaceProjectsPanel = new WorkspaceProjectsPanel();
		workspaceContainer = new TitledPanel("Workspace", workspaceProjectsPanel);
		gbc.gridx++;
		gbc.gridheight = 1;
		add(workspaceContainer, gbc);

		recentProjectsPanel = new JPanel();
		recentProjectsPanel.setLayout(new BorderLayout());
		recentProjectsList = new RecentProjectsList();
		final JScrollPane recentProjectsScroller = new JScrollPane(recentProjectsList);
		recentProjectsPanel.add(recentProjectsScroller, BorderLayout.CENTER);
		recentProjectsContainer = new TitledPanel("Recent Projects", recentProjectsPanel);

		PrefHelper.getUserPreferences().addPreferenceChangeListener( (p) -> {
			if(p.getKey().equals(RecentProjects.PROJECT_HISTORY_PROP)) {
				SwingUtilities.invokeLater( () -> recentProjectsList.updateProjectList() );
			}
		});

		gbc.gridy++;
		add(recentProjectsContainer, gbc);
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
		String s2 = "Create a new project...";

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
					NativeUtilities.isLinux() ? GtkStockIcon.FOLDER_OPEN : WindowsStockIcon.FOLDEROPEN);

		final ImageIcon browseIcn =
				IconManager.getInstance().getSystemStockIcon(stockIcon, defaultFolderIconName, IconSize.SMALL);
		final ImageIcon browseIcnL =
				IconManager.getInstance().getSystemStockIcon(stockIcon, defaultFolderIconName, IconSize.MEDIUM);

		String s1 = "Browse for Project";
		String s2 = "Browse for project folder on disk...";

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
				OSInfo.isMacOs() ? MacOSStockIcon.GenericPreferencesIcon
						: OSInfo.isNix() ? GtkStockIcon.SETTINGS : WindowsStockIcon.SETTINGS;
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
						: NativeUtilities.isLinux() ? GtkStockIcon.FOLDER_VIDEO : WindowsStockIcon.VIDEOFILES);
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

//		ImageIcon videoFolderIcn = IconManager.getInstance().getIcon("places/folder-video", IconSize.SMALL);
//		ImageIcon videoFolderIcnL = IconManager.getInstance().getIcon("places/folder-video", IconSize.MEDIUM);

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

	private final WindowListener windowListener = new WindowListener() {
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
	};

	public void refreshWorkspaceProjects() {
		workspaceProjectsPanel.refresh();
	}
}
