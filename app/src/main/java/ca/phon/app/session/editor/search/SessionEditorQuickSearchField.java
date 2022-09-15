/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.search;

import javax.swing.*;
import javax.swing.table.*;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.text.*;

/**
 * Overrides the default table search field so that the row
 * filter includes options for dealing with excluded records.
 * 
 * 
 */
public class SessionEditorQuickSearchField extends TableSearchField {
	
	private static final long serialVersionUID = -6078848699188278431L;

	public final static String INCLUDE_EXCLUDED_PROP = "include_excluded";
	
	public final static String SEARCH_TYPE_PROP = "search_type";
	
	public final static String CASE_SENSITIVE_PROP = "case_sensitive";
	
	private boolean includeExcludedRecords = false;

	private SearchType searchType = SearchType.PLAIN;
	
	private final Session session;
	
	public SessionEditorQuickSearchField(Session session, JTable table) {
		super(table);
		this.session = session;
		getTextField().setFont(FontPreferences.getTierFont().deriveFont(FontPreferences.getDefaultFontSize()));
	}
	
	@Override
	protected void setupPopupMenu(JPopupMenu menu) {
		final PhonUIAction<Void> incExcludedAct = PhonUIAction.runnable(this::toggleIncludeExcluded);
		incExcludedAct.putValue(PhonUIAction.NAME, "Include excluded records");
		incExcludedAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Include excluded records in search.");
		incExcludedAct.putValue(PhonUIAction.SELECTED_KEY, this.includeExcludedRecords);
		final JCheckBoxMenuItem incExcludedItem = new JCheckBoxMenuItem(incExcludedAct);
		menu.add(incExcludedItem);
		menu.addSeparator();
		
		ButtonGroup btnGroup = new ButtonGroup();
		
		final PhonUIAction<SearchType> usePlainAct = PhonUIAction.consumer(this::setSearchType, SearchType.PLAIN);
		usePlainAct.putValue(PhonUIAction.NAME, "Plain text");
		usePlainAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Plain text search");
		usePlainAct.putValue(PhonUIAction.SELECTED_KEY, searchType == SearchType.PLAIN);
		final JRadioButtonMenuItem usePlainItem = new JRadioButtonMenuItem(usePlainAct);
		menu.add(usePlainItem);
		
		final PhonUIAction<SearchType> useRegexAct = PhonUIAction.consumer(this::setSearchType, SearchType.REGEX);
		useRegexAct.putValue(PhonUIAction.NAME, "Regex");
		useRegexAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Regex search");
		useRegexAct.putValue(PhonUIAction.SELECTED_KEY, searchType == SearchType.REGEX);
		final JRadioButtonMenuItem useRegexItem = new JRadioButtonMenuItem(useRegexAct);
		menu.add(useRegexItem);

		final PhonUIAction<SearchType> usePhonexAct = PhonUIAction.consumer(this::setSearchType, SearchType.PHONEX);
		usePhonexAct.putValue(PhonUIAction.NAME, "Phonex");
		usePhonexAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Phonex search");
		usePhonexAct.putValue(PhonUIAction.SELECTED_KEY, searchType == SearchType.PHONEX);
		final JRadioButtonMenuItem usePhonexItem = new JRadioButtonMenuItem(usePhonexAct);
		menu.add(usePhonexItem);
		
		btnGroup.add(usePlainItem);
		btnGroup.add(useRegexItem);
		btnGroup.add(usePhonexItem);
		
		menu.addSeparator();
		super.setupPopupMenu(menu);
	}
	
	public void setSearchType(SearchType searchType) {
		final SearchType oldType = this.searchType;
		this.searchType = searchType;
		super.firePropertyChange(SEARCH_TYPE_PROP, oldType, searchType);
	}

	@Override
	public RowFilter<TableModel, Integer> getRowFilter(String expr) {
		final RowFilter<TableModel, Integer> filter = new SessionRowFilter(expr, searchType, caseSensitive);
				//super.getRowFilter(expr);
		return new RecordRowFilter(filter);
	}
	
	public void toggleIncludeExcluded() {
		final boolean oldVal = isIncludeExcludedRecords();
		this.includeExcludedRecords = !this.includeExcludedRecords;
		super.firePropertyChange(INCLUDE_EXCLUDED_PROP, oldVal, includeExcludedRecords);
	}

	public boolean isIncludeExcludedRecords() {
		return includeExcludedRecords;
	}

	public void setIncludeExcludedRecords(boolean includeExcludedRecords) {
		final boolean oldVal = isIncludeExcludedRecords();
		this.includeExcludedRecords = includeExcludedRecords;
		super.firePropertyChange(INCLUDE_EXCLUDED_PROP, oldVal, includeExcludedRecords);
	}
	
	@Override
	public void onClearText(PhonActionEvent pae) {
		setText("");
	}
	
	@Override
	public void toggleCaseSensitive() {
		final boolean oldVal = isCaseSensitive();
		this.caseSensitive = !this.caseSensitive;
		super.firePropertyChange(CASE_SENSITIVE_PROP, oldVal, this.caseSensitive);
	}

	private class RecordRowFilter extends RowFilter<TableModel, Integer> {

		private RowFilter<TableModel, Integer> filter;
		
		public RecordRowFilter(RowFilter<TableModel, Integer> filter) {
			super();
			this.filter = filter;
		}
		
		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> arg0) {
			final int idx = arg0.getIdentifier();
			
			if(idx >= 0 && idx < session.getRecordCount()) {
				final Record utt = session.getRecord(idx);
				
				boolean includeRecord = !utt.isExcludeFromSearches() || 
						(utt.isExcludeFromSearches() && includeExcludedRecords);
				if(includeRecord) {
					return filter.include(arg0);
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
	}
	
}
