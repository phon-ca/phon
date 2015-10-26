package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.util.Properties;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.query.report.InventorySectionPanel;
import ca.phon.project.Project;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.datasource.InventoryDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.query.report.io.InventorySection;
import ca.phon.query.report.io.ObjectFactory;

@OpNodeInfo(
		name="Inventory",
		description="Aggregated inventory of query results",
		category="Report"
)		
public class InventoryNode extends OpNode implements NodeSettings {
	
	private InputField resultSetInput = new InputField("result sets", "result sets from query", false,
			true, ResultSet[].class);
	
	private OutputField tableOutput = new OutputField("table", "Inventory as table with session names as columns",
			true, TableDataSource.class);
	
	/** Settings */
	private InventorySection inventorySection;
	
	private InventorySectionPanel inventorySectionPanel;
	
	public InventoryNode() {
		super();
		
		final ObjectFactory factory = new ObjectFactory();
		inventorySection = factory.createInventorySection();
		
//		putField(projectInputField);
		putField(resultSetInput);
		putField(tableOutput);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(inventorySectionPanel != null) {
			inventorySectionPanel = new InventorySectionPanel(inventorySection);
		}
		return inventorySectionPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		props.put("name", inventorySection.getName());
		props.put("caseSensitive", inventorySection.isCaseSensitive());
		props.put("groupByFormat", inventorySection.isGroupByFormat());
		props.put("ignoreDiacritics", inventorySection.isIgnoreDiacritics());
		props.put("includeExcluded", inventorySection.isIncludeExcluded());
		props.put("includeMetadata", inventorySection.isIncludeMetadata());
		props.put("includeResultValue", inventorySection.isIncludeResultValue());
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		inventorySection.setName(properties.getProperty("name"));
		inventorySection.setCaseSensitive(
				Boolean.parseBoolean(properties.getProperty("caseSensitive", "false")));
		inventorySection.setGroupByFormat(
				Boolean.parseBoolean(properties.getProperty("groupByFormat", "false")));
		inventorySection.setIgnoreDiacritics(
				Boolean.parseBoolean(properties.getProperty("ignoreDiacritics", "true")));
		inventorySection.setIncludeExcluded(
				Boolean.parseBoolean(properties.getProperty("includeExcluded", "false")));
		inventorySection.setIncludeMetadata(
				Boolean.parseBoolean(properties.getProperty("includeMetadata", "false")));
		inventorySection.setIncludeResultValue(
				Boolean.parseBoolean(properties.getProperty("includeResultValue", "true")));
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ResultSet[] resultSet = (ResultSet[])context.get(resultSetInput);
		final InventoryDataSource inventoryDataSource = new InventoryDataSource(resultSet, inventorySection);
		
		context.put(tableOutput, inventoryDataSource);
	}

}
