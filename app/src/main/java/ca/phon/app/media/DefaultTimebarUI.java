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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JComponent;
import javax.swing.UIManager;

import ca.phon.util.MsFormatter;

public class DefaultTimebarUI extends TimebarUI {
	
	private Timebar timebar;
	
	private final PropertyChangeListener propListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if(e.getPropertyName().equals("startTime")
					|| e.getPropertyName().equals("endTime")
					|| e.getPropertyName().equals("pixelsPerSecond") 
					|| e.getPropertyName().equals("timeInsets") 
					|| e.getPropertyName().equals("minorTickHeight") 
					|| e.getPropertyName().equals("majorTickHeight") ) {
				timebar.revalidate();
				timebar.repaint();
			} else if(e.getPropertyName().equals("model")) {
				((TimeUIModel)e.getOldValue()).removePropertyChangeListener(this);
				((TimeUIModel)e.getNewValue()).addPropertyChangeListener(this);
			}
		}
		
	};
	
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
		uninstallListeners((Timebar)c);
	}
	
	private void installListeners(Timebar timebar) {
		timebar.addPropertyChangeListener(propListener);
//		timebar.getModel().addPropertyChangeListener(propListener);
	}

	private void uninstallListeners(Timebar timebar) {
		timebar.removePropertyChangeListener(propListener);
//		timebar.getModel().removePropertyChangeListener(propListener);
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension retVal = super.getPreferredSize(comp);
		
		Font font = timebar.getFont();
		FontMetrics fm = timebar.getFontMetrics(font);
		
		int prefHeight = timebar.getTimeModel().getTimeInsets().top + timebar.getTimeModel().getTimeInsets().bottom + 
				(Math.max(timebar.getMajorTickHeight(), fm.getHeight() + timebar.getMinorTickHeight()));
		retVal.height = prefHeight;
		
		return retVal;
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
		float pixelsPerSecond = timebar.getTimeModel().getPixelsPerSecond();
		return (100.0f / pixelsPerSecond);
	}
	
	private float minorTickLength() {
		return majorTickLength() / 10.0f;
	}
	
	protected void paintTicks(Graphics2D g2, Timebar timebar) {
		Stroke minorTickStroke = new BasicStroke(1.0f);
		Stroke majorTickStroke = new BasicStroke(1.2f);
		
		g2.setColor(Color.DARK_GRAY);
		
		Line2D.Double tickLine = new Line2D.Double();
		
		for(float time = timebar.getTimeModel().getStartTime(); time <= timebar.getTimeModel().getEndTime(); time += majorTickLength()) {
			time = (float)TimeUIModel.roundTime(time);
			var x = timebar.getTimeModel().xForTime(time);
			g2.setStroke(majorTickStroke);
			
			tickLine.setLine(x, 0, x, timebar.getMajorTickHeight());
			g2.draw(tickLine);
			
			for(float mt = time + minorTickLength(); mt <= time + (majorTickLength()-(minorTickLength()/2)); mt += minorTickLength()) {
				mt = (float)TimeUIModel.roundTime(mt);
				if(mt > timebar.getTimeModel().getEndTime()) break;
				var x2 = timebar.getTimeModel().xForTime(mt);
				
				g2.setStroke(minorTickStroke);
				
				tickLine.setLine(x2, 0, x2, timebar.getMinorTickHeight());
				g2.draw(tickLine);
			}
		}
	}
	
	protected void paintLabels(Graphics2D g2, Timebar timebar) {
		Font font = timebar.getFont();
		FontMetrics fm = g2.getFontMetrics(font);
		
		g2.setFont(timebar.getFont());
		g2.setColor(Color.BLACK);
		
		Rectangle2D lastTimeRect = null;
		for(float time = timebar.getTimeModel().getStartTime(); time <= timebar.getTimeModel().getEndTime(); time += (2.0 * majorTickLength()) ) {
			time = (float)TimeUIModel.roundTime(time);
			long timeMs = (long)(time * 1000.0f);
			String timeStr = MsFormatter.msToDisplayString(timeMs);
						
			var x = timebar.getTimeModel().xForTime(time);
			Rectangle2D timeRect = fm.getStringBounds(timeStr, g2);
			timeRect.setRect(x, timebar.getMinorTickHeight(), timeRect.getWidth(), timeRect.getHeight());
			
			if(lastTimeRect == null || !lastTimeRect.intersects(timeRect)) {
				if(timeRect.intersects(g2.getClipBounds())) {
					g2.drawString(timeStr, (float)timeRect.getX(), (float)(timeRect.getY() + timeRect.getHeight()));
				}
				lastTimeRect = timeRect;
			}
		}
	}
	
}
