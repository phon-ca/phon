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

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.PhonConstants;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog displayed when creating a new corpus.
 *
 */
public class NewCorpusDialog extends JDialog {

	private static final long serialVersionUID = -4292768829721671922L;
	
	private ProjectWindow projectWindow;
	
	private JComponent titleStep1;
	private JComponent titleStep2;
	private JTextField txtName = new JTextField();
	private JTextArea txtDescription = new JTextArea();
	private JButton btnCancel = new JButton();
	private JButton btnCreateCorpus = new JButton();
	
	private boolean wasCanceled = false;

	/**
	 * Default constructor
	 */
	public NewCorpusDialog(ProjectWindow projectWindow) {
		super(projectWindow);
		setTitle(projectWindow.getProject().getName() + " : New Corpus");
		this.projectWindow = projectWindow;
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		initializePanel();
	}

	/**
	 * Adds fill components to empty cells in the first row and first column
	 * of the grid. This ensures that the grid spacing will be the same as
	 * shown in the designer.
	 * 
	 * @param cols
	 *            an array of column indices in the first row where fill
	 *            components should be added.
	 * @param rows
	 *            an array of row indices in the first column where fill
	 *            components should be added.
	 */
	void addFillComponents(Container panel, int[] cols, int[] rows) {
		Dimension filler = new Dimension(10, 10);

		boolean filled_cell_11 = false;
		CellConstraints cc = new CellConstraints();
		if (cols.length > 0 && rows.length > 0) {
			if (cols[0] == 1 && rows[0] == 1) {
				/** add a rigid area */
				panel.add(Box.createRigidArea(filler), cc.xy(1, 1));
				filled_cell_11 = true;
			}
		}

		for (int index = 0; index < cols.length; index++) {
			if (cols[index] == 1 && filled_cell_11)
				continue;
			panel.add(Box.createRigidArea(filler), cc.xy(cols[index], 1));
		}

		for (int index = 0; index < rows.length; index++) {
			if (rows[index] == 1 && filled_cell_11)
				continue;
			panel.add(Box.createRigidArea(filler), cc.xy(1, rows[index]));
		}
	}

	public JPanel createPanel() {
		JPanel jpanel1 = new JPanel();
		EmptyBorder emptyborder1 = new EmptyBorder(5, 5, 5, 5);
		jpanel1.setBorder(emptyborder1);
		FormLayout formlayout1 = new FormLayout(
				"FILL:25PX:NONE,FILL:DEFAULT:GROW(1.0)",
				"CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:20PX:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:PREF:GROW,CENTER:20PX:NONE,CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		DefaultComponentFactory fac = DefaultComponentFactory.getInstance();

		titleStep1 = fac.createSeparator("Step 1");
		titleStep1.setName("titleStep1");
		jpanel1.add(titleStep1, cc.xywh(1, 1, 2, 1));

		titleStep2 = fac.createSeparator("Step 2");
		titleStep2.setName("titleStep2");
		jpanel1.add(titleStep2, cc.xywh(1, 5, 2, 1));

		txtName.setName("txtName");
		jpanel1.add(txtName, cc.xy(2, 3));

		JScrollPane paneDesc = new JScrollPane(txtDescription);
		jpanel1.add(paneDesc, cc.xy(2, 7));

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Enter a name for the new corpus:");
		jpanel1.add(jlabel1, cc.xy(2, 2));

		JLabel jlabel2 = new JLabel();
		jlabel2.setText("Enter a description for the new corpus:");
		jpanel1.add(jlabel2, cc.xy(2, 6));

		btnCreateCorpus.setActionCommand("Create");
		btnCreateCorpus.setName("btnCreateCorpus");
		btnCreateCorpus.setText("Ok");
		btnCreateCorpus.addActionListener(e -> {
			if(validateForm()) {
				wasCanceled = false;
				NewCorpusDialog.this.setVisible(false);
				NewCorpusDialog.this.dispose();
			}
		});
		btnCreateCorpus.setDefaultCapable(true);
		getRootPane().setDefaultButton(btnCreateCorpus);
		
		btnCancel.setActionCommand("Cancel");
		btnCancel.setName("btnCancel");
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(e -> {
			wasCanceled = true;
			NewCorpusDialog.this.setVisible(false);
			NewCorpusDialog.this.dispose();
		});
		
		JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(btnCreateCorpus, btnCancel);
		
		jpanel1.add(buttonBar, cc.xyw(1, 9, 2));

		addFillComponents(jpanel1, new int[] { 2 }, new int[] { 2,3,4,6,7,8 });
		return jpanel1;
	}

	private void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(NewCorpusDialog.this);
		
		NativeDialogs.showDialog(props);
	}
	
	/**
	 * Initializer
	 */
	protected void initializePanel() {
		setLayout(new BorderLayout());
		add(new DialogHeader(getTitle(), "Create a new corpus."), BorderLayout.NORTH);
		add(createPanel(), BorderLayout.CENTER);
		
		setSize(new Dimension(525, 400));
		setResizable(false);
	}
	
	public boolean wasCanceled() {
		return this.wasCanceled;
	}
	
	public String getCorpusName() {
		return this.txtName.getText();
	}
	
	public String getCorpusDescription() {
		return this.txtDescription.getText();
	}
	
	public boolean validateForm() {
		final String name = getCorpusName();
		if (name == null || name.length() == 0) {
			showMessage(
				"New Corpus",
				"You must specify a non-empty corpus name!");
			return false;
		}
		
		// make sure corpus name does not contain illegal characters
		boolean invalid = false;
		if(name.indexOf('.') >= 0) {
			invalid = true;
		}
		for(char invalidChar:PhonConstants.illegalFilenameChars) {
			if(name.indexOf(invalidChar) >= 0) {
				invalid = true;
				break;
			}
		}
		
		if(invalid) {
			showMessage(
					"New Corpus",
					"Corpus name includes illegal characters.");
			return false;
		}
		
		if (projectWindow.getProject().getCorpora().contains(name)) {
			showMessage("New Corpus",
					"The corpus name you specified already exists.");
			return false;
		}
		return true;
	}
	
}