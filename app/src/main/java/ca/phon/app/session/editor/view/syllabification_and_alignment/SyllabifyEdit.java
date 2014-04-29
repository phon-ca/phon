package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPAElement;
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
