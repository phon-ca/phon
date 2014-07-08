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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.worker.PhonWorker;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
@PhonPlugin(name="default")
public class DeleteSessionEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER =
			Logger.getLogger(DeleteSessionEP.class.getName());
	
	private Project project;
	private String corpus;
	private String session;
	
	private final static String EP_NAME = "DeleteSession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	public class DeleteSessionDialog extends JDialog
	{
		private JComboBox cmbCorpus = new JComboBox();
		private JComboBox cmbSession = new JComboBox();
		private JButton btnDeleteSession = new JButton();
		private JButton btnCancel = new JButton();

		/**
		 * Default constructor
		 */
		public DeleteSessionDialog() {
			super();
			setTitle("Delete Session");
//			parentWindow = this;
			initializePanel();
		}

		/**
		 * Adds fill components to empty cells in the first row and first column of the grid.
		 * This ensures that the grid spacing will be the same as shown in the designer.
		 * @param cols an array of column indices in the first row where fill components should be added.
		 * @param rows an array of row indices in the first column where fill components should be added.
		 */
		void addFillComponents( Container panel, int[] cols, int[] rows )
		{
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
			FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:20PX:NONE,CENTER:DEFAULT:NONE");
			CellConstraints cc = new CellConstraints();
			jpanel1.setLayout(formlayout1);
			
			DefaultComponentFactory fac = DefaultComponentFactory.getInstance();

			JComponent titledseparator1 = fac.createSeparator("Step 1");
			jpanel1.add(titledseparator1,cc.xywh(1,1,2,1));

			JComponent titledseparator2 = fac.createSeparator("Step 2");
			jpanel1.add(titledseparator2,cc.xywh(1,5,2,1));

			JLabel jlabel1 = new JLabel();
			jlabel1.setText("Select the corpus containing the session you wish to delete:");
			jpanel1.add(jlabel1,cc.xy(2,2));

			JLabel jlabel2 = new JLabel();
			jlabel2.setText("Select the session you would like to delete:");
			jpanel1.add(jlabel2,cc.xy(2,6));

			// Add corpus names to list
			List<String> corpora = project.getCorpora();
			for(String corpusName : corpora)
				cmbCorpus.addItem(corpusName);
			
			
			cmbCorpus.addItemListener(new CorpusChangeListener());
			cmbCorpus.setName("cmbCorpus");
			jpanel1.add(cmbCorpus,cc.xy(2,3));

			cmbSession.setName("cmbSession");
			jpanel1.add(cmbSession,cc.xy(2,7));
			
			btnDeleteSession.setActionCommand("Rename");
			btnDeleteSession.setName("btnRenameSession");
			btnDeleteSession.setText("OK");
			btnDeleteSession.addActionListener(new DeleteSessionListener());
			getRootPane().setDefaultButton(btnDeleteSession);

			btnCancel.setActionCommand("Cancel");
			btnCancel.setName("btnCancel");
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new CancelListener());

			final FormLayout btnLayout = new FormLayout(
				"right:pref:grow,right:pref", "pref");	
			final JPanel btnPanel = new JPanel(btnLayout);
			btnPanel.add(btnDeleteSession, cc.xy(1,1));
			btnPanel.add(btnCancel, cc.xy(2,1));
			
			jpanel1.add(btnPanel, cc.xyw(1, 9, 2));

			addFillComponents(jpanel1,new int[]{ 2 },new int[]{ 2,3,4,6,7,8 });
			return jpanel1;
		}

		/**
		 * Initializer
		 */
		protected void initializePanel() {
			setLayout(new BorderLayout());
			add(new DialogHeader(getTitle(), "Delete a session."), BorderLayout.NORTH);
			add(createPanel(), BorderLayout.CENTER);
			
			setSize(new Dimension(525, 300));
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
				List<String> sessions = project.getCorpusSessions(corpusName);
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
		 * Delete Session Listener
		 */
		private class DeleteSessionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				deleteSession(
					(String)cmbCorpus.getSelectedItem(),
					(String)cmbSession.getSelectedItem());
				DeleteSessionDialog.this.dispose();
			}
		}

		/**
		 * Cancel button listener
		 */
		private class CancelListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				DeleteSessionDialog.this.dispose();
			}
		}
	}
	
	private void begin() {
		if(project == null) return;

		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				JDialog dlg = new DeleteSessionDialog();
				dlg.setModal(true);
				dlg.setVisible(true);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		if(initInfo.get("project") == null)
			throw new IllegalArgumentException("Project project not set.");
		
		project = (Project)initInfo.get("project");
		
		if(initInfo.get("corpusName") == null || initInfo.get("sessionName") == null)
			begin();
		else
			deleteSession(initInfo.get("corpusName").toString(), 
				initInfo.get("sessionName").toString());
	}
	
	private void deleteSession(String corpusName, String sessionName) {
		final String fCorpusName = corpusName;
		final String fSessionName = sessionName;
		
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setRunAsync(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setListener(new NativeDialogListener() {
			
			@Override
			public void nativeDialogEvent(NativeDialogEvent arg0) {
				if(arg0.getDialogResult() == 0) {
					UUID writeLock = null;
					try {
						writeLock = project.getSessionWriteLock(fCorpusName, fSessionName);
						project.removeSession(fCorpusName, fSessionName, writeLock);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					} finally {
						if(writeLock != null) {
							try {
								project.releaseSessionWriteLock(fCorpusName, fSessionName, writeLock);
							} catch (IOException e) {
								LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
							}
						}
					}
				}
			}
			
		});
		props.setMessage("Are you sure you want to delete this session? This action is not undoable.");
		props.setHeader("Delete Session");
		props.setOptions(MessageDialogProperties.yesNoOptions);
		NativeDialogs.showMessageDialog(props);
	}
}
