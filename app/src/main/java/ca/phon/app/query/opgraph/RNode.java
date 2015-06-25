package ca.phon.app.query.opgraph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.renjin.sexp.SEXP;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * <p>{@link OpNode} which uses the renjin scripting engine (http://www.renjin.org/) to execute
 * the provided script.
 * 
 * <ul>Inputs
 * <li><b>data</b> the data given to the script.  It's assumed that this data is in the
 * form of a table.  The 'read.table' R function will be used to initialize a new varaiable
 * 'data' in the R script context.</li>
 * <li><b>context</b> Context for the R script.  Optional.</li>
 * </ul>
 * 
 * <ul>Outputs
 * <li><b>data</b> Pass-through output for data</li>
 * <li><b>output</b> Output of script as text</li>
 * <li><b>context</b> Context for R script</li>
 * </ul>
 */
@OpNodeInfo(
	category="Report",
	name="R  Script",
	description="R script with single data input.",
	showInLibrary=true
)
public class RNode extends OpNode implements NodeSettings {
	
	private final static Logger LOGGER = Logger.getLogger(RNode.class.getName());
	
	private final static String SCRIPT_PROP = "script";
	
	private String script = "";
	
	private RSyntaxTextArea textArea;
	
	private final InputField dataInputField =
			new InputField("data", "Data for script in the form of a table", true, true, Object.class);
	
	private final OutputField dataOutputField =
			new OutputField("data", "Data pass-through", true, Object.class);
	private final OutputField scriptResultField =
			new OutputField("result", "Result, this is the object returned by the engine.eval() method.", true, Object.class);
	
	public RNode() {
		super();
		
		putField(dataInputField);
		
		putField(dataOutputField);
		putField(scriptResultField);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine engine = manager.getEngineByName("Renjin");
		if(engine == null) throw new ProcessingException("Renjin scripting engine not available");
		
		final Object inputData = context.get(dataInputField);
		if(inputData != null)
			engine.put("data", inputData);
		
		final String script = getScript();
		try {
			SEXP retVal = (SEXP)engine.eval(script);
			context.put(scriptResultField, retVal);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new ProcessingException(e);
		}

		context.put(dataOutputField, inputData);
	}
	
	public String getScript() {
		return (this.textArea == null ? this.script : this.textArea.getText());
	}

	@Override
	public Component getComponent(GraphDocument document) {
		final JPanel settingsPanel = new JPanel(new BorderLayout());
		String script = getScript();
		if(textArea == null) {
			textArea = new RSyntaxTextArea();
		}
		textArea.setText(script);
		final JScrollPane scroller = new JScrollPane(textArea);
		settingsPanel.add(scroller, BorderLayout.CENTER);
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		retVal.put(SCRIPT_PROP, getScript());
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(SCRIPT_PROP))
			this.script = properties.getProperty(SCRIPT_PROP);
	}

}
