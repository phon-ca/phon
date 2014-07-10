package ca.phon.app.session.editor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.DuplicateRecordAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
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
	
	private JButton viewBtn;
	
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
		final FormLayout layout = new FormLayout(
				"3dlu, pref, 3dlu, pref, 3dlu, pref, "
				+ "fill:pref:grow, right:pref, 5dlu, right:pref, 3dlu",
				"3dlu, pref");
		setLayout(layout);
		final CellConstraints cc = new CellConstraints();
		
		// save button
		final SaveSessionAction saveAction = new SaveSessionAction(getEditor());
		saveButton = new JButton(saveAction);
		saveButton.setText(null);
		add(saveButton, cc.xy(2, 2));
		
		final ImageIcon reloadLayoutIcon = 
				IconManager.getInstance().getIcon("actions/layout-content", IconSize.SMALL);
		final PhonUIAction showViewMenuAct = new PhonUIAction(this, "showViewMenu");
		showViewMenuAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show view menu");
		showViewMenuAct.putValue(PhonUIAction.SMALL_ICON, reloadLayoutIcon);
		viewBtn = new JButton(showViewMenuAct);
		add(viewBtn, cc.xy(4,2));
		
		final ButtonGroup btnGrp = new ButtonGroup();
		final List<JButton> buttons = SegmentedButtonBuilder.createSegmentedButtons(3, btnGrp);
		
		final NewRecordAction newRecordAct = new NewRecordAction(getEditor());
		buttons.get(0).setAction(newRecordAct);
		buttons.get(0).setText(null);
		
		final DuplicateRecordAction dupRecordAct = new DuplicateRecordAction(getEditor());
		buttons.get(1).setAction(dupRecordAct);
		buttons.get(1).setText(null);
		
		final DeleteRecordAction delRecordAct = new DeleteRecordAction(getEditor());
		buttons.get(2).setAction(delRecordAct);
		buttons.get(2).setText(null);
		
		final JComponent btnComp = SegmentedButtonBuilder.createLayoutComponent(buttons);
		
		add(btnComp, cc.xy(6,2));

		navigationPanel = new NavigationPanel(getEditor());
		add(navigationPanel, cc.xy(8, 2));
		
		quickSearch = new SessionEditorQuickSearch(getEditor());
		add(quickSearch.getSearchField(), cc.xy(10, 2));
	}
	
	public void showViewMenu() {
		final SessionEditor editor = getEditor();
		
		final JPopupMenu menu = new JPopupMenu();
		
		editor.getViewModel().setupPerspectiveMenu(menu);
		menu.addSeparator();
		editor.getViewModel().setupViewMenu(menu);
		
		menu.show(viewBtn, 0, viewBtn.getHeight());
	}
	
}
