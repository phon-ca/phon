package ca.phon.ipamap2;

import java.awt.Dimension;

import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;

/**
 * Base class for IPAMapGrid UI
 */
public abstract class IPAMapGridUI extends ComponentUI {
	
	public abstract Dimension getCellDimension();
	
	public abstract JToolTip createToolTip();

}
