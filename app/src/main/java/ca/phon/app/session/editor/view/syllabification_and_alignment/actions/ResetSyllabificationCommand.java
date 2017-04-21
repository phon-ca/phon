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
package ca.phon.app.session.editor.view.syllabification_and_alignment.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationAlignmentEditorView;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabifyEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;

public class ResetSyllabificationCommand extends SyllabificationAlignmentCommand {
	
	private static final long serialVersionUID = 5422818175227127233L;
	
	private final String ipaTier;

	public ResetSyllabificationCommand(SessionEditor editor,
			SyllabificationAlignmentEditorView view, String tier) {
		super(editor, view);
		this.ipaTier = tier;
		
		putValue(NAME, "Syllabify " + ipaTier);
		putValue(SHORT_DESCRIPTION, "Syllabify " + ipaTier);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		final Record r = getEditor().currentRecord();
		
		final Tier<IPATranscript> tier = 
				r.getTier(ipaTier, IPATranscript.class);
		if(tier == null) return;
		
		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		Language syllabifierLanguage = info.getSyllabifierLanguageForTier(tier.getName());
		if(syllabifierLanguage == null)
			syllabifierLanguage = library.defaultSyllabifierLanguage();
		final Syllabifier syllabifier = library.getSyllabifierForLanguage(syllabifierLanguage);
		
		final CompoundEdit edit = new CompoundEdit();
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			final SyllabifyEdit ed = new SyllabifyEdit(getEditor(), tier, i, syllabifier);
			ed.doIt();
			edit.addEdit(ed);
		}
		edit.end();
		
		getEditor().getUndoSupport().postEdit(edit);
	}

}
