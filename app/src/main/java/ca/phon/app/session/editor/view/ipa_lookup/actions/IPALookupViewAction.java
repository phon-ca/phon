/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.ipa_lookup.actions;

import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;

import javax.swing.*;

/**
 * Base class for lookup view actions.
 */
public abstract class IPALookupViewAction extends AbstractAction {

	private static final long serialVersionUID = 7551272812334000000L;

	private final IPALookupView lookupView;
	
	public IPALookupViewAction(IPALookupView view) {
		super();
		this.lookupView = view;
	}
	
	public IPALookupView getLookupView() {
		return this.lookupView;
	}
	
}
