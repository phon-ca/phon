package ca.phon.app.theme;

import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

public class PhonWindowsLookAndFeel extends WindowsLookAndFeel {

	private static final long serialVersionUID = 5210071497839838700L;

	public PhonWindowsLookAndFeel() {
		super();
		
		// setup fonts
		setFontPolicy(new PhonWindowsFontPolicy());
	}
	
}
