package ca.phon.ui;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;

/**
 * Tri-state checkbox using custom icons
 */
public class PhonCheckbox extends JCheckBox  {

    // load icons from Google Material Icons
    private static final String checkedIcon = "check_box";
    private static final String uncheckedIcon = "check_box_outline_blank";
    private static final String partialIcon = "indeterminate_check_box";

    public PhonCheckbox() {
        super();
        init();
    }

    public PhonCheckbox(String text) {
        super(text);
        init();
    }

    public PhonCheckbox(String text, boolean selected) {
        super(text, selected);
        init();
    }

    private void init() {
        setIcon(IconManager.getInstance().getFontIcon(uncheckedIcon, IconSize.SMALL, UIManager.getColor("Button.foreground")));
        setSelectedIcon(IconManager.getInstance().getFontIcon(checkedIcon, IconSize.SMALL, UIManager.getColor("Button.foreground")));
        setDisabledIcon(IconManager.getInstance().getFontIcon(uncheckedIcon, IconSize.SMALL, UIManager.getColor("textInactiveText")));
        setDisabledSelectedIcon(IconManager.getInstance().getFontIcon(checkedIcon, IconSize.SMALL, UIManager.getColor("textInactiveText")));
        setPressedIcon(IconManager.getInstance().getFontIcon(uncheckedIcon, IconSize.SMALL, UIManager.getColor("Button.foreground")));
        setRolloverIcon(IconManager.getInstance().getFontIcon(uncheckedIcon, IconSize.SMALL, UIManager.getColor("Button.foreground")));
        setRolloverSelectedIcon(IconManager.getInstance().getFontIcon(checkedIcon, IconSize.SMALL, UIManager.getColor("Button.foreground")));
        setMargin(new java.awt.Insets(0, 0, 0, 0));
    }

}
