package ca.phon.ipamap2;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;

/**
 * Display a single grid of the IPA Map.
 * 
 */
public class IPAMapGrid extends JComponent {

	private final static String uiClassId = "IPAMapGridUI";
	
	private final Grid grid;

	private Insets cellInsets = new Insets(2, 2, 2, 2);
	
	private List<IPAMapGridMouseListener> mouseListeners = new ArrayList<>();
	
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
	
	public void addCellMouseListener(IPAMapGridMouseListener listener) {
		mouseListeners.add(listener);
	}
	
	public void removeCellMouseListener(IPAMapGridMouseListener listener) {
		mouseListeners.remove(listener);
	}
	
	public void fireCellPressed(Cell cell, MouseEvent me) {
		mouseListeners.forEach( (ml) -> ml.mousePressed(cell, me) );
	}
	
	public void fireCellReleased(Cell cell, MouseEvent me) {
		mouseListeners.forEach( (ml) -> ml.mouseReleased(cell, me) );
	}
	
	public void fireCellClicked(Cell cell, MouseEvent me) {
		mouseListeners.forEach( (ml) -> ml.mouseClicked(cell, me) );
	}
	
	public void fireCellEntered(Cell cell, MouseEvent me) {
		mouseListeners.forEach( (ml) -> ml.mouseEntered(cell, me) );
	}
	
	public void fireCellExited(Cell cell, MouseEvent me) {
		mouseListeners.forEach( (ml) -> ml.mouseExited(cell, me) );
	}
	
}
