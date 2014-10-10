package ca.phon.app.theme;

import javax.swing.UIDefaults;

import org.pushingpixels.lafwidget.LafWidget;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;

public class PhonSubstanceLookAndFeel extends SubstanceLookAndFeel {

	private static final long serialVersionUID = 6899234733111691465L;

	public PhonSubstanceLookAndFeel() {
		super(new PhonSubstanceSkin());
	}

	@Override
	public UIDefaults getDefaults() {
		final UIDefaults retVal = super.getDefaults();
		
		SubstanceLookAndFeel.setFontPolicy(null);
        
        // Create the wrapper font set
        FontPolicy newFontPolicy = new FontPolicy() {
          public FontSet getFontSet(String lafName,
              UIDefaults table) {
            return new PhonUIFontSet();
          }
        };

		SubstanceLookAndFeel.setFontPolicy(newFontPolicy);
		
		retVal.put(
                SubstanceLookAndFeel.COLORIZATION_FACTOR,
                1.0);
		retVal.put(
                SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS,
                Boolean.TRUE);
		retVal.put(LafWidget.TEXT_EDIT_CONTEXT_MENU,
                Boolean.TRUE);

		
		return retVal;
	}
	

}
