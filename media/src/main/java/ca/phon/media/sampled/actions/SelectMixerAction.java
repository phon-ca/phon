package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import javax.sound.sampled.Mixer.Info;

import ca.phon.media.sampled.PCMSegmentView;

public class SelectMixerAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = 8880954932456755853L;
	
	private Info mixerInfo;

	public SelectMixerAction(PCMSegmentView view, Info mixerInfo) {
		super(view);
		
		putValue(NAME, mixerInfo.getName());
		this.mixerInfo = mixerInfo;
	}
	
	public Info getMixerInfo() {
		return this.mixerInfo;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getView().setMixerInfo(getMixerInfo());
	}

}
