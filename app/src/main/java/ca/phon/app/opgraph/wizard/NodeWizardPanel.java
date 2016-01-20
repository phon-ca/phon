package ca.phon.app.opgraph.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.phon.app.opgraph.wizard.actions.AddNodeAction;
import ca.phon.app.opgraph.wizard.actions.EditSettingsAction;
import ca.phon.app.opgraph.wizard.actions.MoveNodeAction;
import ca.phon.app.opgraph.wizard.actions.RemoveNodeAction;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Provides a UI for selecting and ordering nodes in the wizard.
 *
 */
public class NodeWizardPanel extends JPanel {
	
	private WizardExtension wizardExtension;
	
	private GraphDocument document;
	
	private JTable nodeTable;
	private NodeWizardTableModel nodeTableModel;
	
	private JButton addToWizardButton;
	
	private JButton removeFromWizardButton;
	
	private JButton settingsButton;
	
	private JButton moveUpButton;
	
	private JButton moveDownButton;
	
	public NodeWizardPanel(GraphDocument doc, WizardExtension extension) {
		super();
		this.wizardExtension = extension;
		this.document = doc;
	
		init();
	}
	
	private void init() {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		setLayout(layout);
		
		nodeTableModel = new NodeWizardTableModel();
		nodeTable = new JTable(nodeTableModel);
		final JScrollPane scroller = new JScrollPane(nodeTable);
		
		settingsButton = new JButton(new EditSettingsAction(this));
		settingsButton.setText("");
		addToWizardButton = new JButton(new AddNodeAction(this));
		addToWizardButton.setText("");
		removeFromWizardButton = new JButton(new RemoveNodeAction(this));
		removeFromWizardButton.setText("");
		
		moveUpButton = new JButton(new MoveNodeAction(this, MoveNodeAction.UP));
		moveUpButton.setText("");
		moveDownButton = new JButton(new MoveNodeAction(this, MoveNodeAction.DOWN));
		moveDownButton.setText("");
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		add(settingsButton, gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		add(addToWizardButton, gbc);
		
		gbc.gridx = 2;
		add(removeFromWizardButton, gbc);
		
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(moveUpButton, gbc);
		
		gbc.gridy = 2;
		add(moveDownButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 3;
		gbc.gridwidth = 3;
		add(scroller, gbc);
	}
	
	public GraphDocument getDocument() {
		return this.document;
	}
	
	public WizardExtension getWizardExtension() {
		return this.wizardExtension;
	}
	
	/**
	 * Returns the list of selected nodes in the table
	 * <em>not</em> the graph document.
	 * 
	 * @return list of selected nodes in table
	 */
	public List<OpNode> getSelectedNodes() {
		List<OpNode> retVal = new ArrayList<>();
		
		for(int selectedRow:nodeTable.getSelectedRows()) {
			retVal.add(wizardExtension.getNode(selectedRow));
		}
		
		return retVal;
	}
	
	public void updateTable() {
		nodeTableModel.fireTableDataChanged();
	}
	
	private class NodeWizardTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return wizardExtension.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final OpNode node = wizardExtension.getNode(rowIndex);
			
			if(columnIndex == 0) {
				return (wizardExtension.getNodeTitle(node) != null ? 
						wizardExtension.getNodeTitle(node) : node.getName());
			} else if(columnIndex == 1) {
				final OpGraph graph = wizardExtension.getGraph();
				final List<OpNode> nodePath = graph.getNodePath(node.getId());
				final String path = 
						nodePath.stream()
							.map( n -> n.getName() )
							.collect( Collectors.joining("/") );
				return path;
			}
			
			return "";
		}

		@Override
		public String getColumnName(int column) {
			String retVal = super.getColumnName(column);
			
			if(column == 0) {
				retVal = "Step Title";
			} else if (column == 1) {
				retVal = "Node";
			}
			
			return retVal;
		}
		
	}
	
}
