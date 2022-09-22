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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllable.*;
import org.apache.logging.log4j.LogManager;

import java.text.ParseException;

public class SyllabifyEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 4846772248441893228L;

	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SyllabifyEdit.class.getName());

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

			final EditorEvent<SyllabificationAlignmentEditorView.ScEditData> ee =
					new EditorEvent<>(SyllabificationAlignmentEditorView.ScEdit, getEditor(),
							new SyllabificationAlignmentEditorView.ScEditData(grp, -1, SyllableConstituentType.UNKNOWN, SyllableConstituentType.UNKNOWN));
			getEditor().getEventManager().queueEvent(ee);
		} catch (ParseException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void doIt() {
		final IPATranscript ipa = tier.getGroup(groupIndex);
		oldVal = ipa.toString(true);
		
		final StripSyllabifcationVisitor visitor = new StripSyllabifcationVisitor();
		ipa.accept(visitor);
		
		syllabifier.syllabify(ipa.toList());

		final EditorEvent<SyllabificationAlignmentEditorView.ScEditData> ee =
				new EditorEvent<>(SyllabificationAlignmentEditorView.ScEdit, getEditor(),
						new SyllabificationAlignmentEditorView.ScEditData(ipa, -1, SyllableConstituentType.UNKNOWN, SyllableConstituentType.UNKNOWN));
		getEditor().getEventManager().queueEvent(ee);
	}
	
}
