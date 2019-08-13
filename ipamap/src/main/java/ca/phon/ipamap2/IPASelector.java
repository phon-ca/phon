package ca.phon.ipamap2;

import java.util.LinkedHashSet;
import java.util.Set;

import ca.phon.ui.ipamap.io.Cell;

/**
 * Allows for selection of a set of IPA elements.
 * 
 */
public class IPASelector {
	
	

	private final Set<Cell> selectedCells = new LinkedHashSet<>();

	private IPAMapGrid selectedCellGrid;
	
	private IPAMapGridContainer map;
	
	
	public IPASelector() {
		super();
	}
	
}
