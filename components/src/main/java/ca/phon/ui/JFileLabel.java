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
package ca.phon.ui;

import java.io.*;

import javax.swing.*;

import org.apache.commons.lang3.*;

/**
 * Displays a file path as just the name or a shortened
 * string.
 */
public class JFileLabel extends JLabel {
	
	private boolean showNameOnly = true;
	
	private int maxChars = 80;
	
	private File file;
	
	public JFileLabel() {
		super();
	}
	
	public void setFile(File f) {
		this.file = f;
		updateText();
	}
	
	public File getFile() {
		return this.file;
	}
	
	private void updateText() {
		String txt = "";
		
		if(file != null) {
			setToolTipText(file.getAbsolutePath());
			if(showNameOnly) {
				txt = file.getName();
			} else {
				txt = StringUtils.abbreviate(file.getAbsolutePath(), maxChars);
//				txt = StringUtils.shortenStringUsingToken(file.getAbsolutePath(), PhonConstants.ellipsis+"", maxChars);
			}
		}
		
		setText(txt);
	}
	
	public void setShowNameOnly(boolean showNameOnly) {
		this.showNameOnly = showNameOnly;
		updateText();
	}
	
	public boolean isShowNameOnly() {
		return this.showNameOnly;
	}
	
	public void setMaxChars(int v) {
		this.maxChars = v;
		updateText();
	}
	
	public int getMaxChars() {
		return this.maxChars;
	}
}
