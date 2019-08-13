package ca.phon.ipamap2;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import ca.phon.ui.ipamap.io.Cell;

public interface IPAMapCellRenderer {
	
	/**
	 * Determine size of cells based on given mapGrid.
	 */
	public Dimension getCellDimension(IPAMapGrid mapGrid);
	
	/**
	 * Paint cell
	 * 
	 * @param mapGrid
	 * @param g2
	 * @param cell
	 * @param cellRect
	 * @param isHover
	 * @param isPressed
	 * @return
	 */
	public void paintCell(IPAMapGrid mapGrid, Graphics2D g2, Rectangle cellRect, Cell cell, boolean isHover, boolean isPressed);

}
