package ca.phon.app.project;

import bibliothek.gui.dock.common.CControl;
import ca.phon.project.Project;

import javax.swing.*;
import java.awt.*;

/**
 * Displayed in unified project window when project is opened.  This is the first thing the user
 * will see if no sessions are open.
 *
 */
public class ProjectStartPage extends JComponent {

    private final Project project;

    public ProjectStartPage(Project project) {
        super();
        this.project = project;

        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        add(new JLabel("Project start page here"), BorderLayout.CENTER);
    }

}
