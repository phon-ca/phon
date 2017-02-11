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
import ca.phon.ui.jbreadcrumb.BreadcrumbStateBorder;

public class NodeWizardBreadcrumbButton extends JButton {

	private static final long serialVersionUID = -8963234070761177007L;
	
	public NodeWizardBreadcrumbButton() {
		super();
		
		setOpaque(false);
		setBorder(new BreadcrumbStateBorder());
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final Insets insets = getInsets();
		final Rectangle2D rect = new Rectangle2D.Double(insets.left, 0, getWidth()-insets.right-insets.left, getHeight());
		
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
		Point p = GUIHelper.centerTextInRectangle(g2, getText(),
				new Rectangle((int)rect.getX(), 0, (int)rect.getWidth(), (int)getHeight()));
		g2.drawString(getText(), p.x, p.y);
	}
	
}
