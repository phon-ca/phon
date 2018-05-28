/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.nodes.table;

import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.apache.derby.catalog.GetProcedureColumns;

import ca.phon.ui.decorations.TitledPanel;

public class SortNodeSettings implements Cloneable {

	public static enum SortOrder {
		ASCENDING,
		DESCENDING;
		
		public static SortOrder fromString(String txt) {
			SortOrder retVal = null;
			for(SortOrder v:values()) {
				if(v.toString().equalsIgnoreCase(txt)) {
					retVal = v;
					break;
				}
			}
			return retVal;
		}
	};
	
	public static enum SortType {
		PLAIN,
		IPA
	};
	
	public static enum FeatureFamily {
		PLACE,
		MANNER,
		VOICING;
		
		public static FeatureFamily fromString(String txt) {
			FeatureFamily retVal = null;
			for(FeatureFamily v:values()) {
				if(v.toString().equalsIgnoreCase(txt)) {
					retVal = v;
					break;
				}
			}
			return retVal;
		}
	};
	
	private boolean configureAutomatically = true;
	
	private SortOrder autoSortOrder = SortOrder.ASCENDING;

	private final List<SortColumn> sorting = new ArrayList<>();
	
	public SortNodeSettings() {
		super();
		
		sorting.add(new SortColumn());
	}
	
	public boolean isConfigureAutomatically() {
		return this.configureAutomatically;
	}
	
	public void setConfigureAutomatically(boolean configureAutomatically) {
		this.configureAutomatically = configureAutomatically;
	}
	
	public SortOrder getAutoSortOrder() {
		return this.autoSortOrder;
	}
	
	public void setAutoSortOrder(SortOrder sortOrder) {
		this.autoSortOrder = sortOrder;
	}
	
	public void addColumn(String colName, SortType sortType, SortOrder sortOrder) {
		final SortColumn sc = new SortColumn();
		sc.column = colName;
		sc.type = sortType;
		sc.order = sortOrder;
		this.sorting.add(sc);
	}
	
	public List<SortColumn> getSorting() {
		return this.sorting;
	}
	
	public void clear() {
		this.sorting.clear();
	}

	public static class SortColumn {
		private String column;
		private SortType type = SortType.PLAIN;
		
		public String getColumn() {
			return this.column;
		}
		
		public void setColumn(String column) {
			this.column = column;
		}
		
		public SortType getType() {
			return type;
		}
		
		public void setType(SortType type) {
			this.type = type;
		}

		/* Plain Text options */
		private SortOrder order = SortOrder.ASCENDING;
		
		public SortOrder getOrder() {
			return this.order;
		}
		
		public void setOrder(SortOrder order) {
			this.order = order;
		}
		
		/* IPA options */
		private FeatureFamily[] featureOrder = new FeatureFamily[] { FeatureFamily.MANNER, FeatureFamily.PLACE, FeatureFamily.VOICING };
		
		public FeatureFamily[] getFeatureOrder() {
			return featureOrder;
		}
		
		public void setFeatureOrder(FeatureFamily[] order) {
			this.featureOrder = order;
		}
		
	}
	
	@Override
	public Object clone() {
		final SortNodeSettings settings = new SortNodeSettings();
		
		settings.setAutoSortOrder(getAutoSortOrder());
		settings.setConfigureAutomatically(isConfigureAutomatically());
		
		for(SortColumn sc:sorting) {
			SortColumn col = new SortColumn();
			col.column = sc.column;
			col.featureOrder = sc.featureOrder;
			col.order = sc.order;
			col.type = sc.type;
			settings.sorting.add(col);
		}
		
		return settings;
	}

}
