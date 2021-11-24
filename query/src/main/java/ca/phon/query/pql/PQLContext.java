package ca.phon.query.pql;

import ca.phon.project.Project;
import ca.phon.session.Session;

import java.util.*;

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
