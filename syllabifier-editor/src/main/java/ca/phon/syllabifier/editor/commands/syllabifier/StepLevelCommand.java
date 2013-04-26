/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph Framework.
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
package ca.phon.syllabifier.editor.commands.syllabifier;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.IconLibrary;
import ca.gedge.opgraph.app.IconLibrary.IconType;

/**
 * A command that steps the processing context of the active editor. If the
 * active editor has no context, one is created.
 * 
 * @author Jason Gedge
 */
public class StepLevelCommand extends AbstractAction {
	/**
	 * Constructs a step command.
	 */
	public StepLevelCommand() {
		super("Step To Next Level");
		
		final int CTRL = KeyEvent.CTRL_MASK;
		final int SHIFT = InputEvent.SHIFT_MASK;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, CTRL + SHIFT));
		putValue(SMALL_ICON, IconLibrary.getIcon(IconType.DEBUG_STEP_LEVEL, 16, 16));
	}
	
	//
	// AbstractAction
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null) {
			Processor context = document.getProcessingContext();
			if(context == null) {
				context = new Processor(document.getGraph());
				document.setProcessingContext(context);
			}
			
			if(context.hasNext()) {
				context.stepToNextLevel();
				document.updateDebugState(context);
			}
		}
	}

}
