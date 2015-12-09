package ca.phon.app.opgraph.editor.library;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.ListView;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.app.components.canvas.GraphCanvas;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.NodeLibrary;
import ca.phon.ui.text.SearchField;

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
	
	public LibraryView(NodeLibrary library) {
		super();
		
		this.nodeLibrary = library;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		searchField = new SearchField("Filter nodes");
		add(searchField, BorderLayout.NORTH);
		
		libraryPanel = new JPanel(new VerticalLayout());
		
		final Map<String, List<NodeData>> groupMap = nodeLibrary.getCategoryMap();
		for(String group:groupMap.keySet()) {
			final JXCollapsiblePane groupPane = new JXCollapsiblePane(Direction.UP);
			groupPane.setLayout(new BorderLayout());
			
			final NodeGroupListModel listModel = new NodeGroupListModel(groupMap.get(group));
			final JList<NodeData> nodeList = new JList<>(listModel);
			nodeList.setCellRenderer(new NodeDataCellRenderer());
			groupPane.add(nodeList, BorderLayout.CENTER);
			
			// TODO create label
			
			libraryPanel.add(groupPane);
		}
		
		final JScrollPane libraryScroller = new JScrollPane(libraryPanel);
		add(libraryScroller, BorderLayout.CENTER);
	}

}
