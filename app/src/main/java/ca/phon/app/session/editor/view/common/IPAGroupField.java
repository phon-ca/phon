package ca.phon.app.session.editor.view.common;

import java.lang.ref.WeakReference;

import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.syllabifier.Syllabifier;

/**
 * Editor for IPA Transcriptions (validated and blind.)
 */
public class IPAGroupField extends GroupField<IPATranscript> {

	private static final long serialVersionUID = 3938081453789426396L;
	
	private final Syllabifier syllabifier;
	
	private final WeakReference<Transcriber> transcriberRef;

	public IPAGroupField(Tier<IPATranscript> tier,
			int groupIndex) {
		this(tier, groupIndex, null);
	}
	
	public IPAGroupField(Tier<IPATranscript> tier, int groupIndex, Transcriber transcriber) {
		this(tier, groupIndex, transcriber, null);
	}
	
	public IPAGroupField(Tier<IPATranscript> tier, int groupIndex, Transcriber transcriber, Syllabifier syllabifier) {
		super(tier, groupIndex);
		this.syllabifier = syllabifier;
		this.transcriberRef = new WeakReference<Transcriber>(transcriber);
		// init after transcriber is set
		init();
	}

	public Transcriber getTranscriber() {
		return transcriberRef.get();
	}
	
	@Override
	protected void init() {
		if(transcriberRef == null) return;
		super.init();
		addTierEditorListener(new TierEditorListener() {
			
			@Override
			public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
					T oldValue) {
				if(syllabifier != null) {
					final IPATranscript transcript = (IPATranscript)newValue;
					syllabifier.syllabify(transcript.toList());
				}
			}
			
			@Override
			public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue,
					T oldValue) {
			}
			
		});
	}
	
	@Override
	public IPATranscript getGroupValue() {
		IPATranscript retVal = super.getGroupValue();
		final Transcriber transcriber = getTranscriber();
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
	protected void setValidatedObject(IPATranscript object) {
		final Transcriber transcriber = getTranscriber();
		final IPATranscript groupVal = super.getGroupValue();
		
		if(transcriber != null) {
			AlternativeTranscript alts = groupVal.getExtension(AlternativeTranscript.class);
			if(alts == null) {
				alts = new AlternativeTranscript();
				groupVal.putExtension(AlternativeTranscript.class, alts);
			}
			alts.put(transcriber.getUsername(), object);
			super.setValidatedObject(groupVal);
		} else {
			// HACK make sure to copy alternative transcriptions
			final AlternativeTranscript alts = groupVal.getExtension(AlternativeTranscript.class);
			if(alts != null) object.putExtension(AlternativeTranscript.class, alts);
			super.setValidatedObject(object);
		}
	}
	
}
