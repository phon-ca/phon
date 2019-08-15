package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.ObjectFactory;

/**
 * Allows for selection of a set of IPA elements.
 * 
 */
public class IPAElementSelector extends JComponent {
	
	private Grid selectedCellGrid;
	private IPAMapGridContainer selectedMapContainer;
	private IPAMapGrid selectedMap;
	
	private IPAMapGridContainer map;
	
	private IPAMapInfoPane infoPane;
	
	public IPAElementSelector(Set<Cell> selectedCells) {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		var objFactory = new ObjectFactory();
		selectedCellGrid = objFactory.createGrid();
		selectedCellGrid.setName("Selected");
		selectedCellGrid.setCols(42);
		selectedCellGrid.setRows(2);
		selectedMapContainer = new IPAMapGridContainer();
		var gridTuple = selectedMapContainer.addGrid(selectedCellGrid);
		selectedMap = gridTuple.getObj2();
		selectedMap.addCellMouseListener(cellMouseListener);
		
		map = new IPAMapGridContainer();
		map.addDefaultGrids();
		map.setSelectionEnabled(true);
		map.addCellSelectionListener(cellSelectionListener);
		
		infoPane = new IPAMapInfoPane();
		
		map.addCellMouseListener(cellMouseListener);
		
		add(selectedMapContainer, BorderLayout.NORTH);
		add(new JScrollPane(map), BorderLayout.CENTER);
		add(infoPane, BorderLayout.SOUTH);
	}
	
	private IPAMapCellSelectionListener cellSelectionListener = new IPAMapCellSelectionListener() {
		
		@Override
		public void cellSelectionChanged(IPAMapGrid mapGrid, int cellIdx, boolean selected) {
			updateSelectedMap();
		}
		
	};
	
	private IPAMapGridMouseListener cellMouseListener = new IPAMapGridMouseListener() {
		
		@Override
		public void mouseReleased(Cell cell, MouseEvent me) {
			
		}
		
		@Override
		public void mousePressed(Cell cell, MouseEvent me) {
			
		}
		
		@Override
		public void mouseExited(Cell cell, MouseEvent me) {
			infoPane.clear();
		}
		
		@Override
		public void mouseEntered(Cell cell, MouseEvent me) {
			infoPane.update(cell);
		}
		
		@Override
		public void mouseClicked(Cell cell, MouseEvent me) {
			
		}
	};
	
	private void updateSelectedMap() {
		IPAGrids gridFactory = new IPAGrids();
		
		selectedCellGrid.getCell().clear();
		gridFactory.buildGrid(selectedCellGrid, map.getSelectedCells());
		
		selectedMap.revalidate();
		selectedMap.repaint();
	}
	
}
