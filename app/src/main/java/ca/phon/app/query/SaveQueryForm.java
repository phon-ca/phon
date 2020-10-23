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
package ca.phon.app.query;

import java.awt.*;
import java.io.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.commons.lang3.*;

import com.jgoodies.forms.layout.*;

import ca.phon.app.log.*;
import ca.phon.project.*;
import ca.phon.query.history.*;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.history.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.toast.*;
import ca.phon.util.*;

public class SaveQueryForm extends JPanel {
	
	private Project project;

	private QueryScript queryScript;

	private QueryHistoryManager queryHistoryManager;
	
	private QueryHistoryManager stockQueries;
	
	/** UI */
	private JTextField nameField;
	private JRadioButton nameQueryBtn;
	private JRadioButton saveInProjectBtn;
	private JRadioButton saveInUserDirBtn;
	private JRadioButton saveOtherBtn;
	private JLabel projSaveLocField;
	private JLabel libSaveLocField;

	public SaveQueryForm(Project project, QueryScript script, QueryHistoryManager stockQueries, QueryHistoryManager queryHistoryManager) {
		super();
		this.project = project;
		this.queryScript = script;
		this.stockQueries = stockQueries;
		this.queryHistoryManager = queryHistoryManager;
		
		init();
	}
	
	private Project getProject() {
		return this.project;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();

		retVal.width = 500;

		return retVal;
	}

	private void updateLocationFields() {
		projSaveLocField.setText("<html><p>" + QueryScriptLibrary.projectScriptFolder(getProject()) + "</p></html>");
		saveInProjectBtn.setToolTipText(QueryScriptLibrary.projectScriptFolder(getProject()));
		libSaveLocField.setText("<html><p>" + PrefHelper.getUserDataFolder() + File.separator + "script" + "</p></html>");
		saveInUserDirBtn.setToolTipText(PrefHelper.getUserDataFolder() + File.separator + "script");
	}
	
	public JTextField getNameField() {
		return this.nameField;
	}

	private void init() {
		nameField = new JTextField();
		nameField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent de) {
				updateLocationFields();
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
				updateLocationFields();
			}

			@Override
			public void changedUpdate(DocumentEvent de) {
				updateLocationFields();
			}

		});
		
		ButtonGroup btnGrp = new ButtonGroup();
		nameQueryBtn = new JRadioButton("Name entry in query history");
		btnGrp.add(nameQueryBtn);
		saveInProjectBtn = new JRadioButton("Save in project resources");
		btnGrp.add(saveInProjectBtn);
		saveInUserDirBtn = new JRadioButton("Save in user library");
		btnGrp.add(saveInUserDirBtn);
		saveOtherBtn = new JRadioButton("Save in another location...");
		btnGrp.add(saveOtherBtn);
		
		nameQueryBtn.setSelected(true);

		projSaveLocField = new JLabel();
		libSaveLocField = new JLabel();
		updateLocationFields();
		
		final FormLayout formLayout = new FormLayout(
				"3dlu, 12dlu, fill:pref:grow, 3dlu",
				"pref, pref, pref, pref, pref, pref, pref, pref, pref");
		final CellConstraints cc = new CellConstraints();
		setLayout(formLayout);
		
		add(new JLabel("Name:"), cc.xyw(2, 2, 2));
		add(nameField, cc.xy(3, 3));
		
		add(nameQueryBtn, cc.xyw(2, 4, 2));
		
		add(saveInUserDirBtn, cc.xyw(2, 5, 2));
		
		add(saveInProjectBtn, cc.xyw(2, 7, 2));
		
		add(saveOtherBtn, cc.xyw(2, 9, 2));
	}

	public boolean checkForm() {
		// make sure we have a name
		String scriptName = StringUtils.strip(nameField.getText());

		if (scriptName.length() == 0 && !nameQueryBtn.isSelected()) {
			final Toast toast = ToastFactory.makeToast("Please provide a name");
			toast.start(nameField);
			return false;
		} else {
			if(nameQueryBtn.isSelected()) {
				ParamSetType existingNamedParamSet = queryHistoryManager.getParamSetByName(nameField.getText().trim());
				if(existingNamedParamSet == null)
					existingNamedParamSet = stockQueries.getParamSetByName(nameField.getText().trim());
				if(existingNamedParamSet != null) {
					final Toast toast = ToastFactory.makeToast("Query name already exists");
					toast.start(nameField);
					return false;
				}
				
				try {
					// check for existing stock query - don't rename
					final ParamSetType existingStockQuery = stockQueries.getParamSet(queryScript);
					if(existingStockQuery != null) {
						final Toast toast = ToastFactory.makeToast("Cannnot rename stock queries");
						toast.start(nameField);
						return false;
					}
				} catch (PhonScriptException e) {
					LogUtil.warning(e);
				}
			}
		}

		return true;
	}

	private File getSaveLocation() {
		File baseDir = null;

		if (saveInProjectBtn.isSelected()) {
			baseDir = new File(QueryScriptLibrary.projectScriptFolder(getProject()));
		} else if (saveInUserDirBtn.isSelected()) {
			baseDir = new File(PrefHelper.getUserDataFolder() + File.separator + "script");
		} else {
			final OpenDialogProperties props = new OpenDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setRunAsync(false);
			props.setTitle("Select save folder");
			props.setCanChooseDirectories(true);
			props.setCanChooseFiles(false);
			props.setCanCreateDirectories(true);
			props.setAllowMultipleSelection(false);
			
			final List<String> selectedDirs = NativeDialogs.showOpenDialog(props);
			if (selectedDirs.size() > 0) {
				baseDir = new File(selectedDirs.get(0));
			} else {
				return null;
			}
		}

		if (baseDir != null) {
			if (!baseDir.exists()) {
				baseDir.mkdir();
			}
		}

		final String suffix = ".xml";
		int fileIdx = 1;
		File scriptFile = new File(baseDir, getScriptName() + suffix);
		while (scriptFile.exists()) {
			scriptFile = new File(baseDir,
					getScriptName() + (fileIdx++) + suffix);
		}
		
		return scriptFile;
	}
	
	private void nameEntryInQueryHistory() throws IOException {
		String name = nameField.getText().trim().length() > 0 ? nameField.getText().trim() : null;
		try {
			queryHistoryManager.nameParamSet(name, queryScript);
		} catch (PhonScriptException e) {
			throw new IOException(e);
		}
		QueryHistoryManager.save(queryHistoryManager, queryScript);
	}

	public void save() {
		if(nameQueryBtn.isSelected()) {
			try {
				nameEntryInQueryHistory();
			} catch (IOException ex) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(ex.getLocalizedMessage(), ex);
				
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(CommonModuleFrame.getCurrentFrame());
				props.setRunAsync(false);
				props.setTitle("Save failed");
				props.setHeader("Save failed");
				props.setMessage(ex.getLocalizedMessage());
				NativeDialogs.showMessageDialog(props);
				return;
			}
		} else {
			File saveFile = getSaveLocation();
			if(saveFile == null) {
				return;
			}
			File parentDir = saveFile.getParentFile();
			if(!parentDir.exists())
				parentDir.mkdirs();
			try {
				QueryScriptLibrary.saveScriptToFile(queryScript, saveFile.getAbsolutePath());
				
				final QueryName queryName = new QueryName(saveFile.toURI().toURL());
				queryScript.putExtension(QueryName.class, queryName);
			} catch (IOException ex) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(ex.getLocalizedMessage(), ex);
				
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(CommonModuleFrame.getCurrentFrame());
				props.setRunAsync(false);
				props.setTitle("Save failed");
				props.setHeader("Save failed");
				props.setMessage(ex.getLocalizedMessage());
				NativeDialogs.showMessageDialog(props);
				return;
			}
		}
	}

	public String getScriptName() {
		return nameField.getText();
	}
	
}
