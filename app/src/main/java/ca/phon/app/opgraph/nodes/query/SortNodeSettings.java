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
	private String groupBy;

	private final List<SortColumn> sorting = new ArrayList<>();
	
	public SortNodeSettings() {
		super();
	}
	
	public String getGroupBy() {
		return this.groupBy;
	}
	
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	
	public List<SortColumn> getSorting() {
		return this.sorting;
	}
		
	public static abstract class SortColumn {
		private String column;

		public abstract SortType getType();
		
		public String getColumn() {
			return this.column;
		}
		
		public void setColumn(String column) {
			this.column = column;
		}
	}
	
	public static class PlainSortColumn extends SortColumn {
		private SortOrder order = SortOrder.ASCENDING;
		
		public SortOrder getOrder() {
			return this.order;
		}
		
		public void setOrder(SortOrder order) {
			this.order = order;
		}
		
		public SortType getType() {
			return SortType.PLAIN;
		}
	}

	public static class IPASortColumn extends SortColumn {
		private FeatureFamily[] order = new FeatureFamily[] { FeatureFamily.PLACE, FeatureFamily.VOICING };
		
		public FeatureFamily[] getOrder() {
			return order;
		}
		
		public void setOrder(FeatureFamily[] order) {
			this.order = order;
		}
		
		public SortType getType() {
			return SortType.IPA;
		}
	}
}
