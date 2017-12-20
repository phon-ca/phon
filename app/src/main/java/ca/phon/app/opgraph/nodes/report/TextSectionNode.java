package ca.phon.app.opgraph.nodes.report;

import java.awt.*;
import java.util.Properties;

import javax.swing.*;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.app.opgraph.report.tree.*;

@OpNodeInfo(name="Text Section", description="Add Text to Report template", category="Report Tree", showInLibrary=true)
public class TextSectionNode extends ReportSectionNode implements NodeSettings {

	private final InputField textInput =
			new InputField("text", "Text input (overrides settings)", true, true, String.class);
	
	private JPanel settingsPanel;
	private JTextArea textArea;
	
	private final static String TEXT_PROP = TextSectionNode.class.getName() + ".text";
	private String text;
	
	public TextSectionNode() {
		super();
		
		putField(textInput);
		
		putExtension(NodeSettings.class, this);
	}
	
	public String getText() {
		return (textArea != null ? textArea.getText() : this.text);
	}
	
	public void setText(String text) {
		this.text = text;
		if(this.textArea != null) {
			this.textArea.setText(text);
		}
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
			
			textArea = new JTextArea();
			textArea.setText(text);
			JScrollPane scroller = new JScrollPane(textArea);
			settingsPanel.add(scroller, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		retVal.setProperty(TEXT_PROP, getText());
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setText(properties.getProperty(TEXT_PROP, ""));
	}

	@Override
	protected ReportTreeNode createReportSectionNode(OpContext context) {
		final String title = (context.get(sectionNameInput) != null ? context.get(sectionNameInput).toString() : "");
		final String inputText = (context.get(textInput) != null ? context.get(textInput).toString() : null);
		final String sectionText = (inputText != null ? inputText : getText() );
		
		return new TextNode(title, sectionText);
	}

}
