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
package ca.phon.app.session.editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXStatusBar.Constraint.ResizeBehavior;

import com.jgoodies.forms.layout.*;

import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;

public class SessionEditorStatusBar extends JXStatusBar {

	private final static Logger LOGGER = Logger.getLogger(SessionEditorStatusBar.class.getName());

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
					// XXX open session folder in file explorer
					final File corpusFolder =
							new File(getEditor().getProject().getCorpusPath(getEditor().getSession().getCorpus()));
					try {
						OpenFileLauncher.openURL(corpusFolder.toURI().toURL());
					} catch (MalformedURLException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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

		JComponent pbar = new JPanel(new FormLayout("pref",
				(OSInfo.isMacOs() ? "10px" : "pref")));
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		pbar.add(progressBar, (new CellConstraints()).xy(1, 1));

		progressLabel = new JLabel();
		progressLabel.setFont(FontPreferences.getSmallFont());

		add(progressLabel, new JXStatusBar.Constraint(200));
		add(pbar, new JXStatusBar.Constraint(120));
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
