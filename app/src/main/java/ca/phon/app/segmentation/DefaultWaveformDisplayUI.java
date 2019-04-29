package ca.phon.app.segmentation;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import ca.phon.media.sampled.Channel;
import ca.phon.media.sampled.Sampled;
import ca.phon.media.sampled.SampledPainter;
import ca.phon.util.Tuple;

/**
 * Default implementation of UI for {@link WaveformDisplay}
 *
 */
public class DefaultWaveformDisplayUI extends WaveformDisplayUI {
	
	private Map<Channel, BufferedImage> channelImages = new HashMap<>();
	
	private WaveformDisplay display;
	
	private boolean needsRepaint = true;
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		super.installUI(c);
		
		display = (WaveformDisplay)c;
		
		installListeners(display);
	}

	@Override
	public void uninstallUI(JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		super.uninstallUI(c);
		
		uninstallListeners((WaveformDisplay)c);
	}
	
	private void installListeners(WaveformDisplay display) {
		display.addPropertyChangeListener(propListener);
	}
	
	private void uninstallListeners(WaveformDisplay display) {
		display.removePropertyChangeListener(propListener);
	}
	
	public float timeAtX(int x) {
		return x / display.getSecondsPerPixel();
	}
	
	public int xForTime(float time) {
		return (int)Math.round( display.getSecondsPerPixel() * time );
	}
	
	public Rectangle getChannelBounds(Channel ch) {
		int x = 0;
		int y = 0;
		
		for(int i = 0; i < Channel.values().length && i < ch.ordinal(); i++) {
			if(display.isChannelVisible(ch)) {
				y += display.getChannelHeight() + display.getChannelGap();
			}
		}
		
		int w = display.getWidth();
		int h = display.getChannelHeight();
		
		return new Rectangle(x, y, w, h);
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		
		if(needsRepaint) {
			needsRepaint = false;
		
			for(Channel ch:display.availableChannels()) {
				if(display.isChannelVisible(ch)) {
					BufferedImage channelImg = new BufferedImage(display.getWidth(), display.getChannelHeight(), BufferedImage.TYPE_4BYTE_ABGR);
					channelImages.put(ch, channelImg);
				}
			}
			
			(new SampledPaintWorker()).execute();
		}
		
		Graphics2D g2 = (Graphics2D)g;
		
		WaveformDisplay display = (WaveformDisplay)c;
		
		int w = display.getWidth();
		int h = display.getHeight();
		Rectangle bounds = 
				(g.getClipBounds() != null ? g.getClipBounds() : new Rectangle(0, 0, w, h));
		System.out.println(bounds);
		
		// paint background
		if(display.isOpaque()) {
			g2.setColor(display.getBackground());
			g2.fill(bounds);
		}

		int currentY = 0;
		int channelHeight = display.getChannelHeight();
		int gap = display.getChannelGap();
		
		for(Channel ch:display.availableChannels()) {
			if(display.isChannelVisible(ch)) {
				BufferedImage channelImage = channelImages.get(ch);
				g2.drawImage(channelImage, bounds.x, currentY, bounds.x+bounds.width, currentY+channelHeight,
						bounds.x, 0, bounds.x+bounds.width, channelHeight, display);
				currentY += channelHeight + gap;
			}
		}
	}
	
	private final PropertyChangeListener propListener = (e) -> {
		if("sampled".equals(e.getPropertyName())) {
			needsRepaint = true;
			display.revalidate();
		}
	};
	
	private Graphics2D createGraphics(BufferedImage img) {
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
				RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		return g2;
	}

	private class SampledPaintWorker extends SwingWorker<Tuple<Double, Double>, Tuple<Double, Double>> {
		
		@Override
		protected Tuple<Double, Double> doInBackground() throws Exception {
			final Sampled sampled = display.getSampled();
			
			final double width = display.getWidth();
			final double height = display.getChannelHeight();
			final double halfHeight = height / 2.0;
			
			float startTime = display.getStartTime();
			float endTime = display.getEndTime();
			float length = endTime - startTime;
			final float secondsPerPixel = (float)(length / width);
			
			float barSize = 1.0f;
			final Stroke stroke = new BasicStroke(barSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			
			double extrema[] = new double[2];
			final Line2D line = new Line2D.Double();
						
			double maxValue = 0;
			for(int ch = 0; ch < sampled.getNumberOfChannels(); ch++) {
				sampled.getWindowExtrema(ch, startTime, endTime, extrema);
				maxValue = Math.max(maxValue, Math.max(Math.abs(extrema[0]), Math.abs(extrema[1])));
			}
			final double unitPerPixel = maxValue / halfHeight;
			
			double ymindiff, ymaxdiff = 0.0;
			float time = 0.0f;
			double ymin, ymax = halfHeight;
			
			Map<Channel, Graphics2D> gMap = new HashMap<>();
			double lastUpdateX = 0.0;
			for(double x = 0.0; x < width; x += barSize) {
				time = startTime + (float)(x * secondsPerPixel);
				
				for(Channel ch:display.availableChannels()) {
					if(!display.isChannelVisible(ch)) continue;
					
					BufferedImage channelImage = channelImages.get(ch);
					Graphics2D g2 = gMap.get(ch);
					if(g2 == null) {
						 g2 = createGraphics(channelImage);
						 gMap.put(ch, g2);
					}
					g2.setStroke(stroke);
					g2.setColor(display.getChannelColor(ch));
					
					sampled.getWindowExtrema(ch.channelNumber(), time, time + secondsPerPixel, extrema);
					
					ymindiff = Math.abs(extrema[0]) / unitPerPixel;
					ymaxdiff = Math.abs(extrema[1]) / unitPerPixel;
					
					ymin = halfHeight;
					if(extrema[0] < 0) {
						ymin += ymindiff;
					} else {
						ymin -= ymindiff;
					}
					
					ymax = halfHeight;
					if(extrema[1] < 0) {
						ymax += ymaxdiff;
					} else {
						ymax -= ymaxdiff;
					}
					
					line.setLine(x, ymin, x, ymax);
					g2.draw(line);
				}
				
				if(x - lastUpdateX >= 10.0f) {
					publish(new Tuple<>(lastUpdateX, x));
					lastUpdateX = x;
				}
			}
			publish(new Tuple<>(lastUpdateX, (double)display.getWidth()));
			
			return new Tuple<Double, Double>((double)0, (double)display.getWidth());
		}

		@Override
		protected void process(List<Tuple<Double, Double>> chunks) {
			var startX = -1;
			var endX = 0;
			for(var tuple:chunks) {
				startX = (startX == -1 ? (int)Math.floor(tuple.getObj1()) : Math.min(startX, (int)Math.floor(tuple.getObj1())));
				endX = Math.max(endX, (int)Math.ceil(tuple.getObj2()));
				
			}
			System.out.println(String.format("Repaint %d - %d", startX, endX));
			display.repaint(startX, 0, endX, display.getHeight());
		}
		
	}
	
}
