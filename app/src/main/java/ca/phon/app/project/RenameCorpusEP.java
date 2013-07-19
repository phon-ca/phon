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
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PhonConstants;
import ca.phon.worker.PhonWorker;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@PhonPlugin(name="default")
public class RenameCorpusEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(RenameCorpusEP.class.getName());
	
	private Project project;
	
	private final static String EP_NAME = "RenameCorpus";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	public class RenameCorpusDialog extends JDialog
	{
		private static final long serialVersionUID = 7843030451855600249L;
		
		private final JComboBox cmbCorpus = new JComboBox();
		private final JTextField txtName = new JTextField();
		private final JButton btnRenameCorpus = new JButton();
		private final JButton btnCancel = new JButton();

		/**
		 * Default constructor
		 */
		public RenameCorpusDialog() {
			super();
			setTitle("Rename Corpus");
			setModal(false);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
			initializePanel();
//			parentWindow = this;
		}
		
		/**
		 * Constructor. Defaults the selected corpus.
		 * @param corpus  the corpus to select
		 */
		public RenameCorpusDialog(String corpus) {
			this();
			cmbCorpus.setSelectedItem(corpus);
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

		public JPanel createPanel() {
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

			JLabel jlabel1 = new JLabel();
			jlabel1.setText("Select the corpus you wish to rename:");
			jpanel1.add(jlabel1,cc.xy(2,2));

			JLabel jlabel3 = new JLabel();
			jlabel3.setText("Enter the new name for the corpus:");
			jpanel1.add(jlabel3,cc.xy(2,6));

			// Add existing corpora to combo box
			final List<String> corpora = project.getCorpora();
			for(String corpusName : corpora)
				cmbCorpus.addItem(corpusName);

			cmbCorpus.setName("cmbCorpus");
			jpanel1.add(cmbCorpus,cc.xy(2,3));

			txtName.setName("txtName");
			jpanel1.add(txtName,cc.xy(2,7));

			btnRenameCorpus.setActionCommand("Rename");
			btnRenameCorpus.setName("btnRenameCorpus");
			btnRenameCorpus.setText("Ok");
			btnRenameCorpus.addActionListener(new RenameSessionListener());
			getRootPane().setDefaultButton(btnRenameCorpus);
			
			btnCancel.setActionCommand("Cancel");
			btnCancel.setName("btnCancel");
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new CancelListener());

			JComponent buttonBar = 
				ButtonBarFactory.buildOKCancelBar(btnRenameCorpus, btnCancel);
			jpanel1.add(buttonBar, cc.xyw(1, 9, 2));

			addFillComponents(jpanel1,new int[]{ 2 },new int[]{ 2,3,4,6,7,8 });
			return jpanel1;
		}

		/**
		 * Initializer
		 */
		protected void initializePanel() {
			setLayout(new BorderLayout());
			add(new DialogHeader(getTitle(), "Rename a corpus."), BorderLayout.NORTH);
			add(createPanel(), BorderLayout.CENTER);
			
			setSize(new Dimension(525, 280));
			setResizable(false);
		}

		/**
		 * Rename Session Listener
		 */
		private class RenameSessionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String oldName = (String)cmbCorpus.getSelectedItem();
				String newName = txtName.getText();
				
				// check new name for illegal characters
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
					showMessage("Rename Corpus", "Corpus name includes illegal characters.");
					return;
				}

				renameCorpus(oldName, newName);
				RenameCorpusDialog.this.dispose();
			}
		}

		/**
		 * Cancel button listener
		 */
		private class CancelListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				RenameCorpusDialog.this.dispose();
			}
		}
	}
	
	private void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		
		NativeDialogs.showDialog(props);
	}
	
	private void renameCorpus(String oldName, String newName) {
		final String fOldName = oldName;
		final String fNewName = newName;
		
		PhonWorker.getInstance().invokeLater(new Runnable() {
			@Override
			public void run() {
				if (fNewName == null || fNewName.length() == 0) {
					showMessage(
						"Rename Corpus",
						"You must specify a non-empty corpus name!");
					return;
				}
		
				if (project.getCorpora().contains(fNewName)) {
					showMessage(
						"Rename Corpus",
						"The new corpus name you specified already exists!");
					return;
				}
		
				// Create new corpus, transfer sessions over to it and delete
				// the old corpus
				try {
					project.reameCorpus(fOldName, fNewName);
				} catch(IOException e) {
					showMessage("Rename Corpus", 
							"Failed to rename corpus " + fOldName + ". Reason: " + e.getMessage());
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		});
	}

	private void begin() {
		if(project == null) return;
		
		JDialog dlg = new RenameCorpusDialog();
		dlg.setVisible(true);
		while(dlg.isVisible()) {
			try {
				Thread.sleep(500);
			} catch(Exception e) { break; }
		}
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		if(initInfo.get("project") == null)
			throw new IllegalArgumentException("Project project not set.");
		
		project = (Project)initInfo.get("project");
		
		if(initInfo.get("corpus") == null) {
			begin();
			return;
		}
		
		String corpusName = initInfo.get("corpus").toString();
		
		if(initInfo.get("newCorpus") == null) {
			JDialog dlg = new RenameCorpusDialog(corpusName);
			dlg.setVisible(true);
			while(dlg.isVisible()) {
				try {
					Thread.sleep(500);
				} catch(Exception e) { break; }
			}
			return;
		}
		
		String newName = initInfo.get("corpus").toString();
		
		renameCorpus(corpusName, newName);
	}

}
