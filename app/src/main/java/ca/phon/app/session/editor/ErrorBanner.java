package ca.phon.app.session.editor;

import java.awt.*;

import javax.swing.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.*;

import ca.phon.ui.*;

public class ErrorBanner extends MultiActionButton {
	
	public ErrorBanner() {
		super();
		init();
	}

	private void init() {
		setOpaque(false);

		MattePainter matte = new MattePainter(UIManager.getColor("control"));
		RectanglePainter rectPainter = new RectanglePainter(1, 1, 1, 1);
		rectPainter.setFillPaint(PhonGuiConstants.PHON_SHADED);
		CompoundPainter<JXLabel> cmpPainter = new CompoundPainter<JXLabel>(matte, rectPainter);
		super.setBackgroundPainter(cmpPainter);
	}

	@Override
	public Insets getInsets() {
		Insets insets = new Insets(5, 5, 10, 10);
		return insets;
	}
	
}
