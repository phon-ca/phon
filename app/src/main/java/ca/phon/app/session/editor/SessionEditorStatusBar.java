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
package ca.phon.app.session.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ref.*;
import java.time.format.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXStatusBar.Constraint.*;

import ca.phon.app.log.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.*;

public class SessionEditorStatusBar extends JXStatusBar {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SessionEditorStatusBar.class.getName());

	private static final long serialVersionUID = 286465072395883742L;

	/**
	 * Status bar progress
	 */
	private JLabel progressLabel;
	private JProgressBar progressBar;

	private final PhonTaskListener taskListener = new PhonTaskListener() {

		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
			if(newStatus == TaskStatus.RUNNING) {
				progressLabel.setText(task.getName());
				progressBar.setIndeterminate(true);
			} else if(newStatus == TaskStatus.ERROR) {
				progressLabel.setText(task.getException().getLocalizedMessage());
				progressBar.setIndeterminate(false);
				progressBar.setValue(0);
				task.removeTaskListener(this);
			} else if(newStatus == TaskStatus.FINISHED) {
				progressLabel.setText("");
				progressBar.setIndeterminate(false);
				progressBar.setValue(0);
				task.removeTaskListener(this);
			}
		}

		@Override
		public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {
			if(PhonTask.PROGRESS_PROP.equals(property)) {
				final float percentComplete = (Float)newValue;
				if(percentComplete < 0) {
					progressBar.setIndeterminate(true);
				} else {
					progressBar.setIndeterminate(false);
					progressBar.setValue(Math.round(percentComplete*progressBar.getMaximum()));
				}
			}
		}

	};

	private JLabel statusLabel;

	private JLabel sessionPathLabel;

	private ImageIcon modifiedIcon;

	private ImageIcon unmodifiedIcon;

	private JPanel extrasPanel;

	private final WeakReference<SessionEditor> editorRef;

	public SessionEditorStatusBar(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.MODIFIED_FLAG_CHANGED,
				(ee) -> {
					if(getEditor().hasUnsavedChanges()) {
						statusLabel.setIcon(modifiedIcon);
					} else {
						statusLabel.setIcon(unmodifiedIcon);
					}
					statusLabel.setToolTipText(getStatusTooltipText());
				} );

		init();
	}

	public SessionEditor getEditor() {
		return editorRef.get();
	}

	private void init() {
		statusLabel = new JLabel();

		modifiedIcon = IconManager.getInstance().getIcon("actions/document-save", IconSize.XSMALL);
		unmodifiedIcon = IconManager.getInstance().getDisabledIcon("actions/document-save", IconSize.XSMALL);
		if(getEditor().hasUnsavedChanges()) {
			statusLabel.setIcon(modifiedIcon);
		} else {
			statusLabel.setIcon(unmodifiedIcon);
		}
		statusLabel.setToolTipText(getStatusTooltipText());
		add(statusLabel, new JXStatusBar.Constraint(IconSize.XSMALL.getWidth()));

		sessionPathLabel = new JLabel(getEditor().getSession().getCorpus() + "/" +
				getEditor().getSession().getName());
		sessionPathLabel.setFont(FontPreferences.getSmallFont());
		sessionPathLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		sessionPathLabel.setToolTipText(
				getEditor().getProject().getSessionPath(getEditor().getSession()) + " (click to show corpus folder)");
		sessionPathLabel.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount() == 1) {
					final String sessionPath = getEditor().getProject().getSessionPath(getEditor().getSession());
					if(Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browseFileDirectory(new File(sessionPath));
						} catch (Exception e) {
							LogUtil.warning(e);
							Toolkit.getDefaultToolkit().beep();
						}
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				sessionPathLabel.setForeground(Color.blue);
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				sessionPathLabel.setForeground(Color.gray);
			}

		});
		sessionPathLabel.setForeground(Color.gray);

		add(sessionPathLabel, new JXStatusBar.Constraint(ResizeBehavior.FILL));

		extrasPanel = new JPanel(new HorizontalLayout());
		extrasPanel.setOpaque(false);
		add(extrasPanel, new JXStatusBar.Constraint(ResizeBehavior.FILL));

		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);

		progressLabel = new JLabel();
		progressLabel.setFont(FontPreferences.getSmallFont());

		add(progressLabel, new JXStatusBar.Constraint(200));
		add(progressBar, new JXStatusBar.Constraint(120));
		add(new JLabel(), new JXStatusBar.Constraint(5));
	}

	private String getStatusTooltipText() {
		final StringBuffer buf = new StringBuffer();
		final SessionEditor editor = getEditor();

		final Project project = editor.getProject();
		final Session session = editor.getSession();

		if(editor.hasUnsavedChanges()) {
			buf.append("*modified* ");
		}

		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@h:mma");
		buf.append("Last save:");
		buf.append(formatter.format(project.getSessionModificationTime(session)));

		buf.append(" Size:");
		buf.append(ByteSize.humanReadableByteCount(project.getSessionByteSize(session), true));

		return buf.toString();
	}

	public void watchTask(PhonTask task) {
		task.addTaskListener(taskListener);
	}

	public JPanel getExtrasPanel() {
		return this.extrasPanel;
	}

	public JLabel getStatusLabel() {
		return this.statusLabel;
	}

	public JLabel getSessionPathLabel() {
		return this.sessionPathLabel;
	}

	public JLabel getProgressLabel() {
		return this.progressLabel;
	}

	public JProgressBar getProgressBar() {
		return this.progressBar;
	}
}
