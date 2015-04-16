/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Layout tier groups so that they are vertically aligned.
 * Full/flat tiers fill the remainder of the component or
 * the preferred size (width) of the aligned groups whichever
 * is larger.
 */
public class AlignGroupsLayoutProvider implements TierDataLayoutProvider {
	
	private final Map<TierDataConstraint, Rectangle> cachedRects = 
			Collections.synchronizedMap(new HashMap<TierDataConstraint, Rectangle>());
	
	private final AtomicReference<Dimension> prefSizeRef = new AtomicReference<Dimension>();

	@Override
	public void layoutContainer(Container parent, TierDataLayout layout) {
		final Map<Component, TierDataConstraint> constraintMap = layout.getConstraintMap();
		for(Component comp:constraintMap.keySet()) {
			final Dimension prefSize = comp.getPreferredSize();
			
			final TierDataConstraint constraint = constraintMap.get(comp);
			Rectangle rect = alignedRectForConstaint(parent, layout, constraint);
			
			Rectangle compRect = new Rectangle(rect);
			
			if(constraint.getColumnIndex() != TierDataConstraint.TIER_LABEL_COLUMN) {
				compRect.height = prefSize.height;
				if(rect.height > prefSize.height) {
					// center in rect
					compRect.y += (rect.height - prefSize.height) / 2;
				}
			}
			comp.setBounds(compRect);
		}
	}

	@Override
	public Dimension preferredSize(Container parent, TierDataLayout layout) {
		Dimension retVal = prefSizeRef.get();
		if(retVal == null) {
			retVal = calcLayoutSize(parent, layout);
			prefSizeRef.getAndSet(retVal);
		}
		return retVal;
	}

	@Override
	public Dimension minimumSize(Container parent, TierDataLayout layout) {
		final Dimension retVal = new Dimension();
		retVal.height = 1;
		retVal.width = layout.getTierLabelWidth() + layout.getHorizontalGap();
		return retVal;
	}

	@Override
	public Dimension maximumSize(Container parent, TierDataLayout layout) {
		Dimension retVal = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		return retVal;
	}

	@Override
	public void invalidate(Container parent, TierDataLayout layout) {
		cachedRects.clear();
		prefSizeRef.getAndSet(null);
	}
	
	/**
	 * Calculates the layout size.
	 * 
	 * @param parent
	 * @param layout
	 */
	private Dimension calcLayoutSize(Container parent, TierDataLayout layout) {
		Dimension retVal = new Dimension();
		int width = layout.getTierLabelWidth();
		
		for(int i = 1; i <= layout.getColumnCount(); i++) {
			int colWidth = calcAlignedGroupColumnWidth(parent, layout, i);
			width += colWidth + layout.getHorizontalGap();
		}
		
		int height = 0;
		for(int i = 0; i < layout.getRowCount(); i++) {
			int rowHeight = calcAlignedRowHeight(parent, layout, i);
			height += rowHeight + layout.getVerticalGap();
		}
		retVal.width = width;
		retVal.height = height;
		return retVal;
	}
	
	// calculate the size of the given groups width
	// when the GroupMode is ALIGNED
	private int calcAlignedColumnWidth(Container parent, TierDataLayout layout, int col) {
		int width = 0;
		
		final Dimension size = parent.getSize();
		final Dimension prefSize = preferredSize(parent, layout);
		
		if(col == TierDataConstraint.TIER_LABEL_COLUMN) {
			width = layout.getTierLabelWidth();
		} else if(col == TierDataConstraint.FLAT_TIER_COLUMN) {
			width = size.width - layout.getTierLabelWidth() - layout.getHorizontalGap();
		} else if(col == TierDataConstraint.FULL_TIER_COLUMN) {
			width = Math.max(size.width, prefSize.width);		
		} else {
			width = calcAlignedGroupColumnWidth(parent, layout, col);
		}
		
		return width;
	}
	
	// calculate the x coord of the given group
	// when the GroupMode is ALIGNED
	private int calcAlignedColumnX(Container parent, TierDataLayout layout, int col) {
		int x = 0;
		
		if(col == TierDataConstraint.TIER_LABEL_COLUMN) {
			x = 0;
		} else if(col == TierDataConstraint.FULL_TIER_COLUMN) {
			x = 0;
		} else if(col == TierDataConstraint.FLAT_TIER_COLUMN) {
			x = layout.getTierLabelWidth() + layout.getHorizontalGap();
		} else {
			x = layout.getTierLabelWidth() + layout.getHorizontalGap();
			for(int i = TierDataConstraint.GROUP_START_COLUMN; i < col; i++) {
				x += calcAlignedColumnWidth(parent, layout, i) + layout.getHorizontalGap();
			}
		}
		
		return x;
	}
		
	private int calcAlignedRowY(Container parent, TierDataLayout layout, int row) {
		int y = layout.getVerticalGap()/2;
		
		for(int i = 0; i < row; i++) {
			y += calcAlignedRowHeight(parent, layout, i) + layout.getVerticalGap();
		}
		
		return y;
	}
	
	private int calcAlignedGroupColumnWidth(Container parent, TierDataLayout layout, int col) {
		int width = 0;
		
		final Map<Component, TierDataConstraint> constraintMap = layout.getConstraintMap();
		for(Component comp:constraintMap.keySet()) {
			final TierDataConstraint tdc = constraintMap.get(comp);
			if(layout.isGrouped(tdc.getRowIndex()) && tdc.getColumnIndex() == col) {
				width = Math.max(width, comp.getPreferredSize().width + 2);
			}
		}
		
		return width;
	}
	
	private int calcAlignedRowHeight(Container parent, TierDataLayout layout, int row) {
		int height = 0;
		
		final Map<TierDataConstraint, Component> componentMap = layout.getComponentMap();
		for(TierDataConstraint tdc:componentMap.keySet()) {
			if(tdc.getRowIndex() == row) {
				final Component comp = componentMap.get(tdc);
				height = Math.max(height, comp.getPreferredSize().height);
			}
		}
		
		return height;
	}
	
	/**
	 * Get the rectangle for the given constraint.
	 * 
	 * @param tdc
	 * @param parent
	 * @param x
	 * @param y
	 */
	private Rectangle alignedRectForConstaint(Container parent, TierDataLayout layout, TierDataConstraint tdc) {
		Rectangle retVal = cachedRects.get(tdc);
		if(retVal == null) {			
			final int row = tdc.getRowIndex();
			final int col = tdc.getColumnIndex();
			
			int x = 0, y = 0;
			int width = 0, height = 0;
			
			x = calcAlignedColumnX(parent, layout, col);
			y = calcAlignedRowY(parent, layout, row);
			width = calcAlignedColumnWidth(parent, layout, col);
			height = calcAlignedRowHeight(parent, layout, row);
			
			retVal = new Rectangle(x, y, width, height);
			cachedRects.put(tdc, retVal);
		}
		return retVal;
	}

	@Override
	public Rectangle rowRect(Container parent, TierDataLayout layout, int row) {
		final int rowY = calcAlignedRowY(parent, layout, row);
		final int rowHeight = calcAlignedRowHeight(parent, layout, row);
		final int rowWidth = parent.getWidth() - layout.getTierLabelWidth();
		return new Rectangle(layout.getTierLabelWidth(), rowY, rowWidth, rowHeight);
	}
	
}
