package ca.phon.app.session.editor;

import java.awt.Insets;

import javax.swing.UIManager;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.RectanglePainter;

import ca.phon.ui.MultiActionButton;
import ca.phon.ui.PhonGuiConstants;

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
