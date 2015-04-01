package ca.phon.app.session.editor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	public void showEditor(Project project, Session session, boolean blindMode) {
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
