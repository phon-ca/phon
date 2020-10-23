package ca.phon.ui;

import java.awt.*;
import java.net.*;

import javax.swing.*;

/**
 * Create a 'drop-down' icon composed of an original icon
 * and a drop-down arrow.  If <code>paintRollover</code> is
 * <code>true</code> the arrow portion will have a border painted
 * on the left-hand side.
 */
public class DropDownIcon implements Icon {
	
	private final static String ARROW_LOCATION = "ca/phon/ui/arrow.png";
	
	public final static int DEFAULT_GAP = 6;
	
	private Icon icn;
	
	private int gap;
	
	private Icon arrowIcn;
	
	private boolean arrowPainted = true;
	
	public final static int DEFAULT_ICON_POSITION = SwingConstants.CENTER;
	/**
	 * One of: SwingConstants.TOP/CENTER/BOTTOM
	 */
	private int iconPosition = DEFAULT_ICON_POSITION;
	
	private boolean paintRollover;
	
	public DropDownIcon(Icon icn) {
		this(icn, loadArrow(), DEFAULT_GAP, DEFAULT_ICON_POSITION, false);
	}
	
	public DropDownIcon(Icon icn, Icon arrowIcn) {
		this(icn, arrowIcn, DEFAULT_GAP, DEFAULT_ICON_POSITION, false);
	}
	
	public DropDownIcon(Icon icn, int gap) {
		this(icn, loadArrow(), gap, DEFAULT_ICON_POSITION, false);
	}
	
	public DropDownIcon(Icon icn, Icon arrowIcn, int gap) {
		this(icn, arrowIcn, gap, DEFAULT_ICON_POSITION, false);
	}
	
	public DropDownIcon(Icon icn, int gap, int iconPosition) {
		this(icn, loadArrow(), gap, iconPosition, false);
	}
	
	public DropDownIcon(Icon icn, Icon arrowIcn, int gap, int iconPosition) {
		this(icn, arrowIcn, gap, iconPosition, false);
	}
	
	public DropDownIcon(Icon icn, int gap, int iconPosition, boolean paintRollover) {
		this(icn, loadArrow(), gap, iconPosition, paintRollover);
	}
	
	public DropDownIcon(Icon icn, Icon arrowIcn, int gap, int iconPosition, boolean paintRollover) {
		if(icn == null) throw new NullPointerException("Icon may not be null");
		this.icn = icn;
		this.gap = gap;
		this.iconPosition = iconPosition;
		this.paintRollover = paintRollover;
		this.arrowIcn = (arrowIcn != null ? arrowIcn : loadArrow());
	}
	
	private static ImageIcon loadArrow() {
		URL arrowURL = ClassLoader.getSystemResource(ARROW_LOCATION);
		if(arrowURL != null) {
			return new ImageIcon(arrowURL);
		}
		return null;
	}
	
	public void setArrowPainted(boolean painted) {
		this.arrowPainted = painted;
	}
	
	public boolean isArrowPainted() {
		return this.arrowPainted;
	}

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	public int getIconPosition() {
		return iconPosition;
	}

	public void setIconPosition(int iconPosition) {
		this.iconPosition = iconPosition;
	}

	public boolean isPaintRollover() {
		return paintRollover;
	}

	public void setPaintRollover(boolean paintRollover) {
		this.paintRollover = paintRollover;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		int h = getIconHeight();
		icn.paintIcon(c, g, x, y+(h-icn.getIconHeight())/2);
	
		int arrowY = 0;
		switch(getIconPosition()) {
		case SwingConstants.TOP:
			arrowY = 0;
			break;
			
		case SwingConstants.BOTTOM:
			arrowY = y+(h-arrowIcn.getIconHeight());
			break;
			
		case DEFAULT_ICON_POSITION:
		default:
			arrowY = y+(h-arrowIcn.getIconHeight())/2;
		}
		
		if(isArrowPainted())
			arrowIcn.paintIcon(c, g, x+getGap()+icn.getIconWidth(), arrowY);
		
		if(paintRollover) {
			Color brighter = UIManager.getColor( "controlHighlight" ); 
            Color darker = UIManager.getColor( "controlShadow" );
            if( brighter == null || darker == null ) {
                brighter = c.getBackground().brighter();
                darker = c.getBackground().darker();
            }
            
            g.setColor( brighter );
            g.drawLine( x+icn.getIconWidth()+1, y, 
                        x+icn.getIconWidth()+1, y+getIconHeight() );
            g.setColor( darker );
            g.drawLine( x+icn.getIconWidth()+2, y, 
                        x+icn.getIconWidth()+2, y+getIconHeight() );
		}
	}
	
	@Override
	public int getIconWidth() {
		return icn.getIconWidth() + gap + arrowIcn.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return Math.max(icn.getIconHeight(), arrowIcn.getIconHeight());
	}

	/**
	 * Get arrow icon width plush gap
	 */
	public int getArrowAreaWidth() {
		return getGap()/2 + arrowIcn.getIconWidth();
	}
	
	public Rectangle getArrowRect() {
		int x = icn.getIconWidth() + getGap()/2;
		int w = getArrowAreaWidth();
		int y = 0;
		int h = getIconHeight();
		return new Rectangle(x, y, w, h);
	}
	
}
