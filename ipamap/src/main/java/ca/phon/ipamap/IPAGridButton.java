/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.ipamap;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.painter.ComponentPainter;

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
	private ComponentPainter<IPAGridButton> bgPainter;
	
	/**
	 * fg painter
	 */
	private ComponentPainter<IPAGridButton> fgPainter;
	
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
