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
package ca.phon.syllabifier.editor.hooks;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.commands.CommandHook;
import ca.gedge.opgraph.app.commands.Hook;
import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.gedge.opgraph.app.commands.core.NewCommand;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.phon.syllabifier.editor.SyllabifierGraphEditorModel;
import ca.phon.syllabifier.opgraph.nodes.IPASourceNode;

/**
 * Ensures that syllabifier settings are reset and a new
 * {@link IPASourceNode} is added to the new graph.
 */
@Hook(command=NewCommand.class)
public class NewCommandHook implements CommandHook {
	
	private static final Logger LOGGER = Logger
			.getLogger(NewCommandHook.class.getName());

	@Override
	public boolean startCommand(HookableCommand command, ActionEvent evt) {
		return false;
	}

	@Override
	public void endCommand(HookableCommand command, ActionEvent evt) {
		final GraphEditorModel editorModel = GraphEditorModel.getActiveEditorModel();
		if(editorModel instanceof SyllabifierGraphEditorModel) {
			final SyllabifierGraphEditorModel syllabifierEditorModel = 
					(SyllabifierGraphEditorModel)editorModel;
			syllabifierEditorModel.getSettingsPanel().reset();
			
			final IPASourceNode ipaSourceNode = new IPASourceNode();
			final NodeMetadata ipaMeta = new NodeMetadata(10, 10);
			ipaSourceNode.putExtension(NodeMetadata.class, ipaMeta);
			syllabifierEditorModel.getDocument().getGraph().add(ipaSourceNode);
		} else {
			LOGGER.severe("Editor model is of incorrect type!");
		}
	}
	
}
