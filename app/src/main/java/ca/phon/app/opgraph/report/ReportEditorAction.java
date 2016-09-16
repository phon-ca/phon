/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.OpgraphEditorEP;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;

/**
 * Open the node editor with a new Report model.
 *
 */
public class ReportEditorAction extends HookableAction {
	
	private static final long serialVersionUID = -3297019664686256298L;

	private final static Logger LOGGER = Logger.getLogger(ReportEditorAction.class.getName());

	private final static String TXT = "Node editor...";
	
	private final static String DESC = "Open node editor to create new report";
	
	public ReportEditorAction() {
		super();
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ReportOpGraphEditorModel model = new ReportOpGraphEditorModel();
		final Map<String, Object> args = new HashMap<>();
		args.put(OpgraphEditorEP.OPGRAPH_MODEL_KEY, model);
		try {
			PluginEntryPointRunner.executePlugin(OpgraphEditorEP.EP_NAME, args);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
