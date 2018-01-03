/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.log;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

import ca.phon.app.log.actions.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Container for multiple {@link BufferPanels} 
 */
public class MultiBufferPanel extends JPanel implements BufferPanelContainer {

	private static final long serialVersionUID = 8474621417321331439L;
	
	private BufferListModel bufferTableModel;
	private JTable bufferList;
	
	private JComponent bufferPanel;
	private CardLayout cardLayout;

	private JXCollapsiblePane listPane;
	
	private JToolBar toolbar;
	
	private JComboBox<String> bufferNameBox;
	private JButton toggleListButton;
	
	private JButton saveBufferButton;
	
	private JCheckBox openFileAfterSavingBox;
	public final static String OPEN_AFTER_SAVING_PROP = MultiBufferPanel.class.getName() + ".openFileAfterSaving";
	private boolean openFileAfterSaving =
			PrefHelper.getBoolean(OPEN_AFTER_SAVING_PROP, Boolean.TRUE);
	
	private LinkedHashMap<String, BufferPanel> bufferPanelMap = new LinkedHashMap<>();
	
	private EventListenerList listenerList = new EventListenerList();
	
	public MultiBufferPanel() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		toolbar = setupToolbar();
		add(toolbar, BorderLayout.NORTH);
		
		bufferList = setupTable();
		final JScrollPane scroller = new JScrollPane(bufferList);
		
		cardLayout = new CardLayout();
		bufferPanel = new JPanel(cardLayout);
		bufferPanel.add(createNoSelectionPanel(), "no_selection");
		
		listPane = new JXCollapsiblePane(Direction.LEFT);
		listPane.getContentPane().setLayout(new BorderLayout());
		listPane.getContentPane().add(scroller, BorderLayout.CENTER);
		listPane.setCollapsed(true);
		
		add(listPane, BorderLayout.WEST);
		add(bufferPanel, BorderLayout.CENTER);
	}
	
	public boolean isOpenAfterSaving() {
		return this.openFileAfterSaving;
	}
	
	public boolean isListShowing() {
		return !this.listPane.isCollapsed();
	}
	
	public void toggleList() {
		this.listPane.setCollapsed(!this.listPane.isCollapsed());
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}
	
	public JTable getBufferTable() {
		return this.bufferList;
	}
	
	private JPanel createNoSelectionPanel() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		final JLabel lbl = new JLabel("No buffer selected");
		lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
		panel.add(lbl, gbc);
		
		panel.setBorder(BorderFactory.createTitledBorder(""));
		
		return panel;
	}
	
	public BufferPanel createBuffer(String bufferName) {
		int idx = 0;
		final String rootName = bufferName;
		while(bufferPanelMap.containsKey(bufferName)) {
			bufferName = rootName + " (" + (++idx) + ")";
		}
		final BufferPanel bp = new BufferPanel(bufferName);
//		bp.setBorder(BorderFactory.createTitledBorder("Buffer: " + bufferName));
		bp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		bufferPanelMap.put(bufferName, bp);
		bufferTableModel.fireBufferAdded();
		bufferPanel.add(bp, bufferName);
		
		fireBufferAdded(bufferName);
		
		selectBuffer(bufferName);
		
		return bp;
	}
	
	public void onRemoveSelectedBuffers() {
		int selectedIdx[] = bufferList.getSelectedRows();
		List<String> selectedNames = new ArrayList<>();
		for(int idx:selectedIdx) {
			final String selectedName = bufferList.getModel().getValueAt(idx, 0).toString();
			selectedNames.add(selectedName);
		}
		
		for(int i = 0; i < selectedNames.size(); i++) {
			final String name = selectedNames.get(i);
			removeBuffer(name);
			bufferTableModel.fireBufferRemoved(selectedIdx[i] - i);
		}
	}
	
	public void removeBuffer(String bufferName) {
		final BufferPanel bp = bufferPanelMap.get(bufferName);
		if(bp != null) {
			bufferPanel.remove(bp);
			bufferPanelMap.remove(bufferName);
			
			fireBufferRemoved(bufferName);
		}
	}
	
	public BufferPanel getBuffer(String name) {
		return bufferPanelMap.get(name);
	}
	
	public void selectBuffer(String bufferName) {
		final List<String> list = new ArrayList<>(bufferPanelMap.keySet());
		int idx = list.indexOf(bufferName);
		if(idx >= 0) {
			bufferList.setRowSelectionInterval(idx, idx);
			
			if(bufferNameBox.getSelectedItem() != bufferName) {
				bufferNameBox.setSelectedItem(bufferName);
				
				fireBufferSelected(bufferName);
			}
		}
	}
	
	public Collection<String> getBufferNames() {
		return Collections.unmodifiableSet(bufferPanelMap.keySet());
	}
	
	public BufferPanel getCurrentBuffer() {
		int selectedRow = bufferList.getSelectedRow();
		if(selectedRow >= 0) {
			final String selection = bufferList.getModel().getValueAt(selectedRow, 0).toString();
			return bufferPanelMap.get(selection);
		} else {
			return null;
		}
	}
	
	@Override
	public void closeAllBuffers() {
		for(String name:bufferPanelMap.keySet()) {
			final BufferPanel bp = bufferPanelMap.get(name);
			cardLayout.removeLayoutComponent(bp);
		}
		bufferPanelMap.clear();
		
		bufferList.revalidate();
	}
	
	private JToolBar setupToolbar() {
		final JToolBar retVal = new JToolBar();
		retVal.setFloatable(false);
		
		final SaveCurrentBufferAction saveAct = new SaveCurrentBufferAction(this);
		saveBufferButton = new JButton(saveAct);
		retVal.add(saveBufferButton);
				
		openFileAfterSavingBox = new JCheckBox("Open after saving");
		openFileAfterSavingBox.setSelected(openFileAfterSaving);
		openFileAfterSavingBox.addChangeListener( e -> {
			MultiBufferPanel.this.openFileAfterSaving = openFileAfterSavingBox.isSelected();
			PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_SAVING_PROP, MultiBufferPanel.this.openFileAfterSaving);
		});
		retVal.add(openFileAfterSavingBox);
		retVal.addSeparator();
		
		final ImageIcon toggleIcn = 
				IconManager.getInstance().getIcon("actions/view-list-text", IconSize.SMALL);
		final PhonUIAction toggleListAct = new PhonUIAction(this, "toggleList");
		toggleListAct.putValue(PhonUIAction.NAME, "");
		toggleListAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle buffer list");
		toggleListAct.putValue(PhonUIAction.SMALL_ICON, toggleIcn);
		toggleListButton = new JButton(toggleListAct);
		
		retVal.add(toggleListButton);
		
		retVal.add(new JLabel("Buffer:"));
		bufferNameBox = new JComboBox<>(new BufferBoxModel());
		retVal.add(bufferNameBox);
		
		return retVal;
	}
	
	private JTable setupTable() {
		bufferTableModel = new BufferListModel();
		final JTable retVal = new JTable(bufferTableModel);
		
		retVal.getSelectionModel().addListSelectionListener( (l) -> {
			final int selectedRow = bufferList.getSelectedRow();
			if(selectedRow >= 0 && selectedRow < bufferList.getRowCount()) {
				final String bufferName = bufferList.getModel().getValueAt(selectedRow, 0).toString();
				cardLayout.show(bufferPanel, bufferName);
			} else {
				cardLayout.show(bufferPanel, "no_selection");
			}
			SwingUtilities.invokeLater( () -> {
				retVal.scrollRectToVisible(retVal.getCellRect(selectedRow, 0, true));
				bufferNameBox.repaint();
			} );
		});
		
		return retVal;
	}
	
	public void setupSaveAsMenu(JMenu menu) {
//		final SaveCurrentBufferAction saveBufferAct = new SaveCurrentBufferAction(this);
//		menu.add(new JMenuItem(saveBufferAct));
		
		final SaveBufferAsWorkbookAction saveAsWorkbookAct = new SaveBufferAsWorkbookAction(this);
		menu.add(new JMenuItem(saveAsWorkbookAct));
		
		final SaveAllBuffersAction saveAllBuffersAct = new SaveAllBuffersAction(this);
		menu.add(new JMenuItem(saveAllBuffersAct));
		
		final SaveTablesToWorkbookAction saveTablesAct = new SaveTablesToWorkbookAction(this);
		menu.add(new JMenuItem(saveTablesAct));
	}
	
	private class BufferBoxModel implements ComboBoxModel<String> {

		private EventListenerList listeners = new EventListenerList();
		
		@Override
		public int getSize() {
			return bufferPanelMap.size();
		}

		@Override
		public String getElementAt(int index) {
			final List<String> list = new ArrayList<>(bufferPanelMap.keySet());
			final String name = list.get(index);
			return name;
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(ListDataListener.class, l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(ListDataListener.class, l);
		}

		@Override
		public void setSelectedItem(Object anItem) {
			selectBuffer(anItem.toString());
		}

		@Override
		public Object getSelectedItem() {
			return (bufferList != null && getCurrentBuffer() != null ? getCurrentBuffer().getName() : null);
		}
		
	}
	
	private class BufferListModel extends AbstractTableModel {

		private static final long serialVersionUID = 5255792755244100880L;
	
		public void fireBufferAdded() {
			super.fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
		}
		
		public void fireBufferRemoved(int idx) {
			super.fireTableRowsDeleted(idx, idx);
		}

		@Override
		public int getRowCount() {
			return bufferPanelMap.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final List<String> list = new ArrayList<>(bufferPanelMap.keySet());
			final String name = list.get(rowIndex);
			
			Object retVal = new String();
			
			if(columnIndex == 0) {
				retVal = name;
			} else {
				final BufferPanel bp = bufferPanelMap.get(name);
				int size = bp.getLogBuffer().getText().getBytes().length;
				retVal = ByteSize.humanReadableByteCount(size, true);
			}
			
			return retVal;
		}

		@Override
		public String getColumnName(int column) {
			String retVal = new String();
			
			if(column == 0) {
				retVal = "Buffer Name";
			} else if(column == 1) {
				retVal = "Size";
			}
			
			return retVal;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			final List<String> list = new ArrayList<>(bufferPanelMap.keySet());
			final String name = list.get(rowIndex);
			
			final BufferPanel bp = bufferPanelMap.get(name);
			bp.setBufferName(aValue.toString());
			bp.setBorder(BorderFactory.createTitledBorder(bp.getBufferName()));
			
			bufferPanelMap.remove(name);
			bufferPanelMap.put(bp.getBufferName(), bp);
		}
		
	}

	@Override
	public void addListener(BufferPanelContainerListener listener) {
		listenerList.add(BufferPanelContainerListener.class, listener);
	}

	@Override
	public void removeListener(BufferPanelContainerListener listener) {
		listenerList.remove(BufferPanelContainerListener.class, listener);
	}

	@Override
	public List<BufferPanelContainerListener> getListeners() {
		return Arrays.asList(listenerList.getListeners(BufferPanelContainerListener.class));
	}
	
	public void fireBufferAdded(String bufferName) {
		for(BufferPanelContainerListener listener:getListeners()) {
			listener.bufferAdded(bufferName);
		}
	}
	
	public void fireBufferRemoved(String bufferName) {
		for(BufferPanelContainerListener listener:getListeners()) {
			listener.bufferRemoved(bufferName);
		}
	}

	@Override
	public void addSelectionListener(BufferPanelSelectionListener listener) {
		listenerList.add(BufferPanelSelectionListener.class, listener);
	}

	@Override
	public void removeSelectionListener(BufferPanelSelectionListener listener) {
		listenerList.remove(BufferPanelSelectionListener.class, listener);
	}

	@Override
	public List<BufferPanelSelectionListener> getSelectionListeners() {
		return Arrays.asList(listenerList.getListeners(BufferPanelSelectionListener.class));
	}
	
	public void fireBufferSelected(String bufferName) {
		for(BufferPanelSelectionListener listener:getSelectionListeners()) {
			listener.bufferSelected(bufferName);
		}
	}

}
