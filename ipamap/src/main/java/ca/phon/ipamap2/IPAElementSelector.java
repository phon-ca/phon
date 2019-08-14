package ca.phon.ipamap2;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;

/**
 * Allows for selection of a set of IPA elements.
 * 
 */
public class IPAElementSelector extends JComponent {
	
	private Grid selectedCellGrid;
	private IPAMapGrid selectedMapGrid;
	
	private IPAMapGridContainer map;
	
	public IPAElementSelector(Set<Cell> selectedCells) {
		super();
	
		
		init();
	}
	
	private void init() {
		
	}
	
}
