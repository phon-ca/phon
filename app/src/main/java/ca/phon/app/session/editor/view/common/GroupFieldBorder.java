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
	
	private final static char GROUP_START = '[';
	
	private final static char GROUP_END = ']';
	
	private final static int TOP_INSET = 2;
	
	private final static int BOTTOM_INSET = 2;
	
	private final static int SIDE_INSET = 2;
	
	private final static Color COLOR = Color.LIGHT_GRAY;
	
	private final ImageIcon lockIcon = 
			IconManager.getInstance().getIcon("emblems/emblem-readonly", IconSize.XSMALL);

	@Override
	public Insets getBorderInsets(Component c) {
		final Font font = c.getFont();
		final FontMetrics metrics = c.getFontMetrics(font);
		
		final int startWidth = metrics.getWidths()[(int)GROUP_START];
		final int endWidth = metrics.getWidths()[(int)GROUP_END];
		
		final int top = TOP_INSET;
		final int left = startWidth + SIDE_INSET * 2;
		final int right = (isShowLock(c) ? IconSize.XSMALL.getWidth() : 0) + endWidth + SIDE_INSET;
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
		final int baseline = c.getBaseline(width, height);
		
		final Font font = c.getFont();
		final FontMetrics metrics = c.getFontMetrics(font);
		
		final int endWidth = metrics.getWidths()[(int)GROUP_END];
		
		g.setColor(COLOR);
		g.drawString(GROUP_START+"", 1, baseline);
		g.drawString(GROUP_END+"", width-endWidth-(isShowLock(c) ? IconSize.XSMALL.getWidth()-1 : 1), baseline);
				
		if(isShowLock(c)) {
			g.drawImage(lockIcon.getImage(), width-IconSize.XSMALL.getWidth(), height-IconSize.XSMALL.getHeight(), c);
		}
		
		if(c.hasFocus()) {
			g.setColor(Color.blue);
			g.drawRect(x, y, width-1, height-1);
		}
	}
	
}
