package ca.phon.ipamap;

import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ca.phon.ui.SearchField;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;

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
		super.setFocusable(false);
		WindowFocusHandler wfh = new WindowFocusHandler();
		addMouseListener(wfh);
		addFocusListener(wfh);
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
	
	private class WindowFocusHandler extends MouseInputAdapter implements FocusListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			Window pWin = (Window)
				SwingUtilities.getAncestorOfClass(Window.class, e.getComponent());
			pWin.setFocusableWindowState(true);
			setFocusable(true);
			requestFocus();
		}

		@Override
		public void focusGained(FocusEvent arg0) {
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			setFocusable(false);
			Window pWin = (Window)
			SwingUtilities.getAncestorOfClass(Window.class, arg0.getComponent());
			pWin.setFocusableWindowState(false);
		}
		
	}
	
}
