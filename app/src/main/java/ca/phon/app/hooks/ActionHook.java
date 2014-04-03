package ca.phon.app.hooks;

import java.awt.event.ActionEvent;

/**
 * Interface used for {@link HookableAction}s.
 *
 * @param A the action type being hooked
 */
public interface ActionHook<A extends HookableAction> {
	
	/**
	 * Return the action type.
	 * 
	 * @return hookable action class
	 */
	public Class<? extends A> getActionType();

	/**
	 * Method called before the {@link HookableAction#hookableActionPerformed(ActionEvent)} method
	 * is called.  This method may pre-empt the action by returning <code>true</code>.
	 * 
	 * @param action
	 * @param ae
	 * 
	 * @return <code>true</code> if the action should be cancelled, <code>false</code>
	 *  otherwise
	 */
	public boolean beforeAction(A action, ActionEvent ae);
	
	/**
	 * Method called afteer the {@link HookableAction#hookableActionPerformed(ActionEvent)} method
	 * is called.
	 * 
	 * @param action
	 * @param ae
	 * 
	 */
	public void afterAction(A action, ActionEvent ae);
	
}
