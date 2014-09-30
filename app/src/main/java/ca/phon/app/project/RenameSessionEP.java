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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PhonConstants;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@PhonPlugin(name="default")
public class RenameSessionEP implements IPluginEntryPoint {
	
	private static final Logger LOGGER = Logger
			.getLogger(RenameSessionEP.class.getName());
	
	private Project project;
	
	private final static String EP_NAME = "RenameSession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	public class RenameSessionDialog extends JDialog
	{
		private final JButton btnRenameSession = new JButton();
		private final JButton btnCancel = new JButton();
		private final JComboBox cmbCorpus = new JComboBox();
		private final JComboBox cmbSession = new JComboBox();
		private final JTextField txtName = new JTextField();

		/**
		 * Default constructor
		 */
		public RenameSessionDialog() {
			super();
			setTitle("Rename Session");
			setModal(false);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
			initializePanel();
		}
		
		/**
		 * Constructor. Default selects a corpus and session
		 * @param corpus   the corpus to select
		 * @param session  the session to select
		 */
		public RenameSessionDialog(String corpus, String session) {
			this();
			cmbCorpus.setSelectedItem(corpus);
			updateSessionList();
			cmbSession.setSelectedItem(session);
		}

		/**
		 * Adds fill components to empty cells in the first row and first column of the grid.
		 * This ensures that the grid spacing will be the same as shown in the designer.
		 * @param cols an array of column indices in the first row where fill components should be added.
		 * @param rows an array of row indices in the first column where fill components should be added.
		 */
		void addFillComponents( Container panel, int[] cols, int[] rows ) {
			Dimension filler = new Dimension(10,10);

			boolean filled_cell_11 = false;
			CellConstraints cc = new CellConstraints();
			if ( cols.length > 0 && rows.length > 0 ) {
				if ( cols[0] == 1 && rows[0] == 1 ) {
					/** add a rigid area  */
					panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
					filled_cell_11 = true;
				}
			}

			for( int index = 0; index < cols.length; index++ ) {
				if ( cols[index] == 1 && filled_cell_11 )
					continue;
				panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
			}

			for( int index = 0; index < rows.length; index++ ) {
				if ( rows[index] == 1 && filled_cell_11 )
					continue;
				panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
			}
		}

		public JPanel createPanel()
		{
			JPanel jpanel1 = new JPanel();
			EmptyBorder emptyborder1 = new EmptyBorder(5,5,5,5);
			jpanel1.setBorder(emptyborder1);
			FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:20PX:NONE,CENTER:DEFAULT:NONE");
			CellConstraints cc = new CellConstraints();
			jpanel1.setLayout(formlayout1);

			DefaultComponentFactory fac = DefaultComponentFactory.getInstance();

			JComponent titledseparator1 = fac.createSeparator("Step 1");
			jpanel1.add(titledseparator1,cc.xywh(1,1,2,1));

			JComponent titledseparator2 = fac.createSeparator("Step 2");
			jpanel1.add(titledseparator2,cc.xywh(1,5,2,1));

			JComponent titledseparator3 = fac.createSeparator("Step 3");
			jpanel1.add(titledseparator3,cc.xywh(1,9,2,1));

			JLabel jlabel1 = new JLabel();
			jlabel1.setText("Select the corpus containing the session you wish to rename:");
			jpanel1.add(jlabel1,cc.xy(2,2));

			JLabel jlabel2 = new JLabel();
			jlabel2.setText("Select the session you would like to rename:");
			jpanel1.add(jlabel2,cc.xy(2,6));

			JLabel jlabel3 = new JLabel();
			jlabel3.setText("Enter the new name for the session:");
			jpanel1.add(jlabel3,cc.xy(2,10));

			// Add corpus names to list
			final List<String> corpora = project.getCorpora();
			for(String corpusName : corpora)
				cmbCorpus.addItem(corpusName);
			
			cmbCorpus.setName("cmbCorpus");
			cmbCorpus.addItemListener(new CorpusChangeListener());
			jpanel1.add(cmbCorpus,cc.xy(2,3));

			cmbSession.setName("cmbSession");
			jpanel1.add(cmbSession,cc.xy(2,7));
			cmbSession.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if(cmbSession.getSelectedItem() != null) {
						String sessionName = cmbSession.getSelectedItem().toString();
						txtName.setText(sessionName);
					}
				}
				
			});

			txtName.setName("txtName");
			jpanel1.add(txtName,cc.xy(2,11));

			btnRenameSession.setActionCommand("Rename");
			btnRenameSession.setName("btnRenameSession");
			btnRenameSession.setText("Ok");
			btnRenameSession.addActionListener(new RenameSessionListener());
			getRootPane().setDefaultButton(btnRenameSession);
			
			btnCancel.setActionCommand("Cancel");
			btnCancel.setName("btnCancel");
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new CancelListener());

			JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(btnRenameSession, btnCancel);
			jpanel1.add(buttonBar, cc.xyw(1, 13, 2));

			addFillComponents(jpanel1,new int[]{ 2 },new int[]{ 2,3,4,6,7,8,10,11,12 });
			return jpanel1;
		}

		/**
		 * Initializer
		 */
		protected void initializePanel() {
			setLayout(new BorderLayout());
			add(new DialogHeader(getTitle(), "Rename a session."), BorderLayout.NORTH);
			add(createPanel(), BorderLayout.CENTER);
			
			setSize(new Dimension(525, 360));
			setResizable(false);
			
			updateSessionList();
		}

		/**
		 * 
		 */
		private void updateSessionList() {
			// Clear the current list of sessions
			cmbSession.removeAllItems();

			// Update with the new list of sessions
			String corpusName = (String)cmbCorpus.getSelectedItem();
			final List<String> sessions = project.getCorpusSessions(corpusName);
			for(String sessionName : sessions)
				cmbSession.addItem(sessionName);
		}

		/**
		 * Corpus change listener
		 */
		private class CorpusChangeListener implements ItemListener {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				updateSessionList();
			}
		}

		/**
		 * Rename session (`Ok`) button listener
		 */
		private class RenameSessionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String corpusName = (String)cmbCorpus.getSelectedItem();
				String oldName = (String)cmbSession.getSelectedItem();				
				String newName = txtName.getText().trim();
				
				// make sure corpus name does not contain illegal characters
				boolean invalid = false;
				if(newName.indexOf('.') >= 0) {
					invalid = true;
				}
				for(char invalidChar:PhonConstants.illegalFilenameChars) {
					if(newName.indexOf(invalidChar) >= 0) {
						invalid = true;
						break;
					}
				}
				
				if(invalid) {
					ToastFactory.makeToast("Corpus name includes illegal characters.").start(txtName);
					return;
				}

				renameSession(corpusName, oldName, newName);
				RenameSessionDialog.this.dispose();
			}
		}

		/**
		 * Cancel button listener
		 */
		private class CancelListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				RenameSessionDialog.this.dispose();
			}
		}
	}
	
	private void renameSession(String corpusName, String oldName, String newName) {
		final String fCorpusName = corpusName;
		final String fOldName = oldName;
		final String fNewName = newName;
		
		if (fNewName == null || fNewName.length() == 0) {
			ToastFactory.makeToast("You must specify a non-empty session name.").start();
			return;
		}

		// Run through the sessions to see if the corpus specified exists, and
		// and also make sure that the new name isn't the name of an existing
		// corpus
		if (project.getCorpusSessions(fCorpusName).contains(fNewName)) {
			ToastFactory.makeToast("A session with that name already exists.").start();
			return;
		}

		// Transfer XML data to the new session name
		Session session = null;
		try {
			session = project.openSession(fCorpusName, fOldName);
			session.setName(fNewName);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			ToastFactory.makeToast(e.getLocalizedMessage()).start();
			return;
		}
		
		UUID writeLock = null;
		try {
			writeLock = project.getSessionWriteLock(fCorpusName, fNewName);
			project.saveSession(fCorpusName, fNewName, session, writeLock);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			ToastFactory.makeToast(e.getLocalizedMessage()).start();
		} finally {
			if(writeLock != null) {
				try {
					project.releaseSessionWriteLock(fCorpusName, fNewName, writeLock);
				} catch (IOException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
				writeLock = null;
			}
		}
		
		try {
			writeLock = project.getSessionWriteLock(fCorpusName, fOldName);
			project.removeSession(fCorpusName, fOldName, writeLock);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			ToastFactory.makeToast(e.getLocalizedMessage()).start();
		} finally {
			if(writeLock != null) {
				try {
					project.releaseSessionWriteLock(fCorpusName, fOldName, writeLock);
				} catch (IOException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		final EntryPointArgs args = new EntryPointArgs(initInfo);
		if(args.getProject() == null)
			throw new IllegalArgumentException("Project property not set.");
		
		project = args.getProject();
		
		final String corpusName = args.getCorpus();
		final String oldName = args.getSession().getName();
		
		if(args.get("newSession") == null) {
			final Runnable onEDT = new Runnable() {
				
				@Override
				public void run() {
					JDialog dlg =
							(corpusName == null || oldName == null ? new RenameSessionDialog() : new RenameSessionDialog(corpusName, oldName));		
					dlg.setModal(true);
					dlg.setVisible(true);
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				onEDT.run();
			else
				SwingUtilities.invokeLater(onEDT);
		} else {
			String newSession = initInfo.get("newSession").toString();
			renameSession(corpusName, oldName, newSession);
		}		
	}

}

