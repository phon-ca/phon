package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import ca.phon.util.Tuple;

/**
 * Container for {@link IPAMapGrid}s.
 * 
 */
public class IPAMapGridContainer extends JPanel implements Scrollable {
	
	private IPAGrids grids;
	
	private List<IPAMapGrid> mapGrids;
	
	private boolean selectionEnabled = false;
	
	private Map<IPAMapGrid, JXCollapsiblePane> cpMap = new HashMap<>();
		
	private final EventListenerList listenerList = new EventListenerList();
	
	public IPAMapGridContainer() {
		super();
		
		grids = new IPAGrids();
		mapGrids = new ArrayList<>();
		
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
	}
	
	/**
	 * Adds all default {@link Grid}s found in the ipamap.xml layout file.
	 * 
	 * 
	 */
	public void addDefaultGrids() {
		for(var ipaGrid:grids.loadGridData().getGrid()) {
			addGrid(ipaGrid);
		}
	}
	
	/**
	 * Add {@link Grid} to container. The method
	 * creates a new toggle button and {@link IPAMapGrid}
	 * component and returns them in a {@link Tuple}. The
	 * new components are added to the container in a vertical
	 * layout.
	 * 
	 * @param ipaGrid
	 * @return
	 */
	public Tuple<JButton, IPAMapGrid> addGrid(Grid ipaGrid) {
		IPAMapGrid mapGrid = new IPAMapGrid(ipaGrid);
		mapGrid.addCellMouseListener(forwardingMouseListener);
		mapGrid.setFont(new Font("Charis SIL", Font.BOLD, 18));
		mapGrid.setCellFilter(cellFilter);
		mapGrid.setSelectionEnabled(isSelectionEnabled());
		mapGrid.getSelectionModel().addListSelectionListener(new SelectionListener(mapGrid));
		mapGrids.add(mapGrid);
		
		JXCollapsiblePane cp = new JXCollapsiblePane(Direction.DOWN);
		cp.setLayout(new BorderLayout());
		cp.add(mapGrid, BorderLayout.NORTH);
		cp.setAnimated(false);
		cpMap.put(mapGrid, cp);
		
		//System.out.println(mapGrid.getPreferredSize());
		JButton toggleButton = getToggleButton(ipaGrid, cp);
		add(toggleButton);
		add(cp);
		
		return new Tuple<>(toggleButton, mapGrid);
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
		
		cp.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				btn.setVisible(true);
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				btn.setVisible(false);
			}
			
		});
		
		return btn;
	}
	
	/* Selection */
	private class SelectionListener implements ListSelectionListener {

		private IPAMapGrid grid;
		
		public SelectionListener(IPAMapGrid grid) {
			this.grid = grid;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			fireCellSelectionChanged(grid, e.getFirstIndex(), 
					grid.getSelectionModel().isSelectedIndex(e.getFirstIndex()));
		}
		
	}
	
	public boolean isSelectionEnabled() {
		return this.selectionEnabled;
	}
	
	public void setSelectionEnabled(boolean selectionEnabled) {
		this.selectionEnabled = selectionEnabled;
		mapGrids.forEach( (g) -> g.setSelectionEnabled(selectionEnabled) );
	}
	
	public void addCellSelectionListener(IPAMapCellSelectionListener listener) {
		listenerList.add(IPAMapCellSelectionListener.class, listener);
	}
	
	public void removeCellSelecitonListener(IPAMapCellSelectionListener listener) {
		listenerList.remove(IPAMapCellSelectionListener.class, listener);
	}
	
	public void fireCellSelectionChanged(IPAMapGrid grid, int cellIdx, boolean selected) {
		for(IPAMapCellSelectionListener listener:listenerList.getListeners(IPAMapCellSelectionListener.class)) {
			listener.cellSelectionChanged(grid, cellIdx, selected);
		}
	}
	
	/**
	 * Return an unmodifiable list of selected Cells
	 * 
	 * @return list of selected cells
	 */
	public List<Cell> getSelectedCells() {
		List<Cell> retVal = new ArrayList<>();
		for(IPAMapGrid grid:mapGrids) {
			for(int selectedIdx:grid.getSelectionModel().getSelectedIndices()) {
				retVal.add(grid.getGrid().getCell().get(selectedIdx));
			}
		}
		return Collections.unmodifiableList(retVal);
	}
	
	/**
	 * Is the given section visible
	 * 
	 * @param gridName
	 * @return <code>true</code> if section is visible
	 */
	public boolean isGridVisible(String gridName) {
		Optional<IPAMapGrid> mapGrid =
			mapGrids.stream().filter( (grid) -> grid.getGrid().getName().equals(gridName) ).findAny();
		if(mapGrid.isPresent()) {
			return isGridVisible(mapGrid.get());
		} else {
			return false;
		}
	}
	
	public void setGridVisible(String gridName, boolean visible) {
		Optional<IPAMapGrid> mapGrid =
			mapGrids.stream().filter( (grid) -> grid.getGrid().getName().equals(gridName) ).findAny();
		if(mapGrid.isPresent()) {
			setGridVisible(mapGrid.get(), visible);
		}
	}
	
	public boolean isGridVisible(IPAMapGrid mapGrid) {
		if(cpMap.containsKey(mapGrid)) {
			return cpMap.get(mapGrid).isVisible();
		} else {
			return false;
		}
	}
	
	public void setGridVisible(IPAMapGrid mapGrid, boolean visible) {
		if(cpMap.containsKey(mapGrid)) {
			cpMap.get(mapGrid).setVisible(visible);
		}
	}
	
	/* Mouse Events */
	public void addCellMouseListener(IPAMapGridMouseListener listener) {
		listenerList.add(IPAMapGridMouseListener.class, listener);
	}

	public void removeCellMouseListener(IPAMapGridMouseListener listener) {
		listenerList.remove(IPAMapGridMouseListener.class, listener);
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

	public IPAMapGridMouseListener[] getCellMouseListeners() {
		return listenerList.getListeners(IPAMapGridMouseListener.class);
	}

	/* Cell filter */
	private class DelegateCellFilter implements Predicate<Cell> {

		private Predicate<Cell> delegate;
		
		public DelegateCellFilter(Predicate<Cell> delegate) {
			this.delegate = delegate;
		}
				
		@Override
		public boolean test(Cell t) {
			return (delegate != null ? delegate.test(t) : true);
		}
		
	}
	
	private final DelegateCellFilter cellFilter = new DelegateCellFilter( (t) -> true );
	
	public Predicate<Cell> getCellFilter() {
		return this.cellFilter.delegate;
	}
	
	public void setCellFilter(Predicate<Cell> filter) {
		this.cellFilter.delegate = filter;
		revalidate();
	}
	
	/* Cell renderer */
	private class DelegateCellRenderer implements IPAMapCellRenderer {
		
		IPAMapCellRenderer delegate;
		
		public DelegateCellRenderer(IPAMapCellRenderer cellRenderer) {
			this.delegate = cellRenderer;
		}

		@Override
		public Dimension getCellDimension(IPAMapGrid mapGrid) {
			return (delegate != null ? delegate.getCellDimension(mapGrid) : new Dimension());
		}

		@Override
		public void paintCell(IPAMapGrid mapGrid, Graphics2D g2, Rectangle cellRect, Cell cell, boolean isHover,
				boolean isPressed, boolean isSelected) {
			if(delegate != null) {
				delegate.paintCell(mapGrid, g2, cellRect, cell, isHover, isPressed, isSelected);
			}
		}
				
	}
	
	private final DelegateCellRenderer cellRenderer = new DelegateCellRenderer( new DefaultIPAMapCellRenderer() );
	
	public IPAMapCellRenderer getCellRenderer() {
		return cellRenderer.delegate;
	}
	
	public void setCellRenderer(IPAMapCellRenderer cellRenderer) {
		this.cellRenderer.delegate = cellRenderer;
		revalidate();
		repaint();
	}
	
	/*
	 * IPAMap events
	 */
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
	
	/* Scrollable */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		Dimension retVal = super.getPreferredSize();
		retVal.width += (new JScrollBar(SwingConstants.VERTICAL)).getPreferredSize().width;
		return retVal;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int retVal = 0;
		
		if(mapGrids.size() > 0) {
			retVal = mapGrids.get(0).getScrollableUnitIncrement(visibleRect, orientation, direction);
		}
		
		return retVal;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int retVal = 0;
		
		if(mapGrids.size() > 0) {
			retVal = mapGrids.get(0).getScrollableBlockIncrement(visibleRect, orientation, direction);
		}
		
		return retVal;
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
