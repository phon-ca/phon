package ca.phon.session.check;

/**
 * Quick fix for session checks.  Subclasses need to implement the fix() method.
 *
 */
public abstract class SessionQuickFix {
	
	private final ValidationEvent validationEvent;
	
	public SessionQuickFix(ValidationEvent validationEvent) {
		super();
		
		this.validationEvent = validationEvent;
	}
	
	public ValidationEvent getValidationEvent() {
		return validationEvent;
	}
	
	public String getDescription() {
		return "Fix issue";
	}

	/**
	 * Perform the quick fix operation.
	 * 
	 * @return <code>true</code> if the fix was successful
	 */
	public abstract boolean fix();
	
}
