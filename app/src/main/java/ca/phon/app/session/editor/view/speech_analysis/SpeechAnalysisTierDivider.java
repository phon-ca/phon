package ca.phon.app.session.editor.view.speech_analysis;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

public class SpeechAnalysisTierDivider extends JSeparator {
	
	private JComponent comp;

	public SpeechAnalysisTierDivider(JComponent toResize) {
		super(SwingConstants.HORIZONTAL);
		this.comp = toResize;
		
		if(this.comp != null) {
			SeparatorMouseListener l = new SeparatorMouseListener();
			addMouseListener(l);
			addMouseMotionListener(l);
		}
		
	}

	private class SeparatorMouseListener extends MouseInputAdapter {
		
		private boolean valueAdjusting = false;
		
		public SeparatorMouseListener() {
			super();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			valueAdjusting = true;
			((JComponent)e.getSource()).firePropertyChange("valueAdjusting", false, valueAdjusting);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			valueAdjusting = false;
			((JComponent)e.getSource()).firePropertyChange("valueAdjusting", true, valueAdjusting);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Dimension currentSize = comp.getSize();
			Dimension prefSize = comp.getPreferredSize();

			prefSize.height = currentSize.height + e.getY();
			if(prefSize.height < 0) prefSize.height = 0;
			
			comp.setPreferredSize(prefSize);
			comp.revalidate();
		}
		
	}
	
}
