package ca.phon.app.query;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.project.Project;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.history.ParamSetType;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;

public class SaveQueryForm extends JPanel {
	
	private Project project;

	private QueryScript queryScript;

	private QueryHistoryManager queryHistoryManager;
	
	/** UI */
	private JTextField nameField;
	private JRadioButton nameQueryBtn;
	private JRadioButton saveInProjectBtn;
	private JRadioButton saveInUserDirBtn;
	private JRadioButton saveOtherBtn;
	private JLabel projSaveLocField;
	private JLabel libSaveLocField;

	public SaveQueryForm(Project project, QueryScript script, QueryHistoryManager queryHistoryManager) {
		super();
		this.project = project;
		this.queryScript = script;
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
				final ParamSetType existingParamSet = queryHistoryManager.getParamSetByName(nameField.getText().trim());
				if(existingParamSet != null) {
					final Toast toast = ToastFactory.makeToast("Query name already exists in history");
					toast.start(nameField);
					return false;
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
	
	private void nameEntryInQueryHistory() {
		try {
			queryHistoryManager.nameParamSet(nameField.getText(), queryScript);
			// XXX save query history
		} catch (PhonScriptException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public void save() {
		if(nameQueryBtn.isSelected()) {
			nameEntryInQueryHistory();
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
				LogUtil.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				
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
