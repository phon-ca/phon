package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.BevelBorder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.ipamap.IpaMap;
import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.IpaGrids;
import ca.phon.ui.ipamap.io.ObjectFactory;

public class IPAMap extends JComponent {
	
	private IPAGrids grids;
	
	public IPAMap() {
		super();
		
		grids = new IPAGrids();
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		for(var ipaGrid:grids.getGridData().getGrid()) {
			IPAMapGrid mapGrid = new IPAMapGrid(ipaGrid);
			mapGrid.setFont(new Font("Charis SIL Compact", Font.BOLD, 18));
			mapGrid.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY), 
					mapGrid.getGrid().getName()));
			add(mapGrid);
		}
	}
	
	public static void main(String[] args) {
		IPAMap map = new IPAMap();
		JFrame f = new JFrame("Test");
		f.setLayout(new BorderLayout());
		f.add(map, BorderLayout.CENTER);
		
		f.pack();
		f.setVisible(true);
	}
	
}
