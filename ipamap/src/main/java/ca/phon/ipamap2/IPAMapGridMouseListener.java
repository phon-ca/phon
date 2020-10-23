package ca.phon.ipamap2;

import java.awt.event.*;
import java.util.*;

import ca.phon.ui.ipamap.io.*;

public interface IPAMapGridMouseListener extends EventListener {
	
	public void mousePressed(Cell cell, MouseEvent me);

	public void mouseReleased(Cell cell, MouseEvent me);
	
	public void mouseClicked(Cell cell, MouseEvent me);
	
	public void mouseEntered(Cell cell, MouseEvent me);
	
	public void mouseExited(Cell cell, MouseEvent me);
	
}
