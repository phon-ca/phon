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
package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.border.MatteBorder;
import javax.swing.event.MouseInputAdapter;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.welcome.LocalProjectButton;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OSInfo;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class RecentProjectsList extends JPanel {

	private static final long serialVersionUID = 9021308308904778797L;
	
	private ButtonPanel buttonPanel;
	
	private JLabel refreshLabel;
	
	private JLabel clearLabel;
	
	public RecentProjectsList() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		MatteBorder lineBorder = 
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray);
		actionPanel.setBorder(lineBorder);
		
		clearLabel = new JLabel("<html><u style='color: blue;'>Clear History</u></html>");
		clearLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		clearLabel.setToolTipText("Clear recent projects history");
		clearLabel.addMouseListener(new MouseInputAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent me) {
				final RecentProjects history = new RecentProjects();
				history.clearHistory();
			}
			
		});
		actionPanel.add(clearLabel);
		
		ImageIcon icn = IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
		refreshLabel = new JLabel("<html><u style='color: blue;'>Refresh</u></html>");
		refreshLabel.setIcon(icn);
		refreshLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		refreshLabel.setToolTipText("Refresh project list");
		refreshLabel.addMouseListener(new MouseInputAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent me) {
				updateProjectList();
			}
			
		});
		actionPanel.add(refreshLabel);
		
		add(actionPanel, BorderLayout.NORTH);
		
		buttonPanel = new ButtonPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		final JScrollPane scrollPanel = new JScrollPane(buttonPanel);
		add(scrollPanel, BorderLayout.CENTER);
		
		updateProjectList();
	}

	public void updateProjectList() {
		buttonPanel.removeAll();
		buttonPanel.revalidate();
		
		final RecentProjects history = new RecentProjects();
		
		boolean stripeRow = false;
		for(File projectFolder:history) {
			final LocalProjectButton projectButton = getProjectButton(projectFolder);
			
			if(stripeRow) {
				projectButton.setBackground(PhonGuiConstants.PHON_UI_STRIP_COLOR);
				stripeRow = false;
			} else {
				projectButton.setBackground(Color.white);
				stripeRow = true;
			}
			
			buttonPanel.add(projectButton);
		}
	}
	
	private LocalProjectButton getProjectButton(File projectFolder) {
		LocalProjectButton retVal = new LocalProjectButton(projectFolder);
		
		PhonUIAction openAction = new PhonUIAction(this, "onOpenProject", retVal);
		
		final String defaultIconName = "actions/document-open";
		ImageIcon openIcn = 
			IconManager.getInstance().getSystemIconForPath(projectFolder.getAbsolutePath(), defaultIconName, IconSize.SMALL);
		ImageIcon openIcnL =
			IconManager.getInstance().getSystemIconForPath(projectFolder.getAbsolutePath(), defaultIconName, IconSize.MEDIUM);
		
		openAction.putValue(Action.NAME, "Open project");
		openAction.putValue(Action.SHORT_DESCRIPTION, "Open: " + projectFolder.getAbsolutePath());
		openAction.putValue(Action.SMALL_ICON, openIcn);
		openAction.putValue(Action.LARGE_ICON_KEY, openIcnL);
		
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
		} else {
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
		
		retVal.getTopLabel().setIcon(openIcnL);
		retVal.setDefaultAction(openAction);
		
		return retVal;
	}
	
	public void onShowProject(PhonActionEvent pae) {
		LocalProjectButton btn = (LocalProjectButton)pae.getData();
		try {
			OpenFileLauncher.openURL(
					btn.getProjectFile().toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void onOpenProject(PhonActionEvent pae) {
		LocalProjectButton btn = (LocalProjectButton)pae.getData();
		
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_LOCATION, btn.getProjectFile().getAbsolutePath());
		
		PluginEntryPointRunner.executePluginInBackground("OpenProject", args);
	}
	
	class ButtonPanel extends JPanel implements Scrollable {
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return null;
		}
	
		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}
	
		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 20;
		}
	
		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
	
		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}
	
}
