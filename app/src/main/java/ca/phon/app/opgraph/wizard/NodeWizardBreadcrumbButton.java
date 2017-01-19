package ca.phon.app.opgraph.wizard;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;

import ca.phon.ui.GUIHelper;

public class NodeWizardBreadcrumbButton extends JButton {

	private static final long serialVersionUID = -8963234070761177007L;
	
	private static final int RIGHT_WIDTH = 5;
	
	public NodeWizardBreadcrumbButton() {
		super();
		
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(RIGHT_WIDTH, 2*RIGHT_WIDTH, RIGHT_WIDTH, 2*RIGHT_WIDTH));
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final Insets insets = getInsets();
		final Rectangle2D rect = new Rectangle2D.Double(insets.left, 0, getWidth()-RIGHT_WIDTH-insets.left, getHeight());
		
		if(isEnabled())
			g2.setColor(getBackground());
		else
			g2.setColor(UIManager.getColor("Button.background"));
		g2.fill(rect);
		
		if(isEnabled()) {
			g2.setColor(getForeground());
		} else {
			g2.setColor(UIManager.getColor("Button.disabledForeground"));
		}
		Point p = GUIHelper.centerTextInRectangle(g2, getText(), new Rectangle((int)rect.getX(), 0, (int)rect.getWidth()-RIGHT_WIDTH, (int)getHeight()));
		g2.drawString(getText(), p.x, p.y);
	}

	@Override
	protected void paintBorder(Graphics gfx) {
		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		final GeneralPath arrowPath = new GeneralPath();
		arrowPath.moveTo(0, 0);
		arrowPath.lineTo(RIGHT_WIDTH, getHeight()/2-1);
		arrowPath.lineTo(0, getHeight());
		arrowPath.closePath();
		
		if(getInsets().left == 2*RIGHT_WIDTH) {
			final Rectangle2D rect = new Rectangle2D.Double(0, 0, 2*RIGHT_WIDTH, getHeight());
			final Area area = new Area(rect);
			area.subtract(new Area(arrowPath));
			
			if(isEnabled())
				g.setColor(getBackground());
			else
				g.setColor(UIManager.getColor("Button.background"));
			g.fill(area);
			
			if(isEnabled()) {
				g.setColor(getForeground());
			} else {
				g.setColor(UIManager.getColor("Button.disabledForeground"));
			}
			g.drawLine(0, 0, RIGHT_WIDTH, getHeight()/2-1);
			g.drawLine(RIGHT_WIDTH, getHeight()/2-1, 0, getHeight());
		} else {
			g.setColor(getBackground());
			g.fillRect(0, 0, RIGHT_WIDTH, getHeight());
		}
		
		AffineTransform origTrans = g.getTransform();
		
		g.translate((getWidth()-1)-RIGHT_WIDTH, 0);
		if(isEnabled())
			g.setColor(getBackground());
		else
			g.setColor(UIManager.getColor("Button.background"));
		g.fill(arrowPath);
		
		if(isEnabled()) {
			g.setColor(getForeground());
		} else {
			g.setColor(UIManager.getColor("Button.disabledForeground"));
		}
		g.drawLine(0, 0, RIGHT_WIDTH, getHeight()/2-1);
		g.drawLine(RIGHT_WIDTH, getHeight()/2-1, 0, getHeight());
		
		if(isEnabled() && hasFocus()) {
			g.setTransform(origTrans);

			final Rectangle rect = new Rectangle(RIGHT_WIDTH + 1, 2, getWidth() - 2 * RIGHT_WIDTH - 2, getHeight() - 4);
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
	
}
