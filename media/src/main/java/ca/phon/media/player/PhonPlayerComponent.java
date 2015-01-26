package ca.phon.media.player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class PhonPlayerComponent extends JComponent {
	
	private static final long serialVersionUID = 4196967316753261134L;

	public static enum ScaleMode {
		FIT_DISPLAY,
		FILL_DISPLAY
	};
	private ScaleMode scaleMode = ScaleMode.FIT_DISPLAY;
	
	private BufferedImage bufferedImage;
	
	public PhonPlayerComponent() {
		super();
		setDoubleBuffered(false);
	}
	
	public BufferedImage getBufferedImage(int width, int height) {
		if(bufferedImage == null || 
				bufferedImage.getWidth() != width || bufferedImage.getHeight() != height) {
			bufferedImage = 
					GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration().createCompatibleImage(width, height);
			bufferedImage.setAccelerationPriority(1.0f);
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
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		
		int width = getWidth();
		int height = getHeight();
		
		g.fillRect(0, 0, width, height);
		
		final Graphics2D g2 = (Graphics2D)g;
		
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
			
			g2.drawImage(bufferedImage, transform, this);
		}
	}
	
}
