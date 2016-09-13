package ca.phon.app.welcome;

import ca.phon.ui.MultiActionButton;

/**
 * Extension point for adding additional action buttons to the
 * {@link WelcomeWindow}.
 * 
 */
public interface WelcomeWindowAction {

	/**
	 * Create the action button.
	 * 
	 * @param welcome window
	 * @return button
	 */
	public MultiActionButton createButton(WelcomeWindow window);
	
}
