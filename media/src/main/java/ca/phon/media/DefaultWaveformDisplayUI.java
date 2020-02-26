package ca.phon.media;

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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import ca.phon.media.sampled.Channel;
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
	
	private Map<Channel, double[][]> channelExtremaMap = new HashMap<>();
	
	private int prevCacheWidth = -1;
	private float prevCacheStart = -1.0f;
	private float prevCacheEnd = -1.0f;
	private double prevCachedMax = -1.0f;
	private BufferedImage cachedImg = null;

	private WaveformDisplay display;
	
	private volatile boolean needsRepaint = true;
	
	private AtomicReference<SampledWorker> workerRef = new AtomicReference<>();
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		super.installUI(c);
		
		display = (WaveformDisplay)c;
		display.setDoubleBuffered(true);
		
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
			
	private RoundRectangle2D getChannelRect(Channel ch) {
		int x = display.getTimeModel().getTimeInsets().left + display.getChannelInsets().left;
		int y = getChannelY(ch);
		
		var audioMaxX = display.xForTime(
				Math.min(display.getLongSound().length(), display.getTimeModel().getEndTime()) );
		
		int width = (int)(audioMaxX - x);
		int height = display.getChannelHeight();
		int cornerRadius = 5;
		
		return new RoundRectangle2D.Double(x, y, width, height, cornerRadius, cornerRadius);
	}
	
	private void paintChannelBorder(Graphics2D g2, Rectangle rect, Channel ch) {
		g2.setColor(Color.LIGHT_GRAY);
		g2.draw(getChannelRect(ch));
	}
	
	private void paintChannelBackground(Graphics2D g2, Rectangle rect, Channel ch) {
		RoundRectangle2D channelRect = getChannelRect(ch);
		Rectangle2D topRect = new Rectangle2D.Double(
				channelRect.getX(), channelRect.getY(), channelRect.getWidth(), channelRect.getHeight()/2);
		Rectangle2D btmRect = new Rectangle2D.Double(
				channelRect.getX(), channelRect.getCenterY(), channelRect.getWidth(), channelRect.getHeight()/2);
		
		Area topArea = new Area(channelRect);
		topArea.intersect(new Area(topRect));
		topArea.intersect(new Area(g2.getClip()));
		
		Area btmArea = new Area(channelRect);
		btmArea.intersect(new Area(btmRect));
		btmArea.intersect(new Area(g2.getClip()));
		
		Color topColor = UIManager.getColor(BG_COLOR1);
		Color btmColor = UIManager.getColor(BG_COLOR2);
		
		g2.setColor(topColor);
		g2.fill(topArea);
		
		g2.setColor(btmColor);
		g2.fill(btmArea);
	}
	
	private void paintChannelData(Graphics2D g2, Channel ch, int channelY, double visibleX, double startX, double endX) {
		final RoundRectangle2D channelRect = getChannelRect(ch);
		final double halfHeight = channelRect.getHeight() / 2.0;
		
		float barSize = 1.0f;
		final Stroke stroke = new BasicStroke(barSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		double extrema[] = new double[2];
		final Line2D line = new Line2D.Double();
					
		double maxValue = getMaxValue();
		final double unitPerPixel = maxValue / halfHeight;
		
		double ymindiff, ymaxdiff = 0.0;
		double ymin, ymax = halfHeight;
		
		Color topColor = UIManager.getColor(WAV_COLOR1);
		Color btmColor = UIManager.getColor(WAV_COLOR2);
		
		double[][] channelExtrema = channelExtremaMap.get(ch);
		
		int y = channelY;
		for(double x = startX; x <= endX && x < channelExtrema[0].length; x += barSize) {
			g2.setStroke(stroke);
					
			int idx = (int)(x - channelRect.getX());
			extrema[0] = channelExtrema[0][idx];
			extrema[1] = channelExtrema[1][idx];
			
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
			line.setLine(x - visibleX, y+halfHeight, x - visibleX, ymax);
			g2.draw(line);
			
			g2.setColor(btmColor);
			line.setLine(x - visibleX, y+halfHeight, x - visibleX, ymin);
			g2.draw(line);
		}
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		int prefWidth = super.getPreferredSize(comp).width;
	
		int prefHeight = (display.getVisibleChannelCount() * display.getChannelHeight())
				+ (display.getVisibleChannelCount() * (display.getChannelInsets().top + display.getChannelInsets().bottom));
		
		if(prefHeight == 0) {
			prefHeight = display.getChannelHeight() + (display.getChannelInsets().top + display.getChannelInsets().bottom);
		}
	
		return new Dimension(prefWidth, prefHeight);
	}
	
	private int lastStartX = -1;
	private int lastEndX = -1;
	private int currentBufferSize = -1;
	
	@Override
	public void paint(Graphics g, JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		
		float audioLength = 0.0f;
		if(display.getLongSound() != null) {
			audioLength = display.getLongSound().length();
		} else {
			return;
		}
		
		int currentStartX = (int)display.getWindowStartX();
		int currentEndX = (int)display.getWindowEndX();
		
		if(currentStartX != lastStartX
				|| currentEndX != lastEndX) {
			needsRepaint = true;
		}
		int cnt = (int)Math.floor(audioLength * display.getPixelsPerSecond());
		if(needsRepaint) {
			needsRepaint = false;
			
			SampledWorker currentWorker = workerRef.get();
			if(currentWorker != null && !currentWorker.isDone()) {
				currentWorker.cancel(true);
			}
		
			if(currentBufferSize < 0 || currentBufferSize != cnt) {
				channelExtremaMap.clear();
				for(Channel ch:display.availableChannels()) {
					if(display.isChannelVisible(ch)) {
						double[][] extrema = new double[2][];
						extrema[0] = new double[cnt];
						extrema[1] = new double[cnt];
						
						channelExtremaMap.put(ch, extrema);
					}
				}
				currentBufferSize = cnt;
			}
			
			SampledWorker worker = new SampledWorker();
			workerRef = new AtomicReference<>(worker);
			worker.execute();
			
			lastStartX = currentStartX;
			lastEndX = currentEndX;
		}
		
		Graphics2D g2 = (Graphics2D)g;
		setupRenderingHints(g2);
		
		WaveformDisplay display = (WaveformDisplay)c;
		
		Rectangle bounds = 
				(g.getClipBounds() != null ? g.getClipBounds() : display.getVisibleRect());

		// paint background
		if(display.isOpaque()) {
			g2.setColor(display.getBackground());
			g2.fill(bounds);
		}
		
		int minX = display.getTimeModel().getTimeInsets().left;
		int maxX = display.getWidth() - display.getTimeModel().getTimeInsets().right;
		int sx = (int)Math.max(bounds.x, minX);
		int ex = (int)Math.min(bounds.x + bounds.width, maxX);
		Rectangle visibleRect = display.getVisibleRect();
		
		boolean updateCache = (prevCacheStart < 0 || prevCacheStart != display.getWindowStart()
				|| prevCacheEnd < 0 || prevCacheEnd != display.getWindowEnd()
				|| prevCachedMax != cachedMaxValue
				|| prevCacheWidth != (int)visibleRect.getWidth()
				|| cachedImg == null);
		
		for(Channel ch:display.availableChannels()) {
			if(display.isChannelVisible(ch)) {
				paintChannelBackground(g2, bounds, ch);
				paintChannelBorder(g2, bounds, ch);				
			}
		}
		if(updateCache) {
			cachedImg = new BufferedImage(visibleRect.width, display.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D cacheg = (Graphics2D)cachedImg.getGraphics();
			for(Channel ch:display.availableChannels()) {
				if(display.isChannelVisible(ch)) {
					// paint channel border and background
					
					RoundRectangle2D channelRect = getChannelRect(ch);
					paintChannelData(cacheg, ch, (int)channelRect.getY(), visibleRect.getX(), Math.max(minX, visibleRect.x), Math.min(maxX, visibleRect.getMaxX()));
				}
			}
			prevCacheStart = display.getWindowStart();
			prevCacheEnd = display.getWindowEnd();
			prevCachedMax = cachedMaxValue;
			prevCacheWidth = (int)visibleRect.getWidth();
			
			try {
				ImageIO.write(cachedImg, "png", new File("/Users/ghedlund/Desktop/channeldata.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		g2.drawImage(cachedImg, sx, 0, ex, display.getHeight(), 
				sx - visibleRect.x, 0, ex - visibleRect.x, cachedImg.getHeight(), display);
		
		for(var interval:display.getTimeModel().getIntervals()) {
			paintInterval(g2, interval, true);
		}
		
		for(var marker:display.getTimeModel().getMarkers()) {
			paintMarker(g2, marker);
		}
	}
	
	@Override
	public void updateCache() {
		this.needsRepaint = true;
		display.repaint();
	}
	
	private final PropertyChangeListener propListener = (e) -> {
		if("longSound".equals(e.getPropertyName())
				|| "timeModel".equals(e.getPropertyName())
				|| "startTime".equals(e.getPropertyName())
				|| "endTime".equals(e.getPropertyName())
				|| "pixelsPerSecond".equals(e.getPropertyName()) ) {
			
			if("longSound".equals(e.getPropertyName())) {
				cachedMaxValue = 0.0;
			}
			
			needsRepaint = true;
			
			display.revalidate();
			display.repaint();
		}
	};
	
	private double cachedMaxValue = 0.0;
	private double getMaxValue() {
		return cachedMaxValue;
	}
	
	private int getChannelY(Channel ch) {
		int y = display.getChannelInsets().top;
		
		for(int i = 0; i < ch.channelNumber(); i++) {
			y += display.getChannelInsets().bottom + display.getChannelHeight() + display.getChannelInsets().top;
		}
		
		return y;
	}
	
	private void loadWaveformData(Sound snd, Channel ch, float startTime, float endTime) {
		final RoundRectangle2D channelRect = getChannelRect(ch);
		final double width = channelRect.getWidth();
		final float secondsPerPixel = (float)(display.getLongSound().length() / width);
		
		float barSize = 1.0f;
		
		float time = 0.0f;
		
		double startX = display.xForTime(startTime);
		double endX = display.xForTime(endTime);
		
		for(double x = startX; x < endX; x += barSize) {
			time = (float)(x * secondsPerPixel);
			
			double[][] chExtrema = channelExtremaMap.get(ch);
			
			int idx = (int)(x - display.getChannelInsets().left);
			double[] extrema = snd.getWindowExtrema(ch, time, time + secondsPerPixel);
			chExtrema[0][idx] = extrema[0];
			chExtrema[1][idx] = extrema[1];
			
			cachedMaxValue = Math.max(cachedMaxValue, Math.max(Math.abs(extrema[0]), Math.abs(extrema[1])));
		}
	}
	
	private void setupRenderingHints(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	private class SampledWorker extends SwingWorker<Tuple<Float, Float>, Tuple<Float, Float>> {
		 
		@Override
		protected Tuple<Float, Float> doInBackground() throws Exception {
			final LongSound sound = display.getLongSound();

			float incr = 5.0f;
			float time = display.getWindowStart();
			while(time < display.getWindowEnd() && !isCancelled()) {
				float endTime = Math.min(time+incr, display.getEndTime());
				
				final Sound snd = sound.extractPart(time, endTime);
				
				for(Channel ch:display.availableChannels()) {
					if(!display.isChannelVisible(ch)) continue;
					
					loadWaveformData(snd, ch, time, endTime);
				}
				publish(new Tuple<>(time, endTime));
				time = endTime;
			}

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
			var startX = display.xForTime(startTime);
			var endX = display.xForTime(endTime);

			prevCacheStart = -1.0f;
			display.repaint((int)startX, 0, (int)endX, display.getHeight());
		}

		@Override
		protected void done() {
		}

	}

}
