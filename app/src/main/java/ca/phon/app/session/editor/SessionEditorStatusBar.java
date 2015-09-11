package ca.phon.app.session.editor;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXStatusBar.Constraint.ResizeBehavior;

import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonTask.TaskStatus;

public class SessionEditorStatusBar extends JXStatusBar {

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
	
	private final WeakReference<SessionEditor> editorRef;
	
	public SessionEditorStatusBar(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);
		init();
	}
	
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	private void init() {
		statusLabel = new JLabel(getEditor().getSession().getCorpus() + "/" + 
				getEditor().getSession().getName());
		add(statusLabel, new JXStatusBar.Constraint(ResizeBehavior.FILL));
		
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		
		progressLabel = new JLabel() {
			@Override
			public void setText(String txt) {
				super.setText(txt);
				super.setToolTipText(txt);
			}
		};
		
		add(progressLabel, new JXStatusBar.Constraint(200));
		add(progressBar, new JXStatusBar.Constraint(100));
	}
	
	public void watchTask(PhonTask task) {
		task.addTaskListener(taskListener);
	}
	
	public JLabel getStatusLabel() {
		return this.statusLabel;
	}
	
	public JLabel getProgressLabel() {
		return this.progressLabel;
	}
	
	public JProgressBar getProgressBar() {
		return this.progressBar;
	}
}
