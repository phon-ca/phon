package ca.phon.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Layout components in a rigid (i.e., non-scaling) grid.  
 * Each component must be added with a <code>GridCellConstraints</code>
 * object which identifies the (x + w, y + h) cell 
 * location of the component.  Each cell is given equal
 * size and is square.
 * 
 * NOTE: This layout allows for overlaps.
 * 
 */
public class GridCellLayout implements LayoutManager2 {
	
	/**
	 * Number of rows
	 * 
	 */
	private int numRows = 1;
	
	/**
	 * Number of columns
	 */
	private int numCols = 1;

	/**
	 * Width of cells
	 */
	private int cellWidth = 10;
	
	/**
	 * Height of cells
	 */
	private int cellHeight = 10;
	
	public GridCellLayout(int numRows, int numCols) {
		super();
		this.numRows = numRows;
		this.numCols = numCols;
	}

	public GridCellLayout(int numRows, int numCols, int cellWidth,
			int cellHeight) {
		super();
		this.numRows = numRows;
		this.numCols = numCols;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
	}

	/**
	 * Components and their constraints
	 */
	private Map<Component, GridCellConstraint> comps = 
		Collections.synchronizedMap(new HashMap<Component, GridCellConstraint>());
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		// ignore... 
	}

	@Override
	public void layoutContainer(Container parent) {
		Component[] comps = parent.getComponents();
		
		for(int i = 0; i < comps.length; i++) {
			Component c = comps[i];
			
			GridCellConstraint cc = this.comps.get(c);
			if(cc == null) continue;
			
			int compx = cc.x * cellWidth;
			int compy = cc.y * cellHeight;
			int compw = cc.w * cellWidth;
			int comph = cc.h * cellHeight;
			
			c.setBounds(compx, compy, compw, comph);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return calcLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return calcLayoutSize(parent);
	}
	
	@Override
	public Dimension maximumLayoutSize(Container parent) {
		return calcLayoutSize(parent);
	}
	
	private Dimension calcLayoutSize(Container parent) {
		int w = 0;
		int h = 0;
		
		// insets
		w += parent.getInsets().left + parent.getInsets().right;
		h += parent.getInsets().top + parent.getInsets().bottom;
		
		w += numCols * cellWidth;
		h += numRows * cellHeight;
		
		return new Dimension(w, h);
	}

	@Override
	public void removeLayoutComponent(Component parent) {
		comps.remove(parent);
	}

	@Override
	public void addLayoutComponent(Component parent, Object c) {
//		if(c == null || !(c instanceof GridCellConstraint))
//			throw new IllegalArgumentException("Constraint must be of type " + GridCellConstraint.class.getName());
		GridCellConstraint constraint = null;
		if(c == null || !(c instanceof GridCellConstraint)) {
			constraint = new GridCellConstraint(0, 0);
		} else {
			constraint = (GridCellConstraint)c;
		}
		
		// TODO - constraint checks
		
		comps.put(parent, constraint);
	}

	@Override
	public float getLayoutAlignmentX(Container parent) {
		return Component.CENTER_ALIGNMENT;
	}

	@Override
	public float getLayoutAlignmentY(Container parent) {
		return Component.CENTER_ALIGNMENT;
	}

	@Override
	public void invalidateLayout(Container parent) {
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public int getNumCols() {
		return numCols;
	}

	public void setNumCols(int numCols) {
		this.numCols = numCols;
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	public int getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}

}
