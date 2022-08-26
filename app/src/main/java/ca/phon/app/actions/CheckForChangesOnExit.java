package ca.phon.app.actions;

import java.io.IOException;

/**
 * Objects which are not attached to windows but need to be checked for changes on exit.
 *
 */
public interface CheckForChangesOnExit {

	public String getName();

	public boolean hasChanges();

	public void save() throws IOException;

}
