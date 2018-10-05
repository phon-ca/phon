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
