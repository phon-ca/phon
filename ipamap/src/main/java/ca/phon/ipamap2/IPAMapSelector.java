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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.ObjectFactory;
import ca.phon.ui.tristatecheckbox.TristateCheckBox;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;

/**
 * Allows for selection of a set of IPA elements.
 * 
 */
public class IPAMapSelector extends JComponent {
	
	private Grid selectedCellGrid;
	
	private IPAMapGridContainer map;
	
	private IPAMapInfoPane infoPane;
	
	protected IPAGrids ipaGrids;
	
	private Map<String, IPAMapGrid> gridMap = new LinkedHashMap<>();
	
	public IPAMapSelector() {
		super();
		
		init();
		loadGrids();
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
	
	public IPAMapGridContainer getMapGridContainer() {
		return this.map;
	}
	
	public Collection<IPAMapGrid> getMapGrids() {
		return gridMap.values();
	}
	
	public void addCheckBox(final IPAMapGrid mapGrid) {
		JPanel gridPanel = map.getMapGridPanel(mapGrid);
		
		TristateCheckBox triStateCheckbox = new TristateCheckBox();
		triStateCheckbox.setToolTipText("Select all");
		triStateCheckbox.setSelectionState(getSelectionStateForGrid(mapGrid));
		
		triStateCheckbox.addActionListener( (e) -> {
			TristateCheckBoxState currentState = triStateCheckbox.getSelectionState();
			
			switch(currentState) {
			case UNCHECKED:
				mapGrid.selectAll();
				break;
				
			case PARTIALLY_CHECKED:
				mapGrid.selectAll();
				break;
				
			case CHECKED:
				mapGrid.clearSelection();
				break;
				
			default:
				break;
			}
			
			SwingUtilities.invokeLater( () -> 
				triStateCheckbox.setSelectionState(getSelectionStateForGrid(mapGrid)) );
		});
		
		mapGrid.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				triStateCheckbox.setSelectionState(getSelectionStateForGrid(mapGrid));
				triStateCheckbox.repaint();
			}
			
		});
		
		gridPanel.add(triStateCheckbox, BorderLayout.WEST);
	}
	
	private TristateCheckBoxState getSelectionStateForGrid(IPAMapGrid grid) {
		int totalCount = grid.getGrid().getCell().size();
		int selectedCount = grid.getSelectionModel().getSelectedItemsCount();
		
		return (selectedCount == 0 ? TristateCheckBoxState.UNCHECKED :
			(totalCount == selectedCount ? TristateCheckBoxState.CHECKED : TristateCheckBoxState.PARTIALLY_CHECKED));
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		var objFactory = new ObjectFactory();
		selectedCellGrid = objFactory.createGrid();
		selectedCellGrid.setName("Selected");
		selectedCellGrid.setCols(14);
		selectedCellGrid.setRows(2);
//		selectedMapContainer = new IPAMapGridContainer();
//		var gridTuple = selectedMapContainer.addGrid(selectedCellGrid);
//		selectedMap = gridTuple.getObj2();
//		selectedMap.addCellMouseListener(cellMouseListener);
		
		map = new IPAMapGridContainer();
		
		map.setSelectionEnabled(true);
		map.addCellSelectionListener(cellSelectionListener);
		infoPane = new IPAMapInfoPane();
		
		map.addCellMouseListener(cellMouseListener);
		
//		add(selectedMapContainer, BorderLayout.NORTH);
		add(new JScrollPane(map), BorderLayout.CENTER);
		add(infoPane, BorderLayout.SOUTH);
	}
	
	protected void loadGrids() {
		ipaGrids = new IPAGrids();
		ipaGrids.loadDefaultGridData();
		ipaGrids.generateMissingGrids();
		addGrids(ipaGrids);
	}
	
	protected void addGrids(IPAGrids ipaGrids) {
		for(var ipaGrid:ipaGrids.getInternal().getGrid()) {
			var tuple = map.addGrid(ipaGrid);
			gridMap.put(ipaGrid.getName(), tuple.getObj2());
			addCheckBox(tuple.getObj2());
		}		
	}
	
	public Grid getSelectedGrid() {
		return this.selectedCellGrid;
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
				if(selected.contains(c.getText()) || selected.contains(c.getText().replaceAll("\u25cc", ""))) {
					ipaGrid.getSelectionModel().addSelectionInterval(i, i);
				}
			}
		}
		updateSelectedMap();
		
		firePropertyChange("selected", List.of(), getSelected());
	}
	
	public void clearSelection() {
		var prevSelected = getSelected();
		
		selectedCellGrid.getCell().clear();
		updateSelectedMap();
		
		firePropertyChange("selected", prevSelected, getSelected());
	}
	
	public void invertSelection() {
		
	}
	
	/**
	 * Called when cell selected has changed.
	 */
	public void cellSelectionChanged(IPAMapGrid mapGrid, int cellIdx, boolean selected) {
		// override where necessary
	}
	
	private IPAMapCellSelectionListener cellSelectionListener = new IPAMapCellSelectionListener() {
		
		@Override
		public void cellSelectionChanged(IPAMapGrid mapGrid, int cellIdx, boolean selected) {
			updateSelectedMap();
			IPAMapSelector.this.cellSelectionChanged(mapGrid, cellIdx, selected);
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
	}
	
}
