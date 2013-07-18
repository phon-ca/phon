package ca.phon.ipamap;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.painter.Painter;

/**
 * Button class for the IPA map using
 * a custom background painter and 
 * foreground painter.
 *
 */
public class IPAGridButton extends JButton {

	/**
	 * bg painter
	 */
	private Painter<IPAGridButton> bgPainter;
	
	/**
	 * fg painter
	 */
	private Painter<IPAGridButton> fgPainter;
	
	/**
	 * parent 
	 */
	private IPAGridPanel parent;
	
	/**
	 * cell data
	 */
	private Cell cell;
	
	/**
	 * Constructor
	 */
	public IPAGridButton(IPAGridPanel parent, Cell c) {
		super();
		this.parent = parent;
		this.cell = c;
		
		super.setOpaque(false);
		super.setBorderPainted(false);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		
		if(bgPainter != null)
			bgPainter.paint(g2d, this, getWidth(), getHeight());
		super.paintComponent(g2d);
		if(fgPainter != null)
			fgPainter.paint(g2d, this, getWidth(), getHeight());
	}
	
}
