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
package ca.phon.app.project;

import ca.phon.app.log.LogUtil;
import ca.phon.project.MutableProject;
import ca.phon.project.Project;
import ca.phon.project.ProjectPaths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Corpus details for project manager.
 *
 */
public class CorpusDetails extends JPanel {

	// number of sessions
	private JLabel numSessionsLabel;

	// location of corpus folder
	private JLabel locationLabel;

	// description
	private JTextArea corpusDescriptionArea;

	// model
	private final Project project;

	private String corpus;

	public CorpusDetails(Project project, String corpus) {
		super();

		this.project = project;
		this.corpus = corpus;

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		locationLabel = new JLabel();
		locationLabel.setForeground(new Color(0, 90, 140));
		final ProjectPaths projectPaths = project.getExtension(ProjectPaths.class);
		if(projectPaths != null) {
			locationLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			locationLabel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent me) {
					if (corpus != null && Desktop.isDesktopSupported()) {
						final String corpusPath = projectPaths.getCorpusPath(corpus);
						try {
							Desktop.getDesktop().open(new File(corpusPath));
						} catch (IOException e) {
							LogUtil.severe(e);
							Toolkit.getDefaultToolkit().beep();
						}
					}
				}

			});
		}

		numSessionsLabel = new JLabel();

		final JPanel folderPanel = new JPanel(new GridBagLayout());
		folderPanel.setOpaque(false);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		folderPanel.add(new JLabel("Corpus folder:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 5, 0, 0);
		folderPanel.add(locationLabel, gbc);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.insets.left = 0;
		folderPanel.add(new JLabel("Sessions:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		gbc.insets.left = 5;
		folderPanel.add(numSessionsLabel, gbc);

		corpusDescriptionArea = new JTextArea();
		corpusDescriptionArea.setLineWrap(true);
		corpusDescriptionArea.setWrapStyleWord(true);
		corpusDescriptionArea.setRows(5);
		corpusDescriptionArea.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				final MutableProject mutableProject = project.getExtension(MutableProject.class);
				if(mutableProject == null) {
					return;
				}
				if(corpus != null) {
					mutableProject.setCorpusDescription(corpus, corpusDescriptionArea.getText());
				}
			}

			@Override
			public void focusGained(FocusEvent e) {

			}

		});

		final JScrollPane textScroller = new JScrollPane(corpusDescriptionArea);
		textScroller.setOpaque(false);
		textScroller.setBorder(BorderFactory.createTitledBorder("Description"));

		add(folderPanel, BorderLayout.NORTH);
		add(textScroller, BorderLayout.CENTER);
	}

	public String getCorpus() {
		return this.corpus;
	}

	public void setCorpus(String corpus) {
		final MutableProject mutableProject = project.getExtension(MutableProject.class);
		if(mutableProject == null) {
			return;
		}
		if(this.corpus != null && project.hasCorpus(this.corpus)) {
			mutableProject.setCorpusDescription(this.corpus, corpusDescriptionArea.getText());
		}

		this.corpus = corpus;
		update();
	}
	
	void update() {
		if(corpus == null || !project.hasCorpus(corpus)) {
			// clear
			numSessionsLabel.setText("");

			corpusDescriptionArea.setText("");
			corpusDescriptionArea.setEnabled(false);

			locationLabel.setText("");
			locationLabel.setIcon(null);
			locationLabel.setToolTipText("");
		} else {
			final Iterator<String> sessionItr = project.getSessionIterator(corpus);
			int numSessions = 0;
			while(sessionItr.hasNext()) {
				sessionItr.next();
				++numSessions;
			}
			numSessionsLabel.setText(numSessions + "");

			final ProjectPaths projectPaths = project.getExtension(ProjectPaths.class);
			if(projectPaths != null) {
				final String corpusAbsolutePath = projectPaths.getCorpusPath(corpus);
				final Path corpusPath = FileSystems.getDefault().getPath(corpusAbsolutePath);
				final Path projectPath = FileSystems.getDefault().getPath(projectPaths.getLocation());
				final Path relativePath = projectPath.relativize(corpusPath);
				locationLabel.setText(relativePath.toString());
				locationLabel.setForeground(Color.blue);
				locationLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				locationLabel.setToolTipText(corpusAbsolutePath);
			}

			corpusDescriptionArea.setText(project.getCorpusDescription(corpus));
			corpusDescriptionArea.setEnabled(true);
			corpusDescriptionArea.setCaretPosition(0);
		}
	}

}
