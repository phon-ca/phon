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
import ca.phon.util.PrefHelper;

public class SpeechAnalysisWaveformTier extends SpeechAnalysisTier {
	
	private final static String WAV_DISPLAY_HEIGHT = "SpeechAnalysisView.wavDisplayHeight";
	private final int DEFAULT_WAV_DISPLAY_HEIGHT = 100;
	private int wavDisplayHeight =
			PrefHelper.getInt(WAV_DISPLAY_HEIGHT, DEFAULT_WAV_DISPLAY_HEIGHT);
	
	private WaveformDisplay wavDisplay;
	
	public SpeechAnalysisWaveformTier(SpeechAnalysisEditorView parentView) {
		super(parentView);
		
		init();
		setupActionMap();
	}
	
	private void init() {
		wavDisplay = new WaveformDisplay(getTimeModel());
		wavDisplay.setPreferredChannelHeight(50);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setFocusable(true);
		wavDisplay.setBackground(Color.white);
		wavDisplay.setOpaque(true);
		wavDisplay.setFont(FontPreferences.getMonospaceFont());
		
		wavDisplay.addMouseListener(getParentView().getContextMenuAdapter());
		wavDisplay.addMouseListener(getParentView().getCursorAndSelectionAdapter());
		wavDisplay.addMouseMotionListener(getParentView().getCursorAndSelectionAdapter());
		
		setLayout(new BorderLayout());
		add(wavDisplay, BorderLayout.CENTER);
	
		final JSeparator separator =  new SpeechAnalysisTierDivider(wavDisplay);
		add(separator, BorderLayout.SOUTH);
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
	}
	
}
