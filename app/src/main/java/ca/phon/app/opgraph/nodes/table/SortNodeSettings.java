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
package ca.phon.app.opgraph.nodes.table;

import java.util.ArrayList;
import java.util.List;

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
		settings.sorting.clear();
		
		for(SortColumn sc:this.sorting) {
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
