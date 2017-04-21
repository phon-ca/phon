/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.ipa_lookup.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;

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
