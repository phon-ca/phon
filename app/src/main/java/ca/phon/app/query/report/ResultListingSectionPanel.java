/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.query.report;

import static ca.phon.query.report.util.ResultListingFieldBuilder.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.script.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;

import com.jgoodies.forms.layout.*;

import ca.phon.app.query.*;
import ca.phon.query.report.io.*;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.session.*;
import ca.phon.ui.action.*;
import ca.phon.util.icons.*;

/**
 * Section panel for customizable result listings.
 *
 */
public class ResultListingSectionPanel extends SectionPanel<ResultListing> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ResultListingSectionPanel.class.getName());
	
	/**
	 * Help text
	 */
	private final static String INFO_TEXT = 
		"<html><body>" +
		"<i>Result List</i>" +
		"<p>Outputs data for each result in a search.  Data can be printed as a table or listed with one field per line.<p>" +
		"<p>To add/remove fields, use the <img width='12' height='12' src='${field_add_img}' alt='+'/>" +
		"and <img width='12' height='12' src='${field_remove_img}' alt='-'/> buttons in the field outline.</p>" + 
		"</body></html>";
	
	
	
	/*
	 * UI
	 * 
	 */
	/*
	 * List
	 */
	private JXList fieldList;
	
	private JButton addFieldButton;
	
	private JButton delFieldButton;
	
	private JButton upFieldButton;
	
	private JButton downFieldButton;
	
	/*
	 * Form controls
	 */
	private JTextField nameField;
	
	private JPanel namePanel;
	
	/*
	 * Panel for field options 
	 */
	private JPanel fieldOptionsPanel;
	
	/*
	 * Query scripts associated with fields
	 * The editor will work with the query script
	 * objects while editing and update field values
	 * when report is saved.
	 */
	private Map<ResultListingField, ScriptPanel> scriptPanels = 
			Collections.synchronizedMap(new HashMap<ResultListingField, ScriptPanel>());
	
	/*
	 * Table or list?
	 * 
	 */
	private JRadioButton tableOptBox;
	
	private JRadioButton listOptBox;
	
	/*
	 * Include excluded?
	 */
	private JCheckBox includeExcludedBox;
	
	private JPanel fieldPanel;
	
	public ResultListingSectionPanel(ResultListing listing) {
		super(listing);
		
		if(getSection().getField().size() == 0) {
			// setup default list
			for(ResultListingField field:getDefaultFields()) {
				getSection().getField().add(field);
			}
		}
		
		init();
	}
	
	private void init() {
		// get absolute locations of icons
		String addImgRelPath = 
			"data" + File.separator + "icons" +
			File.separator + "16x16" + File.separator + 
			"actions" + File.separator + "list-add.png";
		File addImgFile = new File(addImgRelPath);
		String addImgURI = addImgFile.toURI().toASCIIString();
		
		String removeImgRelPath = 
			"data" + File.separator + "icons" +
			File.separator + "16x16" + File.separator + 
			"actions" + File.separator + "list-remove.png";
		File remImgFile = new File(removeImgRelPath);
		String remImgURI = remImgFile.toURI().toASCIIString();
		
		String infoTxt = INFO_TEXT.replaceAll("\\$\\{field_add_img\\}", addImgURI)
								  .replaceAll("\\$\\{field_remove_img\\}", remImgURI);
		
		super.setInformationText(getClass().getName()+".info", infoTxt);
		
		// list panel
		FormLayout listLayout = new FormLayout(
				"fill:pref:grow, pref",
				"pref, fill:pref:grow");
		JPanel listPanel = new JPanel(listLayout);
		CellConstraints cc = new CellConstraints();
		
		ResultListing resList = getSection();
		fieldList = new JXList(resList.getField().toArray(new ResultListingField[0]));
		fieldList.setCellRenderer(new FieldListCellRenderer());
		fieldList.addListSelectionListener(new FieldListSelectionListener());
		fieldList.setMinimumSize(new Dimension(200, 0));
		
		ActionMap fieldActionMap = fieldList.getActionMap();
		InputMap fieldInputMap = fieldList.getInputMap(JComponent.WHEN_FOCUSED);
		
		PhonUIAction showListAction = new PhonUIAction(this, "onShowPopup");
		ImageIcon addIcn = IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
		showListAction.putValue(PhonUIAction.SMALL_ICON, addIcn);
		showListAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add field...");
		addFieldButton = new JButton(showListAction);
		
		PhonUIAction removeFieldAction = new PhonUIAction(this, "onDelField");
		ImageIcon delIcn = IconManager.getInstance().getIcon("actions/list-remove", IconSize.XSMALL);
		removeFieldAction.putValue(PhonUIAction.SMALL_ICON, delIcn);
		removeFieldAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected field");
		delFieldButton = new JButton(removeFieldAction);
		String delActID = "_remove_field_";
		fieldActionMap.put(delActID, removeFieldAction);
		KeyStroke delKs1 = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		KeyStroke delKs2 = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		fieldInputMap.put(delKs1, delActID);
		fieldInputMap.put(delKs2, delActID);
		
		PhonUIAction moveFieldUpAction = new PhonUIAction(this, "onMoveFieldUp");
		ImageIcon upIcn = IconManager.getInstance().getIcon("actions/go-up", IconSize.XSMALL);
		moveFieldUpAction.putValue(PhonUIAction.SMALL_ICON, upIcn);
		moveFieldUpAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected field up");
		upFieldButton = new JButton(moveFieldUpAction);
		
		PhonUIAction moveFieldDownAction = new PhonUIAction(this, "onMoveFieldDown");
		ImageIcon downIcn = IconManager.getInstance().getIcon("actions/go-down", IconSize.XSMALL);
		moveFieldDownAction.putValue(PhonUIAction.SMALL_ICON, downIcn);
		moveFieldDownAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected field down");
		downFieldButton = new JButton(moveFieldDownAction);
		
		FormLayout topBtnLayout = 
			new FormLayout("fill:pref:grow, right:pref, right:pref, "
					+ (upFieldButton.getPreferredSize().width) + "px", "pref");
		JPanel topBtnPanel = new JPanel(topBtnLayout);
		
		FormLayout sideBtnLayout = 
			new FormLayout("pref", "pref, 3dlu, pref, fill:pref:grow");
		JPanel sideBtnPanel = new JPanel(sideBtnLayout);
		
		JScrollPane listScroller = new JScrollPane(fieldList);
		
		topBtnPanel.add(addFieldButton, cc.xy(2, 1));
		topBtnPanel.add(delFieldButton, cc.xy(3, 1));
		
		sideBtnPanel.add(upFieldButton, cc.xy(1, 1));
		sideBtnPanel.add(downFieldButton, cc.xy(1, 3));
		
		listPanel.add(topBtnPanel, cc.xyw(1, 1, 2));
		listPanel.add(sideBtnPanel, cc.xywh(2, 2, 1, 1));
		listPanel.add(listScroller, cc.xy(1, 2));
		
		// field form
		fieldOptionsPanel = new JPanel(new BorderLayout());
		fieldOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		namePanel = new JPanel(
				new FormLayout("left:pref, 3dlu, fill:pref:grow", "pref"));
		nameField = new JTextField();
		nameField.getDocument().addDocumentListener(new NameFieldListener());
		namePanel.add(new JLabel("Field name:"), cc.xy(1,1));
		namePanel.add(nameField, cc.xy(3, 1));
		
		fieldOptionsPanel.add(namePanel, BorderLayout.NORTH);
		
		// format selection
		tableOptBox = new JRadioButton("Table");
		listOptBox = new JRadioButton("List");
		
		ButtonGroup btnGroup = new ButtonGroup();

		FormatHandler formatHandler = new FormatHandler();
		tableOptBox.setSelected(resList.getFormat() == ResultListingFormatType.TABLE);
		tableOptBox.addActionListener(formatHandler);
		listOptBox.setSelected(!tableOptBox.isSelected());
		listOptBox.addActionListener(formatHandler);
		
		includeExcludedBox = new JCheckBox("Include excluded results");
		includeExcludedBox.setSelected(getSection().isIncludeExcluded());
		includeExcludedBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getSection().setIncludeExcluded(includeExcludedBox.isSelected());
			}
		});
		
		btnGroup.add(tableOptBox);
		btnGroup.add(listOptBox);
		
		FormLayout splitLayout = new FormLayout(
				"200px:nogrow, fill:default:grow",
				"fill:default:grow");
		fieldPanel = new JPanel(splitLayout);
		fieldPanel.add(listPanel, cc.xy(1, 1));
		fieldPanel.add(fieldOptionsPanel, cc.xy(2, 1));
		fieldPanel.setBorder(BorderFactory.createTitledBorder("Field Outline"));
		
		JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		formatPanel.add(new JLabel("Display data as:"));
		formatPanel.add(tableOptBox);
		formatPanel.add(listOptBox);
		formatPanel.add(includeExcludedBox);
		formatPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.add(formatPanel, BorderLayout.NORTH);
		cPanel.add(fieldPanel, BorderLayout.CENTER);
		add(cPanel, BorderLayout.CENTER);
		
		nameField.setEnabled(false);
	}
	
	public JPanel getFieldPanel() {
		return this.fieldPanel;
	}
	
	/**
	 * Get a list of pre-defined fields.
	 * 
	 * @return list of fields
	 */
	private ResultListingField[] getPredefinedFields() {
		List<ResultListingField> retVal = 
			new ArrayList<ResultListingField>();
		
		// query info
		
		// session info
		// session name
		ResultListingField sNameField = createEmptyField();
		sNameField.setTitle("Session Name");
		sNameField.getFieldValue().setLang("Javascript");
		sNameField.getFieldValue().setScript(SESSION_NAME_SCRIPT);
		retVal.add(sNameField);
		
		ResultListingField sDateField = createEmptyField();
		sDateField.setTitle("Session Date");
		sDateField.getFieldValue().setLang("Javascript");
		sDateField.getFieldValue().setScript(SESSION_DATE_SCRIPT);
		retVal.add(sDateField);
		
		ResultListingField sMediaField = createEmptyField();
		sMediaField.setTitle("Session Media");
		sMediaField.getFieldValue().setLang("Javascript");
		sMediaField.getFieldValue().setScript(SESSION_MEDIA_SCRIPT);
		retVal.add(sMediaField);
		
		// record info
		ResultListingField rNumField = createEmptyField();
		rNumField.setTitle("Record #");
		rNumField.getFieldValue().setLang("Javascript");
		rNumField.getFieldValue().setScript(RECORD_NUMBER_SCRIPT);
		retVal.add(rNumField);
		
		ResultListingField rSpeakerField = createEmptyField();
		rSpeakerField.setTitle("Speaker");
		rSpeakerField.getFieldValue().setLang("Javascript");
		rSpeakerField.getFieldValue().setScript(SPEAKER_NAME_SCRIPT);
		retVal.add(rSpeakerField);
		
		ResultListingField rSpeakerAgeField = createEmptyField();
		rSpeakerAgeField.setTitle("Speaker Age");
		rSpeakerAgeField.getFieldValue().setLang("Javascript");
		rSpeakerAgeField.getFieldValue().setScript(SPEAKER_AGE_SCRIPT);
		retVal.add(rSpeakerAgeField);
		
		ResultListingField rSpeakerSexField = createEmptyField();
		rSpeakerSexField.setTitle("Speaker Gender");
		rSpeakerSexField.getFieldValue().setLang("Javascript");
		rSpeakerSexField.getFieldValue().setScript(SPEAKER_GENDER_SCRIPT);
		retVal.add(rSpeakerSexField);
		
		// system tiers
		for(SystemTierType stt:SystemTierType.values()) {
			ResultListingField sttField = createEmptyField();
			sttField.setTitle(stt.getName());
			sttField.getFieldValue().setLang("Javascript");
			
			if(stt == SystemTierType.TargetSyllables) {
				sttField.getFieldValue().setScript(SYLLABIFICATION_SCRIPT.replaceAll("\\$\\{TIER\\}", "0"));
			} else if(stt == SystemTierType.ActualSyllables) {
				sttField.getFieldValue().setScript(SYLLABIFICATION_SCRIPT.replaceAll("\\$\\{TIER\\}", "1"));
			} else if(stt == SystemTierType.SyllableAlignment) {
				sttField.getFieldValue().setScript(ALIGNMENT_SCRIPT);
			} else {
				sttField.getFieldValue().setScript(RECORD_TIER_SCRIPT.replaceAll("\\$\\{TIER\\}", stt.getName()));
			}
			
			retVal.add(sttField);
		}
		
		
		// alignment
		
		
		// result info
		ResultListingField resValField = createEmptyField();
		resValField.setTitle("Result");
		resValField.getFieldValue().setLang("Javascript");
		resValField.getFieldValue().setScript(RESULT_SCRIPT);
		retVal.add(resValField);
		
		ResultListingField resFormatField = createEmptyField();
		resFormatField.setTitle("Result Format");
		resFormatField.getFieldValue().setLang("Javascript");
		resFormatField.getFieldValue().setScript(RESULT_FORMAT_SCRIPT);
		retVal.add(resFormatField);

		ResultListingField exludedField = createEmptyField();
		exludedField.setTitle("Result Excluded");
		exludedField.getFieldValue().setLang("Javascript");
		exludedField.getFieldValue().setScript(RESULT_EXCLUDED_SCRIPT);
		retVal.add(exludedField);
		
		ResultListingField resMetaField = createEmptyField();
		resMetaField.setTitle("Result Metadata");
		resMetaField.getFieldValue().setLang("Javascript");
		resMetaField.getFieldValue().setScript(METADATA_SCRIPT);
		retVal.add(resMetaField);
		
		return retVal.toArray(new ResultListingField[0]);
	}
	
	/*
	 * Update form components with given field data
	 * 
	 * @param field
	 * 
	 */
	private void updateForm(ResultListingField field) {
		nameField.setText(field.getTitle());
		
		fieldOptionsPanel.removeAll();
		fieldOptionsPanel.add(namePanel, BorderLayout.NORTH);
		
		ScriptPanel scriptPanel = scriptPanels.get(field);
		if(scriptPanel == null) {
			QueryScript qs = new QueryScript(field.getFieldValue().getScript());
			QueryScriptContext ctx = qs.getQueryContext();
			ScriptParameters params = new ScriptParameters();
			try {
				params = ctx.getScriptParameters(ctx.getEvaluatedScope());
			} catch (PhonScriptException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
			// setup script parameters
			for(ScriptParam param:params) {
				for(String paramId:param.getParamIds()) {
					ScriptParameter savedParam = null;
					for(ScriptParameter sp:field.getFieldValue().getParam()) {
						if(sp.getName().equals(paramId)) {
							savedParam = sp;
							break;
						}
					}
					
					if(savedParam != null) {
						Object paramVal = null;
						try {
							if(param.getParamType().equals("bool")) {
								paramVal = Boolean.parseBoolean(savedParam.getContent());
							} else if (param.getParamType().equals("enum")) {
								EnumScriptParam esp = (EnumScriptParam)param;
								EnumScriptParam.ReturnValue rVal = null;
								for(EnumScriptParam.ReturnValue v:esp.getChoices()) {
									if(v.toString().equals(savedParam.getContent())) {
										rVal = v;
										break;
									}
								}
								if(rVal != null) 
									paramVal = rVal;
								else
									paramVal = esp.getDefaultValue(paramId);
							} else {
								paramVal = savedParam.getContent();
							}
							param.setValue(paramId, paramVal);
						} catch (Exception e) {
							LOGGER.warn( e.getLocalizedMessage(), e);
						}
					}
				}
			}
			scriptPanel = new ScriptPanel(qs);
			scriptPanels.put(field, scriptPanel);
			scriptPanel.addPropertyChangeListener(new ScriptPanelListener(field));
		}
		fieldOptionsPanel.add(scriptPanel, BorderLayout.CENTER);
		fieldOptionsPanel.revalidate();
		fieldOptionsPanel.repaint();
	}
	
	/*
	 * UI Actions
	 */
	public void onShowPopup(PhonActionEvent pae) {
		// display a popup menu for selecting what field to add
		JPopupMenu menu = new JPopupMenu();
		
		ResultListingField[] defFields = getPredefinedFields();
		for(ResultListingField field:defFields) {
			PhonUIAction fieldAct = new PhonUIAction(this, "onAddField", field);
			fieldAct.putValue(PhonUIAction.NAME, field.getTitle());
			fieldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, field.getFieldValue().getLang());
			fieldAct.putValue(PhonUIAction.LONG_DESCRIPTION, field.getFieldValue().getScript());
			JMenuItem fieldItem = new JMenuItem(fieldAct);
			menu.add(fieldItem);
		}
		menu.addSeparator();
		
		ResultListingField userTierField = createEmptyField();
		userTierField.setTitle("User Defined Tier");
		userTierField.getFieldValue().setLang("Javascript");
		userTierField.getFieldValue().setScript(RECORD_TIER_SCRIPT.replaceAll("\\$\\{TIER\\}", ""));
		PhonUIAction userFieldAct = new PhonUIAction(this, "onAddField", userTierField);
		userFieldAct.putValue(PhonUIAction.NAME, userTierField.getTitle());
		JMenuItem userFieldItem = new JMenuItem(userFieldAct);
		menu.add(userFieldItem);
		
		ResultListingField emptyField = createEmptyField();
		emptyField.setTitle("Untitled");
		PhonUIAction customFieldAct = new PhonUIAction(this, "onAddField", emptyField);
		customFieldAct.putValue(PhonUIAction.NAME, "Custom");
		customFieldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Create a custom field");
		JMenuItem customFieldItem = new JMenuItem(customFieldAct);
		menu.add(customFieldItem);
		
		menu.show(addFieldButton, addFieldButton.getWidth(), addFieldButton.getHeight());
	}
	
	public void onAddField(PhonActionEvent pae) {
		if(pae.getData() == null)
			throw new IllegalArgumentException("field cannot be null");
		if(!(pae.getData() instanceof ResultListingField))
			throw new IllegalArgumentException("not a field!");
		
		ResultListingField field = (ResultListingField)pae.getData();
		ResultListing resList = getSection();
		
		resList.getField().add(field);
		
		// update list
		fieldList.setListData(resList.getField().toArray(new ResultListingField[0]));
		
		fieldList.setSelectedValue(field, true);
		
	}
	
	public void onDelField(PhonActionEvent pae) {
		int selectedField = fieldList.getSelectedIndex();
		
		ResultListing resList = getSection();
		if(selectedField >= 0 && selectedField < resList.getField().size()) {
			ResultListingField removed = resList.getField().remove(selectedField);
			
			// update list
			fieldList.setListData(resList.getField().toArray(new ResultListingField[0]));
			scriptPanels.remove(removed);
		}
	}
	
	public void onMoveFieldUp(PhonActionEvent pae) {
		int selectedField = fieldList.getSelectedIndex();
		
		ResultListing resList = getSection();
		if(selectedField > 0 && selectedField < resList.getField().size()) {
			ResultListingField element = resList.getField().remove(selectedField);
			resList.getField().add(selectedField-1, element);
			
			// update list
			fieldList.setListData(resList.getField().toArray(new ResultListingField[0]));
			fieldList.setSelectedValue(element, true);
		}
	}
	
	public void onMoveFieldDown(PhonActionEvent pae) {
		int selectedField = fieldList.getSelectedIndex();
		
		ResultListing resList = getSection();
		if(selectedField >= 0 && selectedField < resList.getField().size()-1) {
			ResultListingField element = resList.getField().remove(selectedField);
			resList.getField().add(selectedField+1, element);
			
			// update list
			fieldList.setListData(resList.getField().toArray(new ResultListingField[0]));
			fieldList.setSelectedValue(element, true);
		}
	}
	
	/* 
	 * Return the editor mimetype for the given script engine
	 */
	private String getEditorMimetypeForScriptEngineFactory(ScriptEngineFactory factory) {
		String retVal = null;
		
		// look for the first listed mimetype starting with 'text/'
		for(String mimetype:factory.getMimeTypes()) {
			if(mimetype.startsWith("text/")) {
				retVal = mimetype;
				break;
			}
		}
		// didn't find one - use 'text/<lang>'
		if(retVal == null) {
			retVal = "text/" + factory.getLanguageName().toLowerCase();
		}
		
		return retVal;
	}
	
	/**
	 * Property changes for result listing fields
	 */
	private class ScriptPanelListener implements PropertyChangeListener {
		
		private ResultListingField field;
		
		public ScriptPanelListener(ResultListingField field) {
			this.field = field;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String propName = evt.getPropertyName();
//			String value = (evt.getNewValue() != null ? evt.getNewValue().toString() : "");
			if(propName.equals(ScriptPanel.SCRIPT_PROP)) {
				ScriptPanel panel = (ScriptPanel)evt.getSource();
				field.getFieldValue().setScript(panel.getScript().getScript());
			} else if(propName.startsWith(ScriptPanel.PARAM_PREFIX)) {
				String paramid = propName.substring(ScriptPanel.PARAM_PREFIX.length()+1);
				String value = (evt.getNewValue() != null ? evt.getNewValue().toString() : "");
				
				ScriptParameter sp = null;
				for(ScriptParameter savedParam:field.getFieldValue().getParam()) {
					if(savedParam.getName().equals(paramid)) {
						sp = savedParam;
					}
				}
				if(sp == null) {
					sp = (new ObjectFactory()).createScriptParameter();
					sp.setName(paramid);
					field.getFieldValue().getParam().add(sp);
				}
				sp.setContent(value);
			}
		}
		
	}
	
	/**
	 * List selection listener
	 */
	private class FieldListSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int selectedIndex =
				fieldList.getSelectedIndex();
			if(selectedIndex >= 0) {
				nameField.setEnabled(true);
				
				ResultListing resList = getSection();
				ResultListingField field = resList.getField().get(selectedIndex);
				
				updateForm(field);
			} else {
				nameField.setEnabled(false);
			}
		}
		
	}
	
	/*
	 * Methods for updating the currently selected field
	 */
	private void updateCurrentFieldName() {
		int selectedIdx = fieldList.getSelectedIndex();
		if(selectedIdx >= 0) {
			ResultListing resList = getSection();
			ResultListingField selectedField = resList.getField().get(selectedIdx);
			
			selectedField.setTitle(nameField.getText());
			
			// update cell in table
			fieldList.repaint();
		}
	}
	
	/**
	 * Entity object for scripting language selection
	 */
	private class ScriptEngineInfo {
		
		private ScriptEngineFactory factory;
	
		private String mimeType;
		
		private String title;
		
		public ScriptEngineInfo(ScriptEngineFactory factory,
				String mimetype, String title) {
			this.factory = factory;
			this.mimeType = mimetype;
			this.title = title;
		}

		public ScriptEngineFactory getFactory() {
			return factory;
		}

		public void setFactory(ScriptEngineFactory factory) {
			this.factory = factory;
		}

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}
	
	/**
	 * List cell renderer
	 */
	private class FieldListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList arg0, Object arg1,
				int arg2, boolean arg3, boolean arg4) {
			JLabel retVal = (JLabel) super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
			
			ResultListingField f = (ResultListingField)arg1;
			
			if(f.getTitle().length() == 0)
				retVal.setText("<html><i>No title</i></html>");
			else
				retVal.setText(f.getTitle());
			
			return retVal;
		}
		
	}
	
	/**
	 * Name field handler
	 */
	private class NameFieldListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateCurrentFieldName();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateCurrentFieldName();
		}
		
	}
	
	/**
	 * Format listener
	 * 
	 */
	private class FormatHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ResultListingFormatType format = ResultListingFormatType.TABLE;
			
			if(listOptBox.isSelected())
				format = ResultListingFormatType.LIST;
			
			ResultListing resList = getSection();
			resList.setFormat(format);
		}
		
	}
}
