package ca.phon.app.opgraph.nodes.query;

import java.util.ArrayList;
import java.util.List;

public class SortNodeSettings {

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
	
	/*
	 * Grouping column
	 */
	private SortColumn groupBy;

	private final List<SortColumn> sorting = new ArrayList<>();
	
	public SortNodeSettings() {
		super();
		
		setGroupBy(new SortColumn());
		sorting.add(new SortColumn());
	}
	
	public SortColumn getGroupBy() {
		return this.groupBy;
	}
	
	public void setGroupBy(SortColumn groupBy) {
		this.groupBy = groupBy;
	}
	
	public List<SortColumn> getSorting() {
		return this.sorting;
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
		private FeatureFamily[] featureOrder = new FeatureFamily[] { FeatureFamily.PLACE, FeatureFamily.VOICING };
		
		public FeatureFamily[] getFeatureOrder() {
			return featureOrder;
		}
		
		public void setFeatureOrder(FeatureFamily[] order) {
			this.featureOrder = order;
		}
		
	}

}
