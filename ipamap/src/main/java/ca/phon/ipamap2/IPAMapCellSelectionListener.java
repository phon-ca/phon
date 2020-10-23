package ca.phon.ipamap2;

import java.util.*;

/**
 * Functional interface for listening to changes to ipa map
 * cell selection.
 *
 */
public interface IPAMapCellSelectionListener extends EventListener {
	
	public void cellSelectionChanged(IPAMapGrid mapGrid, int cellIdx, boolean selected);

}
