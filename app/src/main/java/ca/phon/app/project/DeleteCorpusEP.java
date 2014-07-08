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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
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
public class DeleteCorpusEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(DeleteCorpusEP.class.getName());
	
	private Project project;
	private String corpus;
	
	private final static String EP_NAME = "DeleteCorpus";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	public class DeleteCorpusDialog extends JDialog
	{
		private static final long serialVersionUID = 4190462168864451914L;
		
		private JComboBox cmbCorpus = new JComboBox();
		private JButton btnDeleteCorpus = new JButton();
		private JButton btnCancel = new JButton();

		/**
		 * Default constructor
		 */
		public DeleteCorpusDialog() {
			super();
			setTitle("Delete Corpus");
			setModal(false);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
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
			FormLayout formlayout1 = new FormLayout("FILL:25PX:NONE,FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:20PX:NONE,CENTER:DEFAULT:NONE");
			CellConstraints cc = new CellConstraints();
			jpanel1.setLayout(formlayout1);

			DefaultComponentFactory fac = DefaultComponentFactory.getInstance();
			JComponent titledseparator1 = fac.createSeparator("Step 1");
			jpanel1.add(titledseparator1,cc.xywh(1,1,2,1));

			JLabel jlabel1 = new JLabel();
			jlabel1.setText("Select the corpus to delete:");
			jpanel1.add(jlabel1,cc.xy(2,2));

			// Add existing corpora to combo box
			final List<String> corpora = project.getCorpora();
			for(String corpusName : corpora)
				cmbCorpus.addItem(corpusName);
			
			
			cmbCorpus.setName("cmbCorpus");
			jpanel1.add(cmbCorpus,cc.xy(2,3));

			btnDeleteCorpus.setActionCommand("Rename");
			btnDeleteCorpus.setName("btnRenameCorpus");
			btnDeleteCorpus.setText("OK");
			btnDeleteCorpus.addActionListener(new DeleteCorpusListener());
			getRootPane().setDefaultButton(btnDeleteCorpus);

			btnCancel.setActionCommand("Cancel");
			btnCancel.setName("btnCancel");
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new CancelListener());

			final ButtonBarBuilder builder = new ButtonBarBuilder();
			builder.addButton(btnDeleteCorpus);
			builder.addButton(btnCancel);
			jpanel1.add(builder.build(), cc.xyw(1, 5, 2));
			
			addFillComponents(jpanel1,new int[]{ 2 },new int[]{ 2,3,4 });
			return jpanel1;
		}

		/**
		 * Initializer
		 */
		protected void initializePanel() {
			setLayout(new BorderLayout());
			add(new DialogHeader(getTitle(), "Delete a corpus."), BorderLayout.NORTH);
			add(createPanel(), BorderLayout.CENTER);
			
			setSize(new Dimension(525, 225));
			setResizable(false);
		}

		/**
		 * Delete Session Listener
		 */
		private class DeleteCorpusListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				deleteCorpus((String)cmbCorpus.getSelectedItem());
				DeleteCorpusDialog.this.dispose();
			}
		}

		/**
		 * Cancel button listener
		 */
		private class CancelListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent evt) {
				DeleteCorpusDialog.this.dispose();
			}
		}
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		if(initInfo.get("project") == null ||
				!(initInfo.get("project") instanceof Project))
			throw new IllegalArgumentException("Project property not set.");

		project = (Project)initInfo.get("project");
		
		if(initInfo.get("corpusName") == null)
			begin();
		else
			deleteCorpus(initInfo.get("corpusName").
					toString());
	}
	
	private void begin() {
		if(project == null ||
				corpus == null)
			return;
		
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				JDialog dlg = new DeleteCorpusDialog();
				dlg.setModal(true);
				dlg.setVisible(true);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}
	
	private void deleteCorpus(String corpusName) {
		final String fCorpusName = corpusName;
		
		PhonWorker.getInstance().invokeLater(new Runnable() {
			@Override
			public void run() {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(CommonModuleFrame.getCurrentFrame());
				props.setRunAsync(false);
				props.setHeader("Delete corpus: " + fCorpusName);
				props.setMessage("All sessions in this corpus will also be deleted! This action cannot be undone.");
				props.setOptions(MessageDialogProperties.okCancelOptions);
				int retVal = NativeDialogs.showMessageDialog(props);
				
				if(retVal == 0) {
					try {
						project.removeCorpus(fCorpusName);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		});
	}
}
