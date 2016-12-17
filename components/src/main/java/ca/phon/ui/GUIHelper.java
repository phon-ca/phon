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
package ca.phon.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * A class similar to {@link SwingUtilities} that has some helper functions
 * for drawing things.
 */
public class GUIHelper {
	/**
	 * Returns the point at which to draw a string so that it appears
	 * center-aligned within a given rectangle.
	 * 
	 * @param g  the graphics context to work with
	 * @param txt  the string
	 * @param rect  the rectangle to place the string in
	 * 
	 * @return the point at which text should be drawn so that it is aligned
	 *         in the center of the given rectangle.
	 */
	public static Point centerTextInRectangle(Graphics g, String txt, Rectangle rect) {
		return placeTextInRectangle(g, txt, rect, SwingConstants.CENTER, SwingConstants.CENTER);
	}

	/**
	 * Returns the point at which to draw a string so that it appears aligned
	 * within a given rectangle.
	 * 
	 * @param g  the graphics context to work with
	 * @param txt  the string
	 * @param rect  the rectangle to place the string in
	 * 
	 * @param horizontalAlignment  the horizontal alignment; one of
	 *             {@link SwingConstants#LEFT}, {@link SwingConstants#CENTER}
	 *             or {@link SwingConstants#RIGHT}
	 *                
	 * @param verticalAlignment  the vertical alignment; one of
	 *             {@link SwingConstants#TOP}, {@link SwingConstants#CENTER}
	 *             or {@link SwingConstants#BOTTOM}
	 * 
	 * @return the point at which text should be drawn so that it is aligned
	 *         appropriately with the given rectangle.
	 */
	public static Point placeTextInRectangle(Graphics g,
	                                         String txt,
	                                         Rectangle rect,
	                                         int horizontalAlignment,
	                                         int verticalAlignment) 
	{
		final FontMetrics fm = g.getFontMetrics();
		final LineMetrics lm = fm.getLineMetrics(txt, g);
		final Rectangle2D r = fm.getStringBounds(txt, g);

		int x = rect.x;
		int y = rect.y;

		switch(horizontalAlignment) {
		case SwingConstants.CENTER:
			x += (rect.width - r.getWidth()) * 0.5;
			break;
		case SwingConstants.RIGHT:
			x += rect.width - r.getWidth();
			break;
		}

		switch(verticalAlignment) {
		case SwingConstants.TOP:
			y += lm.getAscent();
			break;
		case SwingConstants.CENTER:
			y += (rect.height - r.getHeight()) * 0.5 + lm.getAscent();
			break;
		case SwingConstants.BOTTOM:
			y += rect.height - lm.getDescent();
			break;
		}

		return new Point(x, y);
	}

	/**
	 * Gets the interior drawing region of a component. That is, the drawing
	 * area inside of its border.
	 * 
	 * @param comp  the component to get the interior rectangle of
	 * 
	 * @return the interior rectangle
	 */
	public static Rectangle getInterior(JComponent comp) {
		final Insets insets = comp.getInsets();
		final Rectangle rect = new Rectangle(0, 0, comp.getWidth(), comp.getHeight());
		rect.x += insets.left;
		rect.y += insets.top;
		rect.width -= insets.left + insets.right;
		rect.height -= insets.top + insets.bottom;
		return rect;
	}

	/**
	 * Gets a color that "highlights" a given color. If the given color is dark,
	 * the returned color is brighter, otherwise the returned color is darker.
	 *   
	 * @param c  the color to highlight
	 * 
	 * @return a highlighted color
	 */
	public static Color highlightColor(Color c) {
		final int brightness = (int)(0.3*c.getRed() + 0.59*c.getGreen() + 0.11*c.getBlue());
		return (brightness > 127 ? c.darker() : c.brighter());
	}

	/**
	 * Returns the first ancestor of a given component that is of a specified
	 * class, or the component itself if it is an instance of the given class.
	 * 
	 * @param cls  the class to look for
	 * @param comp  the component to start searching from
	 * 
	 * @return  the ancestor of the given class, or <code>null</code> if
	 *          no ancestor of the given class could be found
	 *          
	 * @see SwingUtilities#getAncestorOfClass(Class, Component)
	 */
	public static <T> T getAncestorOrSelfOfClass(Class<T> cls, Component comp) {
		if(cls.isInstance(comp))
			return cls.cast(comp);
		return cls.cast(SwingUtilities.getAncestorOfClass(cls, comp));
	}
}
