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
package ca.phon.ui.text;

import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.*;
import ca.phon.ui.action.*;
import ca.phon.util.Tuple;

import javax.swing.*;
import javax.swing.table.*;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.*;

/**
 * Search field for applying plain text and regular
 * expression row filters to tables.
 */
public class TableSearchField extends SearchField {

	/**
	 * Table
	 */
	private WeakReference<JTable> tableRef;
	
	protected boolean caseSensitive = false;
	
	/**
	 * Display or hide everything on empty filter?
	 */
	private boolean hideOnEmptyFilter = true;
	
	/**
	 * column 'label' (used in prompt)
	 */
	private String columnLabel = "column";
	
	public TableSearchField() {
		super();
	}
	
	public TableSearchField(JTable table) {
		this(table, true);
	}
	
	public TableSearchField(JTable table, boolean hideOnEmpty) {
		super();
		setTable(table);
		hideOnEmptyFilter = hideOnEmpty;
	}
	
	public void setTable(JTable table) {
		this.tableRef = (table == null ? null : new WeakReference<JTable>(table));
	}
	
	public JTable getTable() {
		return (this.tableRef != null ? this.tableRef.get() : null);
	}
	
	public String getColumnLabel() {
		return this.columnLabel;
	}
	
	public void setColumnLabel(String label) {
		this.columnLabel = label;
	}
	
	public void toggleCaseSensitive() {
		caseSensitive = !caseSensitive;
		updateTableFilter();
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}
	
	@Override
	public void onClearText(PhonActionEvent pae) {
		super.onClearText(pae);
		
		resetFilter();
	}
	
	@Override
	protected void setupPopupMenu(JPopupMenu menu) {
		if(getAction() != null) {
			menu.add(getAction());
			menu.addSeparator();
		}
		
		// add type options
		
		
		// add case sensitivity
		final PhonUIAction csAction = PhonUIAction.runnable(this::toggleCaseSensitive);
		csAction.putValue(PhonUIAction.NAME, "Case sensitive");
		final JCheckBoxMenuItem csItem = new JCheckBoxMenuItem(csAction);
		csItem.setSelected(isCaseSensitive());
		menu.add(csItem);
		
		super.setupPopupMenu(menu);
	}
	
	public void resetFilter() {
		setTableFilter(null);
	}
	
	public void updateTableFilter() {
		setTableFilter(getQuery());
	}
	
	public RowFilter<TableModel, Integer> getRowFilter(String expr) {
		RowFilter<TableModel, Integer> retVal = null;
		
		final List<ColumnFilter> colFilters = parseFilters(expr);
		retVal = new ColumnRowFilter(colFilters);

		return retVal;
	}
	
	public void setTableFilter(String expr) {
		if(getTable() == null) return;
		
		final UpdateFilterWorker worker = new UpdateFilterWorker(expr);
		worker.execute();
	}
	
	/**
	 * Parse text into a  list of ColumnFilters
	 */
	protected List<ColumnFilter> parseFilters(String filter) {
		final String filterRegex = 
				"(?:\\s*([ a-zA-Z0-9#*,]+):)?\\s*([^;]+)\\s*";
		final String exprRegex = 
				filterRegex + "(?:;" + filterRegex + ")*";
		final List<ColumnFilter> retVal = 
				new ArrayList<TableSearchField.ColumnFilter>();

		// simple case is a single character, this is most likely NOT
		// a regular expression or a phonex expression
		// return a plain text expression in this case for speedup
		if(filter == null) return retVal;
		if(filter.length() == 0) {
			final PlainColumnFilter pcf = new PlainColumnFilter(new String[]{"*"}, filter);
			retVal.add(pcf);
		} else if(filter.matches(exprRegex)) {
			final Pattern filterPattern = Pattern.compile(filterRegex);
			final Matcher filterMatcher = filterPattern.matcher(filter);
			while(filterMatcher.find()) {
				final String columnText = (filterMatcher.group(1) == null ? "*" : filterMatcher.group(1));
				final String columns[] = columnText.split(",");
				final String expr = filterMatcher.group(2);
				
				ColumnFilter colFilter = null;
				
				if(expr.startsWith("#")) {
					try {
						final PhonexPattern pattern = PhonexPattern.compile(expr.substring(1));
						colFilter = new PhonexColumnFilter(columns, pattern);
					} catch (PhonexPatternException e) {
					}
				}
				
				if(colFilter == null) {
					try {
						final Pattern pattern = Pattern.compile(expr, (!isCaseSensitive() ? Pattern.CASE_INSENSITIVE : 0));
						
						colFilter = new RegexColumnFilter(columns, pattern);
					} catch (PatternSyntaxException e1) {
						colFilter = new PlainColumnFilter(columns, expr);
					}
				}
				retVal.add(colFilter);
			}
		}
		return retVal;
	}
	
	private int getColumnIndex(String colName) {
		final JTable tbl = getTable();
		if(tbl != null) {
			final TableModel model = tbl.getModel();
			for(int i = 0; i < model.getColumnCount(); i++) {
				final String currentCol = model.getColumnName(i);
				if(colName.equalsIgnoreCase(currentCol)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Row filter based on column names
	 * and regular expressions.
	 *
	 */
	private class ColumnRowFilter extends RowFilter<TableModel, Integer> {
		
		private final List<ColumnFilter> columnFilters = 
				new ArrayList<TableSearchField.ColumnFilter>();
		
		ColumnRowFilter(List<ColumnFilter> filters) {
			super();
			this.columnFilters.addAll(filters);
		}

		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			Boolean andMatch = null;
			
			for(ColumnFilter colFilter:columnFilters) {
				
				boolean oredMatch = false;
				for(String colToCheck:colFilter.getColumns()) {
					if(colToCheck.equals("*")) {
						
						for(int i = 0; i < entry.getValueCount(); i++) {
							final String colVal = entry.getStringValue(i);
							oredMatch |= colFilter.checkColumn(colVal);
						}
						
						break; // we already matched against all columns
					} else {
						final int colIdx = getColumnIndex(colToCheck);
						if(colIdx >= 0) {
							final String colText = entry.getStringValue(colIdx);
							oredMatch |= colFilter.checkColumn(colText);
						}
					}
				}
				
				andMatch = (andMatch == null ? oredMatch : andMatch & oredMatch);
			}
			
			return (andMatch == null ? !hideOnEmptyFilter : andMatch.booleanValue());
		}
		
	}
	
	public interface ColumnFilter {
		
		public boolean checkColumn(String val);
		
		public String[] getColumns();
	
	}
	
	/**
	 * Swing worker for updating filter
	 */
	private class UpdateFilterWorker extends SwingWorker<RowFilter<TableModel, Integer>, Object> {

		private String expr;
		
		public UpdateFilterWorker(String expr) {
			super();
			this.expr = expr;
		}
		
		@Override
		protected RowFilter<TableModel, Integer> doInBackground()
				throws Exception {
			return getRowFilter(expr);
		}

		@Override
		protected void done() {
			final TableRowSorter<TableModel> sorter = 
					new TableRowSorter<TableModel>(getTable().getModel());
			RowFilter<TableModel, Integer> filter = null;
			try {
				filter = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			if(filter != null) {
				sorter.setRowFilter(filter);
			}
			getTable().setRowSorter(sorter);
		}
		
		
	}
	
	/**
	 * Column description
	 */
	private class RegexColumnFilter extends Tuple<String[], Pattern> implements ColumnFilter {
		
		public RegexColumnFilter(String[] cols, Pattern expr) {
			super(cols, expr);
		}
		
		public String[] getColumns() {
			return super.getObj1();
		}
		
		public Pattern getPattern() {
			return this.getObj2();
		}
		
		public void setColumns(String[] cols) {
			super.setObj1(cols);
		}
		
		public void setPattern(Pattern pattern) {
			super.setObj2(pattern);
		}
		
		public boolean checkColumn(String val) {
			final Pattern pattern = getPattern();
			final Matcher matcher = pattern.matcher(val);
			
			return matcher.find();
		}
		
		public boolean includeColumn(String colName) {
			for(String checkCol:getColumns()) {
				final boolean isAllCols = checkCol.equals("*");
				final boolean isCorrectCol = 
						(isCaseSensitive() ? checkCol.equals(colName) : checkCol.equalsIgnoreCase(colName));
				if(isAllCols || isCorrectCol) return true;
			}
			return false;
		}
	}
	
	private class PhonexColumnFilter extends Tuple<String[], PhonexPattern> implements ColumnFilter {
		
		public PhonexColumnFilter(String[] obj1, PhonexPattern obj2) {
			super(obj1, obj2);
		}

		@Override
		public boolean checkColumn(String val) {
			// attempt to convert the given string into a list of phones
			try {
				IPATranscript t = IPATranscript.parseIPATranscript(val);
				if(t == null) t = new IPATranscript();
				final PhonexMatcher matcher = getObj2().matcher(t);
				return matcher.find();
			} catch (ParseException e) {
			}
			
			return false;
		}

		@Override
		public String[] getColumns() {
			return super.getObj1();
		}
		
	}
	
	private class PlainColumnFilter extends Tuple<String[], String> implements ColumnFilter {

		public PlainColumnFilter(String[] obj1, String obj2) {
			super(obj1, obj2);
		}

		@Override
		public boolean checkColumn(String val) {
			final String checkVal = (isCaseSensitive() ? val : val.toLowerCase());
			final String incVal = (isCaseSensitive() ? super.getObj2() : super.getObj2().toLowerCase());
			return checkVal.contains(incVal);
		}

		@Override
		public String[] getColumns() {
			return super.getObj1();
		}
		
	}

}
