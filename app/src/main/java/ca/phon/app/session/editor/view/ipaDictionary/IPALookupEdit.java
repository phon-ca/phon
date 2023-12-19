package ca.phon.app.session.editor.view.ipaDictionary;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.plugin.*;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;

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
	 * @param newValue
	 */
	public IPALookupEdit(SessionEditor editor, IPADictionary dictionary, String orthography,
	                     Tier<IPATranscript> tier, IPATranscript newValue) {
		super(editor, tier, newValue);

		this.dictionary = dictionary;
		this.orthography = orthography;
	}


	public IPALookupEdit(Session session, EditorEventManager editorEventManager, Transcriber transcriber,
						 Record record, IPADictionary dictionary, String orthography,
						 Tier<IPATranscript> tier, IPATranscript newValue, boolean valueAdjusting) {
		super(session, editorEventManager, transcriber, record, tier, newValue, valueAdjusting);
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
