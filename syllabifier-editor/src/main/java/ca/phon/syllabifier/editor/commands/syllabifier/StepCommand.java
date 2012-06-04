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
package ca.phon.plugins.opgraph.app.commands.syllabifier;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.ProcessingContext;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.GraphEditor;
import ca.gedge.opgraph.app.IconLibrary;
import ca.gedge.opgraph.app.IconLibrary.IconType;
import ca.phon.plugins.opgraph.app.SyllabifierEditor;

/**
 * A command that steps the processing context of the active editor. If the
 * active editor has no context, one is created.
 * 
 * @author Jason Gedge
 */
public class StepCommand extends AbstractAction {
	
	private final SyllabifierEditor editor;
	
	/**
	 * Constructs a step command.
	 */
	public StepCommand(SyllabifierEditor editor) {
		super("Step");
		
		this.editor = editor;
		
		final int CTRL = KeyEvent.CTRL_MASK;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, CTRL));
		putValue(SMALL_ICON, IconLibrary.getIcon(IconType.DEBUG_STEP, 16, 16));
	}
	
	//
	// AbstractAction
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null) {
			ProcessingContext context = document.getProcessingContext();
			if(context == null) {
				context = new ProcessingContext(document.getGraph());
				document.setProcessingContext(context);
				context.getContext().put("__ipa__", editor.getIPA());
			}
			
			if(context.hasNext()) {
				context.step();
				document.updateDebugState(context);
			}
			
			editor.getIPADisplay().repaint();
		}
	}

}
