package ca.phon.app.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JComponent;
import javax.swing.UIManager;

import ca.phon.util.MsFormatter;

public class DefaultTimebarUI extends TimebarUI {
	
	private Timebar timebar;
	
	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof Timebar))
			throw new IllegalArgumentException("Wrong class");
		super.installUI(c);
		
		timebar = (Timebar)c;
		
		installListeners(timebar);
	}

	@Override
	public void uninstallUI(JComponent c) {
		if(!(c instanceof Timebar))
			throw new IllegalArgumentException("Wrong class");
		super.uninstallUI(c);
	}
	
	private void installListeners(Timebar timebar) {
		
	}

	@Override
	public Dimension getPreferredSize(JComponent comp) {
		int prefWidth =
			 (timebar.getTimeInsets().left+ timebar.getTimeInsets().right) + 
					((int)Math.round( (timebar.getEndTime() - timebar.getStartTime()) * timebar.getPixelsPerSecond()) );
		
		Font font = timebar.getFont();
		FontMetrics fm = timebar.getFontMetrics(font);
		
		int prefHeight = timebar.getTimeInsets().top + timebar.getTimeInsets().bottom + 
				(Math.max(timebar.getLargeTickHeight(), fm.getHeight() + timebar.getSmallTickHeight()));
		return new Dimension(prefWidth, prefHeight);
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
				RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
		paintTicks(g2, timebar);
		paintLabels(g2, timebar);
	}
	
	private float majorTickLength() {
		float pixelsPerSecond = timebar.getPixelsPerSecond();
		return (100.0f / pixelsPerSecond);
	}
	
	private float minorTickLength() {
		return majorTickLength() / 10.0f;
	}
	
	private double round(double x, int scale, RoundingMode roundingMethod) {
		try {
			final double rounded = (new BigDecimal(Double.toString(x))
				.setScale(scale, roundingMethod))
				.doubleValue();
			return rounded == 0d ? 0d * x : rounded;
		} catch (NumberFormatException ex) {
			if (Double.isInfinite(x)) {
				return x;
			} else {
				return Double.NaN;
			}
		}
	}

	protected void paintTicks(Graphics2D g2, Timebar timebar) {
		Stroke minorTickStroke = new BasicStroke(1.0f);
		Stroke majorTickStroke = new BasicStroke(1.2f);
		
		g2.setColor(Color.DARK_GRAY);
		
		for(float time = timebar.getStartTime(); time <= timebar.getEndTime(); time += majorTickLength()) {
			time = (float)round(time, 3, RoundingMode.HALF_UP);
			int x = timebar.xForTime(time);
			g2.setStroke(majorTickStroke);
			g2.drawLine(x, 0, x, timebar.getLargeTickHeight());
			
			for(float mt = time + minorTickLength(); mt <= time + (majorTickLength()-(minorTickLength()/2)); mt += minorTickLength()) {
				mt = (float)round(mt, 3, RoundingMode.HALF_UP);
				if(mt > timebar.getEndTime()) break;
				int x2 = timebar.xForTime(mt);
				
				g2.setStroke(minorTickStroke);
				g2.drawLine(x2, 0, x2, timebar.getSmallTickHeight());
			}
		}
	}
	
	protected void paintLabels(Graphics2D g2, Timebar timebar) {
		Font font = timebar.getFont();
		FontMetrics fm = g2.getFontMetrics(font);
		
		g2.setFont(timebar.getFont());
		g2.setColor(Color.BLACK);
		
		Rectangle2D lastTimeRect = null;
		for(float time = timebar.getStartTime(); time <= timebar.getEndTime(); time += (2.0 * majorTickLength()) ) {
			time = (float)round(time, 3, RoundingMode.HALF_UP);
			long timeMs = (long)(time * 1000.0f);
			String timeStr = MsFormatter.msToDisplayString(timeMs);
						
			int x = timebar.xForTime(time);
			Rectangle2D timeRect = fm.getStringBounds(timeStr, g2);
			timeRect.setRect(x, timebar.getSmallTickHeight(), timeRect.getWidth(), timeRect.getHeight());
			
			if(lastTimeRect == null || !lastTimeRect.intersects(timeRect)) {
				g2.drawString(timeStr, (float)timeRect.getX(), (float)(timeRect.getY() + timeRect.getHeight()));
				lastTimeRect = timeRect;
			}
		}
	}
	
}
