package ca.phon.ipamap2;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.ObjectFactory;

/**
 * Allows for selection of a set of IPA elements.
 * 
 */
public class IPAMapSelector extends JComponent {
	
	private Grid selectedCellGrid;
	private IPAMapGridContainer selectedMapContainer;
	private IPAMapGrid selectedMap;
	
	private IPAMapGridContainer map;
	
	private IPAMapInfoPane infoPane;
	
	private IPAGrids ipaGrids;
	
	private Map<String, IPAMapGrid> gridMap = new LinkedHashMap<>();
	
	public IPAMapSelector() {
		super();
		
		init();
	}
	
	public Set<String> getSectionNames() {
		return gridMap.keySet();
	}
	
	public boolean isSectionVisible(String sectionName) {
		IPAMapGrid grid = gridMap.get(sectionName);
		return (grid != null ? grid.isVisible() : false);
	}
	
	public void setSectionVisible(String sectionName, boolean visible) {
		IPAMapGrid grid = gridMap.get(sectionName);
		if(grid != null) {
			grid.setVisible(visible);
		}
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
		
		ipaGrids = new IPAGrids();
		map = new IPAMapGridContainer();
		
		for(var ipaGrid:ipaGrids.loadGridData().getGrid()) {
			var tuple = map.addGrid(ipaGrid);
			gridMap.put(ipaGrid.getName(), tuple.getObj2());
		}
		
		map.setSelectionEnabled(true);
		map.addCellSelectionListener(cellSelectionListener);
		
		infoPane = new IPAMapInfoPane();
		
		map.addCellMouseListener(cellMouseListener);
		
		add(selectedMapContainer, BorderLayout.NORTH);
		add(new JScrollPane(map), BorderLayout.CENTER);
		add(infoPane, BorderLayout.SOUTH);
	}
	
	public List<String> getSelected() {
		return map.getSelectedCells()
			.stream().map( (c) -> c.getText() )
			.collect(Collectors.toList());
	}
	
	public void setSelected(Collection<String> selected) {
		clearSelection();
		for(var ipaGrid:gridMap.values()) {
			for(int i = 0; i < ipaGrid.getGrid().getCell().size(); i++) {
				Cell c = ipaGrid.getGrid().getCell().get(i);
				if(selected.contains(c.getText())) {
					ipaGrid.getSelectionModel().addSelectionInterval(i, i);
				}
			}
		}
		updateSelectedMap();
	}
	
	public void clearSelection() {
		selectedMap.getGrid().getCell().clear();
		updateSelectedMap();
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