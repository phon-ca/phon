package ca.phon.app.session.editor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Transcriber;

/**
 * SessionEditor entry point
 * 
 * 
 */
@PhonPlugin(name="Session Info")
public class SessionEditorEP implements IPluginEntryPoint {
	
	private final static String EP_NAME = "SessionEditor";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		final Project project = epArgs.getProject();
		final Session session = epArgs.getSession();
		// Are we in blind mode?
		final boolean blindMode = 
				(args.get("blindmode") != null ? (Boolean)args.get("blindmode") : false);
		
		final Runnable onEdt = new Runnable() {
			public void run() {
				showEditor(project, session, blindMode);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEdt.run();
		else
			SwingUtilities.invokeLater(onEdt);
	}

	public void showEditor(Project project, Session session, boolean blindMode) {
		Transcriber transcriber = null;
		if(blindMode) {
			// show transcriber selection dialog
			final TranscriberSelectionDialog tsd = new TranscriberSelectionDialog(session);
			tsd.setModal(true);
			tsd.setSize(new Dimension(400, 350));
			tsd.setLocationByPlatform(true);
			tsd.setVisible(true);
			
			// bail if dialog was canceled
			if(tsd.wasDialogCanceled()) return;
			
			// create a new transcriber if necessary
			if(tsd.isNewTranscriber()) {
				final SessionFactory factory = SessionFactory.newFactory();
				transcriber = factory.createTranscriber();
				transcriber.setUsername(tsd.getUsername());
				transcriber.setRealName(tsd.getRealName());
				transcriber.setUsePassword(tsd.isPasswordRequired());
				if(tsd.isPasswordRequired()) {
					transcriber.setPassword(tsd.getEncryptedPassword());
				}
				session.addTranscriber(transcriber);
			} else {
				transcriber = session.getTranscriber(tsd.getUsername());
			}
		}
		
		final SessionEditor editor = new SessionEditor(project, session, transcriber);
		editor.pack();
		editor.setLocationByPlatform(true);
		editor.setVisible(true);
	}
	
	private class PasswordDialog extends JDialog {
		
		/** The password field */
		private JPasswordField password;
		
		private boolean wasDialogCanceled;
		
		private JButton cancelButton;
		private JButton okButton;
		
		/** The user */
		private final String user;
		
		public PasswordDialog(String user) {
			super();
			
			this.user = user;
			this.wasDialogCanceled = false;
			
			init();
		}
		
		private void init() {
			FormLayout layout = new FormLayout(
					"3dlu, left:pref:noGrow, right:pref:grow, 3dlu",
					"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
			this.setLayout(layout);
			
			String labelText = 
				"Enter password for transcriber: " + user;
			
			this.okButton = new JButton("Ok");
				this.okButton.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent arg0) {
						okHandler();
					}
					
				});
			rootPane.setDefaultButton(okButton);
				
			this.cancelButton = new JButton("Cancel");
				this.cancelButton.addActionListener(new ActionListener() {
	
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelHandler();
					}
					
				});
			
			this.password = new JPasswordField();
			
			CellConstraints cc = new CellConstraints();
			
			JComponent buttonBar = 
				com.jgoodies.forms.factories.ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
			this.add(new JLabel(labelText), cc.xy(2,2));
			this.add(password, cc.xyw(2, 4, 2));
			this.add(buttonBar, cc.xy(3, 6));
		}
		
		private void okHandler() {
			this.wasDialogCanceled = false;
			setVisible(false);
		}
		
		private void cancelHandler() {
			this.wasDialogCanceled = true;
			setVisible(false);
		}
		
		public String getPassword() {
			return new String(password.getPassword());
		}
		
		public boolean wasDialogCanceled() {
			return this.wasDialogCanceled;
		}
	}
}
