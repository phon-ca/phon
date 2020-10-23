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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.*;

import ca.hedlund.desktopicons.*;
import ca.phon.app.log.*;
import ca.phon.app.menu.workspace.*;
import ca.phon.app.workspace.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.menu.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

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


		init();
	}

	public void refresh() {
		projectList.refresh();
	}

	private void init() {
		setLayout(new BorderLayout());

		workspaceBtn = new MultiActionButton();
		workspaceBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		workspaceBtn.getBottomLabel().setForeground(Color.decode("#666666"));
		BgPainter bgPainter = new BgPainter();

		PhonUIAction selectHistoryAct =
				new PhonUIAction(this, "onShowHistory");
		selectHistoryAct.putValue(Action.NAME, "Select workspace");
		selectHistoryAct.putValue(Action.SHORT_DESCRIPTION, "Change workspace folder...");

		ImageIcon workspaceIcnL =
				IconManager.getInstance().getSystemIconForPath(
						Workspace.userWorkspaceFolder().getAbsolutePath(), "places/folder-workspace", IconSize.MEDIUM);

		workspaceBtn.setTopLabelText(WorkspaceTextStyler.toHeaderText("Workspace Folder"));
		workspaceBtn.getTopLabel().setIcon(workspaceIcnL);
		workspaceBtn.getTopLabel().setFont(FontPreferences.getTitleFont());
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
		
		final Preferences prefs = PrefHelper.getUserPreferences();
		prefs.addPreferenceChangeListener(
			evt -> {
				if(evt.getKey().equals(Workspace.WORKSPACE_FOLDER)) {
					final Runnable onEdt = WorkspaceProjectsPanel.this::update;
					if(SwingUtilities.isEventDispatchThread())
						onEdt.run();
					else
						SwingUtilities.invokeLater(onEdt);
				}
				
			}
		);
		update();

		add(contentPanel, BorderLayout.NORTH);
		add(projectList, BorderLayout.CENTER);
	}
	
	public void update() {
		File workspaceFolder = Workspace.userWorkspaceFolder();
		projectList.setFolder(workspaceFolder);	
		workspaceBtn.setBottomLabelText(workspaceFolder.getAbsolutePath() + " (click to change)");	
		refresh();
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
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(Workspace.userWorkspaceFolder());
			} catch (IOException e) {
				LogUtil.warning(e);
				Toolkit.getDefaultToolkit().beep();
			}
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
	}

	public void onResetWorkspace(PhonActionEvent pae) {
		final File defaultWorkspace = Workspace.defaultWorkspaceFolder();
		Workspace.setUserWorkspaceFolder(defaultWorkspace);
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
