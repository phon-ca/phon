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
package ca.phon.app.session.editor.view.syllabification_and_alignment.actions;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationAlignmentEditorView;
import ca.phon.ipa.alignment.*;
import ca.phon.session.Record;
import ca.phon.session.*;

import java.awt.event.ActionEvent;

public class ResetAlignmentCommand extends SyllabificationAlignmentCommand {

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
		
		final PhoneAligner aligner = new PhoneAligner();

		getEditor().getUndoSupport().beginUpdate();
		for(int i = 0; i < r.numberOfGroups(); i++) {
			final Group group = r.getGroup(i);
			final PhoneMap newPm = aligner.calculatePhoneAlignment(group.getIPATarget(), group.getIPAActual());
			
			final TierEdit<PhoneMap> ed = new TierEdit<>(getEditor(), alignmentTier, i, newPm);
			ed.setFireHardChangeOnUndo(i == r.numberOfGroups() - 1);
			getEditor().getUndoSupport().postEdit(ed);
		}
		getEditor().getUndoSupport().endUpdate();
	}

}
