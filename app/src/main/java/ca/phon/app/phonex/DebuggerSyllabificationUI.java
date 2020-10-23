package ca.phon.app.phonex;

import java.awt.*;

import javax.swing.*;

import ca.phon.ui.ipa.*;
import ca.phon.util.icons.*;

public class DebuggerSyllabificationUI extends DefaultSyllabificationDisplayUI {

	private ImageIcon arrowIcon;
	
	private DebuggerSyllabificationDisplay display;
	
	public DebuggerSyllabificationUI(DebuggerSyllabificationDisplay display) {
		super(display);
		
		this.display = display;
		arrowIcon = IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL);
	}
	
	

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		
		c.addPropertyChangeListener("debugIndex", (e) -> {
			c.repaint();
		});
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);
		
		int debugIdx = display.getDebugIndex();
		if(debugIdx >= 0 && debugIdx < display.getNumberOfDisplayedPhones()) {
			Rectangle pRect = super.rectForPhone(debugIdx);
			
			g.drawImage(arrowIcon.getImage(), (int)(pRect.getCenterX() - (arrowIcon.getIconWidth()/2)), (int)pRect.getMaxY(), display);
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension retVal = super.getPreferredSize(c);
		retVal.height += arrowIcon.getIconHeight();
		return retVal;
	}
	
}
