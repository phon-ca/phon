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

import ca.phon.app.workspace.*;
import ca.phon.ui.MultiActionButton;
import ca.phon.util.PrefHelper;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

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

		init();
	}

	public void refresh() {
		projectList.refresh();
	}

	private void init() {
		setLayout(new BorderLayout());

		workspaceBtn = new WorkspaceButton();

		workspaceBtn.getBottomLabel().setForeground(Color.decode("#666666"));
		BgPainter bgPainter = new BgPainter();
		workspaceBtn.setBackgroundPainter(bgPainter);
		workspaceBtn.addMouseListener(bgPainter);
		PrefHelper.getUserPreferences().addPreferenceChangeListener((e) -> {
			if(e.getKey().equals(Workspace.WORKSPACE_FOLDER)) {
				projectList.setFolder(Workspace.userWorkspaceFolder());
			}
		});

		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.white);
		contentPanel.setOpaque(true);
		contentPanel.setLayout(new BorderLayout());

		contentPanel.add(workspaceBtn, BorderLayout.SOUTH);

		projectList = new FolderProjectList();

		add(contentPanel, BorderLayout.NORTH);
		add(projectList, BorderLayout.CENTER);
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
