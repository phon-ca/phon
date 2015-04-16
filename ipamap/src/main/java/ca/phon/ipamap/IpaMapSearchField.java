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
package ca.phon.ipamap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.SearchField;

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
