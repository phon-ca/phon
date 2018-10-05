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
