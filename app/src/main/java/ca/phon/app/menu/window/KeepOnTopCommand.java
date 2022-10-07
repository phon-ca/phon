package ca.phon.app.menu.window;

import ca.phon.app.hooks.HookableAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class KeepOnTopCommand extends HookableAction {

	private final static String TXT = "Keep window on top";

	private final JFrame window;

	public KeepOnTopCommand(JFrame window) {
		this.window = window;
		putValue(NAME, TXT);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		window.setAlwaysOnTop(!window.isAlwaysOnTop());
	}

}
