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
package ca.phon.util.icons;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

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
