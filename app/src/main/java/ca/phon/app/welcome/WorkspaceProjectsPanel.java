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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.menu.workspace.SelectWorkspaceCommand;
import ca.phon.app.workspace.Workspace;
import ca.phon.app.workspace.WorkspaceHistory;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OSInfo;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Start window panel for workspace projects.
 *
 */
public class WorkspaceProjectsPanel extends JPanel {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(WorkspaceProjectsPanel.class.getName());

	/* UI */
	private MultiActionButton workspaceBtn;

	private FolderProjectList projectList;

	public WorkspaceProjectsPanel() {
		super();

		final Preferences prefs = PrefHelper.getUserPreferences();
		prefs.addPreferenceChangeListener(
			evt -> {
				if(evt.getKey().equals(Workspace.WORKSPACE_FOLDER)) {
					final Runnable onEdt = new Runnable() {

						@Override
						public void run() {
							projectList.setFolder(Workspace.userWorkspaceFolder());
							workspaceBtn.setBottomLabelText(Workspace.userWorkspaceFolder().getAbsolutePath());
							refresh();
						}
					};
					if(SwingUtilities.isEventDispatchThread())
						onEdt.run();
					else
						SwingUtilities.invokeLater(onEdt);
				}

			}
		);

		init();
	}

	public void refresh() {
		projectList.refresh();
	}

	private void init() {
		setLayout(new BorderLayout());

		workspaceBtn = new MultiActionButton();
		workspaceBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		BgPainter bgPainter = new BgPainter();

		PhonUIAction selectHistoryAct =
				new PhonUIAction(this, "onShowHistory");
		selectHistoryAct.putValue(Action.NAME, "Select workspace");
		selectHistoryAct.putValue(Action.SHORT_DESCRIPTION, "Select workspace folder from history");

		ImageIcon workspaceIcnL =
				IconManager.getInstance().getSystemIconForPath(
						Workspace.userWorkspaceFolder().getAbsolutePath(), "places/folder-workspace", IconSize.MEDIUM);

		workspaceBtn.setTopLabelText(WorkspaceTextStyler.toHeaderText("Select Workspace Folder"));
		workspaceBtn.getTopLabel().setIcon(workspaceIcnL);
		workspaceBtn.getTopLabel().setFont(FontPreferences.getTitleFont());
		workspaceBtn.setBottomLabelText(Workspace.userWorkspaceFolder().getAbsolutePath());
		workspaceBtn.setBackgroundPainter(bgPainter);
		workspaceBtn.addMouseListener(bgPainter);
		workspaceBtn.addAction(createShowWorkspaceAction());
		workspaceBtn.setDefaultAction(selectHistoryAct);

		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.white);
		contentPanel.setOpaque(true);
		contentPanel.setLayout(new BorderLayout());

		contentPanel.add(workspaceBtn, BorderLayout.SOUTH);

		projectList = new FolderProjectList();

		add(contentPanel, BorderLayout.NORTH);
		add(projectList, BorderLayout.CENTER);
	}

	public void onShowHistory(PhonActionEvent pae) {
		final WorkspaceHistory history = new WorkspaceHistory();

		final JPopupMenu menu = new JPopupMenu();
		final MenuBuilder builder = new MenuBuilder(menu);

		for(File workspaceFolder:history) {
			ImageIcon workspaceIcn =
					IconManager.getInstance().getSystemIconForPath(
							workspaceFolder.getAbsolutePath(), "places/folder-workspace", IconSize.SMALL);
			final PhonUIAction selectAction = new PhonUIAction(this, "onSelectFolder", workspaceFolder);
			selectAction.putValue(PhonUIAction.NAME, workspaceFolder.getAbsolutePath());
			selectAction.putValue(PhonUIAction.SMALL_ICON, workspaceIcn);
			builder.addItem(".", selectAction);
		}

		ImageIcon browseIcn =
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		if(OSInfo.isMacOs()) {
			ImageIcon finderIcon =
					IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.OpenFolderIcon, IconSize.SMALL);
			if(finderIcon != null) browseIcn = finderIcon;
		} else if(OSInfo.isWindows()) {
			ImageIcon explorerIcon =
					IconManager.getInstance().getSystemStockIcon(WindowsStockIcon.FOLDEROPEN, IconSize.SMALL);
			if(explorerIcon != null) browseIcn = explorerIcon;
		}
		builder.addSeparator(".", "browse");

		final Action showWorkspaceAct = createShowWorkspaceAction();
		builder.addItem(".@browse", showWorkspaceAct);

		final SelectWorkspaceCommand cmd = new SelectWorkspaceCommand();
		cmd.putValue(Action.NAME, "Browse for workspace folder...");
		cmd.putValue(Action.SMALL_ICON, browseIcn);
		builder.addItem(".", cmd);

		builder.addSeparator(".", "clear");
		final PhonUIAction clearHistoryAct = new PhonUIAction(this, "onClearHistory");
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear workspace history");
		builder.addItem(".@clear", clearHistoryAct);

		menu.show(workspaceBtn, 0, workspaceBtn.getHeight());
	}

	public void onShowWorkspace() {
		try {
			OpenFileLauncher.openURL(Workspace.userWorkspaceFolder().toURI().toURL());
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private Action createShowWorkspaceAction() {

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

		final PhonUIAction act = new PhonUIAction(this, "onShowWorkspace");
		act.putValue(PhonUIAction.NAME, "Show workspace");
		act.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show workspace folder");
		act.putValue(PhonUIAction.SMALL_ICON, fsIcon);
		act.putValue(PhonUIAction.LARGE_ICON_KEY, fsIconL);

		return act;
	}

	public void onClearHistory() {
		final WorkspaceHistory history = new WorkspaceHistory();
		history.clearHistory();
		history.addToHistory(Workspace.userWorkspaceFolder());
	}

	public void onSelectFolder(File workspaceFolder) {
		Workspace.setUserWorkspaceFolder(workspaceFolder);
		projectList.setFolder(workspaceFolder);
		workspaceBtn.setBottomLabelText(workspaceFolder.getAbsolutePath());
	}

	public void onResetWorkspace(PhonActionEvent pae) {
		final File defaultWorkspace = Workspace.defaultWorkspaceFolder();
		Workspace.setUserWorkspaceFolder(defaultWorkspace);

		projectList.setFolder(defaultWorkspace);
		workspaceBtn.setBottomLabelText(defaultWorkspace.getAbsolutePath());
	}

	/**
	 * Background painter
	 */
	private class BgPainter extends MouseInputAdapter implements Painter<MultiActionButton> {

		private boolean useSelected = false;

		private Color origColor = Color.white;

		private Color selectedColor = new Color(0, 90, 140, 100);

		public BgPainter() {

		}

		@Override
		public void paint(Graphics2D g, MultiActionButton object, int width,
				int height) {
			// create gradient
			g.setColor((origColor != null ? origColor : Color.white));
			g.fillRect(0, 0, width, height);
			if(useSelected) {

				GlowPathEffect effect = new GlowPathEffect();
				effect.setRenderInsideShape(true);
				effect.setBrushColor(selectedColor);

				// get rectangle
				Rectangle2D.Double boundRect =
					new Rectangle2D.Double(0.0f, 0.0f, width, height);

				effect.apply(g, boundRect, 0, 0);
			}
		}

		@Override
		public void mouseEntered(MouseEvent me) {
			useSelected = true;
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent me) {
			useSelected = false;
			repaint();
		}
	}
}
