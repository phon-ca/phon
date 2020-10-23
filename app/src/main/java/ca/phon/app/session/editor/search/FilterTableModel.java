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
package ca.phon.app.session.editor.search;

import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.RowFilter.*;
import javax.swing.event.*;
import javax.swing.table.*;

import ca.phon.worker.*;

/**
 * A table model wrapper that uses a {@link SwingWorker} to
 * perform row filtering.  For large data models, this is a
 * better method than setting the filter on the {@link TableRowSorter}.
 * 
 *
 */
public class FilterTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 5718640634729073280L;

	/**
	 * Delegate data model
	 */
	private final TableModel model;
	
	/**
	 * Row mappings
	 */
	private final List<Integer> rowKeys =
			Collections.synchronizedList(new ArrayList<Integer>());
	
	private RowFilter<TableModel, Integer> rowFilter;
	
	private final TableModelListener listener = new TableModelListener() {
		
		@Override
		public void tableChanged(TableModelEvent arg0) {
			int firstRow = arg0.getFirstRow();
			int lastRow = arg0.getLastRow();
			
			// structure change
			if(firstRow < 0 && lastRow < 0 && arg0.getColumn() < 0) {
				fireTableStructureChanged();
				return;
			}
			
			if(firstRow == lastRow) {
				int myRow = rowKeys.indexOf(firstRow);
				if(arg0.getType() == TableModelEvent.DELETE) {
					if(myRow >= 0) {
						rowKeys.remove(myRow);
						fireTableRowsDeleted(myRow, myRow);
					}
				} else if(arg0.getType() == TableModelEvent.UPDATE) {
					final FilterEntry entry = new FilterEntry(firstRow);
					if(myRow >= 0) {
						if(rowFilter.include(entry)) {
							fireTableRowsUpdated(myRow, myRow);
						} else {
							rowKeys.remove(myRow);
							fireTableRowsDeleted(myRow, myRow);
						}
					} else {
						if(rowFilter.include(entry)) {
							rowKeys.add(firstRow);
							Collections.sort(rowKeys);
							fireTableRowsInserted(rowKeys.indexOf(firstRow), rowKeys.indexOf(firstRow));
						}
					}
				} else {
					fireTableChanged(new TableModelEvent(FilterTableModel.this, myRow, myRow, arg0.getColumn(), arg0.getType()));
				}
			} else {
				setRowFilter(rowFilter);
			}
		}
		
	};
	
	public FilterTableModel(TableModel model) {
		super();
		this.model = model;
		model.addTableModelListener(listener);
	}
	
	public FilterTableModel(FilterTableModel toCopy) {
		super();
		this.model = toCopy.model;
		this.rowFilter = toCopy.rowFilter;
		for(int idx:toCopy.rowKeys) {
			rowKeys.add(idx);
		}
		model.addTableModelListener(listener);
	}
	
	@Override
	public int getColumnCount() {
		return model.getColumnCount();
	}
	
	public int modelToDelegate(int idx) {
		int retVal = -1;
		if(idx >= 0 && idx < rowKeys.size()) 
			retVal = (rowKeys.get(idx) != null ? rowKeys.get(idx) : -1);
		return retVal;
	}
	
	
	@Override
	public Class<?> getColumnClass(int col) {
		return model.getColumnClass(col);
	}
	
	@Override
	public String getColumnName(int col) {
		return model.getColumnName(col);
	}

	@Override
	public int getRowCount() {
		return rowKeys.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= 0 && rowIndex < rowKeys.size()) {
			final Integer rowKey = rowKeys.get(rowIndex);
			
			if(rowKey != null) {
				return model.getValueAt(rowKey, columnIndex);
			}
		}
		return null;
	}
	
	public RowFilter<TableModel, Integer> getRowFilter() {
		return this.rowFilter;
	}
	
	public void cleanup() {
//		exeService.shutdown();
	}
	
	private UpadateWorker worker;
	public void setRowFilter(RowFilter<TableModel, Integer> filter) {
		this.rowFilter = filter;
		
		if(worker != null && !worker.isDone()) {
			worker.stop = true;
		} 
			
		worker = new UpadateWorker();
		PhonWorker.getInstance().invokeLater(worker);
	}
	
	final class FilterEntry extends Entry<TableModel, Integer> {
		
		private int rowIdx;
		
		public FilterEntry(int rowIdx) {
			this.rowIdx = rowIdx;
		}

		@Override
		public Integer getIdentifier() {
			return rowIdx;
		}

		@Override
		public TableModel getModel() {
			return model;
		}

		@Override
		public Object getValue(int index) {
			return model.getValueAt(rowIdx, index);
		}

		@Override
		public int getValueCount() {
			return model.getColumnCount();
		} 
		
	}

	// search delegate data model and adds rows one at a time
	// to this model, firing updates for each added row
	final class UpadateWorker extends SwingWorker<Boolean, Integer> {

		boolean stop = false;
		
		@Override
		protected Boolean doInBackground() throws Exception {
			final RowFilter<TableModel, Integer> filter = getRowFilter();
			publish(-1);
			for(int i = 0; i < model.getRowCount(); i++) {
				if(stop) throw new InterruptedException();
				final FilterEntry entry = new FilterEntry(i);
				if(filter.include(entry)) {
					publish(i);
				}
			}
			return true;
		}

		@Override
		protected void process(List<Integer> chunks) {
			for(Integer rowIdx:chunks) {
				if(rowIdx < 0) {
					rowKeys.clear();
				} else {
					rowKeys.add(rowIdx);
				}
			}
		}

		@Override
		protected void done() {
			try {
				get();
				fireTableDataChanged();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
			
		}
		
		
	}
	
}
