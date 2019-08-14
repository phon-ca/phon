package ca.phon.ipamap2;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ca.phon.ui.CommonModuleFrame;

public class IPAMap  {
	
	public static void main(String[] args) {
		JFrame f = new JFrame("test");
		IPAMapGridContainer mapContainer = new IPAMapGridContainer();
		mapContainer.addDefaultGrids();
		
		f.setLayout(new BorderLayout());
		f.add(new JScrollPane(mapContainer), BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
	}

}
