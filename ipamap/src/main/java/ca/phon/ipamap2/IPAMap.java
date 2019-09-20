package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ca.phon.ui.CommonModuleFrame;

public class IPAMap  {
	
	public static void main(String[] args) {
		JFrame f = new JFrame("test");
		
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new DiacriticSelector(), BorderLayout.CENTER);
		
		f.pack();
		f.setVisible(true);
	}

}
