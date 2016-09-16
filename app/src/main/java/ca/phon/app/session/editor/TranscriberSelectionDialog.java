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
package ca.phon.app.session.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.session.Session;
import ca.phon.session.Transcriber;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.JCrypt;

public class TranscriberSelectionDialog extends JDialog {
	
	private static final long serialVersionUID = 4480663577002010254L;

	/** GUI Components */
	private JTextField realNameField;
	private JTextField usernameField;
	
	private JCheckBox passwordRequiredBox;
	private JPasswordField passwordField;
	private JPasswordField checkField;
	
	private JButton okButton;
	private JButton cancelButton;
	
	private JRadioButton newTranscriptButton;
	private JRadioButton existingTranscriptButton;
	
	private JList existingUserList;
	
	private Session session;
//	private SystemProperties transcriptDb;
	
	/**
	 * Constructor
	 */
	public TranscriberSelectionDialog(Session transcript) {
		super();
		
		this.session = transcript;
		
		initDialog();
	}
	
	/** Init display and listeners */
	private void initDialog() {
		// setup layout
		
		// layout will be seperated into two sections, existing
		// and new transcripts
		FormLayout outerLayout = new FormLayout(
				"3dlu, pref, right:pref:grow, 3dlu",
				"pref,  3dlu, top:pref:noGrow, 3dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref");
		this.getContentPane().setLayout(outerLayout);
		
		// create the 'new' panel first
		FormLayout newLayout = new FormLayout(
				"left:pref:noGrow, 3dlu, fill:pref:grow",
				"bottom:pref:noGrow, 3dlu, bottom:pref:noGrow, 3dlu, bottom:pref:noGrow, 3dlu, bottom:pref:noGrow, 3dlu, bottom:pref:noGrow, fill:pref:grow");
		JPanel newPanel = new JPanel(newLayout);
		
		this.newTranscriptButton = new JRadioButton();
			this.newTranscriptButton.setText("New Transcriber");
			this.newTranscriptButton.setSelected(true);
			this.newTranscriptButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					realNameField.setEnabled(true);
					usernameField.setEnabled(true);
					passwordRequiredBox.setEnabled(true);
					
					if(passwordRequiredBox.isSelected()) {
						passwordField.setEnabled(true);
						checkField.setEnabled(true);
					}
					
					existingUserList.setEnabled(false);
				}
				
			});
			
		this.existingTranscriptButton = new JRadioButton();
			this.existingTranscriptButton.setText("Existing Transcriber");
			this.existingTranscriptButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					realNameField.setEnabled(false);
					usernameField.setEnabled(false);
					passwordRequiredBox.setEnabled(false);
					passwordField.setEnabled(false);
					checkField.setEnabled(false);
					
					existingUserList.setEnabled(true);
				}
				
			});
		
		ButtonGroup bg = new ButtonGroup();
			bg.add(this.newTranscriptButton);
			bg.add(this.existingTranscriptButton);
		
		this.realNameField = new JTextField();
		
		this.usernameField = new JTextField();
		
		this.passwordRequiredBox = new JCheckBox();
		this.passwordRequiredBox.setText("Use password");
			this.passwordRequiredBox.setSelected(false);
			this.passwordRequiredBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					passwordField.setEnabled(passwordRequiredBox.isSelected());
					checkField.setEnabled(passwordRequiredBox.isSelected());
				}
				
			});
			
		this.passwordField = new JPasswordField();
			this.passwordField.setEnabled(false);
			
		this.checkField = new JPasswordField();
			this.checkField.setEnabled(false);
			
		CellConstraints cc = new CellConstraints();
		
		newPanel.add(new JLabel("Full Name:"), cc.xy(1,1));
		newPanel.add(this.realNameField, cc.xy(3, 1));
		
		newPanel.add(new JLabel("Username:"), cc.xy(1, 3));
		newPanel.add(this.usernameField, cc.xy(3, 3));
		
		newPanel.add(this.passwordRequiredBox, cc.xyw(1, 5, 3));
		
		newPanel.add(new JLabel("Password:"), cc.xy(1, 7));
		newPanel.add(this.passwordField, cc.xy(3, 7));
		newPanel.add(this.checkField, cc.xy(3, 9));
		
		// create the 'existing' panel
		FormLayout existingLayout = new FormLayout(
				// just a list
				"fill:pref:grow", "fill:pref:grow");
		JPanel existingPanel = new JPanel(existingLayout);
		
		List<String> existingUserData = new ArrayList<String>();
		for(Transcriber t:session.getTranscribers())
			existingUserData.add(t.getRealName() + " - " + t.getUsername());
		this.existingUserList = new JList(existingUserData.toArray());
			this.existingUserList.setEnabled(false);
		
		existingPanel.add(this.existingUserList, cc.xy(1,1));
		
		// create the button panel
		this.okButton = new JButton("OK");
			this.okButton.setDefaultCapable(true);
			this.okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(checkDialog()) {
						okHandler();
					}
				}
				
			});
		getRootPane().setDefaultButton(okButton);
			
		this.cancelButton = new JButton("Cancel");
			this.cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					cancelHandler();
				}
				
			});
		
		final JComponent bar = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		this.getContentPane().add(
				bar,
				cc.xy(3,9));
		
		this.getContentPane().add(this.newTranscriptButton, cc.xy(2, 1));
		this.getContentPane().add(newPanel, cc.xyw(2, 3, 2));
		this.getContentPane().add(this.existingTranscriptButton, cc.xy(2, 5));
		this.getContentPane().add(new JScrollPane(existingPanel), cc.xyw(2, 7, 2));
	}

	private boolean checkDialog() {
		if(newTranscriptButton.isSelected()) {
			// make sure a user/username is entered
			if(realNameField.getText().length() == 0) {
				final Toast toast = ToastFactory.makeToast("Please enter a name.");
				toast.start(realNameField);
				return false;
			}
			
			if(usernameField.getText().length() == 0) {
				final Toast toast = ToastFactory.makeToast("Please enter a username.");
				toast.start(usernameField);
				return false;
			}
			
			if(passwordRequiredBox.isSelected()) {
				// check to make sure passwords are the same and not empty
				if(passwordField.getPassword().length == 0) {
					final Toast toast = ToastFactory.makeToast("Please enter a password.");
					toast.start(passwordField);
					return false;
				}
				
				if(!(new String(passwordField.getPassword())).equals(new String(checkField.getPassword()))) {
					final Toast toast = ToastFactory.makeToast("Passwords do not match.");
					toast.start(checkField);
					return false;
				}
			}
		} else {
			// make sure a user is selected
			if(existingUserList.getSelectedIndex() < 0) {
				final Toast toast = ToastFactory.makeToast("Plese select a transcriber.");
				toast.start(existingUserList);
				return false;
			}
		}
		
		return true;
	}
	
	private boolean dialogCanceled = true;
	
	private void okHandler() {
		dialogCanceled = false;
		setVisible(false);
	}
	
	private void cancelHandler() {
		dialogCanceled = true;
		setVisible(false);
	}
	
	public boolean wasDialogCanceled() { return dialogCanceled; }
	
	public String getRealName() {
		if(newTranscriptButton.isSelected()) {
			return realNameField.getText();
		} else {
			String userListName = existingUserList.getSelectedValue().toString();
			StringTokenizer st = new StringTokenizer(userListName, "-");
			
			return StringUtils.strip(st.nextToken());
		}
	}
	
	public String getUsername() {
		if(newTranscriptButton.isSelected()) {
			return usernameField.getText();
		} else {
			String userListName = existingUserList.getSelectedValue().toString();
			StringTokenizer st = new StringTokenizer(userListName, "-");
			
			if(st.countTokens() == 2) {
				st.nextToken();
				
				return StringUtils.strip(st.nextToken());
			} else {
				return new String();
			}
		}
	}
	
	public boolean isNewTranscriber() {
		return newTranscriptButton.isSelected();
	}
	
	public boolean isPasswordRequired() {
		if(newTranscriptButton.isSelected()) {
			return passwordRequiredBox.isSelected();
		} else {
			return isPasswordRequired(getUsername());
		}
	}
	
	private boolean isPasswordRequired(String username) {
		boolean retVal = false;
		Transcriber t = session.getTranscriber(username);
		if(t != null) {
			retVal = t.usePassword();
		}
		return retVal;
	}
	
	public String getEncryptedPassword() {
		if(newTranscriptButton.isSelected()) {
			if(passwordRequiredBox.isSelected()) {
				char salt[] = JCrypt.randomSalt();
				String encryptedPass = JCrypt.crypt(
						new String(passwordField.getPassword()), new String(salt));
				return new String(encryptedPass);
			} else
				return null;
		} else {
			return getEncryptedPassword(getUsername());
		}
	}
	
	private String getEncryptedPassword(String username) {
		String retVal = "";
		Transcriber t = session.getTranscriber(username);
		if(t != null) {
			retVal = t.getPassword();
		}
		return retVal;
	}
}

