/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.ipamap;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;

import ca.phon.util.*;

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
