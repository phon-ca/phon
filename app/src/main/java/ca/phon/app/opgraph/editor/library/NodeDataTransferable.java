/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.editor.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import ca.phon.opgraph.library.NodeData;

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
