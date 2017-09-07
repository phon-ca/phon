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
package ca.phon.util.icons;

import java.awt.*;
import java.awt.image.*;

import javax.swing.ImageIcon;

public class HiDPIIcon extends ImageIcon {

	private static final long serialVersionUID = 3622578819603309474L;

	private final Image iconImage;
	
	private final IconSize targetSize;
	
	private Image scaledImage;
	
	public HiDPIIcon(Image iconImage, IconSize size) {
		super();
		this.iconImage = iconImage;
		this.targetSize = size;
	}
	
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		ImageObserver obs = getImageObserver();
		if(obs == null) {
			obs = c;
		}
		
		g.drawImage(getImage(), 0, 0, obs);
	}

	@Override
	public int getIconWidth() {
		return targetSize.getWidth();
	}

	@Override
	public int getIconHeight() {
		return targetSize.getHeight();
	}
	
	@Override
	public Image getImage() {
		if(scaledImage == null) {
			final BufferedImage tmp = new BufferedImage(targetSize.getWidth(), targetSize.getHeight(), 
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.drawImage(iconImage, 0, 0, targetSize.getWidth(), targetSize.getHeight(), null);
			g2.dispose();
			
			scaledImage = tmp;
		}
		return scaledImage;
	}
	
}
