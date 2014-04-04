package ca.phon.app.project;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.orthography.Orthography;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierViewItem;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PhonConstants;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class NewSessionDialog extends JDialog {
	
	private static final long serialVersionUID = 8888896161322222665L;

	private final static Logger LOGGER = Logger.getLogger(NewSessionDialog.class.getName());
	
	private JTextField txtName = new JTextField();
	private JComboBox cmbCorpus = new JComboBox();
	private JButton btnCreateCorpus = new JButton();
	private JButton btnCreateSession = new JButton();
	private JButton btnCancel = new JButton();

	private Project proj;
		
	/**
	 * Default constructor
	 */
	public NewSessionDialog(Project project) {
		super();
		this.proj = project;
		setTitle("New Session");
		setModal(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		initializePanel();
	}
	
	/**
	 * Constructor. Default selects the corpus.
	 */
	public NewSessionDialog(Project project, String corpusName) {
		this(project);
		cmbCorpus.setSelectedItem(corpusName);
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
		FormLayout formlayout1 = new FormLayout(
				"CENTER:25PX:NONE,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:20PX:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:20PX:NONE,CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);
		jpanel1.setBorder(new EmptyBorder(5, 5, 5, 5));

		DefaultComponentFactory fac = DefaultComponentFactory.getInstance();

		JComponent titledseparator1 = fac.createSeparator("Step 1");
		jpanel1.add(titledseparator1, cc.xywh(1, 1, 3, 1));

		JComponent titledseparator2 = fac.createSeparator("Step 2");
		jpanel1.add(titledseparator2, cc.xywh(1, 5, 3, 1));

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Enter a name for the new session:");
		jpanel1.add(jlabel1, cc.xywh(2, 2, 2, 1));

		txtName.setName("txtName");
		jpanel1.add(txtName, cc.xywh(2, 3, 2, 1));

		cmbCorpus.setName("cmbCorpus");
		jpanel1.add(cmbCorpus, cc.xy(2, 7));

//			ImageFactory imgFactory = ImageFactory.getInstance();
//			ImageIcon im = new ImageIcon(imgFactory.getImage("new_corpus", 16, 16));
		ImageIcon im = IconManager.getInstance().getIcon(
				"actions/list-add", IconSize.SMALL);
		btnCreateCorpus.setIcon(im);
		btnCreateCorpus.setName("btnCreateCorpus");
		btnCreateCorpus.addActionListener(new CreateCorpusListener());
		jpanel1.add(btnCreateCorpus, cc.xy(3, 7));

		JLabel jlabel2 = new JLabel();
		jlabel2.setText("Select a corpus to use for this session:");
		jpanel1.add(jlabel2, cc.xy(2, 6));

		btnCreateSession.setActionCommand("Create");
		btnCreateSession.setName("btnCreateSession");
		btnCreateSession.setText("Ok");
		btnCreateSession.setDefaultCapable(true);
		btnCreateSession.addActionListener(new CreateSessionListener());
		getRootPane().setDefaultButton(btnCreateSession);
		
		btnCancel.setActionCommand("Cancel");
		btnCancel.setName("btnCancel");
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(new CancelListener());
		
		final ButtonBarBuilder barBuilder = new ButtonBarBuilder();
		JComponent buttonBar = 
				barBuilder.addButton(btnCreateSession).addButton(btnCancel).build();
		jpanel1.add(buttonBar, cc.xyw(1, 9, 3));

		addFillComponents(jpanel1, new int[] { 2,3 }, new int[] { 2,3,4,6,7,8 });
		return jpanel1;
	}

	/**
	 * Initializer
	 */
	protected void initializePanel() {
		setLayout(new BorderLayout());
		add(new DialogHeader(getTitle(), "Create a new session."), BorderLayout.NORTH);
		add(createPanel(), BorderLayout.CENTER);
		
		setSize(new Dimension(525, 300));
		setResizable(false);

		updateCorporaList();
	}

	/**
	 * Updates the corpora list with the current project's corpora names.
	 */
	private void updateCorporaList() {
		// Clear out the combo box
		cmbCorpus.removeAllItems();
		
		final List<String> corporaNames = proj.getCorpora();
		for (String corpusName : corporaNames)
			cmbCorpus.addItem(corpusName);
	}

	/**
	 * Create corpus button listener.
	 */
	private class CreateCorpusListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			HashMap<String, Object> initInfo = new HashMap<String, Object>();
			initInfo.put("project", proj);
			
			try {
				PluginEntryPointRunner.executePlugin("NewCorpus", initInfo);
			} catch (PluginException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}

			updateCorporaList();
		}
	}

	/**
	 * Create session button listener.
	 */
	private class CreateSessionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			// Ensure a non-empty corpus name (description is optional)
			String sessionName = txtName.getText().trim();
			if (sessionName == null || sessionName.length() == 0) {
				showMessage(
					"New Session",
					"You must specify a non-empty session name!");
				return;
			}

			// make sure invalid characters are not present
			// make sure corpus name does not contain illegal characters
			boolean invalid = false;
			if(sessionName.indexOf('.') >= 0) {
				invalid = true;
			}
			for(char invalidChar:PhonConstants.illegalFilenameChars) {
				if(sessionName.indexOf(invalidChar) >= 0) {
					invalid = true;
					break;
				}
			}
			
			if(invalid) {
				showMessage(
						"New Session",
						"Session name includes illegal characters.");
				return;
			}
			
			String corpusName = (String) cmbCorpus.getSelectedItem();
			try {
				newSession(corpusName, sessionName);
				
				NewSessionDialog.this.dispose();
				
				// open the session
				HashMap<String, Object> initInfo = new HashMap<String, Object>();
				initInfo.put("project", proj);
				initInfo.put("corpusName", corpusName);
				initInfo.put("sessionName", sessionName);
				
				try {
					PluginEntryPointRunner.executePlugin("SessionEditor", initInfo);
				} catch (PluginException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					
					showMessage("Open Session", 
							"Could not open session. Reason: " + e.getMessage());
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				showMessage(
						"New Session",
						"Could not create session. Reason: " + e.getMessage());
			}
		}
	}
	
	private void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(this);
		
		NativeDialogs.showDialog(props);
	}

	private void newSession(String corpusName, String sessionName) 
			throws IOException {
		final String fCorpusName = corpusName;
		final String fSessionName = sessionName;
		
		final SessionFactory factory = SessionFactory.newFactory();
		final Session s = factory.createSession(fCorpusName, fSessionName);
		final Record r = factory.createRecord();
		r.addGroup();
		s.addRecord(r);
		
		final List<TierViewItem> tierView = new ArrayList<TierViewItem>();
		tierView.add(factory.createTierViewItem(SystemTierType.Orthography.getName(), true));
		tierView.add(factory.createTierViewItem(SystemTierType.IPATarget.getName(), true));
		tierView.add(factory.createTierViewItem(SystemTierType.IPAActual.getName(), true));
		tierView.add(factory.createTierViewItem(SystemTierType.Notes.getName(), true));
		tierView.add(factory.createTierViewItem(SystemTierType.Segment.getName(), true));
		s.setTierView(tierView);
		
		final UUID writeLock = proj.getSessionWriteLock(s);
		proj.saveSession(s, writeLock);
		proj.releaseSessionWriteLock(s, writeLock);
	}
	
	/**
	 * Cancel button listener.
	 */
	private class CancelListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			NewSessionDialog.this.dispose();
		}
	}
}