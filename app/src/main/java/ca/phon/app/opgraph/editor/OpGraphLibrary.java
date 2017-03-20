package ca.phon.app.opgraph.editor;

import java.net.URL;

import ca.phon.project.Project;
import ca.phon.util.resources.ResourceLoader;

public interface OpGraphLibrary {

	public ResourceLoader<URL> getStockGraphs();

	public ResourceLoader<URL> getUserGraphs();

	public ResourceLoader<URL> getProjectGraphs(Project project);

	public String getFolderName();

	public String getUserFolderPath();

	public String getProjectFolderPath(Project project);

}
