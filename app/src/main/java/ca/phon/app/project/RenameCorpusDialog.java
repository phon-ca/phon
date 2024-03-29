/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.project;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PhonConstants;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RenameCorpusDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7843030451855600249L;
	
	private final JComboBox<String> cmbCorpus = new JComboBox<>();
	private final JTextField txtName = new JTextField();
	private final JButton btnRenameCorpus = new JButton();
	private final JButton btnCancel = new JButton();
	
	private boolean wasCanceled = false;
	
	private Project project;

	/**
	 * Default constructor
	 * 
	 * @param project
	 */
	public RenameCorpusDialog(Project project) {
		this(project, null);
	}
	
	/**
	 * Constructor. Defaults the selected corpus.
	 * @param project
	 * @param corpus  the corpus to select
	 */
	public RenameCorpusDialog(Project project, String corpus) {
		super();
		
		this.project = project;
		
		setTitle("Rename Corpus");
		setModal(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		initializePanel();
		cmbCorpus.setSelectedItem(corpus);
		txtName.setText(corpus);
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
		btnRenameCorpus.addActionListener( e -> {
			if(!validateForm()) {
				ToastFactory.makeToast("Invalid characters in name").start(txtName);
				return;
			}
			wasCanceled = false;
			setVisible(false);
			dispose();
		});
		getRootPane().setDefaultButton(btnRenameCorpus);
		
		btnCancel.setActionCommand("Cancel");
		btnCancel.setName("btnCancel");
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(e -> {
			wasCanceled = true;
			setVisible(false);
			dispose();
		});

		JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(btnRenameCorpus, btnCancel);
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
	
	public boolean wasCanceled() {
		return this.wasCanceled;
	}
	
	public String getCorpusName() {
		return this.cmbCorpus.getSelectedItem().toString();
	}
	
	public String getNewCorpusName() {
		return this.txtName.getText();
	}
	
	public boolean validateForm() {
		String newName = getNewCorpusName();
		// check new name for illegal characters
		// make sure corpus name does not contain illegal characters
		boolean valid = true;
		if(newName.indexOf('.') >= 0) {
			valid = false;
		}
		for(char invalidChar:PhonConstants.illegalFilenameChars) {
			if(newName.indexOf(invalidChar) >= 0) {
				valid = false;
				break;
			}
		}
		
		return valid;
	}

}
