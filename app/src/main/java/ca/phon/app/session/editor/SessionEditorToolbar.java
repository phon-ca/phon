/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.DuplicateRecordAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.search.SessionEditorQuickSearch;

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
		saveButton.setEnabled(getEditor().isModified());
		add(saveButton, cc.xy(2, 2));
		
		final EditorAction modifiedAct = new DelegateEditorAction(this, "onModifiedChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.MODIFIED_FLAG_CHANGED, modifiedAct);
		
		final ButtonGroup btnGrp = new ButtonGroup();
		final List<JButton> buttons = SegmentedButtonBuilder.createSegmentedButtons(3, btnGrp);
		
		final NewRecordAction newRecordAct = new NewRecordAction(getEditor());
		buttons.get(0).setAction(newRecordAct);
		buttons.get(0).setText(null);
		buttons.get(0).setFocusable(false);
		
		final DuplicateRecordAction dupRecordAct = new DuplicateRecordAction(getEditor());
		buttons.get(1).setAction(dupRecordAct);
		buttons.get(1).setText(null);
		buttons.get(1).setFocusable(false);
		
		final DeleteRecordAction delRecordAct = new DeleteRecordAction(getEditor());
		buttons.get(2).setAction(delRecordAct);
		buttons.get(2).setText(null);
		buttons.get(2).setFocusable(false);
		
		final JComponent btnComp = SegmentedButtonBuilder.createLayoutComponent(buttons);
		
		add(btnComp, cc.xy(6,2));

		navigationPanel = new NavigationPanel(getEditor());
		add(navigationPanel, cc.xy(8, 2));
		
		quickSearch = new SessionEditorQuickSearch(getEditor());
		add(quickSearch.getSearchField(), cc.xy(10, 2));
	}
	
	@RunOnEDT
	public void onModifiedChanged(EditorEvent ee) {
		saveButton.setEnabled(getEditor().isModified());
	}
	
}
