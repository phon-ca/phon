package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

import ca.phon.ipamap.IpaMap;
import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.IpaGrids;
import ca.phon.ui.ipamap.io.ObjectFactory;

/**
 * Container for {@link IPAMapGrid}s.
 * 
 */
public class IPAMapGridContainer extends JComponent implements Scrollable {
	
	private IPAGrids grids;
	
	private List<IPAMapGrid> mapGrids;
		
	private final EventListenerList listenerList = new EventListenerList();
	
	public IPAMapGridContainer() {
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
			mapGrid.setFont(new Font("Arial", Font.BOLD, 18));
			mapGrids.add(mapGrid);
			
			JXCollapsiblePane cp = new JXCollapsiblePane(Direction.DOWN);
			cp.setLayout(new BorderLayout());
			cp.add(mapGrid, BorderLayout.NORTH);
			cp.setAnimated(false);
			
			//System.out.println(mapGrid.getPreferredSize());
			add(getToggleButton(ipaGrid, cp));
			add(cp);
		}
	}
	
	private JXButton getToggleButton(Grid grid, JXCollapsiblePane cp) {
		Action toggleAction = cp.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
		                      UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
		                      UIManager.getIcon("Tree.collapsedIcon"));
		toggleAction.putValue(Action.NAME, grid.getName());

		JXButton btn = new JXButton(toggleAction) {
			@Override
			public Insets getInsets() {
				Insets retVal = super.getInsets();
				
				retVal.top = 0;
				retVal.bottom = 0;
				
				return retVal;
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, 20);
			}
			
			
		};
		
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		
		btn.setBackgroundPainter(new Painter<JXButton>() {
			
			@Override
			public void paint(Graphics2D g, JXButton object, int width, int height) {
				MattePainter mp = new MattePainter(UIManager.getColor("Button.background"));
				mp.paint(g, object, width, height);
			}
		});
		
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setBorderPainted(false);
		btn.setFocusable(false);
		
		btn.putClientProperty("JComponent.sizeVariant", "small");

		btn.revalidate();
		
		return btn;
	}
	
	public static void main(String[] args) {
		IPAMapGridContainer map = new IPAMapGridContainer();
		JFrame f = new JFrame("Test");
		f.setLayout(new BorderLayout());
		f.add(new JScrollPane(map), BorderLayout.CENTER);
		
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
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
}
