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

import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicCheckBoxUI;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class StarBox extends JCheckBox {
	
	private IconSize starSize;
	private ImageIcon starOn;
	private ImageIcon starOff; 

	public StarBox(IconSize size) {
		super();
		
		this.starSize = size;
		
		starOn = 
			IconManager.getInstance().getIcon("misc/metal-star-on", starSize);
		starOff = 
			IconManager.getInstance().getIcon("misc/metal-star-off", starSize);
		
		setUI(new StarBoxUI());
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(starSize.getWidth(), starSize.getHeight());
	}
	
	private class StarBoxUI extends BasicCheckBoxUI {

		@Override
		public void paint(Graphics g, JComponent c) {
			Dimension display = getSize();
			
			g.setColor(c.getBackground());
			g.fillRect(0, 0, display.width, display.height);
			
			// draw icon in middle of display
			int xpos = display.width/2 - starSize.getWidth()/2;
			int ypos = display.height/2 - starSize.getHeight()/2;
			
			Graphics2D g2 = (Graphics2D)g;
			Composite orig = g2.getComposite();
			
			Image img = 
				(isSelected() ? starOn.getImage() : starOff.getImage());
			
			if(isEnabled()) {
				g2.drawImage(img, xpos, ypos, c);
//				AlphaComposite ac = 
//					AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
//				g2.setComposite(ac);
			}
			
			
//			g2.setComposite(orig);
		}
	}
//	
//	public static void main(String[] args) {
//		StarBox sb = new StarBox(IconSize.XLARGE);
////		sb.setEnabled(false);
//		sb.setSelected(true);
//		
//		JFrame f= new JFrame();
//		f.add(sb);
//		f.pack();
//		f.setVisible(true);
//	}
//	
}
