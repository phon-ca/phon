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
package ca.phon.app.opgraph.library;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXCollapsiblePane.*;

import ca.phon.opgraph.app.components.canvas.*;
import ca.phon.opgraph.library.*;
import ca.phon.ui.text.*;
import ca.phon.util.*;

/**
 * <p>Custom library view for Phon node editor.  The view consists
 * of two components: a search field to filter the view contents;
 * and a list displaying the available nodes.</p>
 * 
 * <p>Nodes are organized into named groups, each group is collapsible.
 * The collapsed state of each node group is saved between sessions.
 * </p>
 * 
 * <p>Users may drag elements from each node group into an active
 * {@link GraphCanvas}.</p>
 *
 */
public class LibraryView extends JPanel {
	
	private final static String PANE_STATUS_PREFIX = "nodeLibrary.";
	
	private final static  String PANE_STATUS_SUFFIX = ".collapsed";
	
	private final NodeLibrary nodeLibrary;
	
	private SearchField searchField;
	
	private JPanel libraryPanel;
	
	private List<JList<NodeData>> nodeDataLists = new ArrayList<>();
	
	public LibraryView(NodeLibrary library) {
		super();
		
		this.nodeLibrary = library;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		searchField = new SearchField("Filter nodes");
		add(searchField, BorderLayout.NORTH);
		searchField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilter();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilter();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				
			}
		});
		
		libraryPanel = new JPanel(new VerticalLayout());
		
		@SuppressWarnings("unchecked")
		final DragGestureListener dragListener = (DragGestureEvent dge) -> {
			final JList<NodeData> list = (JList<NodeData>)dge.getComponent();
			final NodeData selectedItem = list.getSelectedValue();
			if(selectedItem != null) {
				final String txt = selectedItem.name;

				final Font font = getFont().deriveFont(Font.BOLD);
				final FontRenderContext frc = new FontRenderContext(null, true, true);
				final Rectangle2D bounds = font.getStringBounds(txt, frc);
				final int txtw = (int)(bounds.getWidth() + 20);
				final int txth = (int)(bounds.getHeight() + 10);

				final BufferedImage DRAG_IMG = new BufferedImage(txtw, txth, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g = DRAG_IMG.createGraphics();
				{
					// Draw background
					g.setColor(new Color(255, 255, 150, 200));
					g.fillRect(0, 0, txtw - 1, txth - 1);

					// Draw border
					g.setColor(Color.BLACK);
					g.drawRect(0, 0, txtw - 1, txth - 1);

					// Draw text
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setFont(font);
					g.setColor(Color.BLACK);
					final LineMetrics lm = font.getLineMetrics(txt, frc);
					final float txtx = (txtw - (float)bounds.getWidth()) * 0.5f;
					final float txty = (txth - (float)bounds.getHeight()) * 0.5f + lm.getAscent();
					g.drawString(txt, txtx, txty);
					g.dispose();
				}

				final Point p = new Point(DRAG_IMG.getWidth() / -2, DRAG_IMG.getHeight() / -2);
				final NodeDataTransferable sel = new NodeDataTransferable(selectedItem);
				dge.getDragSource().startDrag(dge, DragSource.DefaultCopyDrop, DRAG_IMG, p, sel, null);
			}
		};
		
		final Map<String, List<NodeData>> groupMap = nodeLibrary.getCategoryMap();
		for(String group:groupMap.keySet()) {
			final JXCollapsiblePane groupPane = new JXCollapsiblePane(Direction.UP);
			groupPane.setLayout(new BorderLayout());
			
			final String collapsedPref = PANE_STATUS_PREFIX + group + PANE_STATUS_SUFFIX;
			groupPane.setCollapsed( PrefHelper.getBoolean(collapsedPref, Boolean.FALSE) );
			
			final NodeGroupListModel listModel = new NodeGroupListModel(groupMap.get(group));
			final JXList nodeList = new JXList(listModel) {
				@Override
				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
			};
			nodeList.setFixedCellWidth(200);
			
			nodeList.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					nodeList.setFixedCellHeight(10);
					nodeList.setFixedCellHeight(-1);
				}
			});
			
			nodeList.setCellRenderer(new NodeDataCellRenderer());
			nodeList.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					nodeDataLists.parallelStream()
						.filter( (l) -> l != e.getSource() )
						.forEach( (l) -> l.getSelectionModel().clearSelection() );
				}
				
			});
			
			DragSource.getDefaultDragSource()
	          .createDefaultDragGestureRecognizer(nodeList,
	        		  DnDConstants.ACTION_COPY,
	        		  dragListener);
			
			nodeDataLists.add(nodeList);
			groupPane.add(nodeList, BorderLayout.CENTER);
			
			final JButton toggleButton = createCollapsibleLabel(group, groupPane);
			
			libraryPanel.add(toggleButton);
			libraryPanel.add(groupPane);
		}
		
		final JScrollPane libraryScroller = new JScrollPane(libraryPanel);
		add(libraryScroller, BorderLayout.CENTER);
	}
	
	private void updateFilter() {
		final String filter = searchField.getText().trim();
		if(filter.length() == 0) {
			for(JList<NodeData> nodeList:nodeDataLists) {
				final NodeGroupListModel model = 
						(NodeGroupListModel)nodeList.getModel();
				model.setFilter(null);
			}
		} else {
			for(JList<NodeData> nodeList:nodeDataLists) {
				final NodeGroupListModel model = 
						(NodeGroupListModel)nodeList.getModel();
				model.setFilter(filter);
			}
		}
	}
	
	private JButton createCollapsibleLabel(final String title, final JXCollapsiblePane pane) {
		Action toggleAction = pane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
		                      UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
		                      UIManager.getIcon("Tree.collapsedIcon"));
		toggleAction.putValue(Action.NAME, title);
		
		final JButton retVal = new JButton(toggleAction) {
			@Override
			public Insets getInsets() {
				Insets retVal = super.getInsets();
				
				retVal.top = 0;
				retVal.bottom = 0;
				
				return retVal;
			}
	
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, 20);
			}
		};
		
		retVal.addActionListener( (e) -> {
			PrefHelper.getUserPreferences().putBoolean(PANE_STATUS_PREFIX + title + PANE_STATUS_SUFFIX,
					!pane.isCollapsed() );
		});
		retVal.setHorizontalAlignment(SwingConstants.LEFT);
//		retVal.setHorizontalTextPosition(SwingConstants.LEFT);
		
		return retVal;
	}

}
