package ca.phon.app.session.editor.view.ipa_validation.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.ipa_validation.ValidationEditorView;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;

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
