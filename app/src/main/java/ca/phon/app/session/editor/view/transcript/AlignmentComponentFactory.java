package ca.phon.app.session.editor.view.transcript;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.ui.ipa.PhoneMapDisplay;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.util.Iterator;

public class AlignmentComponentFactory implements ComponentFactory {
    @Override
    public JComponent createComponent(AttributeSet attrs) {
        Tier<PhoneAlignment> tier = (Tier<PhoneAlignment>) attrs.getAttribute("tier");

        PhoneMapDisplay display = new PhoneMapDisplay();
        int wordIndex = 0;
        for (Iterator<PhoneMap> i = tier.getValue().iterator(); i.hasNext();) {
            var phoneMap = i.next();
            display.setPhoneMapForWord(wordIndex, phoneMap);
            wordIndex++;
        }

        return display;
    }
}
