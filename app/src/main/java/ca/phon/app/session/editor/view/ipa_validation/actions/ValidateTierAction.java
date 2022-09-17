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
package ca.phon.app.session.editor.view.ipa_validation.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.ipa_validation.ValidationEditorView;
import ca.phon.ipa.*;
import ca.phon.session.*;

import javax.swing.undo.CompoundEdit;
import java.awt.event.ActionEvent;

public class ValidateTierAction extends ValidationAction {

	private static final long serialVersionUID = 8542450495447309480L;

	private final Tier<IPATranscript> tier;
	
	private final Transcriber transcriber;
	
	public ValidateTierAction(SessionEditor editor, ValidationEditorView view,
			Tier<IPATranscript> tier, Transcriber transcriber) {
		super(editor, view);
		this.tier = tier;
		this.transcriber = transcriber;
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final CompoundEdit edit = new CompoundEdit();
		
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			final IPATranscript grp = tier.getGroup(i);
			final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
			if(alts != null && alts.get(transcriber.getUsername()) != null) {
				final IPATranscript ipa = alts.get(transcriber.getUsername());
				alts.setSelected(transcriber.getUsername());
				ipa.putExtension(AlternativeTranscript.class, alts);
				
				final TierEdit<IPATranscript> tierEdit = new TierEdit<IPATranscript>(getEditor(), tier, i, ipa);
				tierEdit.doIt();
				edit.addEdit(tierEdit);
			}
		}
		
		edit.end();
		getEditor().getUndoSupport().postEdit(edit);
	}

}
