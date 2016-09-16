/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.painter;

import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * A painter interface similar to the SwingX painter
 * setup.  Can be used by custom UI components to
 * help with painting.
 */
public interface ComponentPainter<T extends JComponent> {

	/**
	 * Paint the component
	 * 
	 * @param g2d graphics context as a Graphics2D object
	 * @param comp component
	 * @param width width to paint
	 * @param height height to paint
	 */
	public void paint(Graphics2D g2d, T comp,
			int width, int height);
	
}
