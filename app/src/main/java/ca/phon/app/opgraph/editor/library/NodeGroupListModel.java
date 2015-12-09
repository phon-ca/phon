package ca.phon.app.opgraph.editor.library;

import java.util.List;

import javax.swing.DefaultListModel;

import ca.gedge.opgraph.library.NodeData;

/**
 *
 */
public class NodeGroupListModel extends DefaultListModel<NodeData> {

	private static final long serialVersionUID = 7555354396826758697L;

	public NodeGroupListModel() {
		super();
	}
	
	public NodeGroupListModel(List<NodeData> data) {
		super();
		
		data.forEach( (e) -> super.addElement(e) );
	}

}
