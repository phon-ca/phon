package ca.phon.ipamap;

import java.awt.Color;
import java.awt.Graphics2D;

import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.layout.GridCellLayout;
import ca.phon.ui.painter.ComponentPainter;

/**
 * Paints stripes on IPA grid panels.
 *
 * Stripes are painted on every row
 * line if the number of rows > 2.
 */
public class StripePainter implements ComponentPainter<IPAGridPanel> {

	/**
	 * Minimum number of rows before we use
	 * stripes
	 */
	private final static int MIN_ROWS = 3;
	
	/**
	 * Stripe color
	 */
	private Color stripeColor = PhonGuiConstants.PHON_UI_STRIP_COLOR;
	
	public StripePainter() {
		super();
	}

	@Override
	public void paint(Graphics2D g2d, IPAGridPanel comp, int width, int height) {
		// get the grid cell layout
		GridCellLayout gridCellLayout = (GridCellLayout)comp.getGridLayout();
		
		int stripeHeight = gridCellLayout.getCellHeight() * 2;
		int stripWidth = width;
		
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, height);
		
		if(gridCellLayout.getNumRows() >= (2 * MIN_ROWS)) {
			int currentY = stripeHeight;
			g2d.setColor(stripeColor);
			while(currentY < height) {
				g2d.fillRect(0, currentY, stripWidth, stripeHeight);
				currentY += 2 * stripeHeight;
			}
		}
	}
	
}
