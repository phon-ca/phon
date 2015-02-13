package ca.phon.media.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import ca.phon.ui.nativedialogs.OSInfo;

public class PhonPlayerComponent extends JComponent {
	
	private static final long serialVersionUID = 4196967316753261134L;

	public static enum ScaleMode {
		FIT_DISPLAY,
		FILL_DISPLAY
	};
	private ScaleMode scaleMode = ScaleMode.FIT_DISPLAY;
	
	private BufferedImage bufferedImage;
	
	private final Color IMG_BG = Color.BLACK;
	
	private final Color NO_IMG_BG = Color.DARK_GRAY;
	
	public PhonPlayerComponent() {
		super();
		setDoubleBuffered(false);
	}
	
	public BufferedImage getBufferedImage(int width, int height) {
		if(bufferedImage == null ||
				bufferedImage.getWidth() != width || bufferedImage.getHeight() != height) {
			if(width > 0 && height > 0) {
				bufferedImage = 
						GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
						.getDefaultConfiguration().createCompatibleImage(width, height);
				bufferedImage.setAccelerationPriority(1.0f);
			}
		}
		return this.bufferedImage;
	}
	
	public void setBufferedImage(BufferedImage img) {
		this.bufferedImage = img;
	}
	
	public ScaleMode getScaleMode() {
		return this.scaleMode;
	}
	
	public void setScaleMode(ScaleMode scaleMode) {
		this.scaleMode = scaleMode;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if(bufferedImage != null) {
			return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
		} else {
			return super.getPreferredSize();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor((bufferedImage == null ? NO_IMG_BG : IMG_BG));
		
		int width = getWidth();
		int height = getHeight();
		
		g.fillRect(0, 0, width, height);
		
		final Graphics2D g2 = (Graphics2D)g;
		if(!OSInfo.isMacOs()) {
			// BUG JDK/JDK-8017247
			// https://bugs.openjdk.java.net/browse/JDK-8017247
			// these options will result in VERY slow rendering on macosx
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		if(bufferedImage != null) {
			int imgWidth = bufferedImage.getWidth();
			int imgHeight = bufferedImage.getHeight();
			
			final AffineTransform transform = new AffineTransform();
			if(getScaleMode() == ScaleMode.FIT_DISPLAY) {
				double imageRatio = (double)imgHeight/(double)imgWidth;
				double rectRatio = (double)height/(double)width;
				
				double scale = 1.0;
				double offsetX = 0.0;
				double offsetY = 0.0;
				if(imageRatio < rectRatio) {
					// scale on width
					scale = (double)width/(double)imgWidth;
					offsetY = ((double)height - (scale * imgHeight)) / 2.0;
				} else if(rectRatio < imageRatio) {
					// scale on height
					scale = (double)height/(double)imgHeight;
					offsetX = ((double)width - (scale * imgWidth)) / 2.0;
				}
				
				transform.translate(offsetX, offsetY);
				transform.scale(scale, scale);
			} else if(getScaleMode() == ScaleMode.FILL_DISPLAY) {
				double scaleX = (double)width/(double)imgWidth;
				double scaleY = (double)height/(double)imgHeight;
				
				transform.scale(scaleX, scaleY);
			}


			// using 'this' as an imageobserver will cause
			// the drawing to be executed twice on macosx
			g2.drawImage(bufferedImage, transform, null/*this*/);
		}
	}
	
}
