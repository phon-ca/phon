/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor.view.common;

import java.lang.ref.WeakReference;
import java.text.ParseException;

import javax.swing.JToolTip;

import org.apache.logging.log4j.LogManager;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.syllabifier.Syllabifier;

/**
 * Editor for IPA Transcriptions (validated and blind.)
 */
public class IPAGroupField extends GroupField<IPATranscript> {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(IPAGroupField.class.getName());

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
		_init();
	}
	
	public Transcriber getTranscriber() {
		return transcriberRef.get();
	}
	
	@Override
	protected void _init() {
		if(transcriberRef == null) return;
		super._init();
		addTierEditorListener(new TierEditorListener() {
			
			@Override
			public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
					T oldValue) {
				if(syllabifier != null) {
					final IPATranscript transcript = (IPATranscript)newValue;
					if(transcript != null && syllabifier != null)
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
		if(retVal != null && transcriber != null) {
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
		final IPATranscript groupVal = 
				(super.getGroupValue() != null ? super.getGroupValue() : new IPATranscript());
		if(object == null) object = new IPATranscript();
		
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
	
	@Override
	public JToolTip createToolTip() {
		JToolTip retVal = super.createToolTip();
		retVal.setLayout(new VerticalLayout());
		
		return retVal;
	}
	
	@Override
	protected void update() {
		if(syllabifier != null) {
			final IPATranscript validatedObj = getValidatedObject();
			IPATranscript ipa = validatedObj;
			if(validatedObj != null) {
				if(getTranscriber() != null) {
					final AlternativeTranscript alts = validatedObj.getExtension(AlternativeTranscript.class);
					ipa = alts.get(getTranscriber().getUsername());
				}
				syllabifier.syllabify(ipa.toList());
			}
		}
		super.update();
	}

	@Override
	protected boolean validateText() {
		removeAllErrorHighlights();
		
		boolean wasShowingErr = ((GroupFieldBorder)getBorder()).isShowWarningIcon();
		try {
			IPATranscript validatedIPA = IPATranscript.parseIPATranscript(getText());
			setValidatedObject(validatedIPA);
			((GroupFieldBorder)getBorder()).setShowWarningIcon(false);
			setToolTipText(null);
			if(wasShowingErr) repaint();
		} catch (final ParseException e) {
			IPATranscript validatedIPA = new IPATranscript();
			validatedIPA.putExtension(UnvalidatedValue.class, new UnvalidatedValue(getText().trim(), e));
			((GroupFieldBorder)getBorder()).setShowWarningIcon(true);
			
			final StringBuilder sb = new StringBuilder();
			sb.append("Error at character ").append(e.getErrorOffset()).append(": ").append(e.getLocalizedMessage());
			setToolTipText(sb.toString());
			
			addErrorHighlight(Math.max(0, Math.min(e.getErrorOffset(), getText().length())), 
					Math.min(e.getErrorOffset()+1, getText().length()));
			setValidatedObject(validatedIPA);
			if(!wasShowingErr) repaint();
		}
		return true;
	}
	
}
