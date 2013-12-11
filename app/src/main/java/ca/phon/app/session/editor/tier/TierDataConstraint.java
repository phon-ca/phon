package ca.phon.app.session.editor.tier;

/**
 * Contraint object used in TierDataLayouts.
 * 
 * 
 */
public class TierDataConstraint implements Comparable<TierDataConstraint> {

	/**
	 * Column index for tier labels
	 */
	public final static int TIER_LABEL_COLUMN = 0;
	
	/**
	 * Column index for the initial group column
	 */
	public final static int GROUP_START_COLUMN = 1;
	
	/**
	 * Column index for flat tier, flat tiers may extend beyond the current
	 * parent bounds (like group tiers) in aligned mode
	 */
	public final static int FLAT_TIER_COLUMN = -1;
	
	/**
	 * Column index for components that extend across the panel
	 */
	public final static int FULL_TIER_COLUMN = -3;
	
	/**
	 * column index
	 */
	private int columnIndex = -1;
	
	/**
	 * row index
	 * 
	 */
	private int rowIndex = -1;
	
	public TierDataConstraint() {
		this(0, 0);
	}

	public TierDataConstraint(int columnIndex, int rowIndex) {
		super();
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	@Override
	public int compareTo(TierDataConstraint o) {
		final int rowCmp = ((Integer)getRowIndex()).compareTo(o.getRowIndex());
		if(rowCmp == 0) {
			return ((Integer)Math.abs(getColumnIndex())).compareTo(Math.abs(o.getColumnIndex()));
		} else {
			return rowCmp;
		}
	}
	
	
}
