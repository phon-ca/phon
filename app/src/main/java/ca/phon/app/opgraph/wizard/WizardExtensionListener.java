package ca.phon.app.opgraph.wizard;

/**
 * Listener for wizard extension events.
 *
 */
@FunctionalInterface
public interface WizardExtensionListener {

	public void wizardExtensionChanged(WizardExtensionEvent event);
	
}
