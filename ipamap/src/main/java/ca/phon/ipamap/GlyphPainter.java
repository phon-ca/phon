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
package ca.phon.ipamap;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.layout.GridCellLayout;
import ca.phon.ui.painter.ComponentPainter;

/**
 * 
 *
 */
public class GlyphPainter implements ComponentPainter<IPAGridPanel> {
	
	/**
	 * Glyph renderer component
	 */
	private JLabel renderer;
	
	public GlyphPainter() {
		renderer = new JLabel() {
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
				super.paintComponent(g2d);
			}
		};
		renderer.setHorizontalTextPosition(SwingConstants.CENTER);
		renderer.setVerticalTextPosition(SwingConstants.CENTER);
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		renderer.setVerticalAlignment(SwingConstants.CENTER);
		renderer.setBorder(null);
	}

	@Override
	public void paint(Graphics2D g2d, IPAGridPanel comp, int width, int height) {
		GridCellLayout gridLayout = (GridCellLayout)comp.getLayout();
		int cellWidth = gridLayout.getCellWidth();
		int cellHeight = gridLayout.getCellHeight();
		
		renderer.setFont(comp.getFont());
		
		Grid grid = comp.getGrid();
		for(Cell cell:grid.getCell()) {
			String txt = cell.getText();
			renderer.setText(txt);
			
			Rectangle glyphRect = new Rectangle(
					cell.getX() * cellWidth,
					cell.getY() * cellHeight,
					cellWidth * 2, cellHeight * 2);
			SwingUtilities.paintComponent(g2d, renderer, comp, glyphRect);
		}
	}

}
