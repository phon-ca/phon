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
package ca.phon.session.check;

import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.Rank;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.util.PrefHelper;

import java.util.Properties;

@PhonPlugin(name = "Check Phone Alignments", comments = "Check phone alignments")
@Rank(2)
public class CheckAlignment implements SessionCheck, IPluginExtensionPoint<SessionCheck> {

    public final static String RESET_ALIGNMENT = CheckAlignment.class.getName() + ".resetAlignment";
    public final static boolean DEFAULT_RESET_ALIGNMENT = false;
    private boolean resetAlignment = PrefHelper.getBoolean(RESET_ALIGNMENT, DEFAULT_RESET_ALIGNMENT);

    public CheckAlignment() {
        super();
    }

    @Override
    public Class<?> getExtensionType() {
        return SessionCheck.class;
    }

    @Override
    public IPluginExtensionFactory<SessionCheck> getFactory() {
        return (Object... args) -> this;
    }

    @Override
    public boolean performCheckByDefault() {
        return true;
    }

    @Override
    public boolean checkSession(SessionValidator validator, Session session) {
        boolean modified = false;

        for (int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
            final Record r = session.getRecord(rIdx);

            final IPATranscript ipaT = r.getIPATarget();
            final IPATranscript ipaA = r.getIPAActual();
            int maxWords = Math.max(ipaT.words().size(), ipaA.words().size());
            final PhoneAlignment alignmentTier = r.getPhoneAlignment();
            if (alignmentTier.getAlignments().size() != maxWords) {
                // alignment tier does not match number of words
                ValidationEvent evt = new ValidationEvent(session, session.getRecordElementIndex(rIdx), SystemTierType.PhoneAlignment.getName(),
                        "Alignments in tier do not match number of words", new ResetAlignmentQuickFix());
                validator.fireValidationEvent(evt);
            }
            int audiblePhonesT = ipaT.audiblePhones().length();
            int audiblePhonesA = ipaA.audiblePhones().length();
            if (audiblePhonesT != alignmentTier.getFullAlignment().getTopElements().length) {
                // alignment tier does not match number of audible phones in target
                ValidationEvent evt = new ValidationEvent(session, session.getRecordElementIndex(rIdx), SystemTierType.PhoneAlignment.getName(),
                        "Target alignment does not match number of audible phones", new ResetAlignmentQuickFix());
                validator.fireValidationEvent(evt);
            }
            if (audiblePhonesA != alignmentTier.getFullAlignment().getBottomElements().length) {
                // alignment tier does not match number of audible phones in actual
                ValidationEvent evt = new ValidationEvent(session, session.getRecordElementIndex(rIdx), SystemTierType.PhoneAlignment.getName(),
                        "Actual alignment does not match number of audible phones", new ResetAlignmentQuickFix());
                validator.fireValidationEvent(evt);
            }

            if (isResetAlignment()) {
                PhoneAlignment newAlignment = PhoneAlignment.fromTiers(r.getIPATargetTier(), r.getIPAActualTier());
                r.setPhoneAlignment(newAlignment);
                modified = true;
                ValidationEvent evt = new ValidationEvent(ValidationEvent.Severity.INFO, session, session.getRecordElementIndex(rIdx), SystemTierType.PhoneAlignment.getName(),
                        "Alignment was reset for record #" + (rIdx + 1));
                validator.fireValidationEvent(evt);
            }
        }

        return modified;
    }

    public boolean isResetAlignment() {
        return resetAlignment;
    }

    public void setResetAlignment(boolean resetAlignment) {
        this.resetAlignment = resetAlignment;
    }

    @Override
    public boolean checkTranscriptElement(SessionValidator validator, Session session, int elementIndex) {
        return false;
    }

    @Override
    public Properties getProperties() {
        Properties retVal = new Properties();

        retVal.put(RESET_ALIGNMENT, isResetAlignment());

        return retVal;
    }

    @Override
    public void loadProperties(Properties props) {
        setResetAlignment(Boolean.parseBoolean(props.getProperty(RESET_ALIGNMENT, Boolean.toString(DEFAULT_RESET_ALIGNMENT))));
    }

    public static class ResetAlignmentQuickFix extends SessionQuickFix {

        public ResetAlignmentQuickFix() {
            super();
        }

        @Override
        public String getDescription() {
            return "Reset alignment";
        }

        @Override
        public boolean fix(ValidationEvent evt) {
            final Transcript.Element ele = evt.getSession().getTranscript().getElementAt(evt.getElementIndex());
            if (!ele.isRecord()) return false;
            final Record r = ele.asRecord();
            PhoneAlignment newAlignment = PhoneAlignment.fromTiers(r.getIPATargetTier(), r.getIPAActualTier());
            r.setPhoneAlignment(newAlignment);
            return true;
        }

    }

}
