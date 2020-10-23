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
package ca.phon.app.session.editor.view.tier_management;

import java.util.*;

import javax.swing.table.*;

import ca.phon.session.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;

public class TierOrderingTableModel extends AbstractTableModel {
	
	/** The columns */
	public static enum TierOrderingTableColumn {
		LOCK_TIER,
		SHOW_TIER,
		TIER_NAME,
		GROUP_TIER,
		TIER_FONT;
		
		private final String[] columnNames = {
				"Locked",
				"Show Tier",
				"Tier Name",
				"Grouped",
				"Tier Font"
		};
		
		private final Class<?>[] columnClasses = {
			Boolean.class,
			Boolean.class,
			String.class,
			Boolean.class,
			String.class
		};
		
		public String getName() {
			return columnNames[ordinal()];
		}
		
		public Class<?> getClazz() {
			return columnClasses[ordinal()];
		}
	};
	
	/** Session */
	private Session session;

	/** Tier order list */
	private List<TierViewItem> tierView;

	/** Constructor */
	public TierOrderingTableModel(Session session, 
			List<TierViewItem> tierView) {
		super();
		
		this.session = session;
		this.tierView = tierView;
	}
	
	@Override
	public int getColumnCount() {
		return TierOrderingTableColumn.values().length;
	}
	
	@Override
	public String getColumnName(int col) {
		return TierOrderingTableColumn.values()[col].getName();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return TierOrderingTableColumn.values()[col].getClazz();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean isEditable = false;
		
		if(columnIndex == TierOrderingTableColumn.SHOW_TIER.ordinal()
				|| columnIndex == TierOrderingTableColumn.LOCK_TIER.ordinal())
			isEditable = true;
		
		return isEditable;
	}

	public TierViewItem[] getTierView() {
		TierViewItem retVal[] = new TierViewItem[0];
		
		synchronized(tierView) {
			retVal = tierView.toArray(new TierViewItem[0]);
		}
		
		return retVal;
	}
	
	public void setTierView(List<TierViewItem> tierView) {
		this.tierView.clear();
		this.tierView.addAll(tierView);
		super.fireTableDataChanged();
	}
	
	public void setTierView(TierViewItem[] tierView) {
		setTierView(Arrays.asList(tierView));
	}
	
	@Override
	public int getRowCount() {
		return getTierView().length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final TierViewItem tv = getTierView()[rowIndex];
		final TierDescription tierDesc = getTierDescription(tv.getTierName());
		
		Object retVal = null;
		
		if (columnIndex == TierOrderingTableColumn.LOCK_TIER.ordinal()) {
			retVal = tv.isTierLocked();
		} else if (columnIndex == TierOrderingTableColumn.SHOW_TIER.ordinal()) {
			retVal = tv.isVisible();
		}  else if (columnIndex == TierOrderingTableColumn.TIER_NAME.ordinal()) {
			retVal = tv.getTierName();
		} else if (columnIndex == TierOrderingTableColumn.GROUP_TIER.ordinal()) {
			retVal = (tierDesc == null ? false : tierDesc.isGrouped());
		} else if (columnIndex == TierOrderingTableColumn.TIER_FONT.ordinal()) {
			retVal = (tv.getTierFont().equals("default") ?
					PrefHelper.get(FontPreferences.TIER_FONT, FontPreferences.DEFAULT_TIER_FONT) : tv.getTierFont());
		}
		
		return retVal;
	}

	private TierDescription getTierDescription(String tierName) {
		TierDescription retVal = null;
		
		final SystemTierType systemTier = SystemTierType.tierFromString(tierName);
		if(systemTier != null) {
			final SessionFactory factory = SessionFactory.newFactory();
			retVal = factory.createTierDescription(tierName, systemTier.isGrouped());
		} else {
			for(TierDescription userTier:session.getUserTiers()) {
				if(userTier.getName().equals(tierName)) { 
					retVal = userTier;
				}
			}
		}
		
		return retVal;
	}
	
//	private void toggleTierLock(int rowIndex) {
//		final SessionFactory factory = SessionFactory.newFactory();
//		final TierViewItem[] tierView = getTierView();
//		final TierViewItem tv = tierView[rowIndex];
//		final TierViewItem newItem = factory.createTierViewItem(tv.getTierName(), tv.isVisible(), tv.getTierFont(), !tv.isTierLocked());
//		tierView[rowIndex] = newItem;
//		setTierView(tierView);
//		super.fireTableCellUpdated(rowIndex, 
//				TierOrderingTableColumn.LOCK_TIER.ordinal());
//	}
	
	
}
