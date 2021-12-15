package ca.phon.app.project;

import ca.phon.project.Project;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.ToastFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.*;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rtextarea.ToolTipSupplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;

public class RenameProjectDialog extends JDialog {

	private final Project project;

	private JButton btnRenameProject;
	private JButton btnCancel;
	private JTextField projectNameField;

	public RenameProjectDialog(Project project) {
		super();
		setTitle("Rename Project");

		this.project = project;
		init();

		setModal(true);
	}

	private void init() {
		setLayout(new BorderLayout());

		DialogHeader header = new DialogHeader("Rename Project", "");

		projectNameField = new JTextField();
		projectNameField.setText(project.getName());

		JPanel jpanel1 = new JPanel();
		EmptyBorder emptyborder1 = new EmptyBorder(5,5,5,5);
		jpanel1.setBorder(emptyborder1);
		FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		DefaultComponentFactory fac = DefaultComponentFactory.getInstance();

		JComponent titledseparator1 = fac.createSeparator("Project Name:");
		jpanel1.add(titledseparator1,cc.xywh(1,1,2,1));
		jpanel1.add(projectNameField, cc.xywh(1, 2, 2, 1));

		final PhonUIAction okAct = new PhonUIAction(this, "onRenameProject");
		okAct.putValue(PhonUIAction.NAME, "Rename project");
		okAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Assign new project name");
		btnRenameProject = new JButton(okAct);

		final PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		cancelAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Cancel rename project");
		btnCancel = new JButton(cancelAct);

		JComponent btnPanel = ButtonBarBuilder.buildOkCancelBar(btnRenameProject, btnCancel);
		add(header, BorderLayout.NORTH);
		add(jpanel1, BorderLayout.CENTER);
		add(btnPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(btnRenameProject);
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	public void onRenameProject() {
		String projectName = projectNameField.getText();
		if(projectName.trim().length() == 0) {
			ToastFactory.makeToast("Invalid project name").start(projectNameField);
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		// check project name for filename issues
		try {
			File tempFile = File.createTempFile(projectName, ".test");
			tempFile.delete();
		} catch (IOException ex) {
			ToastFactory.makeToast("Invalid characters in project name").start(projectNameField);
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		close();

		project.setName(projectName);
	}

	public void onCancel() {
		close();
	}

}
