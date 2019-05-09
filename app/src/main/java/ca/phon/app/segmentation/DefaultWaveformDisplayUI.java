package ca.phon.app.segmentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import ca.phon.media.sampled.Channel;
import ca.phon.media.sampled.Sampled;
import ca.phon.util.Tuple;

/**
 * Default implementation of UI for {@link WaveformDisplay}
 *
 */
public class DefaultWaveformDisplayUI extends WaveformDisplayUI {
	
	/* Colours */
	// top half of channel
	public final static String BG_COLOR1 = DefaultWaveformDisplayUI.class.getName() + ".bgColor1";
	public final static Color DEFAULT_BGCOLOR1 = Color.decode("#c7d0e4");
		
	// bottom half of channel
	public final static String BG_COLOR2 = DefaultWaveformDisplayUI.class.getName() + ".bgColor2";
	public final static Color DEFAULT_BGCOLOR2 = Color.decode("#9fafd1");
	
	// top half of channel
	public final static String WAV_COLOR1 = DefaultWaveformDisplayUI.class.getName() + ".wavColor1";
	public final static Color DEFAULT_WAVCOLOR1 = Color.decode("#5d5c76");
	
	// bottom half of channel
	public final static String WAV_COLOR2 = DefaultWaveformDisplayUI.class.getName() + ".wavColor2";
	public final static Color DEFAULT_WAVCOLOR2 = Color.decode("#3e3f56");

	static {
		UIManager.getDefaults().put(BG_COLOR1, DEFAULT_BGCOLOR1);
		UIManager.getDefaults().put(BG_COLOR2, DEFAULT_BGCOLOR2);
		UIManager.getDefaults().put(WAV_COLOR1, DEFAULT_WAVCOLOR1);
		UIManager.getDefaults().put(WAV_COLOR2, DEFAULT_WAVCOLOR2);
	}
		
	private Map<Channel, BufferedImage> channelImages = new HashMap<>();
	
	private WaveformDisplay display;
	
	private boolean needsRepaint = true;
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		super.installUI(c);
		
		display = (WaveformDisplay)c;
		display.setBackground(UIManager.getColor("Label.background"));
		
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
		return (x - display.getChannelInsets().left) / display.getPixelsPerSecond();
	}
	
	public int xForTime(float time) {
		return (int)Math.round( display.getPixelsPerSecond() * time ) + display.getChannelInsets().left;
	}
		
	private RoundRectangle2D getChannelRect(Channel ch) {
		int x = display.getChannelInsets().left;
		int y = getChannelY(ch);
		int width = display.getWidth()- ( display.getChannelInsets().left + display.getChannelInsets().right );
		int height = display.getChannelHeight();
		int cornerRadius = 20;
		
		return new RoundRectangle2D.Double(x, y, width, height, cornerRadius, cornerRadius);
	}
	
	private void paintChannelBorder(Graphics2D g2, Channel ch) {
		g2.setColor(Color.LIGHT_GRAY);
		g2.draw(getChannelRect(ch));
	}
	
	private void paintChannelBackground(Graphics2D g2, Channel ch) {
		RoundRectangle2D channelRect = getChannelRect(ch);
		Rectangle2D topRect = new Rectangle2D.Double(
				channelRect.getX(), channelRect.getY(), channelRect.getWidth(), channelRect.getHeight()/2);
		Rectangle2D btmRect = new Rectangle2D.Double(
				channelRect.getX(), channelRect.getCenterY(), channelRect.getWidth(), channelRect.getHeight()/2);
		
		Area topArea = new Area(channelRect);
		topArea.intersect(new Area(topRect));
		
		Area btmArea = new Area(channelRect);
		btmArea.intersect(new Area(btmRect));
		
		Color topColor = UIManager.getColor(BG_COLOR1);
		Color btmColor = UIManager.getColor(BG_COLOR2);
		
		g2.setColor(topColor);
		g2.fill(topArea);
		
		g2.setColor(btmColor);
		g2.fill(btmArea);
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		int prefWidth = (display.getSampled() == null ? 0 :
			 (display.getChannelInsets().left+display.getChannelInsets().right) + 
					((int)Math.round(display.getSampled().getLength() * display.getPixelsPerSecond()) ) );
	
		int prefHeight = (display.getVisibleChannelCount() * display.getChannelHeight())
				+ (display.getVisibleChannelCount() * (display.getChannelInsets().top + display.getChannelInsets().bottom));
	
		return new Dimension(prefWidth, prefHeight);
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
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
				RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
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
		
		for(Channel ch:display.availableChannels()) {
			if(display.isChannelVisible(ch)) {
				RoundRectangle2D channelRect = getChannelRect(ch);
				
				// paint channel border and background
				paintChannelBackground(g2, ch);
				paintChannelBorder(g2, ch);
				
				BufferedImage channelImage = channelImages.get(ch);
				
				
				g2.drawImage(channelImage, bounds.x, (int)channelRect.getY(), bounds.x+bounds.width, (int)(channelRect.getY()+channelRect.getHeight()),
						bounds.x, 0, bounds.x+bounds.width, (int)channelRect.getHeight(), display);
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
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g2;
	}
	
	private double cachedMaxValue = -1;
	private double getMaxValue() {
		if(cachedMaxValue < 0) {
			cachedMaxValue = getMaxValue(display.getStartTime(), display.getEndTime());
		}
		return cachedMaxValue;
	}
	
	private double getMaxValue(float startTime, float endTime) {
		double extrema[] = new double[2];
		Sampled sampled = display.getSampled();
		
		double maxValue = 0;
		for(int ch = 0; ch < sampled.getNumberOfChannels(); ch++) {
			sampled.getWindowExtrema(ch, startTime, endTime, extrema);
			maxValue = Math.max(maxValue, Math.max(Math.abs(extrema[0]), Math.abs(extrema[1])));
		}
		
		return maxValue;
	}
	
	private int getChannelY(Channel ch) {
		int y = display.getChannelInsets().top;
		
		for(int i = 0; i < ch.channelNumber(); i++) {
			y += display.getChannelInsets().bottom + display.getChannelHeight() + display.getChannelInsets().top;
		}
		
		return y;
	}
	
	private void paintWaveformWindow(Sampled sampled, Channel ch, Graphics2D g2, float startTime, float endTime) {
		final double width = display.getWidth();
		final double height = display.getChannelHeight();
		final double halfHeight = height / 2.0;
		final float secondsPerPixel = (float)(sampled.getLength() / width);
		
		float barSize = 1.0f;
		final Stroke stroke = new BasicStroke(barSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		double extrema[] = new double[2];
		final Line2D line = new Line2D.Double();
					
		double maxValue = getMaxValue();
		final double unitPerPixel = maxValue / halfHeight;
		
		double ymindiff, ymaxdiff = 0.0;
		float time = 0.0f;
		double ymin, ymax = halfHeight;
		
		double startX = xForTime(startTime);
		double endX = xForTime(endTime);
		
		Color topColor = UIManager.getColor(WAV_COLOR1);
		Color btmColor = UIManager.getColor(WAV_COLOR2);
		
		int y = 0;
		for(double x = startX; x < endX; x += barSize) {
			time = (float)(x * secondsPerPixel);
			
			g2.setStroke(stroke);
			
			sampled.getWindowExtrema(ch.channelNumber(), time, time + secondsPerPixel, extrema);
			
			ymindiff = Math.abs(extrema[0]) / unitPerPixel;
			ymaxdiff = Math.abs(extrema[1]) / unitPerPixel;
			
			ymin = y + halfHeight;
			if(extrema[0] < 0) {
				ymin += ymindiff;
			} else {
				ymin -= ymindiff;
			}
			
			ymax = y + halfHeight;
			if(extrema[1] < 0) {
				ymax += ymaxdiff;
			} else {
				ymax -= ymaxdiff;
			}
			
			g2.setColor(topColor);
			line.setLine(x, y+halfHeight, x, ymax);
			g2.draw(line);
			
			g2.setColor(btmColor);
			line.setLine(x, y+halfHeight, x, ymin);
			g2.draw(line);
		}
	}

	private class SampledPaintWorker extends SwingWorker<Tuple<Float, Float>, Tuple<Float, Float>> {
		
		@Override
		protected Tuple<Float, Float> doInBackground() throws Exception {
			final Sampled sampled = display.getSampled();
			Map<Channel, Graphics2D> gMap = new HashMap<>();
			
			// load in 5s intervals
			float incr = 5.0f;
			float time = display.getStartTime();
			while(time < display.getEndTime()) {
				float endTime = Math.min(time+incr, display.getEndTime());
				
				for(Channel ch:display.availableChannels()) {
					if(!display.isChannelVisible(ch)) continue;
					
					BufferedImage channelImage = channelImages.get(ch);
					Graphics2D g2 = gMap.get(ch);
					if(g2 == null) {
						 g2 = createGraphics(channelImage);
						 gMap.put(ch, g2);
					}
					
					paintWaveformWindow(sampled, ch, g2, time, endTime);
				}
				publish(new Tuple<>(time, endTime));
				time = endTime;
			}
			
			// done() is not used
			return new Tuple<Float, Float>(0.0f, 0.0f);
		}

		@Override
		protected void process(List<Tuple<Float, Float>> chunks) {
			var startTime = -1.0f;
			var endTime = startTime;
			for(var tuple:chunks) {
				startTime = (startTime < 0.0f ? startTime = tuple.getObj1() : Math.min(tuple.getObj1(), startTime));
				endTime = Math.max(endTime, tuple.getObj2());
			}
			var startX = xForTime(startTime);
			var endX = xForTime(endTime);

			display.repaint((int)startX, 0, (int)endX, display.getHeight());
		}
		
	}
	
}
