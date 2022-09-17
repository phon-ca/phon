package ca.phon.app.session.editor;

import ca.phon.ui.nativedialogs.*;

import java.awt.event.*;
import java.time.ZonedDateTime;

/**
 * A window listener which, on focus, will detect changes to the currently open session on disk and if changes
 * are found will prompt the user to re-load the session.
 */
public class SessionEditorModificationListener implements WindowFocusListener {

	private final static String DLG1_HEADER_TXT = "Session changed on disk";
	private final static String DLG1_MESSAGE_TXT = "Reload session data from disk?";
	private final static String DLG1_MESSAGE_SUFFIX = " (current changes will be lost)";

	private final static String DLG2_HEADER_TXT = "Session changed on disk";
	private final static String DLG2_MESSAGE_TEXT = "Reloading data will lose all current changes, continue?";
	private final static String[] DLG2_OPTIONS = { "Yes, discard changes", "Cancel" };

	private SessionEditor editor;

	private ZonedDateTime lastModificationDate;

	public SessionEditorModificationListener(SessionEditor editor) {
		super();
		this.editor = editor;
		this.lastModificationDate = this.editor.getProject().getSessionModificationTime(this.editor.getSession());

		this.editor.getEventManager().registerActionForEvent(EditorEventType.SESSION_CHANGED_EVT,
				new DelegateEditorAction(this, "onSessionChanged"));

		this.editor.getEventManager().registerActionForEvent(EditorEventType.EDITOR_SAVED_SESSION,
				new DelegateEditorAction(this, "onSessionSaved"));
	}

	@RunOnEDT
	public void onSessionChanged(EditorEvent ee) {
		this.lastModificationDate = this.editor.getProject().getSessionModificationTime(this.editor.getSession());
	}

	@RunOnEDT
	public void onSessionSaved(EditorEvent ee) {
		this.lastModificationDate = this.editor.getProject().getSessionModificationTime(this.editor.getSession());
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		ZonedDateTime currentModifiationTime = this.editor.getProject().getSessionModificationTime(this.editor.getSession());
		if(currentModifiationTime.isAfter(this.lastModificationDate)) {
			showReloadDialog();
		}
	}

	private void showReloadDialog() {
		MessageDialogProperties props = new MessageDialogProperties();
		props.setRunAsync(true);
		props.setParentWindow(this.editor);
		props.setHeader(DLG1_HEADER_TXT);
		props.setMessage(DLG1_MESSAGE_TXT + (editor.hasUnsavedChanges() ? DLG1_MESSAGE_SUFFIX : ""));
		props.setOptions(MessageDialogProperties.yesNoOptions);
		props.setTitle("Reload Session");
		props.setListener(nativeDialogEvent -> {
			if(nativeDialogEvent.getDialogResult() == 0 /* Yes */) {
				if (editor.hasUnsavedChanges()) {
					showLoseChangesDialog();
				} else {
					editor.getEventManager().queueEvent(new EditorEvent(EditorEventType.EDITOR_RELOAD_FROM_DISK));
				}
			}
		});
		NativeDialogs.showMessageDialog(props);
	}

	private void showLoseChangesDialog() {
		MessageDialogProperties props = new MessageDialogProperties();
		props.setRunAsync(true);
		props.setParentWindow(this.editor);
		props.setHeader(DLG2_HEADER_TXT);
		props.setMessage(DLG2_MESSAGE_TEXT);
		props.setOptions(DLG2_OPTIONS);
		props.setTitle("Reload Session");
		props.setListener(nativeDialogEvent -> {
			if(nativeDialogEvent.getDialogResult() == 0 /* Yes */)
				editor.getEventManager().queueEvent(new EditorEvent(EditorEventType.EDITOR_RELOAD_FROM_DISK));
		});
		NativeDialogs.showMessageDialog(props);
	}

	@Override
	public void windowLostFocus(WindowEvent e) {}

}
