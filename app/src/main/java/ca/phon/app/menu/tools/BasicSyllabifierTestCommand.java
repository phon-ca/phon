package ca.phon.app.menu.tools;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.ui.syllable.BasicSyllabifierTest;

public class BasicSyllabifierTestCommand extends HookableAction {

	private static final long serialVersionUID = -1716015830725094452L;
	
	public BasicSyllabifierTestCommand() {
		putValue(NAME, "Syllabifier Test");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BasicSyllabifierTest f = new BasicSyllabifierTest();
		f.pack();
		f.setVisible(true);
	}

}
