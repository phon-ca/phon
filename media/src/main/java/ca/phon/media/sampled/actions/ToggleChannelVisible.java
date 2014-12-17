package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.Channel;
import ca.phon.media.sampled.PCMSegmentView;

public class ToggleChannelVisible extends PCMSegmentViewAction {

	private static final long serialVersionUID = -8010097494927486275L;

	private final Channel channel;

	
	public ToggleChannelVisible(PCMSegmentView view, Channel channel) {
		super(view);
		this.channel = channel;
	
		final boolean isVisible = view.isChannelVisible(channel);
		putValue(NAME, "Show " + channel.getName() + " channel");
		putValue(SHORT_DESCRIPTION, "Toggle " + channel.getName() + " channel visibility");
		putValue(SELECTED_KEY, isVisible);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final boolean newState = !getView().isChannelVisible(channel);
		getView().setChannelVisible(channel, newState);
	}

}
