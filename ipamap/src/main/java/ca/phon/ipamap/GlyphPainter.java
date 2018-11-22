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