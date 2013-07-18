package ca.phon.ipamap;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import ca.phon.util.OSInfo;

public class IpaMapRobot implements IpaMapListener {
	
	private static final Logger LOGGER = Logger.getLogger(IpaMapRobot.class
			.getName());
	
	private Robot robot;
	
	public IpaMapRobot() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	@Override
	public void ipaMapEvent(String txt) {
		// copy text to clipboard
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txt), null);
			
		sendPasteCommand();
	}
	
	private void sendPasteCommand() {
		final int ctrlKey =
				OSInfo.isMacOs() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
		robot.keyPress(ctrlKey);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(ctrlKey);
	}

}
