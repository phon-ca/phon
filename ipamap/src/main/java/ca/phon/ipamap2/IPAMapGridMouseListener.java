package ca.phon.ipamap2;

import java.awt.event.MouseEvent;
import java.util.EventListener;

import ca.phon.ui.ipamap.io.Cell;

public interface IPAMapGridMouseListener extends EventListener {
	
	public void mousePressed(Cell cell, MouseEvent me);

	public void mouseReleased(Cell cell, MouseEvent me);
	
	public void mouseClicked(Cell cell, MouseEvent me);
	
	public void mouseEntered(Cell cell, MouseEvent me);
	
	public void mouseExited(Cell cell, MouseEvent me);
	
}
