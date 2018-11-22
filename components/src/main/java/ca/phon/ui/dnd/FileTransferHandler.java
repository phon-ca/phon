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
package ca.phon.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class FileTransferHandler extends TransferHandler {

	private static final long serialVersionUID = -8840277257866571609L;
	
	public File getFile(Transferable transferable)
		throws IOException {
		List<File> files = getFiles(transferable);
		if(files != null && files.size() > 0)
			return files.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<File> getFiles(Transferable transferable)
		throws IOException {
		List<File> retVal = null;
		try {
			retVal = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException e) {
			throw new IOException(e);
		}
		return retVal;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		boolean retVal = false;
		for(DataFlavor f:transferFlavors) {
			retVal |= f.isFlavorJavaFileListType();
		}
		return retVal;
	}
	
}