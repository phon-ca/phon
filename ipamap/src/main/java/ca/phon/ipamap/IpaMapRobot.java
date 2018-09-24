/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipamap;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Logger;

import ca.phon.util.OSInfo;

public class IpaMapRobot implements IpaMapListener {
	
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IpaMapRobot.class
			.getName());
	
	private Robot robot;
	
	private IpaMap ipaMap;
	
	public IpaMapRobot(IpaMap ipaMap) {
		this.ipaMap = ipaMap;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	public void ipaMapEvent(String txt) {
		// copy text to clipboard
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new IpaTransferrable(txt), ipaMap);
			
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
