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
package ca.phon.app.query.report;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;


/**
 * Select a list of tiers from a table.
 *
 */
public class TierSelector extends JComponent {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(TierSelector.class.getName());
	
	public final static String SELECTION_PROP = "_TIER_SELECTION_";
	public final static String LOADING_FINISHED = "_TIERS_LOADED_";
	public final static String LOADING_STARTED = "_TIERS_LOADING_";
	/* UI */
	private JXTable table;
	private TierSelectionTableModel tableModel;
	
	/** Project */
	private Project project;
	
	public TierSelector(Project project) {
		super();
		
		this.project = project;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		tableModel = new TierSelectionTableModel();
//		tableModel.addTableModelListener(new TableModelListener() {
//			
//			@Override
//			public void tableChanged(TableModelEvent e) {
//				TierSelector.this.firePropertyChange(SELECTION_PROP, false, true);
//			}
//		});
		table = new JXTable(tableModel);
		
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	public String[] getSelectedTiers() {
		return tableModel.getTiers();
	}
	
	public void setSelected(String tier, boolean v) {
		tableModel.tiers.put(tier, v);
		tableModel.fireTableDataChanged();
	}
	
	/* 
	 * Table model.
	 * Most of the logic is here
	 */
	private class TierSelectionTableModel extends AbstractTableModel {
		
		Map<String, Boolean> tiers = 
			new LinkedHashMap<String, Boolean>();
		
		public TierSelectionTableModel() {
			super();
			
			PhonTask pt = new PhonTask() {

				@Override
				public void performTask() {
					super.setStatus(TaskStatus.RUNNING);
					
					TierSelector.super.firePropertyChange(LOADING_STARTED, false, true);
					
					for(SystemTierType stt:SystemTierType.values()) {
						if(stt != SystemTierType.Segment &&
								stt != SystemTierType.ActualSyllables &&
								stt != SystemTierType.TargetSyllables &&
								stt != SystemTierType.SyllableAlignment) {
							synchronized(tiers) {
								tiers.put(stt.getName(), false);
							}
							TierSelectionTableModel.super.fireTableDataChanged();
						}
					}
					
					if(project != null) {
						for(String corpus:project.getCorpora()) {
							for(String session:project.getCorpusSessions(corpus)) {
								try {
									Session t = project.openSession(corpus, session);
									
									for(int i = 0; i < t.getUserTierCount(); i++) {
										final TierDescription depTierDesc = t.getUserTier(i);
										boolean changed = false;
										synchronized(tiers) {
											if(!tiers.keySet().contains(depTierDesc.getName())) {
												tiers.put(depTierDesc.getName(), false);
												changed = true;
											}
										}
										if(changed)
											TierSelectionTableModel.super.fireTableDataChanged();
									}
									
								} catch (IOException e) {
									LOGGER.warn( e.getLocalizedMessage(), e);
								}
							}
						}
					}
					
					TierSelector.super.firePropertyChange(LOADING_FINISHED, false, true);
					super.setStatus(TaskStatus.FINISHED);
				}
				
			};
			PhonWorker.getInstance().invokeLater(pt);
		}
		
		public String[] getTiers() {
			List<String> retVal = new ArrayList<String>();
			synchronized(tiers) {
				String[] keys = tiers.keySet().toArray(new String[0]);
				
				for(String k:keys) {
					if(tiers.get(k))
						retVal.add(k);
				}
			}	
			return retVal.toArray(new String[0]);
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			int retVal = 0;
			synchronized(tiers) {
				retVal = tiers.keySet().size();
			}
			return retVal;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object retVal = "";

			synchronized(tiers) {
				String[] keys = tiers.keySet().toArray(new String[0]);
				if(col == 0) {
					retVal = tiers.get(keys[row]);
				} else if (col == 1) {
					retVal = keys[row];
				}
			}
			
			return retVal;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == 0) 
				return Boolean.class;
			else
				return String.class;
		}

		@Override
		public String getColumnName(int column) {
			String retVal = " ";
			
			if(column == 1)
				retVal = "Metadata key";
			
			return retVal;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean retVal = false;
			
			if(columnIndex == 0)
				retVal = true;
			
			return retVal;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(!(aValue instanceof Boolean) || columnIndex != 0) {
				return;
			}
			Boolean v = (Boolean)aValue;
			
			synchronized(tiers) {
				String[] keys = tiers.keySet().toArray(new String[0]);
				
				tiers.put(keys[rowIndex], v);
			}
			
			firePropertyChange(SELECTION_PROP, false, true);
			super.fireTableCellUpdated(rowIndex, columnIndex);
		}
		
	}
}
