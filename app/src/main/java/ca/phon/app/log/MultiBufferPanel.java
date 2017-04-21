/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.log.actions.SaveAllBuffersAction;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.ByteSize;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Container for multiple {@link BufferPanels} 
 */
public class MultiBufferPanel extends JPanel implements BufferPanelContainer {

	private static final long serialVersionUID = 8474621417321331439L;
	
	private BufferListModel bufferTableModel;
	private JTable bufferList;
	
	private JComponent bufferPanel;
	private CardLayout cardLayout;
	
	private JSplitPane splitPane;
	
	private JToolBar toolbar;
	
	private LinkedHashMap<String, BufferPanel> bufferPanelMap = new LinkedHashMap<>();
	
	public MultiBufferPanel() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		toolbar = setupToolbar();
		add(toolbar, BorderLayout.NORTH);
		
		final JButton saveAllButton = new JButton(new SaveAllBuffersAction(this));
		
		final ImageIcon closeIcon = IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction closeBuffersAct = new PhonUIAction(this, "onRemoveSelectedBuffers");
		closeBuffersAct.putValue(PhonUIAction.SMALL_ICON, closeIcon);
		closeBuffersAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Close selected buffers");
		closeBuffersAct.putValue(PhonUIAction.NAME, "Close");
		final JButton closeBtn = new JButton(closeBuffersAct);
		
		bufferList = setupTable();
		final JScrollPane scroller = new JScrollPane(bufferList);
		
		final FormLayout layout = new FormLayout(
				"left:pref, fill:200px:grow, right:pref, pref",
				"pref, pref, pref, fill:pref:grow");
		final CellConstraints cc = new CellConstraints();
		final JPanel leftPanel = new JPanel(layout);
		
		leftPanel.add(saveAllButton, cc.xy(1, 1));
		leftPanel.add(closeBtn, cc.xy(3, 1));
		leftPanel.add(scroller, cc.xywh(1, 2, 3, 3));
		
		cardLayout = new CardLayout();
		bufferPanel = new JPanel(cardLayout);
		bufferPanel.add(createNoSelectionPanel(), "no_selection");
		
		leftPanel.setBorder(BorderFactory.createTitledBorder("Buffer List"));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, bufferPanel);
		splitPane.setResizeWeight(0.0);
		add(splitPane, BorderLayout.CENTER);
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}
	
	public JSplitPane getSplitPane() {
		return this.splitPane;
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
		while(bufferPanelMap.containsKey(bufferName)) {
			bufferName = bufferName + " (" + (++idx) + ")";
		}
		final BufferPanel bp = new BufferPanel(bufferName);
		bp.setBorder(BorderFactory.createTitledBorder("Buffer: " + bufferName));
		bufferPanelMap.put(bufferName, bp);
		bufferTableModel.fireBufferAdded();
		bufferPanel.add(bp, bufferName);
		
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
			SwingUtilities.invokeLater( () -> retVal.scrollRectToVisible(retVal.getCellRect(selectedRow, 0, true)) );
		});
		
		return retVal;
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

}
