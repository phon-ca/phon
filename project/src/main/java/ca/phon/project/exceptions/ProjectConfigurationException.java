package ca.phon.project.exceptions;

/**
 * Exception 
 */
public class ProjectConfigurationException extends Exception {

	private static final long serialVersionUID = -615834580110057068L;

	public ProjectConfigurationException() {
		super();
	}

	public ProjectConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProjectConfigurationException(String message) {
		super(message);
	}

	public ProjectConfigurationException(Throwable cause) {
		super(cause);
	}
	
}
