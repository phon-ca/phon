package ca.phon.app.session.editor.view.speech_analysis;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

/**
 * 
 */
public class SpeechAnalysisDivider extends JComponent {

	private static final long serialVersionUID = 6318352291908350196L;
	
	private final static String DEFAULT_BG = "#eeeeee";
	
	private final static int DEFAULT_HEIGHT = 8;
	
	private int height;

	public SpeechAnalysisDivider() {
		this(DEFAULT_HEIGHT);
	}
	
	public SpeechAnalysisDivider(int height) {
		super();
		
		this.height = height;
		
		setOpaque(true);
		setBackground(Color.decode(DEFAULT_BG));
		setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	}
	
	@Override
	public Dimension getPreferredSize() {
		final Dimension retVal = super.getPreferredSize();
		retVal.setSize(retVal.getWidth(), getDividerHeight());
		return retVal;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.setColor(getForeground());
	}
	
	public int getDividerHeight() {
		return this.height;
	}
	
	public void setDividerHeight(int height) {
		int oldHeight = this.height;
		this.height = height;
		firePropertyChange("height", oldHeight, height);
	}
	
}
