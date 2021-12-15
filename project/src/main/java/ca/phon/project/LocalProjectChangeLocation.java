package ca.phon.project;

import ca.phon.extensions.Extension;

@Extension(LocalProject.class)
public class LocalProjectChangeLocation implements ChangeProjectLocation {

	private final LocalProject project;

	public LocalProjectChangeLocation(LocalProject project) {
		this.project = project;
	}

	@Override
	public void setProjectLocation(String location) {
		project.setLocation(location);
	}

}
