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
package ca.phon.app.query;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;

/**
 * Dialog for saving canned queries.
 */
public class SaveQueryDialog extends JDialog {
	
	private final static Logger LOGGER = Logger.getLogger(SaveQueryDialog.class.getName());

	private CommonModuleFrame parentFrame;

	private QueryScript queryScript;

	/** UI */
	private JTextField nameField;
	private JCheckBox includeFormOptionsBox;
	private JRadioButton saveInProjectBtn;
	private JRadioButton saveInUserDirBtn;
	private JRadioButton saveOtherBtn;
	private JButton saveBtn;
	private JButton cancelBtn;
	private JLabel projSaveLocField;
	private JLabel libSaveLocField;

	public SaveQueryDialog(CommonModuleFrame parent, QueryScript script) {
		super(parent);
		this.parentFrame = parent;
		this.queryScript = script;
		
		super.setTitle("Save query...");
		super.setResizable(false);

		init();
	}
	
	private Project getProject() {
		return parentFrame.getExtension(Project.class);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();

		retVal.width = 500;

		return retVal;
	}

	private void updateLocationFields() {
//		final PathExpander pe = new PathExpander();
		projSaveLocField.setText("<html><p>" + QueryScriptLibrary.projectScriptFolder(getProject()) + "</p></html>");
		saveInProjectBtn.setToolTipText(QueryScriptLibrary.projectScriptFolder(getProject()));
		libSaveLocField.setText("<html><p>" + PrefHelper.getUserDataFolder() + File.separator + "script" + "</p></html>");
		saveInUserDirBtn.setToolTipText(PrefHelper.getUserDataFolder() + File.separator + "script");
	}

	private void init() {
//		final PathExpander pe = new PathExpander();
		
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
		
		includeFormOptionsBox = new JCheckBox("Include current form settings");
		includeFormOptionsBox.setSelected(true);

		ButtonGroup btnGrp = new ButtonGroup();
		saveInProjectBtn = new JRadioButton("Save in project resources");
		btnGrp.add(saveInProjectBtn);
		saveInUserDirBtn = new JRadioButton("Save in user library");
		btnGrp.add(saveInUserDirBtn);
		saveOtherBtn = new JRadioButton("Save in another location...");
		btnGrp.add(saveOtherBtn);
		saveInUserDirBtn.setSelected(true);

		projSaveLocField = new JLabel();
//		projSaveLocField.setFont(projSaveLocField.getFont().deriveFont(10.0f));
		libSaveLocField = new JLabel();
//		libSaveLocField.setFont(libSaveLocField.getFont().deriveFont(10.0f));
		updateLocationFields();

		saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});
		super.getRootPane().setDefaultButton(saveBtn);

		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
			
		});

		final DialogHeader header =
				new DialogHeader("Save Query", "");
		JComponent btnBar = ButtonBarBuilder.buildOkCancelBar(saveBtn, cancelBtn);

		final FormLayout formLayout = new FormLayout(
				"3dlu, 12dlu, fill:pref:grow, 3dlu",
				"pref, pref, pref, pref, pref, pref, pref, pref, pref, pref");
		final CellConstraints cc = new CellConstraints();
		setLayout(formLayout);
		
		add(header, cc.xyw(2, 1, 2));
		
		add(new JLabel("Name: (without extension)"), cc.xyw(2, 2, 2));
		add(nameField, cc.xy(3, 3));
		add(includeFormOptionsBox, cc.xy(3, 4));
		
		add(saveInUserDirBtn, cc.xyw(2, 5, 2));
//		add(libSaveLocField, cc.xy(3, 6));
		
		add(saveInProjectBtn, cc.xyw(2, 7, 2));
//		add(projSaveLocField, cc.xy(3, 8));
		
		add(saveOtherBtn, cc.xyw(2, 9, 2));
		
		add(btnBar, cc.xyw(2, 10, 2));
	}

	private boolean checkForm() {
		boolean retVal = true;

		// make sure we have a name
		String scriptName = StringUtils.strip(nameField.getText());

		if (scriptName.length() == 0) {
			final Toast toast = ToastFactory.makeToast("Please provide a name");
			toast.start(nameField);
			retVal = false;
		}

		return retVal;
	}

	private File getSaveLocation() {
		File baseDir = null;

		if (saveInProjectBtn.isSelected()) {
			baseDir = new File(QueryScriptLibrary.projectScriptFolder(getProject()));
		} else if (saveInUserDirBtn.isSelected()) {
			baseDir = new File(PrefHelper.getUserDataFolder() + File.separator + "script");
		} else {
			final OpenDialogProperties props = new OpenDialogProperties();
			props.setParentWindow(this);
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

		final String suffix = 
				(includeFormOptionsBox.isSelected() ? ".xml": ".js");
		int fileIdx = 1;
		File scriptFile = new File(baseDir, getScriptName() + suffix);
		while (scriptFile.exists()) {
			scriptFile = new File(baseDir,
					getScriptName() + (fileIdx++) + suffix);
		}

//		if(scriptFile.exists()) {
//			// ask to overwrite
//			int retVal = 
//					NativeDialogs.showOkCancelDialogBlocking(parentFrame, null, "Overwrite file?", "Overwrite file at " + scriptFile.getAbsolutePath() + "?");
//			if(retVal == NativeDialogEvent.CANCEL_OPTION) {
//				return null;
//			}
//		}
		
		return scriptFile;
	}

	private void save() {
		if (checkForm()) {
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
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(this);
				props.setRunAsync(false);
				props.setTitle("Save failed");
				props.setHeader("Save failed");
				props.setMessage(ex.getLocalizedMessage());
				NativeDialogs.showMessageDialog(props);
				return;
			}
			this.setVisible(false);
		}
	}

	public void showDialog() {
		pack();
		super.setLocationRelativeTo(parentFrame);
		setVisible(true);
	}

	public String getScriptName() {
		return nameField.getText();
	}
	
}
