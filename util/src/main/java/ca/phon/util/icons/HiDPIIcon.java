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
