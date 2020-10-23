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
