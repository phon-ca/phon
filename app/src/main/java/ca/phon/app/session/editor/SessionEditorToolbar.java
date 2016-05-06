/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import ca.phon.app.opgraph.assessment.AssessmentLibrary;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.DuplicateRecordAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.search.SessionEditorQuickSearch;
import ca.phon.session.SessionPath;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		final GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		// save button
		final SaveSessionAction saveAction = new SaveSessionAction(getEditor());
		saveButton = new JButton(saveAction);
		saveButton.setText(null);
		saveButton.setEnabled(getEditor().isModified());
		add(saveButton, gbc);
		
		final EditorAction modifiedAct = new DelegateEditorAction(this, "onModifiedChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.MODIFIED_FLAG_CHANGED, modifiedAct);
		
		final ButtonGroup btnGrp = new ButtonGroup();
		final List<JButton> buttons = (new SegmentedButtonBuilder<JButton>(JButton::new)).createSegmentedButtons(3, btnGrp);
		
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
		
		final JComponent btnComp = (new SegmentedButtonBuilder<JButton>(JButton::new)).createLayoutComponent(buttons);
		
		++gbc.gridx;
		gbc.insets = new Insets(2, 5, 2, 2);
		add(btnComp, gbc);
		
		final PhonUIAction assessmentMenuAction = new PhonUIAction(this, "onShowAssessmentMenu");
		assessmentMenuAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/report", IconSize.SMALL));
		assessmentMenuAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show assessment menu");
		final JButton assessmentMenuBtn = new JButton(assessmentMenuAction);
		++gbc.gridx;
		add(assessmentMenuBtn, gbc);
		
//		++gbc.gridx;
//		add(createViewButtons(), gbc);

		navigationPanel = new NavigationPanel(getEditor());
		++gbc.gridx;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		add(Box.createHorizontalGlue(), gbc);
		++gbc.gridx;
		gbc.weightx = 0;
		add(navigationPanel, gbc);
		
		quickSearch = new SessionEditorQuickSearch(getEditor());
		++gbc.gridx;
		gbc.weightx = 1.0;
		add(quickSearch.getSearchField(), gbc);
	}
	
	public JComponent createViewButtons() {
		final EditorViewModel viewModel = getEditor().getViewModel();
		
		final Map<EditorViewCategory, List<String>> viewsByCat = 
				viewModel.getViewsByCategory();
		int numViewBtns = 0;
		for(EditorViewCategory viewCat:viewsByCat.keySet()) {
			if(viewCat == EditorViewCategory.PLUGINS) continue;
			numViewBtns += viewsByCat.get(viewCat).size();
		}
		
		final SegmentedButtonBuilder<JToggleButton> btnBuilder = 
				new SegmentedButtonBuilder<>(JToggleButton::new);
		final ButtonGroup btnGrp = new ButtonGroup();
		final List<JToggleButton> buttons = btnBuilder.createSegmentedButtons(numViewBtns, btnGrp);
		int btnIdx = 0;
		for(EditorViewCategory viewCat:viewsByCat.keySet()) {
			if(viewCat == EditorViewCategory.PLUGINS) continue;
			
			final List<String> viewNames = viewsByCat.get(viewCat);
			for(String viewName:viewNames) {
				JToggleButton btn = buttons.get(btnIdx++);
				btn.setIcon(viewModel.getViewIcon(viewName));
				btn.setSelected(viewModel.isShowing(viewName));
				btn.setToolTipText("Toggle " + viewName);
			}
		}
		
		return btnBuilder.createLayoutComponent(buttons);
	}
	
	public void onShowAssessmentMenu(PhonActionEvent pae) {
		final JButton menuBtn = (JButton)pae.getActionEvent().getSource();
		
		final JPopupMenu menu = new JPopupMenu();
		final AssessmentLibrary library = new AssessmentLibrary();
		final ArrayList<SessionPath> selectedSessions = new ArrayList<>();
		selectedSessions.add(new SessionPath(getEditor().getSession().getCorpus(), getEditor().getSession().getName()));
		
		library.setupMenu(getEditor().getProject(), selectedSessions, menu);
		menu.show(menuBtn, 0, menuBtn.getHeight());
	}
	
	@RunOnEDT
	public void onModifiedChanged(EditorEvent ee) {
		saveButton.setEnabled(getEditor().isModified());
	}
	
}
