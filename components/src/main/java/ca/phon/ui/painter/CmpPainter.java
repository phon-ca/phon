package ca.phon.ui.painter;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

public class CmpPainter<T extends JComponent> implements Painter<T> {

	/**
	 * List of painters
	 */
	private List<Painter<T>> painters = 
		Collections.synchronizedList(new ArrayList<Painter<T>>());
	
	/**
	 * Constructor
	 */
	public CmpPainter() {
		super();
	}
	
	public CmpPainter(Painter<T> ... painters) {
		for(int i = 0; i < painters.length; i++) {
			this.painters.add(painters[i]);
		}
	}
	
	@Override
	public void paint(Graphics2D g2d, T comp, int width, int height) {
		//paint in order
		for(Painter<T> painter:painters) {
			painter.paint(g2d, comp, width, height);
		}
	}
	
}
