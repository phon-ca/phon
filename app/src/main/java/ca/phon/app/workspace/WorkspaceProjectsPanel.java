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
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.app.menu.workspace.SelectWorkspaceCommand;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.workspace.Workspace;

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
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
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
			
		});
		
		init();
	}
	
	public void refresh() {
		projectList.refresh();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		PhonUIAction browseForWorkspaceAction =
			new PhonUIAction(this, "onBrowseForWorkspace");
		browseForWorkspaceAction.putValue(Action.NAME, "Change...");
		browseForWorkspaceAction.putValue(Action.SHORT_DESCRIPTION, "Change workspace folder");
		ImageIcon icn = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		browseForWorkspaceAction.putValue(Action.SMALL_ICON, icn);
		
		workspaceBtn = new MultiActionButton();
//		workspaceBtn = new JLabel("<html><u style='color: blue;'>Change...</u></html>");
//		workspaceBtn.setToolTipText("Change workspace folder");
//		workspaceBtn.getBottomLabel().setBackground(Color.white);
//		workspaceBtn.getBottomLabel().setOpaque(true);
		workspaceBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//		workspaceBtn.setIcon(icn);
		
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
		
		ImageIcon browseIcn = IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		ImageIcon browseIcnL = IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM);
		final Action changeWorkspaceAct = new SelectWorkspaceCommand();
		changeWorkspaceAct.putValue(Action.SMALL_ICON, browseIcn);
		changeWorkspaceAct.putValue(Action.LARGE_ICON_KEY, browseIcnL);
		
		ImageIcon resetIcn = IconManager.getInstance().getIcon("actions/edit-undo", IconSize.SMALL);
		ImageIcon resetIcnL = IconManager.getInstance().getIcon("actions/edit-undo", IconSize.MEDIUM);
		PhonUIAction resetWorkspaceAct = 
			new PhonUIAction(this, "onResetWorkspace");
		resetWorkspaceAct.putValue(Action.NAME, "Reset workspace folder");
		resetWorkspaceAct.putValue(Action.SHORT_DESCRIPTION, "Use default workspace folder");
		resetWorkspaceAct.putValue(Action.SMALL_ICON, resetIcn);
		resetWorkspaceAct.putValue(Action.LARGE_ICON_KEY, resetIcnL);
		
		ImageIcon workspaceIcn = IconManager.getInstance().getIcon("places/folder-workspace", IconSize.SMALL);
		
		workspaceBtn.setTopLabelText(WorkspaceTextStyler.toHeaderText("Workspace Folder"));
		workspaceBtn.getTopLabel().setIcon(workspaceIcn);
		workspaceBtn.getTopLabel().setFont(FontPreferences.getTitleFont());
		workspaceBtn.setBottomLabelText(Workspace.userWorkspaceFolder().getAbsolutePath());
		workspaceBtn.setBackgroundPainter(bgPainter);
		workspaceBtn.addMouseListener(bgPainter);
		workspaceBtn.setDefaultAction(changeWorkspaceAct);
		workspaceBtn.addAction(resetWorkspaceAct);
		
		JXTitledPanel workspacePanel = new JXTitledPanel("Workspace Folder");
//		workspacePanel.setTitleFont(workspacePanel.getTitleFont().deriveFont(Font.BOLD, 14.0f));
		
//		workspacePanel.getContentContainer().setBackground(Color.white);
//		workspacePanel.getContentContainer().setOpaque(true);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.white);
		contentPanel.setOpaque(true);
		contentPanel.setLayout(new BorderLayout());
		
		JXLabel infoLbl = new JXLabel(infoTxt);
		contentPanel.add(infoLbl, BorderLayout.CENTER);
		contentPanel.add(workspaceBtn, BorderLayout.SOUTH);
		
		workspacePanel.setContentContainer(contentPanel);
	
		projectList = new FolderProjectList();
		
		JXTitledPanel listPanel = new JXTitledPanel("Project List");
//		listPanel.setTitleFont(listPanel.getTitleFont().deriveFont(Font.BOLD, 14.0f));
//		listPanel.add(workspacePanel, BorderLayout.NORTH);
		listPanel.getContentContainer().setLayout(new BorderLayout());
		listPanel.getContentContainer().add(projectList, BorderLayout.CENTER);
		
		add(workspacePanel, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		
//		String infoTxt = "";
//		try {
//			BufferedReader r = new BufferedReader(new InputStreamReader(
//					getClass().getResourceAsStream("workspace.txt")));
//			String line = null;
//			while((line = r.readLine()) != null) {
//				infoTxt += line + "\n";
//			}
//			r.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		infoLbl = new JLabel();
//		infoLbl.setBackground(Color.white);
//		infoLbl.setVerticalAlignment(SwingConstants.TOP);
//		infoLbl.setOpaque(true);
//		infoLbl.setText(infoTxt);
//		
//		Dimension dim = infoLbl.getPreferredSize();
//		dim.width = 250;
//		infoLbl.setPreferredSize(dim);
//		
//		ImageIcon brwseIcn = 
//			IconManager.getInstance().getIcon("actions/document-open", IconSize.MEDIUM);
//		PhonUIAction browseForProjectAction = new PhonUIAction(this, "onBrowse");
//		browseForProjectAction.putValue(PhonUIAction.NAME, "Browse for project...");
//		browseForProjectAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for project...");
//		browseForProjectAction.putValue(PhonUIAction.SMALL_ICON, brwseIcn);
//		browseBtn = new JButton(browseForProjectAction);
//		
//		infoPanel = new JXTitledPanel("Workspace Folder");
//		infoPanel.setTitleFont(infoPanel.getTitleFont().deriveFont(Font.BOLD, 14.0f));
//		infoPanel.getContentContainer().setLayout(new BorderLayout());
//		infoPanel.getContentContainer().add(infoLbl, BorderLayout.CENTER);
//		infoPanel.getContentContainer().add(browseBtn, BorderLayout.SOUTH);
//		
//		add(infoPanel, BorderLayout.WEST);
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
//				GradientPaint gp = new GradientPaint(
//						(float)0, 0.0f, new Color(237,243, 254), (float)0.0f, (float)height, new Color(207, 213, 224), true);
//				MattePainter bgPainter = new MattePainter(gp);
//				bgPainter.paint(g, object, width, height);
//				
//				NeonBorderEffect effect  = new NeonBorderEffect();
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
