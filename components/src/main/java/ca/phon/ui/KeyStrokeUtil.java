package ca.phon.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Key stroke utility methods.
 *
 */
public class KeyStrokeUtil {

    /**
     * Convert a KeyStroke to a Mac-style string representation.
     *
     * @param ks the key stroke
     * @return the Mac-style string
     */
    public static String keyStrokeToMacString(KeyStroke ks) {
        StringBuilder sb = new StringBuilder();
        int mod = ks.getModifiers();
        if ((mod & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) sb.append("⌘");
        if ((mod & KeyEvent.SHIFT_DOWN_MASK) != 0) sb.append("⇧");
        if ((mod & KeyEvent.ALT_DOWN_MASK) != 0) sb.append("⌥");
        if ((mod & KeyEvent.CTRL_DOWN_MASK) != 0) sb.append("⌃");
        sb.append(KeyEvent.getKeyText(ks.getKeyCode()));
        return sb.toString();
    }

    /**
     * Convert a KeyStroke to a Windows-style string representation.
     *
     * @param ks the key stroke
     * @return the Windows-style string
     */
    public static String keyStrokeToWindowsString(KeyStroke ks) {
        StringBuilder sb = new StringBuilder();
        int mod = ks.getModifiers();
        if ((mod & KeyEvent.CTRL_DOWN_MASK) != 0) sb.append("Ctrl+");
        if ((mod & KeyEvent.ALT_DOWN_MASK) != 0) sb.append("Alt+");
        if ((mod & KeyEvent.SHIFT_DOWN_MASK) != 0) sb.append("Shift+");
        sb.append(KeyEvent.getKeyText(ks.getKeyCode()));
        return sb.toString();
    }

    /**
     * Convert a KeyStroke to a string representation based on the current OS.
     *
     * @param ks the key stroke
     * @return the OS-specific string
     */
    public static String keyStrokeToString(KeyStroke ks) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            return keyStrokeToMacString(ks);
        } else {
            return keyStrokeToWindowsString(ks);
        }
    }

}
