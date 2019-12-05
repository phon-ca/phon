package ca.phon.app.session.editor.view.speech_analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import ca.phon.app.media.WaveformDisplay;
import ca.phon.ui.fonts.FontPreferences;

public class SpeechAnalysisWaveformTier extends SpeechAnalysisTier {
	
	private WaveformDisplay wavDisplay;
	
	public SpeechAnalysisWaveformTier(SpeechAnalysisEditorView parentView) {
		super(parentView);
		
		init();
	}
	
	private void init() {
		wavDisplay = new WaveformDisplay(getTimeModel());
		wavDisplay.setPreferredChannelHeight(50);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setFocusable(true);
		wavDisplay.setBackground(Color.white);
		wavDisplay.setOpaque(true);
		wavDisplay.setFont(FontPreferences.getMonospaceFont());
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(wavDisplay, BorderLayout.CENTER);
	
		final JSeparator separator =  new JSeparator(SwingConstants.HORIZONTAL);
		separator.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		
		SeparatorMouseListener listener = new SeparatorMouseListener();
		separator.addMouseMotionListener(listener);
		separator.addMouseListener(listener);
		
		getContentPane().add(separator, BorderLayout.SOUTH);
	}
	
	private void setupEditorAction() {
		
	}
	
	private void setupActionMap() {
		final InputMap inputMap = new InputMap();
		final ActionMap actionMap = wavDisplay.getActionMap();
		
		
		
		wavDisplay.setActionMap(actionMap);
		wavDisplay.setInputMap(WHEN_FOCUSED, inputMap);
	}
	
	public WaveformDisplay getWaveformDisplay() {
		return this.wavDisplay;
	}

	@Override
	public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
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
			Dimension currentSize = getSize();
			Dimension prefSize = getPreferredSize();

			prefSize.height = currentSize.height + e.getY();
			if(prefSize.height < 0) prefSize.height = 0;
			
			setPreferredSize(prefSize);
			revalidate();
		}
		
	}

}
