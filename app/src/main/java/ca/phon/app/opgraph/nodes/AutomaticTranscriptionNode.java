package ca.phon.app.opgraph.nodes;

import ca.phon.app.ipalookup.*;
import ca.phon.app.session.editor.view.ipa_lookup.*;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipadictionary.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.util.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * OpNode for performing automatic transcription on a given {@link ca.phon.orthography.Orthography}
 *
 */
@OpNodeInfo(name="Automatic Transcription",  description = "Automatic transcription of orthography",
		category = "IPA Lookup", showInLibrary = true)
public class AutomaticTranscriptionNode extends OpNode implements NodeSettings {

	private InputField orthoInputField = new InputField("orthography", "Orthography for transcription",
			false, true, Orthography.class);

	private OutputField ipaOutputField = new OutputField("ipa", "Automatic transcription",
			true, IPATranscript.class);

	private AutoTranscriptionForm autoTranscriptionForm;

	private Language dictionaryLanguage;

	private boolean transcribeIPATarget;

	private boolean transcribeIPAActual;

	private boolean overwrite;

	private Syllabifier syllabifier;

	public AutomaticTranscriptionNode() {
		super();

		putField(orthoInputField);
		putField(ipaOutputField);

		this.dictionaryLanguage = IPADictionaryLibrary.getInstance().getDefaultLanguage();
		this.transcribeIPATarget = false;
		this.transcribeIPAActual = true;
		this.overwrite = false;
		this.syllabifier = SyllabifierLibrary.getInstance().defaultSyllabifier();

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext opContext) throws ProcessingException {

	}

	public Language getDictionaryLanguage() {
		return (this.autoTranscriptionForm != null ? this.autoTranscriptionForm.getDictionaryLanguage() : dictionaryLanguage);
	}

	public void setDictionaryLanguage(Language dictionaryLanguage) {
		this.dictionaryLanguage = dictionaryLanguage;
		if(this.autoTranscriptionForm != null)
			this.autoTranscriptionForm.setDictionaryLanguage(dictionaryLanguage);
	}

	public boolean isTranscribeIPATarget() {
		return (this.autoTranscriptionForm != null ? this.autoTranscriptionForm.isSetIPATarget() : transcribeIPATarget);
	}

	public void setTranscribeIPATarget(boolean transcribeIPATarget) {
		this.transcribeIPATarget = transcribeIPATarget;
		if(this.autoTranscriptionForm != null)
			this.autoTranscriptionForm.setSetIPATarget(transcribeIPATarget);
	}

	public boolean isTranscribeIPAActual() {
		return (this.autoTranscriptionForm != null ? this.autoTranscriptionForm.isSetIPAActual() : transcribeIPAActual);
	}

	public void setTranscribeIPAActual(boolean transcribeIPAActual) {
		this.transcribeIPAActual = transcribeIPAActual;
		if(this.autoTranscriptionForm != null)
			this.autoTranscriptionForm.setSetIPAActual(transcribeIPAActual);
	}

	public boolean isOverwrite() {
		return (this.autoTranscriptionForm != null ? this.autoTranscriptionForm.isOverwrite() : overwrite);
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
		if(this.autoTranscriptionForm != null)
			this.autoTranscriptionForm.setOverwrite(overwrite);
	}

	public Syllabifier getSyllabifier() {
		return (this.autoTranscriptionForm != null ? this.autoTranscriptionForm.getSyllabifier() : syllabifier);
	}

	public void setSyllabifier(Syllabifier syllabifier) {
		this.syllabifier = syllabifier;
		if(this.autoTranscriptionForm != null)
			this.autoTranscriptionForm.setSyllabifier(syllabifier);
	}

	@Override
	public Component getComponent(GraphDocument graphDocument) {
		if(autoTranscriptionForm == null) {
			autoTranscriptionForm = new AutoTranscriptionForm();
			autoTranscriptionForm.setDictionaryLanguage(this.dictionaryLanguage);
			autoTranscriptionForm.setSetIPATarget(this.transcribeIPATarget);
			autoTranscriptionForm.setSetIPAActual(this.transcribeIPAActual);
			autoTranscriptionForm.setOverwrite(this.overwrite);
			autoTranscriptionForm.setSyllabifier(this.syllabifier);
		}
		return autoTranscriptionForm;
	}

	@Override
	public Properties getSettings() {
		Properties settings = new Properties();
		settings.put("language", getDictionaryLanguage().toString());
		settings.put("transcribeIPATarget", isTranscribeIPATarget());
		settings.put("transcribeIPAActual", isTranscribeIPAActual());
		settings.put("overwrite", isOverwrite());
		settings.put("syllabifier", syllabifier.getLanguage().toString());
		return settings;
	}

	@Override
	public void loadSettings(Properties properties) {
		Language dictLang = Language.parseLanguage(properties.getProperty("language",
				IPADictionaryLibrary.getInstance().getDefaultLanguage().toString()));
		setDictionaryLanguage(dictLang);

		setTranscribeIPATarget(Boolean.parseBoolean(properties.getProperty("transcribeIPATarget", "false")));
		setTranscribeIPAActual(Boolean.parseBoolean(properties.getProperty("transcribeIPAActual", "true")));
		setOverwrite(Boolean.parseBoolean(properties.getProperty("overwrite", "false")));

		Language syllabifierLang = Language.parseLanguage(properties.getProperty("syllabifier",
				SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString()));
		Syllabifier syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(syllabifierLang);
		setSyllabifier(syllabifier);
	}
}
