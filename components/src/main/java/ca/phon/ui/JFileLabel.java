/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.ui;

import java.io.File;

import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;

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
