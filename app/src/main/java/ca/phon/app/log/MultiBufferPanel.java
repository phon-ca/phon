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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
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
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Container for multiple {@link BufferPanels}
 */
public class MultiBufferPanel extends JPanel implements BufferPanelContainer {

	private static final long serialVersionUID = 8474621417321331439L;

	private JComponent bufferPanel;
	private CardLayout cardLayout;

	private JToolBar toolbar;

	private JComboBox<String> bufferNameBox;

	private JButton saveBufferButton;

	private JButton exportAsWorkbookButton;

	private JCheckBox openFileAfterSavingBox;
	public final static String OPEN_AFTER_SAVING_PROP = MultiBufferPanel.class.getName() + ".openFileAfterSaving";
	private boolean openFileAfterSaving =
			PrefHelper.getBoolean(OPEN_AFTER_SAVING_PROP, Boolean.TRUE);

	private BufferPanel currentBufferPanel = null;
	
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

		cardLayout = new CardLayout();
		bufferPanel = new JPanel(cardLayout);
		bufferPanel.add(createNoSelectionPanel(), "no_selection");

		add(bufferPanel, BorderLayout.CENTER);
	}

	public boolean isOpenAfterSaving() {
		return this.openFileAfterSaving;
	}

	public JToolBar getToolbar() {
		return this.toolbar;
	}

	public void setupMenu(JMenu menu) {
		menu.removeAll();
		final MenuBuilder builder = new MenuBuilder(menu);
	
		final boolean hasBuffers = getBufferNames().size() > 0;
	
		// save actions
		final SaveBufferAction saveAct = new SaveBufferAction(this);
		JMenuItem saveItem = builder.addItem(".", saveAct);
		saveItem.setEnabled(hasBuffers);
		
		final SaveAllBuffersAction saveAllAct = new SaveAllBuffersAction(this);
		builder.addItem(".", saveAllAct).setEnabled(hasBuffers);
		
		builder.addSeparator(".", "_saveActions");
		
		// close actions
		final CloseCurrentBufferAction closeAct = new CloseCurrentBufferAction(this);
		builder.addItem(".", closeAct).setEnabled(hasBuffers);
		
		final CloseAllBuffersAction closeAllAct = new CloseAllBuffersAction(this);
		builder.addItem(".", closeAllAct).setEnabled(hasBuffers);
		
		builder.addSeparator(".", "_closeActions");
		
		final JMenu bufferMenu = builder.addMenu(".", "Show buffer");
		for(String bufferName:getBufferNames()) {
			final PhonUIAction showBufferAct = new PhonUIAction(this, "selectBuffer", bufferName);
			showBufferAct.putValue(PhonUIAction.NAME, bufferName);
			showBufferAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show buffer " + bufferName);
			showBufferAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer() != null && getCurrentBuffer().getName().equals(bufferName));
			bufferMenu.add(new JCheckBoxMenuItem(showBufferAct));				
		}
		builder.addSeparator(".", "_buffers");
		
		if(getCurrentBuffer() != null) {
			// view actions
			final PhonUIAction viewAsTextAct = new PhonUIAction(getCurrentBuffer(), "showBuffer");
			viewAsTextAct.putValue(PhonUIAction.NAME, "View as text");
			viewAsTextAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View current buffer data as text");
			viewAsTextAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingBuffer());
			builder.addItem(".", new JCheckBoxMenuItem(viewAsTextAct));
		
			final PhonUIAction viewAsTableAct = new PhonUIAction(getCurrentBuffer(), "showTable");
			viewAsTableAct.putValue(PhonUIAction.NAME, "View as table");
			viewAsTableAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View current buffer data as table");
			viewAsTableAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingTable());
			builder.addItem(".", new JCheckBoxMenuItem(viewAsTableAct));
			
			final PhonUIAction viewAsHtmlAct = new PhonUIAction(getCurrentBuffer(), "showHtml");
			viewAsHtmlAct.putValue(PhonUIAction.NAME, "View as HTML");
			viewAsHtmlAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View current buffer data as HTML");
			viewAsHtmlAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingHtml());
			builder.addItem(".", new JCheckBoxMenuItem(viewAsHtmlAct));
			
			builder.addSeparator(".", "_viewActions");
		}
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
		return createBuffer(bufferName, true);
	}
	
	public BufferPanel createBuffer(String bufferName, boolean select) {
		int idx = 0;
		final String rootName = bufferName;
		while(bufferPanelMap.containsKey(bufferName)) {
			bufferName = rootName + " (" + (++idx) + ")";
		}
		final BufferPanel bp = new BufferPanel(bufferName);
//		bp.setBorder(BorderFactory.createTitledBorder("Buffer: " + bufferName));
		bp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		bufferPanelMap.put(bufferName, bp);
		bufferPanel.add(bp, bufferName);

		fireBufferAdded(bufferName);

		if(select)
			selectBuffer(bufferName);

		return bp;
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
			currentBufferPanel = bufferPanelMap.get(bufferName);
			
			cardLayout.show(bufferPanel, bufferName);
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
		return currentBufferPanel;
	}

	@Override
	public void closeAllBuffers() {
		for(String name:bufferPanelMap.keySet()) {
			final BufferPanel bp = bufferPanelMap.get(name);
			cardLayout.removeLayoutComponent(bp);
		}
		bufferPanelMap.clear();
	}

	/*
	 * Helper methods
	 *
	 * These methods are used by the html report viewer
	 */
	public void saveAsCSV(String bufferName) {
		final SaveBufferAction act = new SaveBufferAction(this, bufferName);
		SwingUtilities.invokeLater( () -> act.actionPerformed(new ActionEvent(this, -1, "save")) );
	}

	public void saveAsWorkbook(String bufferName) {
		final SaveBufferAsWorkbookAction act = new SaveBufferAsWorkbookAction(this, bufferName);
		SwingUtilities.invokeLater( () -> act.actionPerformed(new ActionEvent(this, -1, "save")) );
	}

	private JToolBar setupToolbar() {
		final JToolBar retVal = new JToolBar();
		retVal.setFloatable(false);

		final SaveBufferAction saveAct = new SaveBufferAction(this);
		saveBufferButton = new JButton(saveAct);
		retVal.add(saveBufferButton);

		final SaveBufferAsWorkbookAction exportAsWorkbookAct = new SaveBufferAsWorkbookAction(this);
		exportAsWorkbookButton = new JButton(exportAsWorkbookAct);
		retVal.add(exportAsWorkbookButton);

		openFileAfterSavingBox = new JCheckBox("Open after saving");
		openFileAfterSavingBox.setSelected(openFileAfterSaving);
		openFileAfterSavingBox.addChangeListener( e -> {
			MultiBufferPanel.this.openFileAfterSaving = openFileAfterSavingBox.isSelected();
			PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_SAVING_PROP, MultiBufferPanel.this.openFileAfterSaving);
		});
		retVal.add(openFileAfterSavingBox);
		retVal.addSeparator();

		retVal.add(new JLabel("Buffer:"));
		bufferNameBox = new JComboBox<>(new BufferBoxModel());
		bufferNameBox.addItemListener( (e) -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				selectBuffer(bufferNameBox.getSelectedItem().toString());
			}
		});
		retVal.add(bufferNameBox);

		return retVal;
	}
	
	private class BufferBoxModel extends DefaultComboBoxModel<String> {

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
