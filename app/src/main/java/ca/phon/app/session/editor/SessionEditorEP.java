/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.autosave.Autosaves;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Transcriber;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.JCrypt;

/**
 * SessionEditor entry point
 * 
 * 
 */
@PhonPlugin(name="Session Info")
public class SessionEditorEP implements IPluginEntryPoint {
	
	private static final Logger LOGGER = Logger
			.getLogger(SessionEditorEP.class.getName());
	
	public final static String EP_NAME = "SessionEditor";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		final Project project = epArgs.getProject();
		
		String corpusName = epArgs.getCorpus();
		String sessionLoc = (String)epArgs.get(EntryPointArgs.SESSION_NAME);
		String sessionName = sessionLoc;
		if(corpusName == null && sessionLoc != null) {
			int firstDot = sessionLoc.indexOf('.');
			if(firstDot > 0) {
				corpusName = sessionLoc.substring(0, firstDot);
				sessionName = sessionLoc.substring(firstDot+1);
			}
		}
		
		final AtomicReference<Session> sessionRef = new AtomicReference<>();
		try {
			sessionRef.set(epArgs.getSession());
		} catch (IOException e1) {
			Toolkit.getDefaultToolkit().beep();
			LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setRunAsync(false);
			props.setTitle("Unable to open session");
			props.setHeader("Unabel to open session");
			props.setOptions(MessageDialogProperties.okCancelOptions);
			
			// unable to open, check autosave!
			final Autosaves autosaves = project.getExtension(Autosaves.class);
			if(autosaves != null && autosaves.hasAutosave(corpusName, sessionName)) {
				// ask to open autosave file
				props.setMessage("An autosave file was found for this session, open autosave file?");
				int retVal = NativeDialogs.showMessageDialog(props);
				if(retVal == 1) return;
				
				try {
					final Session autosaveSession = autosaves.openAutosave(corpusName, sessionName);
					sessionRef.set(autosaveSession);
				} catch (IOException e2) {
					LOGGER.log(Level.SEVERE, e2.getLocalizedMessage(), e2);
				}
			} else {
				props.setMessage(e1.getLocalizedMessage());
				props.setOptions(MessageDialogProperties.okOptions);
				NativeDialogs.showMessageDialog(props);
				
				return;
			}
		}
		
		// Are we in blind mode?
		final boolean blindMode = 
				(args.get("blindmode") != null ? (Boolean)args.get("blindmode") : false);
		
		final boolean grabFocus =
				(args.get("grabFocus") != null ? (Boolean)args.get("grabFocus") : true);
		
		final Runnable onEdt = new Runnable() {
			public void run() {
				showEditor(project, sessionRef.get(), blindMode, grabFocus);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEdt.run();
		else
			try {
				SwingUtilities.invokeAndWait(onEdt);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
	}

	/**
	 * @param project
	 * @param session
	 * @param blindMode
	 */
	public void showEditor(Project project, Session session, boolean blindMode, boolean grabFocus) {
		// look for an already open editor
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof SessionEditor) {
				final SessionEditor editor = (SessionEditor)cmf;
				if(editor.getProject() == project && 
						(editor.getSession().getCorpus().equals(session.getCorpus()) &&
								editor.getSession().getName().equals(session.getName()))) {
					editor.requestFocus();
					editor.toFront();
					return;
				}
			}
		}
		
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
				if(transcriber != null && transcriber.usePassword()) {
					final PasswordDialog dlg = new PasswordDialog(transcriber.getUsername());
					dlg.setModal(true);
					dlg.pack();
					dlg.setLocationRelativeTo(tsd);
					dlg.setVisible(true);
					
					// wait
					
					if(dlg.wasDialogCanceled()) return; // bail if dialog was cancelled
					
					char salt[] = new char[2];
					salt[0] = transcriber.getPassword().charAt(0);
					salt[1] = transcriber.getPassword().charAt(1);
					
					// check password
					final String passwd = JCrypt.crypt(dlg.getPassword(), new String(salt));
					if(!passwd.equals(transcriber.getPassword())) {
						final MessageDialogProperties props = new MessageDialogProperties();
						props.setRunAsync(false);
						props.setTitle("Incorrect password");
						props.setMessage("Password incorrect, please try again.");
						NativeDialogs.showMessageDialog(props);
						
						return;
					}
				}
			}
		}
		
		final SessionEditor editor = new SessionEditor(project, session, transcriber);
		
		// load editor perspective
		final RecordEditorPerspective prevPerspective = 
				RecordEditorPerspective.getPerspective(RecordEditorPerspective.LAST_USED_PERSPECTIVE_NAME);
		final RecordEditorPerspective perspective = 
				(prevPerspective != null ? prevPerspective : RecordEditorPerspective.getPerspective(RecordEditorPerspective.DEFAULT_PERSPECTIVE_NAME));
		editor.getViewModel().setupWindows(perspective);
		
		editor.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				editor.getViewModel().applyPerspective(perspective);
				
				if(grabFocus) {
					// XXX this code causes issues with result set editor focus in macosx
					if(editor.getViewModel().isShowing(RecordDataEditorView.VIEW_NAME)) {
						editor.getViewModel().getView(RecordDataEditorView.VIEW_NAME).requestFocus();
					} else {
						for(String viewName:editor.getViewModel().getViewNames()) {
							if(editor.getViewModel().isShowing(viewName)) {
								editor.getViewModel().getView(viewName).requestFocus();
								break;
							}
						}
					}
				}
				e.getWindow().removeWindowListener(this);
			}
			
		});
		
		// positioning is handled by applyPerspective
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
			super(CommonModuleFrame.getCurrentFrame());
			
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
				ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
			
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
