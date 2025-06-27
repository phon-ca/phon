/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.syllabificationAlignment;

import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.SyllabifierSelector;
import ca.phon.util.Language;

import javax.swing.*;
import java.awt.*;

public class SyllabificationSettingsPanel extends JPanel {

	public final static String IPA_TARGET_SYLLABIFIER_PROP = "_ipa_target_syllabifier_";

	public final static String IPA_ACTUAL_SYLLABIFIER_PROP = "_ipa_actual_syllabifier_";

	private final Session session;

	private SyllabifierSelector ipaTargetSelector;
	private SyllabifierSelector ipaActualSelector;

	public SyllabificationSettingsPanel(Session session) {
		super();
		this.session = session;
		
		init();
	}

	private void init() {
		final Syllabifier ipaTargetSyllabifier = SyllabifierOptions.findSyllabifier(session, null, SystemTierType.IPATarget.getName());
		final String ipaTargetLang = ipaTargetSyllabifier != null ? ipaTargetSyllabifier.getLanguage().toString() : SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString();
		final Syllabifier ipaActualSyllabifier = SyllabifierOptions.findSyllabifier(session, null, SystemTierType.IPAActual.getName());
		final String ipaActualLang = ipaActualSyllabifier != null ? ipaActualSyllabifier.getLanguage().toString() : SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString();

		ipaTargetSelector = new SyllabifierSelector();
		ipaTargetSelector.setSelectedLanguage(Language.parseLanguage(ipaTargetLang));
		ipaTargetSelector.addListSelectionListener( (e) -> {
			String currentSyllabifier = SyllabifierOptions.getSyllabifierForTier(session, SystemTierType.IPATarget.getName());
			SyllabifierOptions.setSyllabifierForTier(session, SystemTierType.IPATarget.getName(), ipaTargetSelector.getSelectedSyllabifier().getLanguage().toString());
			firePropertyChange(IPA_ACTUAL_SYLLABIFIER_PROP, currentSyllabifier != null ? Language.parseLanguage(currentSyllabifier) : null, ipaTargetSelector.getSelectedSyllabifier().getLanguage() );
		});

		ipaActualSelector = new SyllabifierSelector();
		ipaActualSelector.setSelectedLanguage(Language.parseLanguage(ipaActualLang));
		ipaActualSelector.addListSelectionListener( (e) -> {
			String currentSyllabifier = SyllabifierOptions.getSyllabifierForTier(session, SystemTierType.IPAActual.getName());
			SyllabifierOptions.setSyllabifierForTier(session, SystemTierType.IPAActual.getName(), ipaActualSelector.getSelectedSyllabifier().getLanguage().toString());
			firePropertyChange(IPA_ACTUAL_SYLLABIFIER_PROP, currentSyllabifier != null ? Language.parseLanguage(currentSyllabifier) : null, ipaActualSelector.getSelectedSyllabifier().getLanguage() );
		});

		SwingUtilities.invokeLater( () -> {
			ipaTargetSelector.ensureIndexIsVisible(ipaTargetSelector.getSelectedIndex());
			ipaActualSelector.ensureIndexIsVisible(ipaActualSelector.getSelectedIndex());
		});

		JScrollPane ipaTargetScroller = new JScrollPane(ipaTargetSelector);
		ipaTargetScroller.setBorder(BorderFactory.createTitledBorder("IPA Target Syllabifier"));

		JScrollPane ipaActualScroller = new JScrollPane(ipaActualSelector);
		ipaActualScroller.setBorder(BorderFactory.createTitledBorder("IPA Actual Syllabifier"));

		setLayout(new GridLayout(2, 1));
		add(ipaTargetScroller);
		add(ipaActualScroller);
	}

	public Language getSelectedTargetSyllabifier() {
		final Syllabifier syllabifier = ipaTargetSelector.getSelectedSyllabifier();
		if(syllabifier != null) {
			return syllabifier.getLanguage();
		}
		return null;
	}
	
	public Language getSelectedActualSyllabifier() {
		final Syllabifier syllabifier = ipaActualSelector.getSelectedSyllabifier();
		if(syllabifier != null) {
			return syllabifier.getLanguage();
		}
		return null;
	}

}

