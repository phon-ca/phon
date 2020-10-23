package ca.phon.ipamap2;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

import ca.phon.ui.ipamap.io.*;

/**
 * Display a single grid of the IPA Map.
 * 
 */
public class IPAMapGrid extends JComponent implements Scrollable {

	private final static String uiClassId = "IPAMapGridUI";
	
	private final Grid grid;

	private Insets cellInsets = new Insets(2, 2, 2, 2);
	
	private final EventListenerList listenerList = new EventListenerList();
	
	private IPAMapCellRenderer cellRenderer = new DefaultIPAMapCellRenderer();
	
	// by default show all cells
	private Predicate<Cell> cellFilter = (c) -> true ;
	
	// enable selection on click?
	private boolean selectionEnabled = false;
	
	// selection model
	private ListSelectionModel selectionModel = new DefaultListSelectionModel();
	
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
	
	public IPAMapCellRenderer getCellRenderer() {
		return this.cellRenderer;
	}
	
	public void setCellRenderer(IPAMapCellRenderer cellRenderer) {
		var oldVal = this.cellRenderer;
		this.cellRenderer = cellRenderer;
		super.firePropertyChange("cellRenderer", oldVal, cellRenderer);
	}
	
	public Predicate<Cell> getCellFilter() {
		return this.cellFilter;
	}
	
	public void setCellFilter(Predicate<Cell> cellFilter) {
		var oldVal = this.cellFilter;
		this.cellFilter = cellFilter;
		super.firePropertyChange("cellFilter", oldVal, cellFilter);
	}
	
	public boolean isSelectionEnabled() {
		return this.selectionEnabled;
	}
	
	public void setSelectionEnabled(boolean enabled) {
		var oldVal = this.selectionEnabled;
		this.selectionEnabled = enabled;
		super.firePropertyChange("selectionEnabled", oldVal, enabled);
	}
	
	public ListSelectionModel getSelectionModel() {
		return this.selectionModel;
	}
	
	public void clearSelection() {
		getSelectionModel().clearSelection();
	}
	
	public void invertSelection() {
		int[] currentSelection = getSelectionModel().getSelectedIndices();
		clearSelection();
		for(int i = 0; i < getGrid().getCell().size(); i++) {
			if(Arrays.binarySearch(currentSelection, i) < 0) {
				getSelectionModel().addSelectionInterval(i, i);
			}
		}
	}
	
	public void selectAll() {
		var selectionModel = getSelectionModel();
		selectionModel.clearSelection();
		selectionModel.addSelectionInterval(0, getGrid().getCell().size()-1);
	}
	
	public void setSelectionModel(ListSelectionModel selectionModel) {
		var oldVal = this.selectionModel;
		this.selectionModel = selectionModel;
		super.firePropertyChange("selectionModel", oldVal, selectionModel);
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
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (int)getCellRenderer().getCellDimension(this).getHeight();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return (int)getCellRenderer().getCellDimension(this).getHeight() * 10;
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
