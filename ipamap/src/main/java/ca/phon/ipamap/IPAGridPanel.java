package ca.phon.ipamap;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.layout.GridCellLayout;
import ca.phon.ui.painter.Painter;

/**
 * Panel for IPA Grids
 */
public class IPAGridPanel extends JPanel {
	
	/** 
	 * Parent display, used to get cell dimensions
	 * scale, and font
	 */
	private IpaMap parent;
	
	/**
	 * The grid info
	 */
	private Grid ipaGrid;
	
	/**
	 * Cells in a map
	 */
	private Map<Rectangle, Cell> cellRects = 
		Collections.synchronizedMap(new HashMap<Rectangle, Cell>());
	
	/**
	 * Grid cell layout
	 */
	private GridCellLayout gridLayout;
	
	/**
	 * Painter
	 * 
	 */
	private Painter<IPAGridPanel> painter;
	
	public IPAGridPanel(IpaMap parent, Grid grid) {
		this.ipaGrid = grid;
		this.parent = parent;
		

		MouseHandler mouseHandler = new MouseHandler();
		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);
		this.addMouseWheelListener(mouseHandler);
		
		setupGrid();
	}
	
	private void setupGrid() {
		Dimension cellDim = parent.getCellDimension();
		
		gridLayout = new GridCellLayout(ipaGrid.getRows(), ipaGrid.getCols(),
				(int)cellDim.getWidth(), (int)cellDim.getHeight());
//		super.setLayout(gridLayout);
		
	}
	
	public GridCellLayout getGridLayout() {
		return this.gridLayout;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return gridLayout.preferredLayoutSize(getParent());
	}
	
	public Grid getGrid() {
		return this.ipaGrid;
	}
	
	public void setGrid(Grid g) {
		this.ipaGrid = g;
		setupGrid();
	}
	
	public IpaMap getParent() {
		return this.parent;
	}
	
	public void setParent(IpaMap p) {
		this.parent = p;
	}
	
	public void setPainter(Painter<IPAGridPanel> painter) {
		this.painter = painter;
	}
	
	public Painter<IPAGridPanel> getPainter() {
		return this.painter;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		
		painter.paint(g2d, this, getWidth(), getHeight());
	}
	
	/**
	 * Mouse listener
	 */
	private class MouseHandler extends MouseInputAdapter {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			super.mouseClicked(arg0);
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			Point p = arg0.getPoint();
			
			// calculate cell row & col
			int row = (int)(p.getX() % gridLayout.getCellWidth());
			int col = (int)(p.getY() % gridLayout.getCellHeight());
			
			System.out.println("[" + row + "," + col + "]");
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			super.mousePressed(arg0);
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			super.mouseReleased(arg0);
		}
		
	}
}
