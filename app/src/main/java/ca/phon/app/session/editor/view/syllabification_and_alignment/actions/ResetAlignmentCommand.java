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

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationAlignmentEditorView;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;

public class ResetAlignmentCommand extends SyllabificationAlignmentCommand {

	private static final long serialVersionUID = 8113102199705193951L;
	
	private final static String TXT = "Reset alignment";
			
	private final static String DESC = "Reset alignment";

	public ResetAlignmentCommand(SessionEditor editor,
			SyllabificationAlignmentEditorView view) {
		super(editor, view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final Record r = getEditor().currentRecord();
		final Tier<PhoneMap> alignmentTier = r.getPhoneAlignment();
		
		final CompoundEdit edit = new CompoundEdit();
		final PhoneAligner aligner = new PhoneAligner();
		
		for(int i = 0; i < r.numberOfGroups(); i++) {
			final Group group = r.getGroup(i);
			final PhoneMap newPm = aligner.calculatePhoneMap(group.getIPATarget(), group.getIPAActual());
			
			final TierEdit<PhoneMap> ed = new TierEdit<PhoneMap>(getEditor(), alignmentTier, i, newPm);
			ed.doIt();
			edit.addEdit(ed);
			
		}
		final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, getView(), SystemTierType.SyllableAlignment.getName());
		getEditor().getEventManager().queueEvent(ee);
		
		edit.end();
		
		getEditor().getUndoSupport().postEdit(edit);
	}

}
