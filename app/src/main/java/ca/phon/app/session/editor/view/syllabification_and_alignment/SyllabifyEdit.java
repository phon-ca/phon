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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllable.StripSyllabifcationVisitor;
import ca.phon.syllable.SyllabificationInfo;

public class SyllabifyEdit extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = 4846772248441893228L;

	private final static Logger LOGGER = Logger
			.getLogger(SyllabifyEdit.class.getName());

	private final Tier<IPATranscript> tier;
	
	private final Syllabifier syllabifier;
	
	private final int groupIndex;
	
	private String oldVal = null;
	
	public SyllabifyEdit(SessionEditor editor, Tier<IPATranscript> ipaTier, int groupIndex, Syllabifier syllabifier) {
		super(editor);
		this.tier = ipaTier;
		this.groupIndex = groupIndex;
		this.syllabifier = syllabifier;
	}
	
	@Override
	public void undo() {
		if(oldVal == null) return;
		try {
			final IPATranscript oldTranscript = IPATranscript.parseIPATranscript(oldVal);
			final IPATranscript grp = tier.getGroup(groupIndex);
			
			if(oldTranscript.length() != grp.length()) return;
			for(int j = 0; j < oldTranscript.length(); j++) {
				final SyllabificationInfo oldInfo = oldTranscript.elementAt(j).getExtension(SyllabificationInfo.class);
				grp.elementAt(j).putExtension(SyllabificationInfo.class, oldInfo);
			}
			
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getEditor().getUndoSupport(), grp);
			getEditor().getEventManager().queueEvent(ee);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void doIt() {
		final IPATranscript ipa = tier.getGroup(groupIndex);
		oldVal = ipa.toString(true);
		
		final StripSyllabifcationVisitor visitor = new StripSyllabifcationVisitor();
		ipa.accept(visitor);
		
		syllabifier.syllabify(ipa.toList());
		
		final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getSource(), ipa);
		getEditor().getEventManager().queueEvent(ee);
	}
	
}
