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
package ca.phon.ui.layout;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GridCellTest extends JPanel {
	
	public GridCellTest() {
		init();
	}
	
	private void init() {
		GridCellLayout layout = new GridCellLayout(10, 10, 20, 20);
		setLayout(layout);
		
		this.setOpaque(true);
		this.setBackground(Color.white);
		
		// create some labels to go into the grid
		JLabel l1 = new JLabel();
		l1.setBackground(Color.red);
		l1.setOpaque(true);
		
		JLabel l2 = new JLabel();
		l2.setBackground(Color.blue);
		l2.setOpaque(true);
		
		JLabel l3 = new JLabel();
		l3.setBackground(Color.green);
		l3.setOpaque(true);
		
		add(l1, GridCellConstraint.xy(2, 2));
		add(l2, GridCellConstraint.xywh(3,2,2,2));
		add(l3, GridCellConstraint.xywh(7, 5, 3, 3));
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("Test");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		GridCellTest t = new GridCellTest();
		f.add(t);
		f.pack();
		f.setVisible(true);
	}

}
