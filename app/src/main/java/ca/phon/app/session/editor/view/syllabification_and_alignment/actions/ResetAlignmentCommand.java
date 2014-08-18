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
