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
package ca.phon.ui.painter;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

public class CmpPainter<T extends JComponent> implements ComponentPainter<T> {

	/**
	 * List of painters
	 */
	private List<ComponentPainter<T>> painters = 
		Collections.synchronizedList(new ArrayList<ComponentPainter<T>>());
	
	/**
	 * Constructor
	 */
	public CmpPainter() {
		super();
	}
	
	public CmpPainter(ComponentPainter<T> ... painters) {
		for(int i = 0; i < painters.length; i++) {
			this.painters.add(painters[i]);
		}
	}
	
	@Override
	public void paint(Graphics2D g2d, T comp, int width, int height) {
		//paint in order
		for(ComponentPainter<T> painter:painters) {
			painter.paint(g2d, comp, width, height);
		}
	}
	
}
