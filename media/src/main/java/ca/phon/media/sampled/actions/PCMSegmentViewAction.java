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

import javax.swing.AbstractAction;

import ca.phon.media.sampled.PCMSegmentView;

public abstract class PCMSegmentViewAction extends AbstractAction {

	private static final long serialVersionUID = -1601105535567999442L;

	private final PCMSegmentView view;
	
	public PCMSegmentViewAction(PCMSegmentView view) {
		super();
		this.view = view;
	}

	public PCMSegmentView getView() {
		return this.view;
	}
	
}
