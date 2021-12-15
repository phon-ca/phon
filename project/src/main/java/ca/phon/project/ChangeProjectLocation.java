package ca.phon.project;

/**
 * Extension for {@link Project}s which allow changing location of project at runtime
 * (such as when changing the name of the project folder)
 */
public interface ChangeProjectLocation {

	public void setProjectLocation(String location);

}
