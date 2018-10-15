package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;

@OpNodeInfo(name="Column Percentage", category="Table", description="Calculate percentage using a divisor and dividend columns.", showInLibrary=true)
public class ColumnPercentageNode extends TableOpNode implements NodeSettings {

	private JPanel settingsPanel;
	private JTextField divisorColumnField;
	private JTextArea dividendColumnsArea;
	
	private String divsorColumn;
	
	private List<String> dividendColumns;
	
	public ColumnPercentageNode() {
		super();
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Component getComponent(GraphDocument document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadSettings(Properties properties) {
		// TODO Auto-generated method stub
		
	}

	
}
