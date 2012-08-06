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
