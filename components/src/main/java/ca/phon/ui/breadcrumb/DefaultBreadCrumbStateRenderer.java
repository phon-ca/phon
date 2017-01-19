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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class DefaultBreadCrumbStateRenderer<S, V> extends JLabel implements BreadCrumbStateRenderer<S, V> {
	
	private final static int RIGHT_WIDTH = 5;

	private static final long serialVersionUID = 2519454994582489644L;
	
	private boolean drawFocus = false;
	
	public DefaultBreadCrumbStateRenderer() {
		super();
		
		setOpaque(false);
	}

	@Override
	public JComponent createStateComponent(BreadCrumbViewer<S, V> viewer, int stateIdx, S state, V value,
			boolean hasFocus) {
		
		setFont(viewer.getFont());
		setText(value.toString());
		
		if(stateIdx == viewer.getBreadcrumb().size()-1) {
			setBorder(new EmptyBorder(5, RIGHT_WIDTH, 5, 2*RIGHT_WIDTH));
		} else {
			setBorder(new EmptyBorder(5, 2*RIGHT_WIDTH, 5, 2*RIGHT_WIDTH));
		}
		
		if(stateIdx == 0) {
			setBackground(viewer.getCurrentStateBackground());
			setForeground(viewer.getCurrentStateForeground());
		} else {
			setBackground(viewer.getStateBackground());
			setForeground(viewer.getStateForeground());
		}
		
		drawFocus = hasFocus;
		
		return this;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final Insets insets = getInsets();
		final Rectangle2D rect = new Rectangle2D.Double(insets.left, 0, getWidth()-RIGHT_WIDTH-insets.left, getHeight());
		
		g2.setColor(getBackground());
		g2.fill(rect);
		
		super.paintComponent(g2);
	}

	@Override
	protected void paintBorder(Graphics gfx) {
		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final GeneralPath arrowPath = new GeneralPath();
		arrowPath.moveTo(0, 0);
		arrowPath.lineTo(RIGHT_WIDTH, getHeight()/2-1);
		arrowPath.lineTo(0, getHeight());
		arrowPath.closePath();
		
		if(getInsets().left == 2*RIGHT_WIDTH) {
			final Rectangle2D rect = new Rectangle2D.Double(0, 0, 2*RIGHT_WIDTH, getHeight());
			final Area area = new Area(rect);
			area.subtract(new Area(arrowPath));
			
			g.setColor(getBackground());
			g.fill(area);
			
			// draw border line
			g.setColor(getForeground());
			g.drawLine(0, 0, RIGHT_WIDTH, getHeight()/2-1);
			g.drawLine(RIGHT_WIDTH, getHeight()/2-1, 0, getHeight());
		} else {
			g.setColor(getBackground());
			g.fillRect(0, 0, RIGHT_WIDTH, getHeight());
		}
		
		AffineTransform origTrans = g.getTransform();
		
		g.translate((getWidth()-1)-RIGHT_WIDTH, 0);
		g.setColor(getBackground());
		g.fill(arrowPath);
	
		// draw border line
		g.setColor(getForeground());
		g.drawLine(0, 0, RIGHT_WIDTH, getHeight()/2-1);
		g.drawLine(RIGHT_WIDTH, getHeight()/2-1, 0, getHeight());
		
		if(drawFocus) {
			g.setTransform(origTrans);

			final Rectangle rect = new Rectangle(RIGHT_WIDTH + 1, 2, getWidth() - 2 * RIGHT_WIDTH - 2, getHeight() - 4);
			int vx, vy;

			g.setColor(UIManager.getDefaults().getColor("Button.focus"));

			// draw upper and lower horizontal dashes
			for (vx = rect.x; vx < (rect.x + rect.width); vx += 2) {
				g.fillRect(vx, rect.y, 1, 1);
				g.fillRect(vx, rect.y + rect.height - 1, 1, 1);
			}

			// draw left and right vertical dashes
			for (vy = rect.y; vy < (rect.y + rect.height); vy += 2) {
				g.fillRect(rect.x, vy, 1, 1);
				g.fillRect(rect.x + rect.width - 1, vy, 1, 1);
			}
		}
	}

}
