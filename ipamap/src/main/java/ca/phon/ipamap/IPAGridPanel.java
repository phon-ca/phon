/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.layout.GridCellLayout;
import ca.phon.ui.painter.ComponentPainter;

/**
 * Panel for IPA Grids
 */
public class IPAGridPanel extends JPanel {
	
	/** 
	 * Parent display, used to get cell dimensions
	 * scale, and font
	 */
	private IpaMap parent;
	
	/**
	 * The grid info
	 */
	private Grid ipaGrid;
	
	/**
	 * Cells in a map
	 */
	private Map<Rectangle, Cell> cellRects = 
		Collections.synchronizedMap(new HashMap<Rectangle, Cell>());
	
	/**
	 * Grid cell layout
	 */
	private GridCellLayout gridLayout;
	
	/**
	 * Painter
	 * 
	 */
	private ComponentPainter<IPAGridPanel> painter;
	
	public IPAGridPanel(IpaMap parent, Grid grid) {
		this.ipaGrid = grid;
		this.parent = parent;
		
		setupGrid();
	}
	
	private void setupGrid() {
		Dimension cellDim = parent.getCellDimension();
		
		gridLayout = new GridCellLayout(ipaGrid.getRows(), ipaGrid.getCols(),
				(int)cellDim.getWidth(), (int)cellDim.getHeight());
	}
	
	public GridCellLayout getGridLayout() {
		return this.gridLayout;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return gridLayout.preferredLayoutSize(getParent());
	}
	
	public Grid getGrid() {
		return this.ipaGrid;
	}
	
	public void setGrid(Grid g) {
		this.ipaGrid = g;
		setupGrid();
	}
	
	public IpaMap getParent() {
		return this.parent;
	}
	
	public void setParent(IpaMap p) {
		this.parent = p;
	}
	
	public void setPainter(ComponentPainter<IPAGridPanel> painter) {
		this.painter = painter;
	}
	
	public ComponentPainter<IPAGridPanel> getPainter() {
		return this.painter;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		
		painter.paint(g2d, this, getWidth(), getHeight());
	}

}
