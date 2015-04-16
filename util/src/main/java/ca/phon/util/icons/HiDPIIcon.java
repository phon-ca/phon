/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

public class HiDPIIcon extends ImageIcon {

	private static final long serialVersionUID = 3622578819603309474L;

	private final Image iconImage;
	
	private final IconSize targetSize;
	
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
		
		int width = iconImage.getWidth(obs);
		int height = iconImage.getHeight(obs);
		float widthScale = (float)targetSize.getWidth() / (float)width;
		float heightScale = (float)targetSize.getHeight() / (float)height;
		
		final Graphics2D g2 = (Graphics2D)g.create(x, y, width, height);
		g2.scale(widthScale, heightScale);
		g2.drawImage(iconImage, 0, 0, obs);
		g2.scale(1.0, 1.0);
		g2.dispose();
	}
	
}
