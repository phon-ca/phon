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
package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

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
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.welcome.LocalProjectButton;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class RecentProjectsList extends JPanel {

	private static final long serialVersionUID = 9021308308904778797L;
	
	private ButtonPanel buttonPanel;
	
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
		
		clearLabel = new JLabel("<html><u style='color: rgb(0, 90, 140);'>Clear History</u></html>");
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
		ImageIcon openIcn = (projectFolder.exists() ? IconManager.getInstance().getSystemIconForPath(projectFolder.getAbsolutePath(), defaultIconName, IconSize.SMALL)
				: IconManager.getInstance().getIcon("blank", IconSize.SMALL));
		ImageIcon openIcnL = (projectFolder.exists() ? IconManager.getInstance().getSystemIconForPath(projectFolder.getAbsolutePath(), defaultIconName, IconSize.MEDIUM)
				: IconManager.getInstance().getIcon("blank", IconSize.SMALL));
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
		} else if(OSInfo.isMacOs()) {
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
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(btn.getProjectFile());
			} catch (IOException e) {
				LogUtil.warning(e);
				Toolkit.getDefaultToolkit().beep();
			}
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
