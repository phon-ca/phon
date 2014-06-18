package ca.phon.app.session.editor.view.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Custom layout management for tier data.  Layout implementation
 * is decided by the currently selected algorithm.
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
	private final Map<Component, TierDataConstraint> constraintMap = new WeakHashMap<Component, TierDataConstraint>();

	private final Map<TierDataConstraint, Component> componentMap = new HashMap<TierDataConstraint, Component>();
	
	// current layout type
	private volatile TierDataLayoutType layoutType = TierDataLayoutType.ALIGN_GROUPS;
	
	// layout provider
	private volatile TierDataLayoutProvider layoutProvider = null;
	
	// tier label width
	private volatile int tierLabelWidth = DEFAULT_TIER_LABEL_WIDTH;
	
	// horizontal gap
	private volatile int horizontalGap = DEFAULT_H_GAP;
	
	// vertical gap
	private volatile int verticalGap = DEFAULT_V_GAP;
	
	/**
	 * Default constructor
	 */
	public TierDataLayout() {
		this(TierDataLayoutType.ALIGN_GROUPS, DEFAULT_TIER_LABEL_WIDTH,
				DEFAULT_H_GAP, DEFAULT_V_GAP);
	}
	
	public TierDataLayout(TierDataLayoutType layoutType) {
		this(layoutType, DEFAULT_TIER_LABEL_WIDTH, DEFAULT_H_GAP, DEFAULT_V_GAP);
	}
	
	public TierDataLayout(TierDataLayoutType layoutType, int tierLabelWidth,
			int hgap, int vgap) {
		super();
		this.layoutType = layoutType;
		this.layoutProvider = layoutType.createLayoutProvider();
		this.horizontalGap = hgap;
		this.verticalGap = vgap;
	}
	
	public TierDataLayoutType getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(TierDataLayoutType layoutType) {
		this.layoutType = layoutType;
		this.layoutProvider = layoutType.createLayoutProvider();
	}
	
	public TierDataLayoutProvider getLayoutProvider() {
		return this.layoutProvider;
	}

	public int getTierLabelWidth() {
		return tierLabelWidth;
	}

	public void setTierLabelWidth(int tierLabelWidth) {
		this.tierLabelWidth = tierLabelWidth;
	}

	public int getHorizontalGap() {
		return horizontalGap;
	}

	public void setHorizontalGap(int horizontalGap) {
		this.horizontalGap = horizontalGap;
	}

	public int getVerticalGap() {
		return verticalGap;
	}

	public void setVerticalGap(int verticalGap) {
		this.verticalGap = verticalGap;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// unsupported
		throw new UnsupportedOperationException();
	}

	@Override
	public void layoutContainer(Container parent) {
		getLayoutProvider().layoutContainer(parent, this);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent)  {
		return getLayoutProvider().minimumSize(parent, this);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return getLayoutProvider().preferredSize(parent, this);
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
		getLayoutProvider().invalidate(target, this);
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return getLayoutProvider().maximumSize(target, this);
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
		return maxRow + 1;
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
	 * Get the constraint -> component map for this layout.
	 * The returned map is not modifiable.
	 * 
	 * @return component map
	 */
	Map<TierDataConstraint, Component> getComponentMap() {
		return Collections.unmodifiableMap(componentMap);
	}
	
	/**
	 * Get the component -> constraint map for this layout.
	 * The returned map is not modifiable.
	 * 
	 * @return constraint map
	 */
	Map<Component, TierDataConstraint> getConstraintMap() {
		return Collections.unmodifiableMap(constraintMap);
	}
	
	/**
	 * Get constraint for given component
	 * 
	 * @param comp
	 * @return constraint or <code>null</code> if component
	 *  is not part of the layout
	 */
	TierDataConstraint constraintForComponent(Component comp) {
		return constraintMap.get(comp);
	}
	
	/**
	 * Get component for given constraint
	 * 
	 * @param constraint
	 * @return component or <code>null</code> if no component
	 *  with tthe given constraint was found
	 */
	Component componentForConstraint(TierDataConstraint constraint) {
		return componentMap.get(constraint);
	}
	
}

