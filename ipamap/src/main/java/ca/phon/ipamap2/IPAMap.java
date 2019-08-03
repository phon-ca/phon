package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
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
	
	private List<IPAMapGrid> mapGrids;
	
	private final EventListenerList listenerList = new EventListenerList();
	
	public IPAMap() {
		super();
		
		grids = new IPAGrids();
		mapGrids = new ArrayList<>();
		
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		for(var ipaGrid:grids.getGridData().getGrid()) {
			IPAMapGrid mapGrid = new IPAMapGrid(ipaGrid);
			mapGrid.addCellMouseListener(forwardingMouseListener);
			mapGrid.setFont(new Font("Charis SIL Compact", Font.BOLD, 18));
			mapGrid.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY), 
					mapGrid.getGrid().getName()));
			mapGrids.add(mapGrid);
			
			//System.out.println(mapGrid.getPreferredSize());
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
	
	public void addCellMouseListener(IPAMapGridMouseListener listener) {
		listenerList.add(IPAMapGridMouseListener.class, listener);
	}

	public void removeCellMouseListener(IPAMapGridMouseListener listener) {
		listenerList.remove(IPAMapGridMouseListener.class, listener);
	}
	
	public IPAMapGridMouseListener[] getCellMouseListeners() {
		return listenerList.getListeners(IPAMapGridMouseListener.class);
	}

	public void fireCellPressed(Cell cell, MouseEvent me) {
		Arrays.stream(getCellMouseListeners()).forEach( (ml) -> ml.mousePressed(cell, me) );
	}
	
	public void fireCellReleased(Cell cell, MouseEvent me) {
		Arrays.stream(getCellMouseListeners()).forEach( (ml) -> ml.mouseReleased(cell, me) );
	}
	
	public void fireCellClicked(Cell cell, MouseEvent me) {
		Arrays.stream(getCellMouseListeners()).forEach( (ml) -> ml.mouseClicked(cell, me) );
	}
	
	public void fireCellEntered(Cell cell, MouseEvent me) {
		Arrays.stream(getCellMouseListeners()).forEach( (ml) -> ml.mouseEntered(cell, me) );
	}
	
	public void fireCellExited(Cell cell, MouseEvent me) {
		Arrays.stream(getCellMouseListeners()).forEach( (ml) -> ml.mouseExited(cell, me) );
	}
	
	private final IPAMapGridMouseListener forwardingMouseListener = new IPAMapGridMouseListener() {
		
		@Override
		public void mouseReleased(Cell cell, MouseEvent me) {
			fireCellReleased(cell, me);
		}
		
		@Override
		public void mousePressed(Cell cell, MouseEvent me) {
			fireCellPressed(cell, me);
		}
		
		@Override
		public void mouseExited(Cell cell, MouseEvent me) {
			fireCellExited(cell, me);
		}
		
		@Override
		public void mouseEntered(Cell cell, MouseEvent me) {
			fireCellEntered(cell, me);
		}
		
		@Override
		public void mouseClicked(Cell cell, MouseEvent me) {
			fireCellClicked(cell, me);
		}
		
	};
	
	private class IPAMapGridPanel extends JPanel implements Scrollable {
	
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}
	
		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return (mapGrids.size() > 0 ? mapGrids.get(0).getUI().getCellDimension().height : 0);
		}
	
		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return (mapGrids.size() > 0 ? mapGrids.get(0).getUI().getCellDimension().height * 4 : 0);
		}
	
		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
	
		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

	}
	
}
