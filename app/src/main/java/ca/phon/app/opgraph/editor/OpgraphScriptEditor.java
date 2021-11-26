package ca.phon.app.opgraph.editor;

import ca.phon.app.opgraph.nodes.*;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.components.canvas.GraphCanvasSelectionListener;
import ca.phon.query.db.Script;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class OpgraphScriptEditor extends JPanel {

	private JComboBox<ScriptNode> scriptNodeBox;

	private ScriptNodeEditor scriptNodeEditor;

	private final GraphDocument graphDocument;

	private final Map<ScriptNode, String> unsavedChanges = new LinkedHashMap<>();

	public OpgraphScriptEditor(GraphDocument graphDocument) {
		super();

		this.graphDocument = graphDocument;
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		scriptNodeEditor = new ScriptNodeEditor();

		add(scriptNodeEditor, BorderLayout.CENTER);

		final ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					if(scriptNodeBox.getSelectedItem() != null && scriptNodeBox.getSelectedItem() != scriptNodeEditor.getScriptNode()) {
						graphDocument.getSelectionModel().setSelectedNode(((ScriptNode)scriptNodeBox.getSelectedItem()).toOpNode());
					}
				}
			}
		};

		scriptNodeBox = new JComboBox<>();
		scriptNodeBox.setRenderer(new ScriptNodeCellRenderer());
		scriptNodeBox.addItemListener(itemListener);

		add(scriptNodeBox, BorderLayout.NORTH);

		this.graphDocument.getSelectionModel().addSelectionListener(new GraphCanvasSelectionListener() {

			@Override
			public void nodeSelectionChanged(Collection<OpNode> oldSelection, Collection<OpNode> newSelection) {
				if(scriptNodeEditor.getScriptNode() != null) {
					if(scriptNodeEditor.hasChanges()
						&& graphDocument.getSelectionModel().getSelectedNode() != scriptNodeEditor.getScriptNode().toOpNode()) {
						unsavedChanges.put(scriptNodeEditor.getScriptNode(), scriptNodeEditor.getText());
					} else {
						unsavedChanges.remove(scriptNodeEditor.getScriptNode());
					}
				}
				scriptNodeBox.removeItemListener(itemListener);
				scriptNodeBox.removeAllItems();
				if(newSelection.size() == 1) {
					OpNode selectedNode = newSelection.iterator().next();
					if(selectedNode instanceof ScriptNode && (scriptNodeEditor.getScriptNode() == null || selectedNode != scriptNodeEditor.getScriptNode().toOpNode())) {
						scriptNodeBox.addItem((ScriptNode)selectedNode);
						scriptNodeEditor.setScriptNode((ScriptNode) selectedNode);
						if(unsavedChanges.get(scriptNodeEditor.getScriptNode()) != null) {
							scriptNodeEditor.setText(unsavedChanges.get(scriptNodeEditor.getScriptNode()));
							unsavedChanges.remove(scriptNodeEditor.getScriptNode());
						}
					} else {
						scriptNodeEditor.setScriptNode(null);
					}
				} else {
					scriptNodeEditor.setScriptNode(null);
				}

				unsavedChanges.keySet().forEach( (sn) -> {
					if(sn == scriptNodeEditor.getScriptNode()) return;
					scriptNodeBox.addItem(sn);
				});

				scriptNodeBox.setSelectedItem(scriptNodeEditor.getScriptNode());
				scriptNodeBox.addItemListener(itemListener);
			}

		});
	}


	private class ScriptNodeCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if(value != null) {
				ScriptNode scriptNode = (ScriptNode) value;
				String text = scriptNode.toOpNode().getName() + " (" + scriptNode.toOpNode().getId() + ")";
				text += (index == 0 && scriptNodeEditor.hasChanges() || index > 1 ? " *" : "");
				retVal.setText(text);
			}

			return retVal;
		}

	}

}
