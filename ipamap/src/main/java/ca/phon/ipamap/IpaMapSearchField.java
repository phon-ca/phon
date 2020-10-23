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
package ca.phon.ipamap;

import javax.swing.*;

import ca.phon.ui.action.*;
import ca.phon.ui.text.*;

/**
 * Custom search field for the ipa map
 *
 */
public class IpaMapSearchField extends SearchField {
	
	public final static String SEARCH_TYPE_PROP = "_search_type_";
	
	/**
	 * Search type
	 */
	public static enum SearchType {
		ALL,
		FEATURES;
		
		private String displayTxt[] = {
				"Glyphs",
				"Features"
		};
		
		public String getDisplayString() {
			return displayTxt[ordinal()];
		}
	}
	
	private SearchType searchType = SearchType.ALL;

	public IpaMapSearchField() {
		super();
//		super.setFocusable(false);
//		WindowFocusHandler wfh = new WindowFocusHandler();
//		super.queryField.addMouseListener(wfh);
//		super.queryField.addFocusListener(wfh);
	}

	public IpaMapSearchField(String prompt) {
		super(prompt);
		setSearchType(SearchType.ALL);
		
	}
	
	public void setSearchType(SearchType st) {
		SearchType oldType = this.searchType;
		this.searchType = st;
		
		setPrompt("Search " + searchType.getDisplayString());
		super.firePropertyChange(SEARCH_TYPE_PROP, oldType, this.searchType);
	}
	
	public SearchType getSearchType() {
		return this.searchType;
	}

	@Override
	public void onShowContextMenu(PhonActionEvent pae) {
		JPopupMenu popupMenu = new JPopupMenu();
		
//		JMenuItem titleItem = new JMenuItem("-- Search Type --");
//		titleItem.setEnabled(false);
//		popupMenu.add(titleItem);
		
		for(SearchType st:SearchType.values()) {
			PhonUIAction selSearchTypeAct = new PhonUIAction(this, "onSelectSearchType", st);
			selSearchTypeAct.putValue(PhonUIAction.NAME, st.getDisplayString());
			
			JCheckBoxMenuItem itm = new JCheckBoxMenuItem(selSearchTypeAct);
			itm.setSelected(st == getSearchType());
//			JMenuItem itm = new JMenuItem(selSearchTypeAct);
			popupMenu.add(itm);
		}
		
		popupMenu.show(this, 0, getHeight());
	}
	
	/**
	 * Select search type
	 */
	public void onSelectSearchType(PhonActionEvent pae) {
		setSearchType((SearchType)pae.getData());
	}
	
//	private class WindowFocusHandler extends MouseInputAdapter implements FocusListener {
//
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			Window pWin = (Window)
//				SwingUtilities.getAncestorOfClass(Window.class, e.getComponent());
//			pWin.setFocusableWindowState(true);
//			setFocusable(true);
//			requestFocus();
//		}
//
//		@Override
//		public void focusGained(FocusEvent arg0) {
//		}
//
//		@Override
//		public void focusLost(FocusEvent arg0) {
//			setFocusable(false);
//			Window pWin = (Window)
//			SwingUtilities.getAncestorOfClass(Window.class, arg0.getComponent());
//			pWin.setFocusableWindowState(false);
//		}
//		
//	}
	
}
