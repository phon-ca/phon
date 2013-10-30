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

package ca.phon.media.player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;

/**
 * AWT canvas for movie drawing.
 */
public class PhonPlayerCanvas extends Panel {
	
	private static final long serialVersionUID = 3113428360412298334L;
	
	/**
	 * Current message string
	 */
	private String msgString;
	
	public PhonPlayerCanvas() {
		super();
		this.msgString = "";
	}
	
	public void setMessage(String msg) {
		this.msgString = msg;
	}
	
	public String getMessage() {
		return this.msgString;
	}

	/**
	 * Helper method to show an info message
	 *
	 * @param msg
	 */
	public void showInfoMessage(String msg) {
	}

	/**
	 * Helper method to show a media error
	 */
	public void showErrorMessage(String msg) {
		
	}

	/**
	 * Can the movie be placed in the viewing area?
	 *
	 */
	public boolean isUsable() {
		return (super.isDisplayable() && super.isShowing());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		g.setColor(Color.white);
		// paint message string
		g.drawString(msgString, 10, 20);
	}

	
	
}
