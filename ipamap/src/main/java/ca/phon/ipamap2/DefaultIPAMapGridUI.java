package ca.phon.ipamap2;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.event.*;

import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.*;

import ca.phon.ui.ipamap.io.*;

public class DefaultIPAMapGridUI extends IPAMapGridUI {
	
	protected IPAMapGrid ipaGrid;
	
	private RTree<Integer, com.github.davidmoten.rtree.geometry.Rectangle> glyphRectTree;
	
	private IPAMapInfoPane currentToolTip;

	public DefaultIPAMapGridUI() {
		super();
		
		glyphRectTree = RTree.create();
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
	
	@Override
	public void paint(Graphics g, JComponent c) {
		final Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		if(ipaGrid.isOpaque()) {
			g2.setColor(ipaGrid.getBackground());
			g2.fill(g.getClipBounds());
		}
		paintStripes(g2);

		IPAMapCellRenderer cellRenderer = ipaGrid.getCellRenderer();
		Dimension cellDim = cellRenderer.getCellDimension(ipaGrid);
				
		glyphRectTree = RTree.create();
		for(int cellIdx = 0; cellIdx < ipaGrid.getGrid().getCell().size(); cellIdx++) {
			Cell cell = ipaGrid.getGrid().getCell().get(cellIdx);
			
			Predicate<Cell> cellFilter = ipaGrid.getCellFilter();
			if(cellFilter == null || cellFilter.test(cell)) {
				Rectangle cellRect = getCellRect(cell, cellDim);
				glyphRectTree = glyphRectTree.add(cellIdx,
						Geometries.rectangle(cellRect.getX(), cellRect.getY(), 
								cellRect.getX() + cellRect.getWidth(), cellRect.getY() + cellRect.getHeight()));
				cellRenderer.paintCell(ipaGrid, g2, cellRect, cell, isCellEntered(cellIdx), isCellPressed(cellIdx), isCellSelected(cellIdx));
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
	
	protected void paintStripes(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fill(g2.getClipBounds());
		
		Dimension cellDim = ipaGrid.getCellRenderer().getCellDimension(ipaGrid);
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
		Dimension cellDimension = ipaGrid.getCellRenderer().getCellDimension(ipaGrid);
		
		// add one to column count for popup windows 
		int w = (cellDimension.width * (ipaGrid.getColumnCount()+1)) 
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
	
	private boolean isCellSelected(int cellIdx) {
		int[] selected = ipaGrid.getSelectionModel().getSelectedIndices();
		return ipaGrid.isSelectionEnabled() 
				&& Arrays.stream(selected).boxed().collect(Collectors.toList()).contains(cellIdx);
	}
	
	private void toggleCellSelected(int cellIdx) {
		if(ipaGrid.getSelectionModel().isSelectedIndex(cellIdx)) {
			ipaGrid.getSelectionModel().removeSelectionInterval(cellIdx, cellIdx);
		} else {
			ipaGrid.getSelectionModel().addSelectionInterval(cellIdx, cellIdx);
		}
	}
		
	private IPAMapGridMouseAdapter mouseListener = new IPAMapGridMouseAdapter();
		
	private class IPAMapGridMouseAdapter extends MouseInputAdapter {

		private int currentlyPressedCell = -1;
		
		private int currentlyEnteredCell = -1;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			var entries = glyphRectTree.search(Geometries.point(e.getX(), e.getY()));
			entries.map( entry -> entry.value() ).forEach( value -> {
				ipaGrid.fireCellClicked(ipaGrid.getGrid().getCell().get(value), e);
				if(ipaGrid.isSelectionEnabled())
					toggleCellSelected(value);
			});
		}

		@Override
		public void mousePressed(MouseEvent e) {
			var entries = glyphRectTree.search(Geometries.point(e.getX(), e.getY()));
			entries.map( entry -> entry.value() ).forEach( value -> {
				currentlyPressedCell = value;
				ipaGrid.fireCellPressed(ipaGrid.getGrid().getCell().get(value), e);
				ipaGrid.repaint();
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
			var entries = glyphRectTree.search(Geometries.point(e.getX(), e.getY()));
			entries.map( entry -> entry.value() ).forEach( value -> {
				intersectedCell.set(value);
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

		@Override
		public void mouseExited(MouseEvent e) {
			if(currentlyEnteredCell >= 0) {
				ipaGrid.fireCellExited(ipaGrid.getGrid().getCell().get(currentlyEnteredCell), e);
				currentlyEnteredCell = -1;
				ipaGrid.repaint();
			}
		}
		
	};
	
}
