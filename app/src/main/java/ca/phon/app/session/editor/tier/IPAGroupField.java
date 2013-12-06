package ca.phon.app.session.editor.tier;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;

public class IPAGroupField extends GroupField<IPATranscript> {

	private static final long serialVersionUID = 3938081453789426396L;

	public IPAGroupField(SessionEditor editor, Tier<IPATranscript> tier,
			int groupIndex) {
		super(editor, tier, groupIndex);
	}

	@Override
	public IPATranscript getGroupValue() {
		IPATranscript retVal = super.getGroupValue();
		final Transcriber transcriber = getEditor().getDataModel().getTranscriber();
		if(transcriber != null) {
			final AlternativeTranscript alts = retVal.getExtension(AlternativeTranscript.class);
			if(alts != null) {
				final IPATranscript t = alts.get(transcriber.getUsername());
				retVal = (t == null ? new IPATranscript() : t);
			} else {
				retVal = new IPATranscript();
			}
		}
		return retVal;
	}

	@Override
	public void setGroupValue(IPATranscript val) {
		final Transcriber transcriber = getEditor().getDataModel().getTranscriber();
		if(transcriber != null) {
			final IPATranscript groupVal = super.getGroupValue();
			AlternativeTranscript alts = groupVal.getExtension(AlternativeTranscript.class);
			if(alts == null) {
				alts = new AlternativeTranscript();
				groupVal.putExtension(AlternativeTranscript.class, alts);
			}
			alts.put(transcriber.getUsername(), val);
		} else {
			super.setGroupValue(val);
		}
	}
	
}
