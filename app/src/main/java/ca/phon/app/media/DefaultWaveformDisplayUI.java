package ca.phon.app.media;

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
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.media.LongSound;
import ca.phon.media.Sound;
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
	
	private Map<Channel, double[][]> channelExtremaMap = new HashMap<>();
	
	private WaveformDisplay display;
	
	private boolean needsRepaint = true;
	
	private AtomicReference<SampledPaintWorker> workerRef = new AtomicReference<>();
	
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
			
	private RoundRectangle2D getChannelRect(Channel ch) {
		int x = display.getChannelInsets().left;
		int y = getChannelY(ch);
		int width = display.getWidth()- ( display.getChannelInsets().left + display.getChannelInsets().right );
		int height = display.getChannelHeight();
		int cornerRadius = 5;
		
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
	
	private void paintChannelData(Graphics2D g2, Channel ch, double startX, double endX) {
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
		
		int y = (int)channelRect.getY();
		for(double x = startX; x <= endX && x < channelExtrema[0].length; x += barSize) {
			g2.setStroke(stroke);
					
			int idx = (int)Math.round((x - channelRect.getX()));
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
			line.setLine(x, y+halfHeight, x, ymax);
			g2.draw(line);
			
			g2.setColor(btmColor);
			line.setLine(x, y+halfHeight, x, ymin);
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
	
	@Override
	public void paint(Graphics g, JComponent c) {
		if(!(c instanceof WaveformDisplay))
			throw new IllegalArgumentException("Wrong class");
		
		float audioLength = 0.0f;
		if(display.getLongSound() != null) {
			audioLength = display.getLongSound().length();
		}
		
		int cnt = (int)Math.floor(audioLength * display.getPixelsPerSecond());
		if(needsRepaint) {
			needsRepaint = false;
			
			SampledPaintWorker currentWorker = workerRef.get();
			if(currentWorker != null && !currentWorker.isDone()) {
				currentWorker.cancel(true);
			}
		
			channelExtremaMap.clear();
			for(Channel ch:display.availableChannels()) {
				if(display.isChannelVisible(ch)) {
					double[][] extrema = new double[2][];
					extrema[0] = new double[cnt];
					extrema[1] = new double[cnt];
					
					channelExtremaMap.put(ch, extrema);
				}
			}
			
			SampledPaintWorker worker = new SampledPaintWorker();
			workerRef = new AtomicReference<>(worker);
			worker.execute();
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
		
		// paint background
		if(display.isOpaque()) {
			g2.setColor(display.getBackground());
			g2.fill(bounds);
		}
		
		for(Channel ch:display.availableChannels()) {
			if(display.isChannelVisible(ch)) {
				// paint channel border and background
				paintChannelBackground(g2, ch);
				paintChannelBorder(g2, ch);
				
				RoundRectangle2D channelRect = getChannelRect(ch);
				int sx = (int) Math.max(channelRect.getX(), bounds.x);
				int ex = (int) Math.min(channelRect.getX()+channelRect.getWidth(), bounds.x + bounds.width);
				paintChannelData(g2, ch, sx, ex);
			}
		}
		
		for(var interval:display.getTimeModel().getIntervals()) {
			paintInterval(g2, interval);
		}
	}
	
	@Override
	public void paintInterval(Graphics2D g2, Interval interval) {
		var startX = display.xForTime(interval.getStartMarker().getTime());
		var endX = display.xForTime(interval.getEndMarker().getTime());
		
		var rect = new Rectangle2D.Double(startX, 0, endX-startX, display.getHeight());
		g2.setColor(interval.getColor());
		g2.fill(rect);
		
		super.paintInterval(g2, interval);
	}
	
	private final PropertyChangeListener propListener = (e) -> {
		if("longSound".equals(e.getPropertyName())
				|| "timeModel".equals(e.getPropertyName())
				|| "startTime".equals(e.getPropertyName())
				|| "endTime".equals(e.getPropertyName())
				|| "pixelsPerSecond".equals(e.getPropertyName()) ) {
			
			needsRepaint = true;
			
			display.revalidate();
			display.repaint();
		}
	};
	
	private double cachedMaxValue = 0;
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
		final double width = display.getWidth();
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

	private class SampledPaintWorker extends SwingWorker<Tuple<Float, Float>, Tuple<Float, Float>> {
		
		@Override
		protected Tuple<Float, Float> doInBackground() throws Exception {
			final LongSound sound = display.getLongSound();

			// load in 5s intervals
			float incr = 5.0f;
			float time = display.getStartTime();
			while(time < display.getEndTime() && !isCancelled()) {
				float endTime = Math.min(time+incr, display.getEndTime());
				
				final Sound snd = sound.extractPart(time, endTime);
				
				for(Channel ch:display.availableChannels()) {
					if(!display.isChannelVisible(ch)) continue;
					
					loadWaveformData(snd, ch, time, endTime);
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
			var startX = display.xForTime(startTime);
			var endX = display.xForTime(endTime);

			display.repaint((int)startX, 0, (int)endX, display.getHeight());
		}
		
	}
	
}
