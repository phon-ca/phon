package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.plugin.*;
import ca.phon.session.Tier;

import java.util.List;

/**
 * A tier edit which originates from an automatic transcription.
 * Additional rules/transformations may be executed on the returned
 * transcription before the edito occurs.
 *
 */
public class IPALookupEdit extends TierEdit<IPATranscript> {

	/** The dictionary used */
	private final IPADictionary dictionary;

	/** Orthography used for lookup */
	private final String orthography;

	/** Automatic transcription including transformations */
	private IPATranscript automaticTranscription;

	/**
	 * Constructor
	 *
	 * @param editor
	 * @param dictionary
	 * @param tier
	 * @param groupIndex
	 * @param newValue
	 */
	public IPALookupEdit(SessionEditor editor, IPADictionary dictionary, String orthography,
	                     Tier<IPATranscript> tier, int groupIndex, IPATranscript newValue) {
		super(editor, tier, groupIndex, newValue);

		this.dictionary = dictionary;
		this.orthography = orthography;
	}

	public String getOrthography() {
		return this.orthography;
	}

	public IPADictionary getDictionary() {
		return this.dictionary;
	}

	@Override
	public IPATranscript getNewValue() {
		if(this.automaticTranscription == null) {
			automaticTranscription = super.getNewValue();

			List<IPluginExtensionPoint<IPALookupPostProcessor>> extPts =
					PluginManager.getInstance().getExtensionPoints(IPALookupPostProcessor.class);
			for(var extPt:extPts) {
				IPALookupPostProcessor postprocessor = extPt.getFactory().createObject();
				automaticTranscription = postprocessor.postProcess(getDictionary(), getOrthography(), automaticTranscription);
			}
		}
		return this.automaticTranscription;
	}

}
