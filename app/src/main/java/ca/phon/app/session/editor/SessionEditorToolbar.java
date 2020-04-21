/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.phon.app.opgraph.analysis.AnalysisLibrary;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.actions.DuplicateRecordAction;
import ca.phon.app.session.editor.actions.NewRecordAction;
import ca.phon.app.session.editor.actions.PlayAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.PlayCustomSegmentAction;
import ca.phon.app.session.editor.actions.PlaySegmentAction;
import ca.phon.app.session.editor.actions.PlaySpeechTurnAction;
import ca.phon.app.session.editor.actions.SaveSessionAction;
import ca.phon.app.session.editor.search.SessionEditorQuickSearch;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.timeline.SegmentLabel;
import ca.phon.session.SessionPath;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
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
	
	private DropDownButton viewLayoutButton;
	
	private DropDownButton playSegmentButton;
	
	private SessionEditorQuickSearch quickSearch;
	
	public SessionEditorToolbar(SessionEditor editor) {
		super();
		editorRef = new WeakReference<SessionEditor>(editor);
		init();
		
		editor.getMediaModel().getSegmentPlayback().addPropertyChangeListener(segmentPlaybackListener);
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
		saveButton.setEnabled(getEditor().hasUnsavedChanges());
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
		
		JPopupMenu viewLayoutMenu = new JPopupMenu();
		viewLayoutMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				viewLayoutMenu.removeAll();
				getEditor().getViewModel().setupLayoutMenu(viewLayoutMenu);
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		final ImageIcon viewLayoutIcn = IconManager.getInstance().getIcon("actions/layout-content", IconSize.SMALL);
		final PhonUIAction viewLayoutAct = new PhonUIAction(this, "noop");
		viewLayoutAct.putValue(PhonUIAction.SMALL_ICON, viewLayoutIcn);
		viewLayoutAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show view layout menu");
		viewLayoutAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		viewLayoutAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		viewLayoutAct.putValue(DropDownButton.BUTTON_POPUP, viewLayoutMenu);
		viewLayoutButton = new DropDownButton(viewLayoutAct);
		viewLayoutButton.setOnlyPopup(true);
		
		++gbc.gridx;
		add(viewLayoutButton, gbc);
		
		JPopupMenu playSegmentMenu = new JPopupMenu();
		playSegmentMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				playSegmentMenu.removeAll();
				setupPlaySegmentMenu(new MenuBuilder(playSegmentMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		final ImageIcon playIcn = IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL);
		final PhonUIAction playSegmentAct = new PhonUIAction(this, "playPause");
		playSegmentAct.putValue(PhonUIAction.NAME, "Play segment");
		playSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play segment");
		playSegmentAct.putValue(PhonUIAction.SMALL_ICON, playIcn);
		playSegmentAct.putValue(DropDownButton.BUTTON_POPUP, playSegmentMenu);
		playSegmentButton = new DropDownButton(playSegmentAct);
		
		++gbc.gridx;
		add(playSegmentButton, gbc);

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
	
	public void playPause(PhonActionEvent pae) {
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		final SegmentPlayback segPlayback = mediaModel.getSegmentPlayback();
		if(segPlayback.isPlaying()) {
			segPlayback.stopPlaying();
		} else {
			(new PlaySegmentAction(getEditor())).actionPerformed(pae.getActionEvent());
		}
	}
	
	private void setupPlaySegmentMenu(MenuBuilder builder) {
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		final SegmentPlayback segPlayback = mediaModel.getSegmentPlayback();
		
		if(segPlayback.isPlaying()) {
			final PhonUIAction stopAct = new PhonUIAction(segPlayback, "stopPlaying");
			stopAct.putValue(PhonUIAction.NAME, "Stop playback");
			stopAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
			stopAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Stop segment playback");
			builder.addItem(".", stopAct);
			
			builder.addSeparator(".", "s1");
		}
		
		boolean enabled = (mediaModel.isSessionAudioAvailable() || 
				(mediaModel.isSessionMediaAvailable() && getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)));
		builder.addItem(".", new PlaySegmentAction(getEditor())).setEnabled(enabled);
		builder.addItem(".", new PlayCustomSegmentAction(getEditor())).setEnabled(enabled);
		builder.addItem(".", new PlaySpeechTurnAction(getEditor())).setEnabled(enabled);
		builder.addItem(".", new PlayAdjacencySequenceAction(getEditor())).setEnabled(enabled);
	}
	
	@RunOnEDT
	public void onModifiedChanged(EditorEvent ee) {
		saveButton.setEnabled(getEditor().hasUnsavedChanges());
	}

	private final PropertyChangeListener segmentPlaybackListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			SegmentPlayback segPlayback = (SegmentPlayback)evt.getSource();
			if(SegmentPlayback.PLAYBACK_PROP.equals(evt.getPropertyName())) {
				if(segPlayback.isPlaying()) {
					playSegmentButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
					playSegmentButton.setText("Stop playback");
				} else {
					playSegmentButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
					playSegmentButton.setText("Play segment");
				}
			}
		}
		
	};
	
}
