package ca.phon.app.segmentation;

import javax.swing.JPanel;

import ca.phon.media.WaveformDisplay;
import ca.phon.project.Project;
import ca.phon.session.Session;

/**
 * Segmentation UI
 * 
 * The segmentation panel is divided into tiers stacked
 * vertically. The following tiers are available:
 * <ul>
 * 	<li>media player (if video is available)</li>
 *  <li>full audio waveform with ability to select zoomed waveform window size and location of loaded media</li>
 *  <li>zoomed audio waveform displaying the currently selected window</li>
 *  <li>a tier for each speaker showing their segments on the media timeline</li>
 * </ul>
 */
public class SegmentationPanel extends JPanel {

	private static final long serialVersionUID = -304225052574120216L;

	private SegmentationModel model;
	
	private WaveformDisplay fullWaveformDisplay;
	
	public SegmentationPanel(Project project, Session session) {
		this(new SegmentationModel(project, session));
	}
	
	public SegmentationPanel(SegmentationModel model) {
		super();
		
		this.model = model;
		
		init();
	}
	
	private void init() {
		
	}
	
	public SegmentationModel getModel() {
		return this.model;
	}
	
	public void setSegmentationModel(SegmentationModel model) {
		SegmentationModel oldModel = this.model;
		this.model = model;
		firePropertyChange("segmentationModel", oldModel, model);
	}
	
}
