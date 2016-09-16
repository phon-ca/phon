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
package ca.phon.app.session.editor.view.ipa_validation;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXRadioGroup;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

/**
 * Auto-validate options.
 *
 */
public class AutoValidateDialog extends JDialog {
	
	private static final long serialVersionUID = -6377433669667286462L;

	public static enum AutoValidateReturnValue {
		OK,
		CANCEL
	};
	
	/* UI */
	private JCheckBox validateTargetBox;
	private JCheckBox validateActualBox;
	private JCheckBox overwriteDataBox;
	
	private JXRadioGroup<String> trGroup;
	
	private RecordFilterPanel recordFilterPanel;
	
	private DialogHeader header;
	
	private Project project;
	
	private Session session;
	
	private JButton okButton;
	
	private JButton cancelButton;
	
	private AutoValidateReturnValue retVal = AutoValidateReturnValue.CANCEL;
	
	
	/**
	 * Constructor
	 */
	public AutoValidateDialog(Project project, Session t) {
		super();
		super.setTitle("Phon : Auto-validate Session");
		this.project = project;
		this.session = t;
		init();
	}
	
	private void init() {
		header = new DialogHeader("Auto-validate Session", "Fill IPA tiers for the session.");
		
		FormLayout tierLayout = new FormLayout(
				"fill:pref:grow", "pref, pref, pref");
		CellConstraints cc = new CellConstraints();
		
		JPanel tierPanel = new JPanel(tierLayout);
		
		validateTargetBox = new JCheckBox("Auto-validate IPA Target");
		validateTargetBox.setSelected(true);
		
		validateActualBox = new JCheckBox("Auto-validate IPA Actual");
		validateActualBox.setSelected(true);
		
		overwriteDataBox = new JCheckBox("Overwrite existing data");
		overwriteDataBox.setSelected(false);
		
		tierPanel.add(validateTargetBox, cc.xy(1, 1));
		tierPanel.add(validateActualBox, cc.xy(1, 2));
		tierPanel.add(overwriteDataBox, cc.xy(1, 3));
		tierPanel.setBorder(BorderFactory.createTitledBorder("Tiers"));
		
		FormLayout trLayout = new FormLayout(
				"fill:pref:grow", "pref, pref, pref");
		JPanel trPanel = new JPanel(trLayout);
		
		trGroup = new JXRadioGroup<String>();
		trGroup.setLayoutAxis(BoxLayout.Y_AXIS);
		
		List<String> trNames = new ArrayList<String>();
		for(Transcriber tr:session.getTranscribers()) {
			trNames.add(getTrDisplayString(tr));
		}
		
//		ITranscriber[] trs = session.getTranscribers().toArray(new ITranscriber[0]);
		trGroup.setValues(trNames.toArray(new String[0]));
		if(trNames.size() > 0)
			trGroup.setSelectedValue(trNames.get(0));
		
		trPanel.add(new JLabel("If only one IPA transcript exists it is selected."), cc.xy(1,1));
		trPanel.add(new JLabel("If more than one IPA transcript exists, choose IPA for transcriber:"), cc.xy(1, 2));
		trPanel.add(trGroup, cc.xy(1,3));
		
		trPanel.setBorder(BorderFactory.createTitledBorder("IPA Selection"));
		
		recordFilterPanel = new RecordFilterPanel(project, session);
		recordFilterPanel.setBorder(BorderFactory.createTitledBorder("Record Selection"));
		FormLayout centerLayout = new FormLayout(
				"fill:pref:grow", "pref, pref, pref");
		JPanel centerPanel = new JPanel(centerLayout);
		
		centerPanel.add(tierPanel, cc.xy(1,1));
		centerPanel.add(trPanel, cc.xy(1,2));
		centerPanel.add(recordFilterPanel, cc.xy(1,3));
		
		// button bar
		PhonUIAction okAction = new PhonUIAction(this, "onOk");
		okAction.putValue(Action.NAME, "Ok");
		okAction.putValue(Action.SHORT_DESCRIPTION, "Perform auto-validation and close dialog");
		
		okButton = new JButton(okAction);
		
		PhonUIAction cancelAction = new PhonUIAction(this, "onCancel");
		cancelAction.putValue(Action.NAME, "Cancel");
		cancelAction.putValue(Action.SHORT_DESCRIPTION, "Cancel auto-validation and close dialog");
		
		cancelButton = new JButton(cancelAction);
		
		final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		
		setLayout(new BorderLayout());
		add(header, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(buttonBar, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(okButton);
	}
	
	public AutoValidateReturnValue showModalDialog() {
		this.setModal(true);
		pack();
		setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		setVisible(true);
		
		return retVal;
	}
	
	private String getTrDisplayString(Transcriber tr) {
		String displayName = "";
		if(tr.getRealName().length() > 0 && !tr.getRealName().equals("<Unknown>"))
			displayName += tr.getRealName();
		displayName += (displayName.length() > 0 ? " : " : "") + tr.getUsername();
		return displayName;
	}

	public AutoValidateTask getTask() {
		AutoValidateTask retVal = new AutoValidateTask(session);
		
		retVal.setValidateActual(validateActualBox.isSelected());
		retVal.setValidateTarget(validateTargetBox.isSelected());
		retVal.setOverwriteData(overwriteDataBox.isSelected());
		
		// get transcriber from string selection
		Transcriber prefTr = null;
		if(trGroup.getSelectedValue() != null) {
			for(Transcriber tr:session.getTranscribers()) {
				String trStr = getTrDisplayString(tr);
				if(trStr.equals(trGroup.getSelectedValue())) {
					prefTr = tr;
					break;
				}
			}
		}
		retVal.setPreferredTranscriber(prefTr);
		retVal.setRecordFilter(recordFilterPanel.getRecordFilter());
		
		return retVal;
	}
	
	public void onOk(PhonActionEvent pae) {
		retVal = AutoValidateReturnValue.OK;
		setVisible(false);
	}
	
	public void onCancel(PhonActionEvent pae) {
		retVal = AutoValidateReturnValue.CANCEL;
		setVisible(false);
	}
	
}
