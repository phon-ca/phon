package ca.phon.app.session.editor.view.syllabification_and_alignment.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationAlignmentEditorView;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabifyEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.impl.CompoundDictionary;
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
	public void actionPerformed(ActionEvent e) {
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
