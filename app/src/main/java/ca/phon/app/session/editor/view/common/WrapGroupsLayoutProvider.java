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
package ca.phon.app.session.editor.view.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Layout provider which wraps groups components.
 *
 */
public class WrapGroupsLayoutProvider implements TierDataLayoutProvider {

	
	private final Map<TierDataConstraint, Rectangle> cachedRects = 
			Collections.synchronizedMap(new HashMap<TierDataConstraint, Rectangle>());
	
	private final List<Rectangle> rowRects = 
			Collections.synchronizedList(new ArrayList<Rectangle>());
	
	private final AtomicReference<Dimension> prefSizeRef = new AtomicReference<Dimension>();
	
	@Override
	public void layoutContainer(Container parent, TierDataLayout layout) {
		final Map<Component, TierDataConstraint> constraintMap = layout.getConstraintMap();
		for(Component comp:constraintMap.keySet()) {
			final TierDataConstraint constraint = constraintMap.get(comp);
			
			final Dimension prefSize = comp.getPreferredSize();
			
			Rectangle rect = wrappedRectForConstraint(parent, layout, constraint);

			if(constraint.getColumnIndex() != TierDataConstraint.TIER_LABEL_COLUMN) {
				if(rect.height > prefSize.height){
					rect.y += (rect.height - prefSize.height) / 2; 
				}
				rect.height = prefSize.height;
			}
			
			comp.setBounds(rect);
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
		final int minWidth = calcMinimumWidth(parent, layout);
		final int minHeight = preferredSize(parent, layout).height;
		return new Dimension(minWidth, minHeight);
	}

	@Override
	public Dimension maximumSize(Container parent, TierDataLayout layout) {
		Dimension retVal = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		return retVal;
	}

	@Override
	public void invalidate(Container parent, TierDataLayout layout) {
		prefSizeRef.getAndSet(null);
		cachedRects.clear();
		rowRects.clear();
	}
	
	/**
	 * Calculate minimum width.  This is the largest preferred width
	 * for group 0 (including flat-pref sized tiers.)
	 */
	private final int calcMinimumWidth(Container parent, TierDataLayout layout) {
		int retVal = layout.getTierLabelWidth() + layout.getHorizontalGap();
		
		int group0Width = 0;
		
		final Map<TierDataConstraint, Component> componentMap = layout.getComponentMap();
		for(TierDataConstraint tdc:componentMap.keySet()) {
			if(tdc.getColumnIndex() == TierDataConstraint.GROUP_START_COLUMN) {
				final Component comp = componentMap.get(tdc);
				final Dimension compPrefSize = comp.getPreferredSize();
				group0Width = Math.max(group0Width, compPrefSize.width);
			}
		}
		
		retVal += group0Width;
		
		return retVal;
	}
	
	private Rectangle wrappedRectForConstraint(Container parent, TierDataLayout layout, TierDataConstraint tdc) {
		Rectangle retVal = cachedRects.get(tdc);
		if(retVal == null) {
			// layout has not been calculated
			calcLayout(parent, layout);
			retVal = cachedRects.get(tdc);
		}
		if(tdc.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN) {
			// adjust row height
			retVal.height = 
					(tdc.getRowIndex() < rowRects.size() ? rowRects.get(tdc.getRowIndex()).height : 0);
		}
		return retVal;
	}
	
	private Dimension calcLayoutSize(Container parent, TierDataLayout layout) {
		Dimension retVal = new Dimension();
		if(cachedRects.size() == 0)
			calcLayout(parent, layout);
		int width = layout.getTierLabelWidth() + layout.getHorizontalGap();
		int maxGroupWidth = 0;
		int height = 0;
		
		for(TierDataConstraint tdc:cachedRects.keySet()) {
			final Rectangle rect = cachedRects.get(tdc);
			// only consider GROUPED tiers in calculation of max width, other
			// tiers should conform to maxGroupWidth
			if(tdc.getColumnIndex() >= TierDataConstraint.GROUP_START_COLUMN) {
				maxGroupWidth = Math.max(maxGroupWidth, rect.width);
			}
			height = Math.max(height, rect.y + rect.height);
		}
		retVal = new Dimension(width + maxGroupWidth, height);
		return retVal;
	}
	
	private void calcLayout(Container parent, TierDataLayout layout) {
		final Map<TierDataConstraint, Component> componentMap = layout.getComponentMap();
		final Map<Component, TierDataConstraint> constraintMap = layout.getConstraintMap();
		// get an ordered list of constraints
		final List<TierDataConstraint> orderedConstraints = 
				new ArrayList<TierDataConstraint>(constraintMap.values());
		Collections.sort(orderedConstraints);
		
		// keep a reference to each row's bounding rectangle
		rowRects.clear();
		Rectangle rowRect = new Rectangle();
		rowRect.x = 0; rowRect.y = layout.getVerticalGap()/2;
		int currentRow = 0;
		rowRects.add(rowRect);
		
		int currentX = 0;
		int currentY = layout.getVerticalGap()/2;
		
		final Dimension size = parent.getSize();
		
		for(TierDataConstraint constraint:orderedConstraints) {
			// check for a row change
			if(currentRow != constraint.getRowIndex()) {
				currentX = 0;
				currentY = rowRect.y + rowRect.height + layout.getVerticalGap();
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
			int compWidth = constraint.getColumnIndex() == TierDataConstraint.TIER_LABEL_COLUMN ? layout.getTierLabelWidth() : prefSize.width + 2;
			int compHeight = prefSize.height;

			// wrap components
			if(compX + compWidth > size.width && constraint.getColumnIndex() > TierDataConstraint.GROUP_START_COLUMN) {
				compX = layout.getTierLabelWidth() + layout.getHorizontalGap();
				compY = rowRect.y + rowRect.height + layout.getVerticalGap();
			}
			
			// adjust size of full/flat tiers correctly
			if(constraint.getColumnIndex() == TierDataConstraint.FLAT_TIER_COLUMN) {
				compX = layout.getTierLabelWidth() + layout.getHorizontalGap();
				compWidth = size.width - compX;
			} else if(constraint.getColumnIndex() == TierDataConstraint.FULL_TIER_COLUMN) {
				compX = 0;
				compWidth = size.width;
			}
			
			final Rectangle compRect = new Rectangle(compX, compY, compWidth, compHeight);
			rowRect.add(compRect);
			cachedRects.put(constraint, compRect);
			
			currentX = compRect.x + compRect.width + layout.getHorizontalGap();
			currentY = compRect.y;
		}
	}

	@Override
	public Rectangle rowRect(Container parent, TierDataLayout layout, int row) {
		final Rectangle rowRect =
				new Rectangle(rowRects.size() > row ? rowRects.get(row) : new Rectangle());
		
		rowRect.x = layout.getTierLabelWidth();
		rowRect.width = parent.getWidth();
		
		return rowRect;
	}

}
