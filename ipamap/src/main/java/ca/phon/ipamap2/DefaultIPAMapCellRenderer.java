/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ipamap2;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

import ca.phon.ui.ipamap.io.*;

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
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
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
