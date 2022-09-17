package ca.phon.query.pql;

import ca.phon.project.Project;

/**
 * Context for a PQL query.
 */
public class PQLContext {

	// may be null
	private Project project;

	public PQLContext() {
		this(null);
	}

	public PQLContext(Project project) {
		super();
		this.project = null;
	}



}
