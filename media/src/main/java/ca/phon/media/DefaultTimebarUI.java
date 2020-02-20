package ca.phon.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.UIManager;

import ca.phon.media.TimeUIModel.Marker;
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
	}

	private void uninstallListeners(Timebar timebar) {
		timebar.removePropertyChangeListener(propListener);
	}
	
	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension retVal = super.getPreferredSize(comp);
		
		Font font = timebar.getFont();
		FontMetrics fm = timebar.getFontMetrics(font);
		
		int prefHeight =
				(fm.getHeight() * 2) + timebar.getMinorTickHeight();
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
		paintMarkers(g2, timebar);
	}
	
	protected void paintTicks(Graphics2D g2, Timebar timebar) {
		Font font = timebar.getFont();
		FontMetrics fm = g2.getFontMetrics(font);
		
		Stroke minorTickStroke = new BasicStroke(1.0f);
		Stroke majorTickStroke = new BasicStroke(1.2f);
		
		g2.setColor(Color.DARK_GRAY);
		
		Line2D.Double tickLine = new Line2D.Double();
		
		Rectangle clipRect = timebar.getVisibleRect();
		int leftInset = timebar.getTimeModel().getTimeInsets().left;
		int rightInset = timebar.getTimeModel().getTimeInsets().right;
		int startX = (clipRect.x < leftInset ? leftInset : 
			// start at most recent major tick
			clipRect.x - (clipRect.x % 100));
		
		for(int tickPos = startX; 
				tickPos <= clipRect.x + clipRect.width && tickPos <= timebar.getWidth() - rightInset; 
				tickPos += 10) {
			if(tickPos > timebar.xForTime(timebar.getTimeModel().getEndTime()))
				break;
			
			if( (tickPos - leftInset) % 100 == 0 ) {
				g2.setStroke(majorTickStroke);
				tickLine.setLine(tickPos, fm.getHeight() - 2, tickPos, fm.getHeight()- 2 + timebar.getMajorTickHeight());
				
				if( (tickPos - leftInset) % 200 == 0 ) {
					long timeMs = (long)(timebar.timeAtX(tickPos) * 1000.0f);
					String timeStr = MsFormatter.msToDisplayString(timeMs);
					
					Rectangle2D timeRect = fm.getStringBounds(timeStr, g2);
					timeRect.setRect(tickPos, 0, timeRect.getWidth(), timeRect.getHeight());
					g2.drawString(timeStr, (float)timeRect.getX(), (float)(timeRect.getY() + fm.getAscent()));
				}
			} else {
				g2.setStroke(minorTickStroke);
				tickLine.setLine(tickPos, fm.getHeight(), tickPos, fm.getHeight() + timebar.getMinorTickHeight());
			}
			g2.draw(tickLine);
		}
	}
	
	private void paintMarkers(Graphics2D g2, Timebar timebar) {
		Font font = timebar.getFont();
		FontMetrics fm = g2.getFontMetrics(font);
		
		List<Marker> markerList = new ArrayList<>();
		markerList.addAll(timebar.getTimeModel().getMarkers());
		timebar.getTimeModel().getIntervals().forEach( (i) -> {
			markerList.add(i.getStartMarker());
			markerList.add(i.getEndMarker());
		});
		markerList.sort( (m1, m2) -> {
			return Float.valueOf(m1.getTime()).compareTo(m2.getTime());
		});
		
		int markerY = fm.getHeight() + timebar.getMinorTickHeight();
		
		Map<Marker, Rectangle2D> markerRects = new HashMap<>();
		for(Marker marker:markerList) {
			int markerX = (int)Math.round(timebar.xForTime(marker.getTime()));
			long timeMs = (long)(marker.getTime() * 1000.0f);
			String timeStr = MsFormatter.msToDisplayString(timeMs);
			Rectangle2D timeRect = fm.getStringBounds(timeStr, g2);
			timeRect.setRect(markerX, markerY, timeRect.getWidth(), timeRect.getHeight());
			
			g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.7f));
			g2.fill(timeRect);

			g2.setColor(marker.getColor());
			g2.drawString(timeStr, (float)timeRect.getX(), (float)(timeRect.getY() + fm.getAscent()));
			
			markerRects.put(marker, timeRect);
		}
	}
	
}
