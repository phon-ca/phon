package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.session.SystemTierType;

/**
 * 
 */
public class IPAVariabilityNode extends TableOpNode implements NodeSettings {
	
	private JCheckBox ignoreDiacriticsBox;
	
	private JPanel settingsPanel;
	
	private boolean ignoreDiacritics = true;
	
	public IPAVariabilityNode() {
		super();
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource table = (TableDataSource)context.get(tableInput);
		
		int ipaTidx = super.getColumnIndex(table, SystemTierType.IPATarget.getName());
		if(ipaTidx < 0) {
			throw new ProcessingException(null, "Table has no " + SystemTierType.IPATarget.getName() + " column.");
		}
		
		int ipaAidx = super.getColumnIndex(table, SystemTierType.IPAActual.getName());
		if(ipaAidx < 0) {
			throw new ProcessingException(null, "Table has no " + SystemTierType.IPAActual.getName() + " column.");
		}
		
		// group by session if info is available
		// TODO make this an option
		int sessionIdx = super.getColumnIndex(table, "Session");
		
		
	}

	@Override
	public Component getComponent(GraphDocument document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}

}
