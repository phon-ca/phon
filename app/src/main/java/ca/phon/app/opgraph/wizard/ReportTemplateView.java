/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.gedge.opgraph.app.GraphDocument;
import ca.phon.app.opgraph.wizard.WizardExtensionEvent.EventType;

/**
 * View for editing report prefix/suffix templates.
 * 
 */
public class ReportTemplateView extends JPanel {
	
	private static final long serialVersionUID = -3918559605149317608L;

	private GraphDocument graphDocument;

	private RSyntaxTextArea textArea;
	
	private final static String[] reportNames = { "Report Prefix", "Report Suffix" };
	private JComboBox<String> templateNameBox;
	
	public ReportTemplateView(GraphDocument document) {
		super();
		
		this.graphDocument = document;
		getWizardExtension().addWizardExtensionListener( (e) -> {
			if(e.getEventType() == EventType.REPORT_TEMPLATE_CHANGED) {
				if(!textArea.hasFocus() 
						&& templateNameBox.getSelectedItem().equals(e.getReportName())) {
					// update current text
					textArea.setText(e.getReportContent());
				}
			}
		});
		
		init();
	}
	
	public GraphDocument getDocument() {
		return this.graphDocument;
	}
	
	public WizardExtension getWizardExtension() {
		return getDocument().getGraph().getExtension(WizardExtension.class);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		templateNameBox = new JComboBox<>(reportNames);
		templateNameBox.addItemListener( (e) -> {
			final String reportName = templateNameBox.getSelectedItem().toString();
			final String reportContent = getWizardExtension().getReportTemplate(reportName).getTemplate();
			textArea.setText(reportContent);
		});
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JLabel("Report template:"), BorderLayout.WEST);
		topPanel.add(templateNameBox, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
		
		textArea = new RSyntaxTextArea();
		textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_NONE);
		final RTextScrollPane textScroller = new RTextScrollPane(textArea);
		add(textScroller, BorderLayout.CENTER);
		textArea.setText(getWizardExtension().getReportTemplate(reportNames[0]).getTemplate());
		textArea.getDocument().addDocumentListener(docListener);
	}

	private void updateTemplate() {
//		final ReportTemplateEdit edit = new ReportTemplateEdit(getWizardExtension(),
//				reportNames[templateNameBox.getSelectedIndex()], textArea.getText());
//		getDocument().getUndoSupport().postEdit(edit);
	}
	
	private final DocumentListener docListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateTemplate();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateTemplate();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
		}
		
	};
	
}
