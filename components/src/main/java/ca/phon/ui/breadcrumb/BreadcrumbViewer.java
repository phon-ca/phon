/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.ui.GUIHelper;
import ca.phon.util.Tuple;

/**
 * A breadcrumb component. A breadcrumb shows a linear navigation history.
 * 
 * @param <S>  the type of state in the breadcrumb
 * @param <V>  the type of value in the breadcrumb
 */
public class BreadcrumbViewer<S, V> extends JPanel implements Scrollable {
	/** The width of the right-hand side of a breadcrumb */ 
	private static final int RIGHT_WIDTH = 10;
	
	private static final int HGAP = 2;

	private class StateComponent<S, V> extends JLabel {
		/** The state for this component */
		public final S state;

		public StateComponent(S state, V value) {
			super(value == null ? "" : value.toString());
			
			this.state = state;

			setOpaque(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			if(state != null)
				setBorder(new EmptyBorder(5, 3, 5, 2*RIGHT_WIDTH));
		}
		
		@Override
		public Font getFont() {
			return BreadcrumbViewer.this.getFont();
		}

		@Override
		protected void paintBorder(Graphics gfx) {
			if(state == null) {
				super.paintBorder(gfx);
			} else {
				Graphics2D g = (Graphics2D)gfx;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				final int x = (getWidth() - 1) - RIGHT_WIDTH;
				final int y = getHeight() / 2;
				
				g.setColor(getForeground());
				g.drawLine(x - 1, getHeight() - 1, getWidth() - 2, y);
				g.drawLine(x - 1, 0, getWidth() - 2, y);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			final Rectangle rect = GUIHelper.getInterior(this);
			final Point p = GUIHelper.centerTextInRectangle(g, getText(), rect);

			g.setColor(getForeground());
			g.drawString(getText(), p.x, p.y);
		}
	}

	/** The breadcrumb this component is viewing */
	private Breadcrumb<S, V> breadcrumb;

	/** The breadcrumb listener for this component */
	private BreadcrumbListener<S, V> listener = new BreadcrumbListener<S, V>() {
		@Override
		public void stateChanged(S oldState, S newState) {
			// Find the component associated with the new state and remove
			// all state components after it
			synchronized(getTreeLock()) {
				removeAll();

				int width = 0;
				for(Tuple<S, V> statePair : breadcrumb) {
					final S state = statePair.getObj1();
					final StateComponent<S, V> comp = new StateComponent<S, V>(state, statePair.getObj2());
					comp.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							breadcrumb.gotoState(state);
						}
					});

					add(comp, 0);
					width += comp.getPreferredSize().width;
				}
				revalidate();
				
				scrollRectToVisible(new Rectangle(width-1, 0, 1, 1));
			}

		}

		@Override
		public void stateAdded(final S state, V value) {
		}
	};

	/**
	 * Default constructor
	 * 
	 * @param breadcrumb  the breadcrumb to display
	 */
	public BreadcrumbViewer(Breadcrumb<S, V> breadcrumb) {
		super(new HorizontalLayout(HGAP));

		add(new StateComponent<S, V>(null, null));

		setBreadcrumb(breadcrumb);
		setOpaque(false);
	}

	/**
	 * Gets the breadcrumb this component is viewing.
	 * 
	 * @return  the breadcrumb, or <code>null</code> if no breadcrumb being viewed
	 */
	public Breadcrumb<S, V> getBreadcrumb() {
		return breadcrumb;
	}

	/**
	 * Sets the breadcrumb this component is viewing.
	 * 
	 * @param breadcrumb  the breadcrumb
	 */
	public void setBreadcrumb(Breadcrumb<S, V> breadcrumb) {
		if(breadcrumb != this.breadcrumb) {
			// Remove old information
			if(this.breadcrumb != null) {
				this.breadcrumb.removeBreadcrumbListener(listener);
				while(getComponentCount() > 1)
					remove(1);
			}

			this.breadcrumb = breadcrumb;

			// Add new information
			if(this.breadcrumb != null) {
				this.breadcrumb.addBreadcrumbListener(listener);
				for(Tuple<S, V> state : this.breadcrumb)
					listener.stateAdded(state.getObj1(), state.getObj2());
			}
		}
	}

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
