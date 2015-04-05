package ca.phon.app.session.editor.view.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

public class MediaFileDropListener implements DropTargetListener {

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		for(DataFlavor flavor:dtde.getTransferable().getTransferDataFlavors()) {
			try {
				System.out.println(flavor.toString());
				System.out.println(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
			} catch (UnsupportedFlavorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		for(DataFlavor flavor:dtde.getTransferable().getTransferDataFlavors()) {
			try {
				System.out.println(flavor.toString());
				System.out.println(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
			} catch (UnsupportedFlavorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
