package ca.phon.ipamap2;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.Scrollable;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.ComponentUI;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;

/**
 * Display a single grid of the IPA Map.
 * 
 */
public class IPAMapGrid extends JComponent implements Scrollable {

	private final static String uiClassId = "IPAMapGridUI";
	
	private final Grid grid;

	private Insets cellInsets = new Insets(2, 2, 2, 2);
	
	private boolean collapsed = false;
	
	private final EventListenerList listenerList = new EventListenerList();
	
	public IPAMapGrid(Grid grid) {
		super();
		this.grid = grid;
	
		updateUI();
	}

	@Override
	public void updateUI() {
		setUI(new DefaultIPAMapGridUI());
	}
	
	@Override
	public void setUI(ComponentUI gridUI) {
		if(!(gridUI instanceof IPAMapGridUI))
			throw new IllegalArgumentException("Incorrect UI type");
		super.setUI(gridUI);
	}
	
	@Override
	public String getUIClassID() {
		return super.getUIClassID();
	}
	
	@Override
	public IPAMapGridUI getUI() {
		return (IPAMapGridUI)super.getUI();
	}
	
	public Grid getGrid() {
		return this.grid;
	}
	
	public int getColumnCount() {
		return grid.getCols();
	}
	
	public int getRowCount() {
		return grid.getRows();
	}
	
	public Insets getCellInsets() {
		return this.cellInsets;
	}
	
	public void setCellInsets(Insets cellInsets) {
		var oldVal = this.cellInsets;
		this.cellInsets = cellInsets;
		super.firePropertyChange("cellInsets", oldVal, cellInsets);
	}
	
	public boolean isCollapsed() {
		return this.collapsed;
	}
	
	public void setCollapsed(boolean collapsed) {
		var oldVal = this.collapsed;
		this.collapsed = collapsed;
		super.firePropertyChange("collapsed", oldVal, collapsed);
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
	
	@Override
	public JToolTip createToolTip() {
		System.out.print("Here");
		return getUI().createToolTip();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (int)getUI().getCellDimension().getHeight() * direction;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (int)getUI().getCellDimension().getHeight() * 10 * direction;
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
