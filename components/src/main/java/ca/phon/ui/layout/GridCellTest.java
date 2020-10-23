/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ui.layout;

import java.awt.*;

import javax.swing.*;

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
