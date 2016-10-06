package ca.phon.app.opgraph.wizard;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.JTree;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;

public class OpGraphCheckBoxTree extends TristateCheckBoxTree {

	public static TristateCheckBoxTreeModel createModel(OpGraph graph, boolean propogateState) {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode(graph);
		root.setEnablePartialCheck(false);
		root.setPropogateState(propogateState);
		setupTree(root, graph, propogateState);
		
		return new TristateCheckBoxTreeModel(root);
	}
	
	private static void setupTree(TristateCheckBoxTreeNode parent, OpGraph graph, boolean propogateState) {
		final Iterator<OpNode> nodeItr = graph.iterator();
		while(nodeItr.hasNext()) {
			final OpNode node = nodeItr.next();
			final TristateCheckBoxTreeNode treeNode = new TristateCheckBoxTreeNode(node);
			treeNode.setEnablePartialCheck(false);
			treeNode.setPropogateState(propogateState);
			parent.add(treeNode);
			
			if(node instanceof MacroNode) {
				final MacroNode macroNode = (MacroNode)node;
				setupTree(treeNode, macroNode.getGraph(), propogateState);
			}
		}
	}
	
	public OpGraphCheckBoxTree(OpGraph graph) {
		this(graph, true);
	}
	
	public OpGraphCheckBoxTree(OpGraph graph, boolean propogateState) {
		super(createModel(graph, propogateState));
		
		setCellRenderer(new OpGraphTreeRenderer());
		setCellEditor(new OpGraphTreeEditor(this));
	}
	
	private class OpGraphTreeRenderer extends TristateCheckBoxTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component retVal = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			final String text = 
					(value == getRoot() ? "root" : ((OpNode)((TristateCheckBoxTreeNode)value).getUserObject()).getName());
			if(retVal instanceof TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel) {
				((TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)retVal).getLabel().setText(text);
			}
			
			return retVal;
		}
		
	}
	
	private class OpGraphTreeEditor extends TristateCheckBoxTreeCellEditor {

		public OpGraphTreeEditor(JTree tree) {
			super(tree);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			Component retVal = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
			
			final String text = 
					(value == getRoot() ? "root" : ((OpNode)((TristateCheckBoxTreeNode)value).getUserObject()).getName());
			if(retVal instanceof TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel) {
				((TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)retVal).getLabel().setText(text);
			}
			
			return retVal;
		}
		
	}
	
}
