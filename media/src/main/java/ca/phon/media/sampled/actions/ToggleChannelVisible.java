/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
