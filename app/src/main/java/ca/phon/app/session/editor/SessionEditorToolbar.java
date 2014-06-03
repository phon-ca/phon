package ca.phon.app.session.editor;

import java.lang.ref.WeakReference;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.search.SessionEditorQuickSearch;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * Session editor toolbar
 *
 */
public class SessionEditorToolbar extends JPanel {

	private static final long serialVersionUID = 8875349433878914528L;

	private final WeakReference<SessionEditor> editorRef;
	
	/**
	 * Buttons
	 */
	private JButton saveButton;
	// TODO other buttons
	
	private NavigationPanel navigationPanel;
	
	private SessionEditorQuickSearch quickSearch;
	
	public SessionEditorToolbar(SessionEditor editor) {
		super();
		editorRef = new WeakReference<SessionEditor>(editor);
		init();
	}
	
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	private void init() {
		final FormLayout layout = new FormLayout("3dlu, pref, fill:pref:grow, right:pref, 5dlu, right:pref, 3dlu",
				"3dlu, pref");
		setLayout(layout);
		final CellConstraints cc = new CellConstraints();
		
		final JPanel buttonPanel = new JPanel();
		BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(buttonLayout);
		
//		// load button icons
//		final ImageIcon saveIcon = 
//			IconManager.getInstance().getIcon("actions/filesave", IconSize.SMALL);
//		final ImageIcon reloadLayoutIcon = 
//			IconManager.getInstance().getIcon("actions/layout-content", IconSize.SMALL);
//		final ImageIcon newRecordIcon = 
//			IconManager.getInstance().getIcon("misc/record-add", IconSize.SMALL);
//		final ImageIcon deleteRecordIcon = 
//			IconManager.getInstance().getIcon("misc/record-delete", IconSize.SMALL);
//		final ImageIcon duplicateRecordIcon = 
//			IconManager.getInstance().getIcon("misc/record-duplicate", IconSize.SMALL);
		
		// save button
		final SaveSessionAction saveAction = new SaveSessionAction(getEditor());
		saveButton = new JButton(saveAction);
		saveButton.setText(null);
//		final PhonUIAction saveAction = new PhonUIAction(this, "saveSession");
//		saveAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save session");
//		saveAction.putValue(PhonUIAction.SMALL_ICON, saveIcon);
//		saveButton = new JButton(saveAction);
//		saveButton.setEnabled(getEditor().isModified());  // enable on modified flag change
		
		buttonPanel.add(saveButton);
		add(buttonPanel, cc.xy(2, 2));
		
		navigationPanel = new NavigationPanel(getEditor());
		add(navigationPanel, cc.xy(4, 2));
		
		quickSearch = new SessionEditorQuickSearch(getEditor());
		add(quickSearch.getSearchField(), cc.xy(6, 2));
	}
	
}
