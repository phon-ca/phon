/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
