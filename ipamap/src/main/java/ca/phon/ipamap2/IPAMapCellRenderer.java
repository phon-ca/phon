package ca.phon.ipamap2;

import javax.swing.JComponent;

import ca.phon.ui.ipamap.io.Cell;

public interface IPAMapCellRenderer {
	
	/**
	 * Return the cell renderer for the given cell information
	 * 
	 * @param mapGrid
	 * @param cell
	 * @param isHover
	 * @param isPressed
	 * @return
	 */
	public JComponent getCellRenderer(IPAMapGrid mapGrid, Cell cell, boolean isHover, boolean isPressed);

}
