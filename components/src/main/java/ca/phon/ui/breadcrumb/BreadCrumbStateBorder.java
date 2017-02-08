package ca.phon.ui.breadcrumb;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Default {@link Border} for {@link BreadCrumb} states.  Border
 * will be drawn with arrow tail and head.  Border will be
 * painted using the components background color and outlined
 * with the foreground color.
 * 
 */
public class BreadCrumbStateBorder implements Border {

	public final static int TOP_INSET = 5;
	private int topInset;
	
	public final static int BOTTOM_INSET = 5;
	private int bottomInset;
	
	public final static int ARROW_WIDTH = 5;
	private int arrowWidth;
	
	private boolean drawTail;
	
	public BreadCrumbStateBorder() {
		this(TOP_INSET, BOTTOM_INSET, ARROW_WIDTH, true);
	}
	
	public BreadCrumbStateBorder(boolean drawTail) {
		this(TOP_INSET, BOTTOM_INSET, ARROW_WIDTH, drawTail);
	}
	
	public BreadCrumbStateBorder(int topInset, int bottomInset, int arrowWidth, boolean drawTail) {
		super();
		this.topInset = topInset;
		this.bottomInset = bottomInset;
		this.arrowWidth = arrowWidth;
		this.drawTail = drawTail;
	}
	
	public int getTopInset() {
		return topInset;
	}

	public void setTopInset(int topInset) {
		this.topInset = topInset;
	}

	public int getBottomInset() {
		return bottomInset;
	}

	public void setBottomInset(int bottomInset) {
		this.bottomInset = bottomInset;
	}

	public int getArrowWidth() {
		return arrowWidth;
	}

	public void setArrowWidth(int arrowWidth) {
		this.arrowWidth = arrowWidth;
	}

	public boolean isDrawTail() {
		return drawTail;
	}

	public void setDrawTail(boolean drawTail) {
		this.drawTail = drawTail;
	}

	@Override
	public void paintBorder(Component c, Graphics gfx, int x, int y, int width, int height) {
		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Insets insets = getBorderInsets(c);
		
		final GeneralPath arrowPath = new GeneralPath();
		arrowPath.moveTo(0, 0);
		arrowPath.lineTo(arrowWidth, c.getHeight()/2-1);
		arrowPath.lineTo(0, c.getHeight());
		arrowPath.closePath();
		
		final Color background = 
				(c.isEnabled() ? c.getBackground() : UIManager.getColor("Button.background"));
		final Color foreground =
				(c.isEnabled() ? c.getForeground() : UIManager.getColor("Button.disabledForeground"));
		
		if(insets.left == 2*arrowWidth) {
			final Rectangle2D rect = new Rectangle2D.Double(0, 0, 2*arrowWidth, c.getHeight());
			final Area area = new Area(rect);
			area.subtract(new Area(arrowPath));
			
			g.setColor(background);
			g.fill(area);
			
			// draw border line
//			g.setColor(foreground);
//			g.drawLine(0, 0, arrowWidth, c.getHeight()/2-1);
//			g.drawLine(arrowWidth, c.getHeight()/2-1, 0, c.getHeight());
		} else {
			g.setColor(background);
			g.fillRect(0, 0, arrowWidth, c.getHeight());
		}
		
		AffineTransform origTrans = g.getTransform();
		
		g.translate(c.getWidth()-insets.right, 0);
		g.setColor(background);
		
		g.fillRect(0, 0, arrowWidth-1, c.getHeight());
		
		g.setTransform(origTrans);
		g.translate(c.getWidth()-arrowWidth-1, 0);
		
		g.fill(arrowPath);
	
		// draw border line
		g.setColor(foreground);
		g.drawLine(0, 0, arrowWidth, c.getHeight()/2-1);
		g.drawLine(arrowWidth, c.getHeight()/2-1, 0, c.getHeight());
		
		if(c.hasFocus()) {
			g.setTransform(origTrans);

			final Rectangle rect = new Rectangle(insets.left - 2, 2, c.getWidth() - insets.left - insets.right + 4, c.getHeight() - 4);
			int vx, vy;

			g.setColor(UIManager.getDefaults().getColor("Button.focus"));

			// draw upper and lower horizontal dashes
			for (vx = rect.x; vx < (rect.x + rect.width); vx += 2) {
				g.fillRect(vx, rect.y, 1, 1);
				g.fillRect(vx, rect.y + rect.height - 1, 1, 1);
			}

			// draw left and right vertical dashes
			for (vy = rect.y; vy < (rect.y + rect.height); vy += 2) {
				g.fillRect(rect.x, vy, 1, 1);
				g.fillRect(rect.x + rect.width - 1, vy, 1, 1);
			}
		}
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(
				topInset,
				(drawTail ? 2*arrowWidth : arrowWidth),
				bottomInset,
				2*arrowWidth
			);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

}
