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
/**
 * 
 */
package ca.phon.syllabifier.editor.commands.syllabifier;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.phon.syllabifier.editor.SyllabifierEditor;

/**
 * Menu provider for core functions.
 * 
 * @author Jason Gedge
 */
public class SyllabifierMenuProvider implements MenuProvider {
	
	private final SyllabifierEditor editor;
	
	public SyllabifierMenuProvider(SyllabifierEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public void installItems(final GraphEditorModel model, PathAddressableMenu menu) {
		menu.addMenu("syllabifier", "Syllabifier");
		
		menu.addMenuItem("syllabifier/run", new RunCommand(editor));
		final JMenuItem stop = menu.addMenuItem("syllabifier/stop", new StopCommand());
		menu.addSeparator("syllabifier");
		menu.addMenuItem("syllabifier/step", new StepCommand(editor));
		menu.addMenuItem("syllabifier/step level", new StepLevelCommand());
		menu.addMenuItem("syllabifier/step into", new StepIntoCommand());
		menu.addMenuItem("syllabifier/step out of", new StepOutOfCommand());

		stop.setEnabled(false);
		
		model.getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				stop.setEnabled(evt.getNewValue() != null);
			}
		});
	}

	@Override
	public void installPopupItems(Object context, MouseEvent me, GraphEditorModel model, PathAddressableMenu menu) {
		//
	}
}
