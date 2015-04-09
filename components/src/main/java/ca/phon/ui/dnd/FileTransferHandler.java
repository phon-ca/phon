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
