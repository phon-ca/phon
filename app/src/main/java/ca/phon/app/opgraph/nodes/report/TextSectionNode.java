/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph.nodes.report;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import ca.phon.app.opgraph.report.tree.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;

@OpNodeInfo(name="Text Section", description="Add Text to Report template", category="Report", showInLibrary=true)
public class TextSectionNode extends ReportSectionNode implements NodeSettings {

	protected final InputField textInput =
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
