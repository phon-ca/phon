package ca.phon.app.opgraph.editor.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import ca.gedge.opgraph.library.NodeData;

public class NodeDataTransferable implements Transferable {

	public static DataFlavor NODE_DATA_FLAVOR = 
			new DataFlavor(NodeData.class, "Node Data");
	
	private final NodeData data;
	
	public NodeDataTransferable(NodeData data) {
		super();
		this.data = data;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { NODE_DATA_FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getRepresentationClass() == NodeData.class;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if(flavor.getRepresentationClass() == NodeData.class) {
			return data;
		} else 
			throw new UnsupportedFlavorException(flavor);
	}

}
