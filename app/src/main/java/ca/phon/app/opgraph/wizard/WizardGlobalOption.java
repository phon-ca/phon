package ca.phon.app.opgraph.wizard;

import javax.swing.JComponent;

/**
 * Interface for adding global options to the node wizard
 * from plug-ins.
 */
public interface WizardGlobalOption {
	
	public String getName();
	
	public Class<?> getType();
	
	public Object getDefaultValue();
	
	public Object getValue();
	
	public JComponent getGlobalOptionsComponent();

}
