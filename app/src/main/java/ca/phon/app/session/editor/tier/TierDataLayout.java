package ca.phon.app.session.editor.tier;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

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
	 * Enumeration to determine how groups across
	 * tiers are setup
	 */
	public static enum GroupMode {
		/**
		 * Align groups across tiers, the layout
		 * will continue to increase in horizontal
		 * space as group data grows 
		 */
		ALIGNED, 
		/**
		 * Left-align and wrap information by group
		 *
		 */
		WRAPPED;
	}
	
	
	/**
	 * default width of the tier label section
	 */
	private final static int DEFAULT_TIER_LABEL_WIDTH = 150;
	
	private final static int DEFAULT_H_GAP = 5;
	
	private final static int DEFAULT_V_GAP = 10;

	/**
	 * map of component <-> constraints
	 */
	private final Map<Component, TierDataConstraint> constraintMap = new WeakHashMap<>();

	private final Map<TierDataConstraint, Component> componentMap = new HashMap<>();
	
	private final ArrayList<Rectangle> rowRects = new ArrayList<>();
	
	/**
	 * cached size calculations
	 */
	private final Map<TierDataConstraint, Rectangle> cachedRects =
			Collections.synchronizedMap(new HashMap<TierDataConstraint, Rectangle>());
	
	private final AtomicReference<Dimension> prefSizeRef = new AtomicReference<>();
	
	private volatile GroupMode groupMode = GroupMode.ALIGNED;
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		// unsupported
		throw new UnsupportedOperationException();
	}

	@Override
	public void layoutContainer(Container parent) {
		for(Component comp:constraintMap.keySet()) {
			final TierDataConstraint constraint = constraintMap.get(comp);
			Rectangle rect = new Rectangle();
			if(getGroupMode() == GroupMode.ALIGNED) {
				rect = alignedRectForConstaint(constraint, parent);
			} else if(getGroupMode() == GroupMode.WRAPPED) {
				rect = wrappedRectForConstraint(constraint, parent);
			}
			comp.setBounds(rect);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Dimension prefSize = preferredLayoutSize(parent);
		prefSize.width = DEFAULT_TIER_LABEL_WIDTH + DEFAULT_H_GAP;
		return prefSize;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Dimension retVal = prefSizeRef.get();
		if(retVal == null) {
			retVal = calcLayoutSize(parent, false);
			prefSizeRef.getAndSet(retVal);
		}
		return retVal;
	}

	private Dimension calcLayoutSize(Container parent, boolean fill) {
		Dimension retVal = new Dimension();
		if(getGroupMode() == GroupMode.ALIGNED) {
			int width = DEFAULT_TIER_LABEL_WIDTH;
			
			for(int i = 1; i <= getColumnCount(); i++) {
				int colWidth = calcAlignedGroupColumnWidth(i);
				width += colWidth + DEFAULT_H_GAP;
			}
			
			int height = 0;
			for(int i = 0; i < getRowCount(); i++) {
				int rowHeight = calcAlignedRowHeight(i);
				height += rowHeight + DEFAULT_V_GAP;
			}
			if(fill)
				retVal = new Dimension(Math.max(width, parent.getSize().width), Math.max(height, parent.getSize().height));
			else
				retVal = new Dimension(width, height);
		} else if(getGroupMode() == GroupMode.WRAPPED) {
			if(cachedRects.size() == 0)
				calcLayout(parent);
			int width = DEFAULT_TIER_LABEL_WIDTH + DEFAULT_H_GAP;
			int maxGroupWidth = 0;
			int height = 0;
			
			for(TierDataConstraint tdc:cachedRects.keySet()) {
				final Rectangle rect = cachedRects.get(tdc);
				if(tdc.getColumnIndex() != TierDataConstraint.TIER_LABEL_COLUMN) {
					maxGroupWidth = Math.max(maxGroupWidth, rect.width);
				}
				height = Math.max(height, rect.y + rect.height);
			}
			retVal = new Dimension(width + maxGroupWidth, height);
		}
		return retVal;
	}
	
	@Override
	public void removeLayoutComponent(Component comp) {
		final TierDataConstraint tdc = constraintMap.remove(comp);
		componentMap.remove(tdc);
	}

	public boolean hasLayoutComponent(Component comp) {
		return constraintMap.containsKey(comp);
	}
	
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if(!(constraints instanceof TierDataConstraint)) 
			throw new IllegalArgumentException("layout constraint must be of type " + TierDataConstraint.class.getName());
		final TierDataConstraint constraint = TierDataConstraint.class.cast(constraints);
		constraintMap.put(comp, constraint);
		componentMap.put(constraint, comp);
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
		cachedRects.clear();
		prefSizeRef.getAndSet(null);
		rowRects.clear();
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		Dimension retVal = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	/**
	 * Get the number of columns.  This is the highest
	 * number of columns.
	 * 
	 * @return columns
	 */
	public int getColumnCount() {
		int maxCol = 0;
		for(TierDataConstraint tdc:constraintMap.values()) {
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
		for(TierDataConstraint tdc:constraintMap.values()) {
			maxRow = Math.max(maxRow, tdc.getRowIndex());
		}
		return maxRow;
	}
	
	/**
	 * Return the group mode
	 * 
	 * @return group mode
	 */
	public GroupMode getGroupMode() {
		return this.groupMode;
	}
	
	public void setGroupMode(GroupMode mode) {
		this.groupMode = mode;
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
		for(TierDataConstraint tdc:constraintMap.values()) {
			if(tdc.getRowIndex() == row) {
				if(tdc.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN) {
					continue;
				} else if(tdc.getColumnIndex() == TierDataConstraint.FLAT_TIER_COLUMN 
						|| tdc.getColumnIndex() == TierDataConstraint.FULL_TIER_COLUMN) {
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
	 * @param parent
	 * @param x
	 * @param y
	 */
	private Rectangle alignedRectForConstaint(TierDataConstraint tdc, Container parent) {
		Rectangle retVal = cachedRects.get(tdc);
		if(retVal == null) {			
			final int row = tdc.getRowIndex();
			final int col = tdc.getColumnIndex();
			
			int x = 0, y = 0;
			int width = 0, height = 0;
			
			x = calcAlignedColumnX(col, parent);
			y = calcAlignedRowY(row);
			width = calcAlignedColumnWidth(col, parent);
			height = calcAlignedRowHeight(row);
			
			retVal = new Rectangle(x, y, width, height);
			cachedRects.put(tdc, retVal);
		}
		return retVal;
	}
	
	private Rectangle wrappedRectForConstraint(TierDataConstraint tdc, Container parent) {
		Rectangle retVal = cachedRects.get(tdc);
		if(retVal == null) {
			// layout has not been calculated
			calcLayout(parent);
			retVal = cachedRects.get(tdc);
		}
		if(tdc.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN) {
			// adjust row height
			retVal.height = 
					(tdc.getRowIndex() < rowRects.size() ? rowRects.get(tdc.getRowIndex()).height : 0);
		}
		return retVal;
	}
	
	/*
	 * Calculation method for WRAPPED group mode
	 */
	private void calcLayout(Container parent) {
		// get an ordered list of constraints
		final List<TierDataConstraint> orderedConstraints = 
				new ArrayList<>(constraintMap.values());
		Collections.sort(orderedConstraints);
		
		// keep a reference to each row's bounding rectangle
		rowRects.clear();
		Rectangle rowRect = new Rectangle();
		rowRect.x = 0; rowRect.y = 0;
		int currentRow = 0;
		rowRects.add(rowRect);
		
		int currentX = 0;
		int currentY = 0;
		
		final Dimension size = parent.getSize();
		
		for(TierDataConstraint constraint:orderedConstraints) {
			// check for a row change
			if(currentRow != constraint.getRowIndex()) {
				currentX = 0;
				currentY = rowRect.y + rowRect.height + DEFAULT_V_GAP;
				rowRect = new Rectangle();
				rowRect.x = currentX;
				rowRect.y = currentY;
				
				rowRects.add(rowRect);
				currentRow = constraint.getRowIndex();
			}
			
			final Component comp = componentMap.get(constraint);
			final Dimension prefSize = comp.getPreferredSize();
			
			int compX = currentX;
			int compY = currentY;
			int compWidth = constraint.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN ? DEFAULT_TIER_LABEL_WIDTH : prefSize.width + 2;
			int compHeight = prefSize.height;
			
			// wrap components
			if(compX + compWidth > size.width && constraint.getColumnIndex() > TierDataConstraint.GROUP_START_COLUMN) {
				compX = DEFAULT_TIER_LABEL_WIDTH + DEFAULT_H_GAP;
				compY = rowRect.y + rowRect.height + DEFAULT_V_GAP;
			}
			
			final Rectangle compRect = new Rectangle(compX, compY, compWidth, compHeight);
			rowRect.add(compRect);
			cachedRects.put(constraint, compRect);
			
			currentX = compRect.x + compRect.width + DEFAULT_H_GAP;
			currentY = compRect.y;
		}
	}
	
	// calculate the x coord of the given group
	// when the GroupMode is ALIGNED
	private int calcAlignedColumnX(int col, Container parent) {
		int x = 0;
		
		if(col == TierDataConstraint.TIER_LABEL_COLUMN) {
			x = 0;
		} else if(col == TierDataConstraint.FULL_TIER_COLUMN) {
			x = 0;
		} else if(col == TierDataConstraint.FLAT_TIER_COLUMN) {
			x = DEFAULT_TIER_LABEL_WIDTH + DEFAULT_H_GAP;
		} else {
			x = DEFAULT_TIER_LABEL_WIDTH + DEFAULT_H_GAP;
			for(int i = TierDataConstraint.GROUP_START_COLUMN; i < col; i++) {
				x += calcAlignedColumnWidth(i, parent) + DEFAULT_H_GAP;
			}
		}
		
		return x;
	}
	
	// calculate the size of the given groups width
	// when the GroupMode is ALIGNED
	private int calcAlignedColumnWidth(int col, Container parent) {
		int width = 0;
		
		final Dimension size = parent.getSize();
		final Dimension prefSize = preferredLayoutSize(parent);
		
		if(col == TierDataConstraint.TIER_LABEL_COLUMN) {
			width = DEFAULT_TIER_LABEL_WIDTH;
		} else if(col == TierDataConstraint.FLAT_TIER_COLUMN) {
			width = size.width - DEFAULT_TIER_LABEL_WIDTH - DEFAULT_H_GAP;
		} else if(col == TierDataConstraint.FULL_TIER_COLUMN) {
			width = Math.max(size.width, prefSize.width);		
		} else {
			width = calcAlignedGroupColumnWidth(col);
		}
		
		return width;
	}
	
	private int calcAlignedGroupColumnWidth(int col) {
		int width = 0;
		
		for(Component comp:constraintMap.keySet()) {
			final TierDataConstraint tdc = constraintMap.get(comp);
			if(isGrouped(tdc.getRowIndex()) && tdc.getColumnIndex() == col) {
				width = Math.max(width, comp.getPreferredSize().width + 2);
			}
		}
		
		return width;
	}

	private int calcAlignedRowHeight(int row) {
		int height = 0;
		
		for(TierDataConstraint tdc:componentMap.keySet()) {
			if(tdc.getRowIndex() == row) {
				final Component comp = componentMap.get(tdc);
				height = Math.max(height, comp.getPreferredSize().height);
			}
		}
		
		return height;
	}
	
	private int calcAlignedRowY(int row) {
		int y = 0;
		
		for(int i = 0; i < row; i++) {
			y += calcAlignedRowHeight(i) + DEFAULT_V_GAP;
		}
		
		return y;
	}
	
}

