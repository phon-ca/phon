/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.phon.app.menu.workspace.SelectWorkspaceCommand;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.workspace.Workspace;
import ca.phon.workspace.WorkspaceHistory;

/**
 * Start window panel for workspace projects.
 *
 */
public class WorkspaceProjectsPanel extends JPanel {
	
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
		
		String infoTxt = "";
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream("workspace.txt")));
			String line = null;
			while((line = r.readLine()) != null) {
				infoTxt += line + "\n";
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BgPainter bgPainter = new BgPainter();
		
		PhonUIAction selectHistoryAct = 
				new PhonUIAction(this, "onShowHistory");
		selectHistoryAct.putValue(Action.NAME, "Select workspace");
		selectHistoryAct.putValue(Action.SHORT_DESCRIPTION, "Select workspace folder from history");
		
		ImageIcon workspaceIcn = 
				IconManager.getInstance().getSystemIconForPath(
						Workspace.userWorkspaceFolder().getAbsolutePath(), "places/folder-workspace", IconSize.SMALL);
		
		workspaceBtn.setTopLabelText(WorkspaceTextStyler.toHeaderText("Workspace Folder"));
		workspaceBtn.getTopLabel().setFont(FontPreferences.getTitleFont());
		workspaceBtn.getBottomLabel().setIcon(workspaceIcn);
		workspaceBtn.setBottomLabelText(Workspace.userWorkspaceFolder().getAbsolutePath());
		workspaceBtn.setBackgroundPainter(bgPainter);
		workspaceBtn.addMouseListener(bgPainter);
		workspaceBtn.setDefaultAction(selectHistoryAct);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.white);
		contentPanel.setOpaque(true);
		contentPanel.setLayout(new BorderLayout());
		
		JXLabel infoLbl = new JXLabel(infoTxt);
		contentPanel.add(infoLbl, BorderLayout.CENTER);
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
							Workspace.userWorkspaceFolder().getAbsolutePath(), "places/folder-workspace", IconSize.SMALL);
			final PhonUIAction selectAction = new PhonUIAction(this, "onSelectFolder", workspaceFolder);
			selectAction.putValue(PhonUIAction.NAME, workspaceFolder.getAbsolutePath());
			selectAction.putValue(PhonUIAction.SMALL_ICON, workspaceIcn);
			builder.addItem(".", selectAction);
		}
	
		ImageIcon browseIcn = 
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		if(OSInfo.isMacOs()) {
			ImageIcon finderIcon =
					IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.FinderIcon, IconSize.SMALL);
			if(finderIcon != null) browseIcn = finderIcon;
		} else if(OSInfo.isWindows()) {
			ImageIcon explorerIcon = 
					IconManager.getInstance().getSystemIconForPath("C:\\Windows\\explorer.exe", IconSize.SMALL);
			if(explorerIcon != null) browseIcn = explorerIcon;
		}
		builder.addSeparator(".", "browse");
		final SelectWorkspaceCommand cmd = new SelectWorkspaceCommand();
		cmd.putValue(Action.NAME, "Browse for workspace folder...");
		cmd.putValue(Action.SMALL_ICON, browseIcn);
		builder.addItem(".@browse", cmd);
		
		builder.addSeparator(".", "clear");
		final PhonUIAction clearHistoryAct = new PhonUIAction(this, "onClearHistory");
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear workspace history");
		builder.addItem(".@clear", clearHistoryAct);
		
		menu.show(workspaceBtn, 0, workspaceBtn.getHeight());
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
		
		private Color selectedColor = new Color(0, 100, 200, 100);
		
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
