package ca.phon.ipamap;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Logger;

import ca.phon.util.OSInfo;

public class IpaMapRobot implements IpaMapListener {
	
	private static final Logger LOGGER = Logger.getLogger(IpaMapRobot.class
			.getName());
	
	private Robot robot;
	
	private IpaMap ipaMap;
	
	public IpaMapRobot(IpaMap ipaMap) {
		this.ipaMap = ipaMap;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	@Override
	public void ipaMapEvent(String txt) {
		// copy text to clipboard
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new IpaTransferrable(txt), ipaMap);
			
		sendPasteCommand();
	}
	
	private void sendPasteCommand() {
//		final int ctrlKey =
//				OSInfo.isMacOs() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
//		robot.keyPress(ctrlKey);
//		robot.keyPress(KeyEvent.VK_V);
//		robot.keyRelease(KeyEvent.VK_V);
//		robot.keyRelease(ctrlKey);
	}

	public class IpaTransferrable implements Transferable {
		
		private final String val;
		
		public IpaTransferrable(String val) {
			super();
			this.val = val;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{ DataFlavor.stringFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == DataFlavor.stringFlavor;
		}

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			return val.toString();
		}
		
	}
	
}
