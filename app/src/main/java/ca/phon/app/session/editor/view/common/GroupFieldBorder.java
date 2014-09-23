package ca.phon.app.session.editor.view.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Border for group text fields
 *
 */
public class GroupFieldBorder implements Border {
	
	private final static int TOP_INSET = 2;
	
	private final static int BOTTOM_INSET = 2;
	
	private final static int SIDE_INSET = 2;
	
	private final static Color COLOR = Color.LIGHT_GRAY;
	
	private final static int NOTCH_WIDTH = 4;
	
	private final ImageIcon lockIcon = 
			IconManager.getInstance().getIcon("emblems/emblem-readonly", IconSize.XSMALL);

	@Override
	public Insets getBorderInsets(Component c) {
		final int startWidth = NOTCH_WIDTH + 1;
		final int endWidth = NOTCH_WIDTH + 1;
		
		final int top = TOP_INSET;
		final int left = startWidth + SIDE_INSET * 2;
		final int right = endWidth + SIDE_INSET;
		final int bottom = BOTTOM_INSET;
		
		return new Insets(top, left, bottom, right);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}
	
	private boolean isShowLock(Component c) {
		return (c instanceof JTextComponent ? !((JTextComponent)c).isEditable() || !c.isEnabled() : !c.isEnabled());
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		g.setColor(COLOR);
		
		g.drawLine(x+SIDE_INSET, y+TOP_INSET, x+SIDE_INSET, y+(height-BOTTOM_INSET));
		g.drawLine(x+SIDE_INSET, y+TOP_INSET, x+SIDE_INSET+NOTCH_WIDTH, y+TOP_INSET);
		g.drawLine(x+SIDE_INSET, y+(height-BOTTOM_INSET), x+SIDE_INSET+NOTCH_WIDTH, y+(height-BOTTOM_INSET));
		
		g.drawLine(x+(width-SIDE_INSET)-1, y+TOP_INSET, x+(width-SIDE_INSET)-1, y+(height-BOTTOM_INSET));
		g.drawLine(x+(width-SIDE_INSET)-1, y+TOP_INSET, x+(width-SIDE_INSET-NOTCH_WIDTH)-1, y+TOP_INSET);
		g.drawLine(x+(width-SIDE_INSET)-1, y+(height-BOTTOM_INSET), x+(width-SIDE_INSET-NOTCH_WIDTH)-1, y+(height-BOTTOM_INSET));
		
		if(isShowLock(c)) {
			g.drawImage(lockIcon.getImage(), width-IconSize.XSMALL.getWidth(), height-IconSize.XSMALL.getHeight(), c);
		}
		
		if(c.hasFocus()) {
			g.setColor(Color.blue);
			g.drawRect(x, y, width-1, height-1);
		}
	}
	
}
