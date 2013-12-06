package ca.phon.app.session.editor.tier;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Custom layout management for tier data.
 * Data is laid out like a table, with tier data either aligned
 * vertically as below:
 * 
 * E.g., 
 * <pre>
 * +==================================+
 * | orthography | grp1 | grp2 | grp3 |
 * |----------------------------------|
 * | ipa target  | grp1 | grp2 | grp3 |
 * |----------------------------------|
 * | ipa actual  | grp1 | grp2 | grp3 |
 * |----------------------------------|
 * | segment     |                    |
 * |----------------------------------|
 * | notes       |                    |
 * |----------------------------------|
 * |  ...                             |
 * +==================================+
 * </pre>
 * 
 * or in a natural flow as below:
 * 
 * <pre>
 * +==================================+
 * | orthography | hello world ht     |
 * |----------------------------------|
 * | ipa target  |                    |
 * |----------------------------------|
 * | ipa actual  |                    |
 * |----------------------------------|
 * | segment     |                    |
 * |----------------------------------|
 * | notes       |                    |
 * |----------------------------------|
 * |  ...                             |
 * +==================================+
 * </pre>
 */
public class TierDataLayout implements LayoutManager2 {
	
	/**
	 * default width of the tier label section
	 */
	private final static int DEFAULT_TIER_LABEL_WIDTH = 150;
	
	private final static int DEFAULT_H_GAP = 5;
	
	private final static int DEFAULT_V_GAP = 10;

	/**
	 * map of component <-> constraints
	 */
	private final Map<Component, TierDataConstraint> componentMap = new WeakHashMap<>();
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		// unsupported
		throw new UnsupportedOperationException();
	}

	@Override
	public void layoutContainer(Container parent) {
		final Dimension size = parent.getSize();
		final Dimension prefSize = preferredLayoutSize(parent);
		for(Component comp:componentMap.keySet()) {
			final TierDataConstraint constraint = componentMap.get(comp);
			
			Rectangle compRect = new Rectangle();
			final int row = constraint.getRowIndex();
			if(isGrouped(row)) {
				compRect = rectForConstaint(constraint);
			} else {
				if(constraint.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN)
					compRect = rectForConstaint(constraint);
				else {
					final int rowY = getRowY(row);
					final int rowHeight = getRowHeight(row);
					final int cellX = getColX(1);
					compRect = new Rectangle(cellX, rowY, Math.max(size.width, prefSize.width) - cellX, rowHeight);
				}
			}
			comp.setBounds(compRect);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int width = DEFAULT_TIER_LABEL_WIDTH;
		
		for(int i = 1; i <= getColumnCount(); i++) {
			int colWidth = getColumnWidth(i);
			width += colWidth;
		}
		
		int height = 0;
		for(int i = 0; i < getRowCount(); i++) {
			int rowHeight = getRowHeight(i);
			height += rowHeight;
		}
		
		return new Dimension(width, height);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		componentMap.remove(comp);
	}

	public boolean hasLayoutComponent(Component comp) {
		return componentMap.containsKey(comp);
	}
	
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if(!(constraints instanceof TierDataConstraint)) 
			throw new IllegalArgumentException("layout constraint must be of type " + TierDataConstraint.class.getName());
		final TierDataConstraint constraint = TierDataConstraint.class.cast(constraints);
		componentMap.put(comp, constraint);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}
	
	/**
	 * Get the number of columns.  This is the highest
	 * number of columns.
	 * 
	 * @return columns
	 */
	public int getColumnCount() {
		int maxCol = 0;
		for(TierDataConstraint tdc:componentMap.values()) {
			maxCol = Math.max(maxCol, tdc.getColumnIndex());
		}
		return maxCol;
	}
	
	/**
	 * Get the number of rows.
	 * 
	 * @return rows
	 */
	public int getRowCount() {
		int maxRow = 0;
		for(TierDataConstraint tdc:componentMap.values()) {
			maxRow = Math.max(maxRow, tdc.getRowIndex());
		}
		return maxRow;
	}
	
	/**
	 * Get the height of the given row.
	 * 
	 * @param row
	 */
	public int getRowHeight(int row) {
		int height = 0;
		
		for(Component comp:componentMap.keySet()) {
			final TierDataConstraint tdc = componentMap.get(comp);
			if(tdc.getRowIndex() == row) {
				height = Math.max(height, comp.getPreferredSize().height);
			}
		}
		
		return height;
	}
	
	/**
	 * Get the width of the given column.
	 * 
	 * @param col
	 */
	public int getColumnWidth(int col) {
		int width = 0;
		
		if(col == TierDataConstraint.TIER_LABEL_COLUMN) {
			width = DEFAULT_TIER_LABEL_WIDTH;
		} else {
			for(Component comp:componentMap.keySet()) {
				final TierDataConstraint tdc = componentMap.get(comp);
				if(isGrouped(tdc.getRowIndex()) && tdc.getColumnIndex() == col) {
					width = Math.max(width, comp.getPreferredSize().width + 2);
				}
			}
		}
		
		return width;
	}
	
	/**
	 * Get the starting y value for the given row.
	 * 
	 * @param row
	 */
	public int getRowY(int row) {
		int y = 0;
		
		for(int i = 0; i < row; i++) {
			y += getRowHeight(i) + DEFAULT_V_GAP;
		}
		
		return y;
	}
	
	/**
	 * Get the starting x value for the given col.
	 * 
	 * @param col
	 */
	public int getColX(int col) {
		int x = 0;
		
		for(int i = 0; i < col; i++) {
			x += getColumnWidth(i) + DEFAULT_H_GAP;
		}
		
		return x;
	}
	
	/**
	 * Is the given row grouped?
	 * 
	 * @param row
	 * 
	 * @return <code>true</code> if the specified row is grouped,
	 * <code>false</code> otherwise
	 */
	public boolean isGrouped(int row) {
		boolean retVal = true;
		for(TierDataConstraint tdc:componentMap.values()) {
			if(tdc.getRowIndex() == row) {
				if(tdc.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN) {
					continue;
				} else if(tdc.getColumnIndex() == TierDataConstraint.FLAT_TIER_COLUMN) {
					retVal = false;
					break;
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Get the rectangle for the given constraint.
	 * 
	 * @param tdc
	 */
	public Rectangle rectForConstaint(TierDataConstraint tdc) {
		final int row = tdc.getRowIndex();
		final int col = tdc.getColumnIndex();
		
		final int x = getColX(col);
		final int y = getRowY(row);
		final int width = getColumnWidth(col);
		final int height = getRowHeight(row);
		
		return new Rectangle(x, y, width, height);
	}
}
