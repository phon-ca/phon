package ca.phon.ipamap2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ca.phon.ui.ipamap.io.Cell;

public class DefaultIPAMapCellRenderer implements IPAMapCellRenderer {
	
	private JLabel glyphRenderer;
	
	public DefaultIPAMapCellRenderer() {
		super();
		
		glyphRenderer = new JLabel();
		glyphRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		glyphRenderer.setHorizontalTextPosition(SwingConstants.CENTER);
		glyphRenderer.setVerticalAlignment(SwingConstants.CENTER);
		glyphRenderer.setVerticalTextPosition(SwingConstants.CENTER);
		glyphRenderer.setDoubleBuffered(false);
	}

	@Override
	public void paintCell(IPAMapGrid mapGrid, Graphics2D g2, Rectangle cellRect, Cell cell, boolean isHover,
			boolean isPressed, boolean isSelected) {
		var roundRect = new RoundRectangle2D.Double(
				cellRect.x + mapGrid.getCellInsets().left,
				cellRect.y + mapGrid.getCellInsets().top,
				cellRect.width - mapGrid.getCellInsets().left - mapGrid.getCellInsets().right,
				cellRect.height - mapGrid.getCellInsets().top - mapGrid.getCellInsets().bottom,
				5, 5);
		
		if(isHover) {
			g2.setColor(Color.yellow);
			g2.fill(roundRect);
		}
		if(isSelected) {
			g2.setColor(Color.BLUE);
			g2.draw(roundRect);
		}
		if(isPressed) {
			g2.setColor(Color.GRAY);
			g2.draw(roundRect);
		}
		
		glyphRenderer.setBorder(
				BorderFactory.createEmptyBorder(mapGrid.getCellInsets().top, mapGrid.getCellInsets().left, 
						mapGrid.getCellInsets().bottom, mapGrid.getCellInsets().right));
		glyphRenderer.setFont(mapGrid.getFont());
		glyphRenderer.setText(cell.getText());
		SwingUtilities.paintComponent(g2, glyphRenderer, mapGrid.getParent(), cellRect);
	}

	@Override
	public Dimension getCellDimension(IPAMapGrid ipaGrid) {
		Dimension retVal = new Dimension(0, 0);
		
		String tStr = (char)0x25cc + "" + (char)0x1d50;
		glyphRenderer.setText(tStr);
		glyphRenderer.setFont(ipaGrid.getFont());
		glyphRenderer.setBorder(BorderFactory.createEmptyBorder(ipaGrid.getCellInsets().top, 
				ipaGrid.getCellInsets().left,
				ipaGrid.getCellInsets().bottom, 
				ipaGrid.getCellInsets().right));
		retVal = glyphRenderer.getPreferredSize();
		retVal.height += ipaGrid.getCellInsets().top + ipaGrid.getCellInsets().bottom;
		retVal.width += ipaGrid.getCellInsets().left + ipaGrid.getCellInsets().right;
		retVal.height /= 2;
		retVal.width /= 2;

		return retVal;
	}

}
