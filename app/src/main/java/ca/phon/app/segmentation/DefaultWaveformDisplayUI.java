package ca.phon.app.segmentation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
	
	private Map<Channel, SampledPainter> channelPainters = new HashMap<>();
	
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
	
	private SampledPainter getChannelPainter(Channel ch) {
		SampledPainter retVal = channelPainters.get(ch);
		if(retVal == null) {
			retVal = new SampledPainter(ch.channelNumber());
			channelPainters.put(ch, retVal);
		}
		return retVal;
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
				SampledPainter painter = getChannelPainter(ch);
				synchronized(painter) {
					g2.drawImage(painter.getBufferdImage(), bounds.x, currentY, bounds.x+bounds.width, currentY+channelHeight, 
							bounds.x, 0, bounds.x+bounds.width, channelHeight, display);
				}
				currentY += channelHeight + gap;
			}
		}
	}
	
	private final PropertyChangeListener propListener = (e) -> {
		if("sampled".equals(e.getPropertyName())) {
			((WaveformDisplay)e.getSource()).repaint();
		}
	};

	private class SampledPaintWorker extends SwingWorker<Tuple<Float, Float>, Tuple<Float, Float>> {
		
		@Override
		protected Tuple<Float, Float> doInBackground() throws Exception {
			Sampled sampled = display.getSampled();
			
			float startTime = display.getStartTime();
			float endTime = display.getEndTime();
			
			float currentStart = startTime;
			// 50 pixel windows
			float window = 50 / display.getSecondsPerPixel();
			
			while(currentStart < endTime) {
				float windowStart = currentStart;
				float windowEnd = Math.min(currentStart+window, sampled.getLength());
				for(Channel ch:display.availableChannels()) {
					SampledPainter channelPainter = getChannelPainter(ch);
					synchronized(channelPainter) {
						if(channelPainter.getBufferdImage() == null) {
							BufferedImage img = new BufferedImage(display.getWidth(), display.getChannelHeight(), BufferedImage.TYPE_4BYTE_ABGR);
							channelPainter.setBufferedImage(img);
						}
						
						BufferedImage channelImg = channelPainter.getBufferdImage();
						Graphics2D imgG2 = channelImg.createGraphics();
						channelPainter.paintWindow(sampled, imgG2, windowStart, windowEnd);
					}
				}
				publish(new Tuple<>(windowStart, windowEnd));
				currentStart = windowEnd;
			}
			
			return new Tuple<>(display.getStartTime(), display.getEndTime());
		}

		@Override
		protected void process(List<Tuple<Float, Float>> chunks) {
			for(var tuple:chunks) {
				var windowStart = tuple.getObj1();
				var windowEnd = tuple.getObj2();
				
				var startX = xForTime(windowStart);
				var endX = xForTime(windowEnd);
				
				display.repaint(startX, 0, (endX-startX), display.getHeight());
			}
		}
		
	}
	
}
