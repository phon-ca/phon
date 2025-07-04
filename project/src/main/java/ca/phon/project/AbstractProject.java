package ca.phon.project;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.*;
import jakarta.xml.bind.*;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;

public abstract class AbstractProject implements Project, ProjectEvents {

	/**
	 * Local projects no longer create this file but will read from it
	 * Remote projects require this file exists
	 */
	@Deprecated
	protected final static String PROJECT_XML_FILE = "project.xml";

	private final ExtensionSupport extSupport;

	private final List<ProjectListener> projectListeners =
			Collections.synchronizedList(new ArrayList<ProjectListener>());

	public AbstractProject() {
		super();

		extSupport = new ExtensionSupport(Project.class, this);
		extSupport.initExtensions();

		extSupport.putExtension(ProjectEvents.class, this);
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public List<ProjectListener> getProjectListeners() {
		return Collections.unmodifiableList(projectListeners);
	}

	@Override
	public void addProjectListener(ProjectListener listener) {
		if(!projectListeners.contains(listener)) {
			projectListeners.add(listener);
		}
	}

	@Override
	public void removeProjectListener(ProjectListener listener) {
		projectListeners.remove(listener);
	}

	@Override
	public void fireProjectStructureChanged(ProjectEvent pe) {
		final List<ProjectListener> listeners = getProjectListeners();
		for(ProjectListener listener:listeners) {
			listener.projectStructureChanged(pe);
		}
	}

	@Override
	public void fireProjectDataChanged(ProjectEvent pe) {
		final List<ProjectListener> listeners = getProjectListeners();
		for(ProjectListener listener:listeners) {
			listener.projectDataChanged(pe);
		}
	}

	@Override
	public void fireProjectWriteLocksChanged(ProjectEvent pe) {
		final List<ProjectListener> listeners = getProjectListeners();
		for(ProjectListener listener:listeners) {
			listener.projectWriteLocksChanged(pe);
		}
	}

}
