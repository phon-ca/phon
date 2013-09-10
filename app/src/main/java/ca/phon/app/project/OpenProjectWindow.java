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
package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXList;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Window displayed at application start to create a new project, browse for a
 * project or open a recent project.
 * 
 */
public class OpenProjectWindow extends CommonModuleFrame {

	private JButton newProjectButton;

	private JButton browseButton;

	private JButton clearRecentButton;

	private JXList recentProjectList;

	private DialogHeader header;
	
	private DefaultListModel recentProjectListModel;
	
	private OpenProjectEP controller;

	public OpenProjectWindow(OpenProjectEP controller) {
		super("Open Project");
		
		this.controller = controller;

		init();
	}

	private void init() {
		// setup layout
		FormLayout windowLayout = new FormLayout(
				"5dlu, pref, 3dlu, fill:pref:grow, 5dlu",
				"pref, 3dlu, fill:pref:grow, 5dlu");
		CellConstraints cc = new CellConstraints();
		setLayout(windowLayout);

		FormLayout leftLayout = new FormLayout("fill:pref:grow",
				"pref, 3dlu, pref");
		JPanel leftPanel = new JPanel(leftLayout);

		newProjectButton = new JButton("New Project...");
		newProjectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newProject();
			}
			
		});
		leftPanel.add(newProjectButton, cc.xy(1, 1));

		browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openLocalProject();
			}
			
		});
		leftPanel.add(browseButton, cc.xy(1, 3));

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(DefaultComponentFactory.getInstance().createSeparator(
				"Recent Projects"), BorderLayout.NORTH);

		recentProjectListModel = new DefaultListModel();
		recentProjectList = new JXList(recentProjectListModel);
		recentProjectList.setCellRenderer(new RecentProjectListCellRenderer());
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = recentProjectList.locationToIndex(e.getPoint());
					openRecentProject(index + 1);
				}
			}
		};
		recentProjectList.addMouseListener(mouseListener);
		updateRecentProjectList();

		rightPanel.add(new JScrollPane(recentProjectList), BorderLayout.CENTER);

		clearRecentButton = new JButton("Clear list");
		clearRecentButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
//				UserPrefManager.clearRecentProjects();
//				updateRecentProjectList();
			}
			
		});

		JComponent buttonBar = ButtonBarFactory
				.buildRightAlignedBar(clearRecentButton);
		rightPanel.add(buttonBar, BorderLayout.SOUTH);

		header = new DialogHeader("Open Project",
				"Create a new project or open a project on disk.");

		add(header, cc.xyw(1, 1, 5));
		add(leftPanel, cc.xy(2, 3));
		add(rightPanel, cc.xy(4, 3));
	}

	private void openRecentProject(int recentProjectIndex) {
//		SystemProperties recentProjects = UserPrefManager
//				.getUserRecentProjects();
//
//		String projectString = recentProjects.getProperty(
//				"ca.phon.project.recent." + recentProjectIndex).toString();
//
//		if (projectString.length() == 0)
//			return;
//
//		controller.setProjectPath(projectString);
//		controller.loadProject();
	}

	private void openLocalProject() {
		// get the filename
		FileFilter filters[] = new FileFilter[1];
		filters[0] = FileFilter.phonFilter;

		Window window = OpenProjectEP.getFrame();
		
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(window);
		props.setRunAsync(false);
		props.setCanChooseFiles(false);
		props.setCanChooseDirectories(true);
		props.setTitle("Open Project");
		final List<String> selectedPaths = NativeDialogs.showOpenDialog(props);
		
		if (selectedPaths.size() > 0 && selectedPaths.get(0).length() > 0) {
			controller.setProjectPath(selectedPaths.get(0));
			controller.loadProject();
		}
	}

	private void newProject() {
		controller.newProject();
	}

	/**
	 * Update the recent project list
	 */
	private void updateRecentProjectList() {
//		recentProjectListModel.removeAllElements();
//
//		// get the recent project list
//		SystemProperties recentProjects = UserPrefManager
//				.getUserRecentProjects();
//
//		for (int i = 1; i <= 5; i++) {
//			String projectString = recentProjects.getProperty(
//					"ca.phon.project.recent." + i).toString();
//
//			if (projectString.length() > 0)
//				recentProjectListModel.addElement(projectString);
//		}
//
//		recentProjectList.repaint();
	}
}
