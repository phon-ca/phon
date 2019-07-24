package ca.phon.ipamap2;

import javax.swing.JComponent;
import javax.swing.JLabel;

import ca.phon.ui.ipamap.io.Cell;

public class DefaultIPAMapCellRenderer extends JLabel implements IPAMapCellRenderer {

	public DefaultIPAMapCellRenderer() {
		super();
		
		setDoubleBuffered(false);
	}
	
	@Override
	public JComponent getCellRenderer(IPAMapGrid mapGrid, Cell cell, boolean isHover, boolean isPressed) {
		// TODO Auto-generated method stub
		return null;
	}

}
