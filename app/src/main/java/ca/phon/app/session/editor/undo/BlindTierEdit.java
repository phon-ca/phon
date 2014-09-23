package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;

public class BlindTierEdit extends TierEdit<IPATranscript> {

	private static final long serialVersionUID = 3332539046795927372L;
	
	private final Transcriber transcriber;
	
	private final IPATranscript ipa;
	
	private final IPATranscript oldValue;
	
	private final IPATranscript newValue;

	public BlindTierEdit(SessionEditor editor, Tier<IPATranscript> tier,
			int groupIndex, Transcriber transcriber, IPATranscript newValue, IPATranscript ipa) {
		super(editor, tier, groupIndex, ipa);
		this.ipa = ipa;
		this.transcriber = transcriber;
		this.newValue = newValue;
		
		final AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		if(alts != null) {
			oldValue = alts.get(transcriber.getUsername());
		} else {
			oldValue = null;
		}
	}
	
	@Override
	public String getUndoPresentationName() {
		return super.getUndoPresentationName() + " (" + transcriber.getUsername() + ")";
	}
	
	@Override
	public String getRedoPresentationName() {
		return super.getRedoPresentationName() + " (" + transcriber.getUsername() + ")";
	}
	
	@Override
	public void doIt() {
		AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		if(alts == null) {
			alts = new AlternativeTranscript();
			ipa.putExtension(AlternativeTranscript.class, alts);
		}
		alts.put(transcriber.getUsername(), newValue);
		super.doIt();
	}
	
	@Override
	public void undo() {
		AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		if(alts == null) {
			alts = new AlternativeTranscript();
			ipa.putExtension(AlternativeTranscript.class, alts);
		}
		alts.put(transcriber.getUsername(), oldValue);
		super.undo();
	}
	
}
