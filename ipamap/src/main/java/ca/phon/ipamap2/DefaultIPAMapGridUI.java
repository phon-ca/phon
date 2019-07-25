package ca.phon.ipamap2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ca.phon.ui.ipamap.io.Cell;
import gnu.trove.procedure.TIntProcedure;
import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;

public class DefaultIPAMapGridUI extends IPAMapGridUI {
	
	protected IPAMapGrid ipaGrid;
	
	protected JLabel glyphRenderer;
	
	private RTree glyphRectTree;

	public DefaultIPAMapGridUI() {
		super();
		
		glyphRenderer = new JLabel();
		glyphRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		glyphRenderer.setHorizontalTextPosition(SwingConstants.CENTER);
		glyphRenderer.setVerticalAlignment(SwingConstants.CENTER);
		glyphRenderer.setVerticalTextPosition(SwingConstants.CENTER);
		glyphRenderer.setDoubleBuffered(false);
		
		glyphRectTree = new RTree();
	}
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof IPAMapGrid))
			throw new IllegalArgumentException("Wrong class");
		super.installUI(c);
		
		ipaGrid = (IPAMapGrid)c;
		ipaGrid.addMouseListener(mouseListener);
		ipaGrid.addMouseMotionListener(mouseListener);
	
		ipaGrid.setOpaque(true);
		ipaGrid.setBackground(Color.WHITE);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
		c.removeMouseListener(mouseListener);
		c.removeMouseMotionListener(mouseListener);
	}

	public Dimension getCellDimension() {
		Dimension retVal = new Dimension(0, 0);
		
		String tStr = (char)0x25cc + "" + (char)0x1d50;
		glyphRenderer.setText(tStr);
		glyphRenderer.setFont(ipaGrid.getFont());
		glyphRenderer.setBorder(BorderFactory.createEmptyBorder(ipaGrid.getCellInsets().top, 
				ipaGrid.getCellInsets().left,
				ipaGrid.getCellInsets().bottom, 
				ipaGrid.getCellInsets().right));
		retVal = glyphRenderer.getPreferredSize();
		retVal.height += ipaGrid.getCellInsets().top + ipaGrid.getCellInsets().bottom;
		retVal.width += ipaGrid.getCellInsets().left + ipaGrid.getCellInsets().right;
		retVal.height /= 2;
		retVal.width /= 2;

		return retVal;
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		final Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		if(ipaGrid.isOpaque()) {
			g2.setColor(ipaGrid.getBackground());
			g2.fill(g.getClipBounds());
		}
		paintStripes(g2);

		Dimension cellDim = getCellDimension();
		glyphRenderer.setFont(ipaGrid.getFont());
		glyphRenderer.setBorder(
				BorderFactory.createEmptyBorder(ipaGrid.getCellInsets().top, ipaGrid.getCellInsets().left, 
						ipaGrid.getCellInsets().bottom, ipaGrid.getCellInsets().right));
		
		glyphRectTree.reset();
		glyphRectTree.init(null);
		for(int cellIdx = 0; cellIdx < ipaGrid.getGrid().getCell().size(); cellIdx++) {
			Cell cell = ipaGrid.getGrid().getCell().get(cellIdx);
			Rectangle cellRect = getCellRect(cell, cellDim);
			if(cellRect.intersects(g.getClipBounds())) {
				glyphRectTree.add(new net.sf.jsi.Rectangle(cellRect.x, cellRect.y, 
						cellRect.x+cellRect.width, cellRect.y+cellRect.height), cellIdx);
				paintCell(g2, cell, cellIdx, cellRect);
			}
		}
	}
	
	public Rectangle getCellRect(Cell cell, Dimension cellDim) {
		var cellX = ipaGrid.getInsets().left + (cell.getX() * (int)cellDim.getWidth());
		var cellY = ipaGrid.getInsets().top + (cell.getY() * (int)cellDim.getHeight());
		var cellW = cell.getW() * (int)cellDim.getWidth();
		var cellH = cell.getH() * (int)cellDim.getHeight();
		
		return new Rectangle (cellX, cellY, cellW, cellH);
	}
	
	protected void paintCell(Graphics2D g2, Cell cell, int cellIdx, Rectangle cellRect) {
		var roundRect = new RoundRectangle2D.Double(
				cellRect.x + ipaGrid.getCellInsets().left,
				cellRect.y + ipaGrid.getCellInsets().top,
				cellRect.width - ipaGrid.getCellInsets().left - ipaGrid.getCellInsets().right,
				cellRect.height - ipaGrid.getCellInsets().top - ipaGrid.getCellInsets().bottom,
				5, 5);
		
		if(isCellEntered(cellIdx)) {
			g2.setColor(Color.yellow);
			g2.fill(roundRect);
		}
		if(isCellPressed(cellIdx)) {
			g2.setColor(Color.GRAY);
			g2.draw(roundRect);
		}
		
		glyphRenderer.setText(cell.getText());
		SwingUtilities.paintComponent(g2, glyphRenderer, ipaGrid, cellRect);
		
				
	}
	
	protected void paintStripes(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fill(g2.getClipBounds());
		
		Dimension cellDim = getCellDimension();
		Rectangle stripeRect = new Rectangle(ipaGrid.getInsets().left, 
				ipaGrid.getInsets().top + cellDim.height * 2, 
				ipaGrid.getWidth() - ipaGrid.getInsets().left - ipaGrid.getInsets().right, cellDim.height*2);
		
		while(stripeRect.intersects(g2.getClipBounds())) {
			g2.setColor(Color.decode("0xF0F8FF"));
			g2.fill(stripeRect);
		
			stripeRect.y += cellDim.getHeight() * 4;
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension cellDimension = getCellDimension();
		
		int w = (cellDimension.width * ipaGrid.getColumnCount()) 
				+ ipaGrid.getInsets().left + ipaGrid.getInsets().right;
		int h = (cellDimension.height * ipaGrid.getRowCount()) 
				+ ipaGrid.getInsets().top + ipaGrid.getInsets().bottom;
		
		return new Dimension(w, h);
	}
	
	private boolean isCellPressed(int cellIdx) {
		return (mouseListener.currentlyPressedCell >= 0 
				&& cellIdx == mouseListener.currentlyPressedCell);
	}
	
	private boolean isCellEntered(int cellIdx) {
		return (mouseListener.currentlyEnteredCell >= 0 
				&& cellIdx == mouseListener.currentlyEnteredCell);
	}
	
	private IPAMapGridMouseAdapter mouseListener = new IPAMapGridMouseAdapter();
		
	private class IPAMapGridMouseAdapter extends MouseInputAdapter {

		private int currentlyPressedCell = -1;
		
		private int currentlyEnteredCell = -1;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			glyphRectTree.intersects(new net.sf.jsi.Rectangle(e.getX(), e.getY(), e.getX(), e.getY()), new TIntProcedure() {
				
				@Override
				public boolean execute(int value) {
					ipaGrid.fireCellClicked(ipaGrid.getGrid().getCell().get(value), e);
					return true;
				}
				
			});
		}

		@Override
		public void mousePressed(MouseEvent e) {
			glyphRectTree.intersects(new net.sf.jsi.Rectangle(e.getX(), e.getY(), e.getX(), e.getY()), new TIntProcedure() {
				
				@Override
				public boolean execute(int value) {
					currentlyPressedCell = value;
					ipaGrid.fireCellPressed(ipaGrid.getGrid().getCell().get(value), e);
					ipaGrid.repaint();
					return true;
				}
				
			});
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(currentlyPressedCell >= 0) {
				ipaGrid.fireCellReleased(ipaGrid.getGrid().getCell().get(currentlyPressedCell), e);
				currentlyPressedCell = -1;
				ipaGrid.repaint();
			}
			currentlyPressedCell = -1;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			AtomicInteger intersectedCell = new AtomicInteger(-1);
			glyphRectTree.intersects(new net.sf.jsi.Rectangle(e.getX(), e.getY(), e.getX(), e.getY()), new TIntProcedure() {
				
				@Override
				public boolean execute(int value) {
					intersectedCell.set(value);
					return true;
				}
				
			});
			
			if(intersectedCell.get() >= 0 && currentlyEnteredCell != intersectedCell.get()) {
				if(currentlyEnteredCell >= 0) {
					ipaGrid.fireCellExited(ipaGrid.getGrid().getCell().get(currentlyEnteredCell), e);
				}
				currentlyEnteredCell = intersectedCell.get();
				ipaGrid.fireCellEntered(ipaGrid.getGrid().getCell().get(currentlyEnteredCell), e);
				ipaGrid.repaint();
			} else if(intersectedCell.get() < 0 && currentlyEnteredCell >= 0) {
				ipaGrid.fireCellExited(ipaGrid.getGrid().getCell().get(currentlyEnteredCell), e);
				currentlyEnteredCell = -1;
				ipaGrid.repaint();
			}
		}
		
	};
	
}
