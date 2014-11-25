package ca.phon.query.analysis;

import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;

public class QueryAnalysisInput implements IExtendable {
	
	private Project project;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(QueryAnalysisInput.class, this);

	private List<SessionPath> sessions;
	
	public QueryAnalysisInput() {
		super();
		extSupport.initExtensions();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<SessionPath> getSessions() {
		return sessions;
	}

	public void setSessions(List<SessionPath> sessions) {
		this.sessions = sessions;
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
