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
package ca.phon.app.session.editor.view.common;

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
	
	/*
	 * Flat tiers
	 */
	/**
	 * Flat tiers - width is max(prefSize, parentSize) 
	 */
	public final static int FLAT_TIER_COLUMN = -1;
	
	/**
	 * Flat tiers - width is prefSize of component
	 */
	public final static int FLAT_TIER_PREF_COLUMN = -2;
	
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
