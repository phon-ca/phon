package ca.phon.app.opgraph.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Provides a UI for selecting and ordering nodes in the wizard.
 *
 */
public class NodeWizardPanel extends JPanel {
	
	private WizardExtension wizardExtension;
	
	private GraphDocument document;
	
	private JTable nodeTable;
	private NodeWizardTableModel nodeTabelModel;
	
	private JButton addToWizardButton;
	
	private JButton removeFromWizardButton;
	
	private JButton moveUpButton;
	
	private JButton moveDownButton;
	
	public NodeWizardPanel(GraphDocument doc, WizardExtension extension) {
		super();
		this.wizardExtension = extension;
		this.document = doc;
	
		init();
	}
	
	private void init() {
		setLayout(new FormLayout("fill:pref:grow, pref", "pref, fill:pref:grow"));
		final CellConstraints cc = new CellConstraints();
		
		nodeTabelModel = new NodeWizardTableModel();
		nodeTable = new JTable(nodeTabelModel);
		final JScrollPane scroller = new JScrollPane(nodeTable);
		
		add(scroller, cc.xywh(1, 2, 2, 1));
		
		final ImageIcon addIcon = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "onAddToWizard");
		addAct.putValue(PhonUIAction.NAME, "Add to wizard");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add currently selected node(s) to wizard");
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcon);
		addToWizardButton = new JButton(addAct);
		
		
		final ImageIcon removeIcon = IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemoveFromWizard");
		removeAct.putValue(PhonUIAction.NAME, "Remove from wizard");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove currently selected nodes from wizard");
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcon);
		removeFromWizardButton = new JButton(removeAct);
		final JComponent addRemovePanel = 
				ButtonBarBuilder.buildOkCancelBar(removeFromWizardButton, addToWizardButton);
		add(addRemovePanel, cc.xy(1, 1));
	}
	
	public void onAddToWizard() {
		final OpNode node = document.getSelectionModel().getSelectedNode();
		if(node != null) {
			wizardExtension.addNode(node);
			nodeTabelModel.fireTableRowsInserted(nodeTabelModel.getRowCount()-1, nodeTabelModel.getRowCount()-1);
		}
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
				return node.getName();
			} else if(columnIndex == 1) {
				return node.getId();
			}
			
			return "";
		}

		@Override
		public String getColumnName(int column) {
			String retVal = super.getColumnName(column);
			
			if(column == 0) {
				retVal = "Node name";
			} else if (column == 1) {
				retVal = "ID";
			}
			
			return retVal;
		}
		
	}
	
}
