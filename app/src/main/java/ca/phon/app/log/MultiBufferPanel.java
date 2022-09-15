/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.log;

import ca.phon.app.log.actions.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;

/**
 * Container for multiple {@link BufferPanel}s
 */
public class MultiBufferPanel extends JPanel implements BufferPanelContainer {

	private static final long serialVersionUID = 8474621417321331439L;

	private JComponent bufferPanel;
	private CardLayout cardLayout;

	protected JToolBar toolbar;
	protected JButton saveBufferButton;
	protected JButton exportAsWorkbookButton;
	protected JCheckBox openFileAfterSavingBox;
	protected JComboBox<String> bufferNameBox;

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

		toolbar = new JToolBar();
		setupToolbar(toolbar);
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
		
		if(getCurrentBuffer() != null && getCurrentBuffer().isShowingHtml()) {
			final PrintHTMLBufferAction printAct = new PrintHTMLBufferAction(getCurrentBuffer());
			builder.addItem(".", printAct);
			builder.addSeparator(".", "_printActions");
		}
		
		// close actions
		final CloseCurrentBufferAction closeAct = new CloseCurrentBufferAction(this);
		builder.addItem(".", closeAct).setEnabled(hasBuffers);
		
		final CloseAllBuffersAction closeAllAct = new CloseAllBuffersAction(this);
		builder.addItem(".", closeAllAct).setEnabled(hasBuffers);
		
		builder.addSeparator(".", "_closeActions");
		
		final JMenu bufferMenu = builder.addMenu(".", "Show buffer");
		for(String bufferName:getBufferNames()) {
			final PhonUIAction<String> showBufferAct = PhonUIAction.consumer(this::selectBuffer, bufferName);
			showBufferAct.putValue(PhonUIAction.NAME, bufferName);
			showBufferAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show buffer " + bufferName);
			showBufferAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer() != null && getCurrentBuffer().getName().equals(bufferName));
			bufferMenu.add(new JCheckBoxMenuItem(showBufferAct));				
		}
		builder.addSeparator(".", "_buffers");
		
		if(getCurrentBuffer() != null) {
			// view actions
			final PhonUIAction<Void> viewAsTextAct = PhonUIAction.runnable(getCurrentBuffer()::showBuffer);
			viewAsTextAct.putValue(PhonUIAction.NAME, "View as text");
			viewAsTextAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View current buffer data as text");
			viewAsTextAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingBuffer());
			builder.addItem(".", new JCheckBoxMenuItem(viewAsTextAct));
		
			final PhonUIAction<Void> viewAsTableAct = PhonUIAction.runnable(getCurrentBuffer()::showTable);
			viewAsTableAct.putValue(PhonUIAction.NAME, "View as table");
			viewAsTableAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View current buffer data as table");
			viewAsTableAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingTable());
			builder.addItem(".", new JCheckBoxMenuItem(viewAsTableAct));
			
			final PhonUIAction<Void> viewAsHtmlAct = PhonUIAction.runnable(getCurrentBuffer()::showHtml);
			viewAsHtmlAct.putValue(PhonUIAction.NAME, "View as HTML");
			viewAsHtmlAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "View current buffer data as HTML");
			viewAsHtmlAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingHtml());
			builder.addItem(".", new JCheckBoxMenuItem(viewAsHtmlAct));
			
			if(getCurrentBuffer().isShowingHtml() && PrefHelper.getBoolean("phon.debug", false)) {
				final PhonUIAction<Void> toggleDebugAct = PhonUIAction.runnable(
						(getCurrentBuffer().isShowingHtmlDebug() ? getCurrentBuffer()::hideHtmlDebug : getCurrentBuffer()::showHtmlDebug));
				toggleDebugAct.putValue(PhonUIAction.NAME, "Debug");
				toggleDebugAct.putValue(PhonUIAction.SELECTED_KEY, getCurrentBuffer().isShowingHtmlDebug());
				builder.addItem(".", new JCheckBoxMenuItem(toggleDebugAct));
			}
			
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
		bp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		bufferPanelMap.put(bufferName, bp);
		bufferPanel.add(bp, bufferName);
		
		bp.addPropertyChangeListener(BufferPanel.SHOWING_BUFFER_PROP, 
				(e) -> setupToolbar(toolbar) );

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
			setupToolbar(toolbar);
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

	/**
	 * Setup toolbar for current buffer.  This method is called whenever the buffer is changed
	 * or the buffer view type has changed.
	 * 
	 * @param toolbar
	 */
	protected void setupToolbar(JToolBar toolbar) {
		toolbar.setFloatable(false);
		toolbar.removeAll();
		
		final SaveBufferAction saveAct = new SaveBufferAction(this);
		if(saveBufferButton == null) {
			saveBufferButton = new JButton(saveAct);
		}
		saveBufferButton.setEnabled(currentBufferPanel != null);
		toolbar.add(saveBufferButton);
		
		final SaveBufferAsWorkbookAction exportAsWorkbookAct = new SaveBufferAsWorkbookAction(this);
		if(exportAsWorkbookButton == null) {
			exportAsWorkbookButton = new JButton(exportAsWorkbookAct);
		}
		boolean canExport = 
				( currentBufferPanel != null && currentBufferPanel.isShowingTable() )
				|| ( currentBufferPanel != null && currentBufferPanel.getExtension(ExcelExporter.class) != null );
		
		exportAsWorkbookButton.setEnabled( canExport );
		toolbar.add(exportAsWorkbookButton);
		
		if(openFileAfterSavingBox == null) {
			openFileAfterSavingBox = new JCheckBox("Open after saving");
			openFileAfterSavingBox.setSelected(openFileAfterSaving);
			openFileAfterSavingBox.addChangeListener( e -> {
				MultiBufferPanel.this.openFileAfterSaving = openFileAfterSavingBox.isSelected();
				PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_SAVING_PROP, MultiBufferPanel.this.openFileAfterSaving);
			});
		}
		toolbar.add(openFileAfterSavingBox);
		toolbar.addSeparator();

		toolbar.add(new JLabel("Buffer:"));
		if(bufferNameBox == null) {
			bufferNameBox = new JComboBox<>(new BufferBoxModel());
			bufferNameBox.addItemListener( (e) -> {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					selectBuffer(bufferNameBox.getSelectedItem().toString());
				}
			});
		}
		toolbar.add(bufferNameBox);
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
