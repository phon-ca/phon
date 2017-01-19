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
package ca.phon.ui.breadcrumb;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * A {@link BreadCrumb} component. A {@link BreadCrumbViewer} shows a linear navigation history.
 * 
 * @param <S>  the type of state in the breadCrumb
 * @param <V>  the type of value in the breadCrumb
 */
public class BreadCrumbViewer<S, V> extends JComponent implements Scrollable {
	
	private static final long serialVersionUID = 4673118307259766794L;

	public final static String uiClassId = "BreadCrumbViewerUI";

	/** The {@link BreadCrumb} this component is viewing */
	private BreadCrumb<S, V> breadCrumb;
	
	private Color stateBackground;
	private Color stateForeground;
	private Color currentStateBackground;
	private Color currentStateForeground;
	
	/** State renderer */
	private BreadCrumbStateRenderer<? super S, ? super V> stateRenderer;

	/**
	 * Default constructor
	 * 
	 * @param breadCrumb  the breadCrumb to display
	 */
	public BreadCrumbViewer(BreadCrumb<S, V> breadcrumb) {
		super();
		setLayout(null);
		setBreadcrumb(breadcrumb);
		
		setUI(new DefaultBreadCrumbViewerUI());
		setOpaque(true);
	}

	public BreadCrumbViewerUI getBreadcrumbViewerUI() {
		return (BreadCrumbViewerUI) ui;
	}

	public void setUI(BreadCrumbViewerUI ui) {
        super.setUI(ui);
    }

	@Override
	public void updateUI() {
		setUI((BreadCrumbViewerUI) UIManager.getUI(this));

		BreadCrumbStateRenderer<? super S, ? super V> renderer = getStateRenderer();
		if (renderer instanceof Component) {
			SwingUtilities.updateComponentTreeUI((Component) renderer);
		}
	}
	
	public Color getCurrentStateBackground() {
		return currentStateBackground;
	}
	
	public Color getStateBackground() {
		return stateBackground;
	}

	public void setStateBackground(Color stateBackground) {
		final Color oldBackground = this.stateBackground;
		this.stateBackground = stateBackground;
		firePropertyChange("stateBackground", oldBackground, stateBackground);
	}

	public Color getStateForeground() {
		return stateForeground;
	}

	public void setStateForeground(Color stateForeground) {
		final Color oldForeground = this.stateForeground;
		this.stateForeground = stateForeground;
		firePropertyChange("stateForeground", oldForeground, stateForeground);
	}

	public void setCurrentStateBackground(Color selectionBackground) {
		final Color oldBackground = this.currentStateBackground;
		this.currentStateBackground = selectionBackground;
		firePropertyChange("currentStateBackround", oldBackground, selectionBackground);
	}

	public Color getCurrentStateForeground() {
		return currentStateForeground;
	}

	public void setCurrentStateForeground(Color selectionForeground) {
		final Color oldForeground = this.currentStateForeground;
		this.currentStateForeground = selectionForeground;
		firePropertyChange("currentStateForeground", oldForeground, selectionForeground);
	}

	/**
	 * Gets the breadCrumb this component is viewing.
	 * 
	 * @return  the breadCrumb, or <code>null</code> if no breadCrumb being viewed
	 */
	public BreadCrumb<S, V> getBreadcrumb() {
		return breadCrumb;
	}
	
	public BreadCrumbStateRenderer<? super S, ? super V> getStateRenderer() {
		return this.stateRenderer;
	}
	
	public void setStateRenderer(BreadCrumbStateRenderer<? super S, ? super V> renderer) {
		final BreadCrumbStateRenderer<? super S, ? super V> oldRenderer = this.stateRenderer;
		this.stateRenderer = renderer;
		
		firePropertyChange("stateRenderer", oldRenderer, renderer);
	}

	/**
	 * Sets the breadCrumb this component is viewing.
	 * 
	 * @param breadCrumb  the breadCrumb
	 */
	public void setBreadcrumb(BreadCrumb<S, V> breadcrumb) {
		final BreadCrumb<S, V> oldBreadcrumb = this.breadCrumb;
		this.breadCrumb = breadcrumb;
		
		firePropertyChange("breadCrumb", oldBreadcrumb, breadcrumb);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = getBreadcrumbViewerUI().getPreferredSize();
		
		for(int i = 0; i < getComponentCount(); i++) {
			final Rectangle compBounds = getComponent(i).getBounds();
			retVal.width = Math.max(retVal.width, compBounds.x+compBounds.width);
			retVal.height = Math.max(retVal.height, compBounds.y+compBounds.height);
		}
		
		return retVal;
	}
	
	/* Scrollable */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		final Dimension dim = getPreferredSize();
		dim.width = Integer.MAX_VALUE;
		return dim;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 20;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 100;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}
	
}
