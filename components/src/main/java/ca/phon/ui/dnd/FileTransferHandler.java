/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
