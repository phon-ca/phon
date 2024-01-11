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
package ca.phon.app.session.editor.view.tierManagement;

import ca.phon.session.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class TierOrderingTableModel extends AbstractTableModel {
	
	/** The columns */
	public static enum TierOrderingTableColumn {
		LOCK_TIER,
		SHOW_TIER,
		BLIND,
		TIER_NAME,
		CHAT_TIER_NAME,
		TIER_TYPE,
		ALIGNED,
		TIER_FONT;

		private final String[] columnNames = {
				"Locked",
				"Visible",
				"Blind",
				"Tier Name",
				"CHAT Tier Name",
				"Type",
				"Aligned",
				"Tier Font"
		};
		
		private final Class<?>[] columnClasses = {
			Boolean.class,
			Boolean.class,
			Boolean.class,
			String.class,
			String.class,
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
	private final Session session;

	/** Tier order list */
	private final List<TierViewItem> tierView;

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
				|| columnIndex == TierOrderingTableColumn.LOCK_TIER.ordinal()
				|| columnIndex == TierOrderingTableColumn.BLIND.ordinal()
				|| columnIndex == TierOrderingTableColumn.ALIGNED.ordinal())
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
		final SystemTierType systemTier = SystemTierType.tierFromString(tv.getTierName());
		final UserTierType userTierType = UserTierType.fromPhonTierName(tv.getTierName());
		Object retVal = null;
		if (columnIndex == TierOrderingTableColumn.LOCK_TIER.ordinal()) {
			retVal = tv.isTierLocked();
		} else if (columnIndex == TierOrderingTableColumn.SHOW_TIER.ordinal()) {
			retVal = tv.isVisible();
		} else if (columnIndex == TierOrderingTableColumn.BLIND.ordinal()) {
			retVal = tierDesc.isBlind();
		}  else if (columnIndex == TierOrderingTableColumn.TIER_NAME.ordinal()) {
			retVal = tv.getTierName();
		} else if (columnIndex == TierOrderingTableColumn.CHAT_TIER_NAME.ordinal()) {
			if(systemTier != null) {
				retVal = (systemTier == SystemTierType.Orthography ? "*<ID>" : systemTier.getChatTierName());
			} else {
				if(userTierType != null) {
					retVal = userTierType.getChatTierName();
				} else {
					retVal = UserTierType.determineCHATTierName(session, tv.getTierName());
				}
			}
		} else if (columnIndex == TierOrderingTableColumn.TIER_TYPE.ordinal()) {
			return tierDesc.getDeclaredType().getSimpleName();
		} else if (columnIndex == TierOrderingTableColumn.ALIGNED.ordinal()) {
			retVal = !tierDesc.isExcludeFromAlignment();
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
			retVal = factory.createTierDescription(systemTier);
		} else {
			for(TierDescription userTier:session.getUserTiers()) {
				if(userTier.getName().equals(tierName)) { 
					retVal = userTier;
				}
			}
		}
		return retVal;
	}
	
}
