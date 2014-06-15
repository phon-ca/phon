/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 */
@PhonPlugin(
		name="default"
)
public class CopySessionEP implements IPluginEntryPoint {
	
	private static final Logger LOGGER = Logger
			.getLogger(CopySessionEP.class.getName());
	
	private Project project1;
	private String corpus1;
	private String session;
	private Project project2;
	private String corpus2;
	private boolean force = false;
	private boolean move = false;
	
	private final static String EP_NAME = "CopySession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	/**
	 * Move a session from one corpus to another.
	 * @param fOldCorpus  the corpus to take the session from
	 * @param fNewCorpus  the corpus to move the session to
	 * @param fSession    the session to move
	 */
	public void copySession(Project origProject,
			Project destProject, String oldCorpus, String newCorpus, String session,
			boolean overwrite)
	{
		final Project fOrigProject = origProject;
		final Project fDestProject = destProject;
		final String fOldCorpus = oldCorpus;
		final String fNewCorpus = newCorpus;
		final String fSession = session;
		final boolean fOverwrite = overwrite;
		
		if(!fOverwrite) {
			if(fDestProject.getCorpusSessions(newCorpus).contains(fSession)) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(CommonModuleFrame.getCurrentFrame());
				props.setOptions(MessageDialogProperties.okOptions);
				props.setTitle("Move Session");
				props.setMessage("Session already exists in the specified corpus. Session was not copied.");
				
				NativeDialogs.showMessageDialog(props);
				return;
			}
		}
	
		try {
			final Session s = fOrigProject.openSession(fOldCorpus, fSession);
			s.setCorpus(fNewCorpus);
			
			final UUID writeLock = fDestProject.getSessionWriteLock(s);
			fDestProject.saveSession(s, writeLock);
			fDestProject.releaseSessionWriteLock(s, writeLock);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			ToastFactory.makeToast(e.getLocalizedMessage()).start();
		}
		
		// now delete the original
		if(move) {
			try {
				final UUID writeLock = fOrigProject.getSessionWriteLock(fOldCorpus, fSession);
				fOrigProject.removeSession(fOldCorpus, fSession, writeLock);
				fOrigProject.releaseSessionWriteLock(fOldCorpus, fSession, writeLock);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				ToastFactory.makeToast(e.getLocalizedMessage()).start();
			}
			
		}
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		final EntryPointArgs args = new EntryPointArgs(initInfo);
		
		project1 = args.getProject();
		corpus1 = args.getCorpus();
		session = args.getSession().getName();
		
		if(initInfo.get("destproject") != null)
			project2 = (Project)args.get("destproject");
		
		if(initInfo.get("destcorpus") != null)
			corpus2 = (String)initInfo.get("destcorpus");
		
		if(initInfo.get("overwrite") != null)
			force = (Boolean)initInfo.get("overwrite");
		
		if(initInfo.get("move") != null)
			move = (Boolean)initInfo.get("move");
		
		begin();
	}
	
	private void begin() {
		if(
				project1 == null ||
				project2 == null ||
				corpus1 == null ||
				corpus2 == null ||
				session == null) {
			final CopySessionForm csf = new CopySessionForm();
			
			if(project1 != null)
				csf.setSelectedProject(project1);
			
			if(corpus1 != null)
				csf.setSelectedCorpus(corpus1);
			
			if(session != null)
				csf.setSelectedSession(session);
			
			if(project2 != null)
				csf.setDestinationProject(project2);
			
			if(corpus2 != null)
				csf.setSelectedCorpus(corpus2);
			
			// show the window
			String titleString = 
				(move ? "Move " : "Copy ") + "Session";
			final CommonModuleFrame dialog = new CommonModuleFrame(titleString);
			
			// setup display
			FormLayout layout = new FormLayout(
					"fill:pref:grow, right:pref",
					"pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu");
			dialog.getContentPane().setLayout(layout);
			
			DialogHeader header = new DialogHeader(titleString, "");
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
					dialog.dispose();
					
					// copy the session
					copySession(
							csf.getSelectedProject(),
							csf.getDestinationProject(),
							csf.getSelectedCorpus(),
							csf.getDestinationCorpus(),
							csf.getSelectedSession(),
							force);
				}
			
			});
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
					dialog.dispose();
				}
				
			});
			
			CellConstraints cc = new CellConstraints();
			dialog.getContentPane().add(header, cc.xyw(1,1, 2));
			dialog.getContentPane().add(csf, cc.xyw(1, 3, 2));
			
			final ButtonBarBuilder builder = new ButtonBarBuilder();
			builder.addButton(okButton);
			builder.addButton(cancelButton);
			
			dialog.getContentPane().add(
					builder.getPanel(), cc.xy(2, 5));
			
			dialog.pack();
			dialog.setVisible(true);
		} else {
			// we have enough info, don't show dialog
			copySession(project1, project2, corpus1, corpus2, session, force);
		}
	}
}
