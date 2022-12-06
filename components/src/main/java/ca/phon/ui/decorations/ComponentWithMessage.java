package ca.phon.ui.decorations;

import javax.swing.*;
import java.awt.*;

/**
 * A wrapper for a component with a displayed message (usually a warning/error) at either the bottom or top
 * of the component.
 *
 *
 */
public class ComponentWithMessage<T extends JComponent> extends JComponent {

    private final T component;

    private final JLabel label;

    public ComponentWithMessage(T component) {
        this(component, null, "", false);
    }

    public ComponentWithMessage(T component, String message) {
        this(component, null, message);
    }

    public ComponentWithMessage(T component, Icon icn, String message) {
        this(component, icn, message, true);
    }

    public ComponentWithMessage(T component, Icon icn, String message, boolean messageVisible) {
        super();

        this.component = component;
        label = new JLabel();
        init(icn, message, messageVisible);
    }

    private void init(Icon icon, String message, boolean messageVisible) {
        setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        gbc.weighty = 0.0f;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(component, gbc);
        ++gbc.gridy;
        add(label, gbc);
        if(icon != null)
            label.setIcon(icon);
        if(message != null)
            label.setText(message);
        label.setVisible(messageVisible);
    }
    
    public T getComponent() {
        return this.component;
    }

    public JLabel getLabel() {
        return this.label;
    }

    public void updateLabel(Icon icon, String message, boolean visible) {
        getLabel().setIcon(icon);
        getLabel().setText(message);
        getLabel().setVisible(visible);
    }

}
