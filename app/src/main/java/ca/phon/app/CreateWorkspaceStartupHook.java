package ca.phon.app;

import ca.phon.app.hooks.*;
import ca.phon.app.log.LogUtil;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.*;

/**
 * Creates workspace folder on Phon startup if it does not exist.
 *
 */
public class CreateWorkspaceStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	@Override
	public void startup() throws PluginException {
		if(!Workspace.userWorkspaceFolder().exists()) {
			LogUtil.info("Create workspace folder at " + Workspace.userWorkspaceFolder().getAbsolutePath());
			if(!Workspace.userWorkspaceFolder().mkdirs()) {
				LogUtil.warning("Unable to create workspace folder at "  + Workspace.userWorkspaceFolder().getAbsolutePath());
			}
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}
}
