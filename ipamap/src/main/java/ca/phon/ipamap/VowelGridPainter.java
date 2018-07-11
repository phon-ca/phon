/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipamap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ca.phon.ui.layout.GridCellLayout;
import ca.phon.ui.painter.ComponentPainter;
import ca.phon.util.Tuple;

/**
 * Paints the standard IPA vowel grid
 * on the background of a grid panel.
 * 
 * NOTES: This class will need to be adjusted
 * if the layout in ipagrids.xml changes.
 */
public class VowelGridPainter implements ComponentPainter<IPAGridPanel> {

	private Point2D[] dotPts = {
			new Point2D.Float(2.5f, 1f),
			new Point2D.Float(12.5f, 1f),
			new Point2D.Float(21.5f, 1f),
			new Point2D.Float(4.5f, 5.0f),
			new Point2D.Float(13.5f, 5.0f),
			new Point2D.Float(21.5f, 5.0f),
			new Point2D.Float(6.5f, 9.0f),
			new Point2D.Float(14.5f, 9.0f),
			new Point2D.Float(21.5f, 9.0f),
			new Point2D.Float(8.5f, 13.0f),
			new Point2D.Float(21.5f, 13.0f)
	};
	
	private LineSegment lines[] = {
			// far-left diag line
			new LineSegment(dotPts[0], dotPts[9]),
			
			// middle diag line
			new LineSegment(dotPts[1], new Point2D.Float(13.75f, 6.0f)),
			new LineSegment(new Point2D.Float(14.25f, 8.0f), new Point2D.Float(14.75f, 10.0f)),
			new LineSegment(new Point2D.Float(15.25f, 12.0f), new Point2D.Float(15.5f, 13.0f)),
			
			// right vert line
			new LineSegment(dotPts[2], dotPts[10]),
			
			// top horizontal line
			new LineSegment(new Point2D.Float(5.0f, 1.0f), new Point2D.Float(10.0f, 1.0f)),
			new LineSegment(new Point2D.Float(15.0f, 1.0f), new Point2D.Float(19.0f, 1.0f)),
			
			// 2nd horizontal line
			new LineSegment(new Point2D.Float(7.0f, 5.0f), new Point2D.Float(11.0f, 5.0f)),
			new LineSegment(new Point2D.Float(16.0f, 5.0f), new Point2D.Float(19.0f, 5.0f)),
			
			// 3rd horizontal line
			new LineSegment(new Point2D.Float(9.0f, 9.0f), new Point2D.Float(12.0f, 9.0f)),
			new LineSegment(new Point2D.Float(17.0f, 9.0f), new Point2D.Float(19.0f, 9.0f)),
			
			// bottom horizontal line
			new LineSegment(new Point2D.Float(11.0f, 13.0f), new Point2D.Float(19.0f, 13.0f)),
			
			// extra-vowels line
			new LineSegment(new Point2D.Float(26.0f, 6.0f), new Point2D.Float(26.0f, 8.0f))
	};
	
	@Override
	public void paint(Graphics2D g2d, IPAGridPanel comp, int width, int height) {
		GridCellLayout grid = (GridCellLayout)comp.getGridLayout();
		
		int gridw = grid.getCellWidth();
		int gridh = grid.getCellHeight();
		
		// draw points
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		g2d.setColor(Color.lightGray);
		
		for(Point2D dotPt:this.dotPts) {
			Ellipse2D ellipse = convertPointToCircle(dotPt, gridw, gridh, comp.getParent().getScale());
			g2d.fill(ellipse);
		}
		
		Stroke s = new BasicStroke(3.0f*comp.getParent().getScale());
		g2d.setStroke(s);
		for(LineSegment line:lines) {
			Line2D l = new Line2D.Float(
					convertGridPointToView(line.getObj1(), gridw, gridh), 
					convertGridPointToView(line.getObj2(), gridw, gridh) );
			g2d.draw(l);
		}
	}
	
	private Point2D convertGridPointToView(Point2D p, int cw, int ch) {
		Point2D.Float retVal = new Point2D.Float();
		
		retVal.x = (float)p.getX() * cw;
		retVal.y = (float)p.getY() * ch;
		
		return retVal;
	}
	
	private Ellipse2D convertPointToCircle(Point2D p, int cw, int ch, float scale) {
		Ellipse2D.Float retVal = new Ellipse2D.Float();
		
		Point2D.Float centre = new Point2D.Float();
		
		float dotSize = 12.0f * scale;
		
		centre.x = (float)((float)cw * p.getX());
		centre.y = (float)((float)ch * p.getY());
		
		retVal.x = (float)centre.x - dotSize/2;
		retVal.width = dotSize;
		retVal.y = (float)centre.y - dotSize/2;
		retVal.height = dotSize;
		
		return retVal;
	}

	private class LineSegment extends Tuple<Point2D, Point2D> {
		
		public LineSegment(Point2D a, Point2D b) {
			super(a, b);
		}
	}
}
