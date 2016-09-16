/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.gedge.opgraph.OpNode;
import ca.phon.ui.text.PromptedTextField;

public class NodeWizardSettingsPanel extends JPanel {
	
	public static final String WIZARD_INFO = "_wizard_info_";
	
	private WizardExtension wizardExtension;
	
	private Map<String, SettingsPanel> panels = new HashMap<>();
	
	private CardLayout cardLayout = new CardLayout();
	
	private JPanel cardPanel;
	
	private JList<String> stepList;
	
	public NodeWizardSettingsPanel(WizardExtension wizardExtension) {
		super();
		
		this.wizardExtension = wizardExtension;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		cardPanel = new JPanel(cardLayout);
		
		final SettingsPanel panel = new SettingsPanel();
		panel.setTitle(wizardExtension.getWizardTitle());
		panel.setInfo(wizardExtension.getWizardMessage());
		panel.setFormat(wizardExtension.getWizardMessageFormat());
		panels.put(WIZARD_INFO, panel);
		cardPanel.add(panel, WIZARD_INFO);
		
		for(OpNode node:wizardExtension) {
			final String id = node.getId();
			final SettingsPanel nodePanel = new SettingsPanel();
			nodePanel.setTitle(wizardExtension.getNodeTitle(node));
			nodePanel.setInfo(wizardExtension.getNodeMessage(node));
			nodePanel.setFormat(wizardExtension.getNodeMessageFormat(node));
			panels.put(id, nodePanel);
			cardPanel.add(nodePanel, id);
		}
		
		stepList = new JList<>(new StepListModel());
		stepList.addListSelectionListener( (l) -> {
			int selectedIdx = stepList.getSelectedIndex();
			if(selectedIdx >= 0 && selectedIdx < stepList.getModel().getSize()) {
				final String selectedId = 
						(selectedIdx == 0 ? WIZARD_INFO : wizardExtension.getNode(selectedIdx-1).getId());
				cardLayout.show(cardPanel, selectedId);
			}
		});
		final JScrollPane stepScroller = new JScrollPane(stepList);
		stepList.setFixedCellWidth(200);
		
		final JSplitPane splitPane = new JSplitPane(SwingConstants.VERTICAL);
		splitPane.setResizeWeight(0);
		splitPane.setLeftComponent(stepScroller);
		splitPane.setRightComponent(cardPanel);
		add(splitPane, BorderLayout.CENTER);
	}
	
	public String getTitle(String step) {
		String retVal = "";
		
		final SettingsPanel panel = panels.get(step);
		if(panel != null) {
			retVal = panel.getTitle();
		}
		
		return retVal;
	}
	
	public String getMessage(String step) {
		String retVal = "";
		
		final SettingsPanel panel = panels.get(step);
		if(panel != null) {
			retVal = panel.getInfo();
		}
		
		return retVal;
	}
	
	public WizardInfoMessageFormat getFormat(String step) {
		final SettingsPanel panel = panels.get(step);
		return (panel != null ? panel.getFormat() : WizardInfoMessageFormat.HTML);
	}
	
	private class StepListModel extends AbstractListModel<String> {

		@Override
		public int getSize() {
			return wizardExtension.size() + 1;
		}

		@Override
		public String getElementAt(int index) {
			if(index == 0) {
				return wizardExtension.getWizardTitle();
			} else {
				return wizardExtension.getNodeTitle(wizardExtension.getNode(index-1));
			}
		}
		
		public void update() {
			super.fireContentsChanged(this, 0, getSize());
		}
		
	}
	
	private class SettingsPanel extends JPanel {
		
		private PromptedTextField titleField;
		
		private JComboBox<WizardInfoMessageFormat> formatBox;
		
		private RSyntaxTextArea infoArea;
		
		SettingsPanel() {
			super();
			
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			titleField = new PromptedTextField("Enter title");
			final JPanel topPanel = new JPanel(new BorderLayout());
			topPanel.add(titleField, BorderLayout.CENTER);
			topPanel.setBorder(BorderFactory.createTitledBorder("Title"));
			add(topPanel, BorderLayout.NORTH);
			titleField.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					final StepListModel model = (StepListModel)stepList.getModel();
					model.update();
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					
				}
			});
			
			formatBox = new JComboBox<>(WizardInfoMessageFormat.values());
			formatBox.addItemListener( e -> {
				if(getFormat() == WizardInfoMessageFormat.HTML) {
					infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
				} else {
					infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
				}
			});
			
			infoArea = new RSyntaxTextArea();
			infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
			final JPanel centerPanel = new JPanel(new BorderLayout());
			final JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			formatPanel.add(formatBox);
			centerPanel.add(formatPanel, BorderLayout.NORTH);
			centerPanel.add(new RTextScrollPane(infoArea, true), BorderLayout.CENTER);
			centerPanel.setBorder(BorderFactory.createTitledBorder("Info"));
			add(centerPanel, BorderLayout.CENTER);
		}
		
		public void setTitle(String title) {
			titleField.setText(title);
		}
		
		public String getTitle() {
			return titleField.getText();
		}
		
		public void setInfo(String info) {
			infoArea.setText(info);
		}
		
		public String getInfo() {
			return infoArea.getText();
		}
		
		public void setFormat(WizardInfoMessageFormat format) {
			formatBox.setSelectedItem(format);
		}
		
		public WizardInfoMessageFormat getFormat() {
			return (WizardInfoMessageFormat)formatBox.getSelectedItem();
		}
		
	}

}
