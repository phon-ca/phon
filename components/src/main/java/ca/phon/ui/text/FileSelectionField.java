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
package ca.phon.ui.text;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.SystemColor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Special text field for selecting file/folders.
 */
public class FileSelectionField extends JPanel {
	
	private static final long serialVersionUID = 7059011387085702827L;

	/**
	 * Property for changes to the selected file
	 */
	public final static String FILE_PROP = "_selected_file_";
	
	protected PromptedTextField textField;
	
	/**
	 * Browse button
	 */
	private JButton browseButton;
	
	/**
	 * Should the path be validated?
	 */
	private boolean validatePath = true;
	
	/**
	 * Selection mode
	 */
	public static enum SelectionMode {
		FILES,
		FOLDERS;
	}
	private SelectionMode mode = SelectionMode.FILES;
	
	/**
	 * File filter
	 */
	private FileFilter fileFilter = null;
	
	/**
	 * Constructor
	 */
	public FileSelectionField() {
		super();
		setBackground(Color.white);
		init();
		textField.getDocument().addDocumentListener(validationListener);
		textField.addFocusListener(focusListener);
		textField.setDragEnabled(true);
		textField.setTransferHandler(new FileSelectionTransferHandler());
	}
	
	private void init() {
		final FormLayout layout = new FormLayout("fill:pref:grow, pref", "pref");
		final CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		textField = new PromptedTextField();
		add(textField, cc.xy(1,1));
		
		final ImageIcon browseIcon = 
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		final PhonUIAction browseAct = 
				new PhonUIAction(this, "onBrowse");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse...");
		browseAct.putValue(PhonUIAction.SMALL_ICON, browseIcon);
		browseButton = new JButton(browseAct);
		browseButton.putClientProperty("JButton.buttonType", "square");
		browseButton.setCursor(Cursor.getDefaultCursor());
		add(browseButton, cc.xy(2,1));
		
		setBorder(textField.getBorder());
		textField.setBorder(null);
		
		textField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final File f = getSelectedFile();
				setFile(f);
			}
		});
	}

	/**
	 * Return the selected file/folder
	 * 
	 * @return selected file or <code>null</code> 
	 *  if invalid/empty
	 */
	public File getSelectedFile() {
		File retVal = null;
		
		if(textField.getState() != FieldState.PROMPT) {
			String txt = textField.getText();
			if(txt.length() > 0) {
				retVal = new File(txt);
			}
		}
		
		return retVal;
	}

	/**
	 * Set the current file
	 * 
	 * @param file
	 */
	protected File lastSelectedFile = null;
	public void setFile(File f) {
		if(f == null) {
			textField.setText("");
			textField.setState(FieldState.PROMPT);
			
		} else {
			textField.setState(FieldState.INPUT);
			
			String path = f.getAbsolutePath();
			textField.setText(path);
		}
		super.firePropertyChange(FILE_PROP, lastSelectedFile, f);
		lastSelectedFile = f;
	}
	
	private void checkPath() {
		if(textField.getState() == FieldState.PROMPT || !validatePath) return;
		File f = getSelectedFile();
		boolean valid = true;
		String toolTip = "";
		if(f != null) {
			if(!f.exists()) {
				valid = false;
				toolTip = "File not found";
			}
	
			if(mode == SelectionMode.FILES) {
				valid &= f.isFile();
				if(!f.isFile()) {
					toolTip = "Not a regular file";
				}
			} else if(mode == SelectionMode.FOLDERS) {
				valid &= f.isDirectory();
				if(!f.isDirectory()) {
					toolTip = "Not a folder";
				}
			}
			
			if(fileFilter != null && !fileFilter.accept(f)) {
				valid = false;
				toolTip = "File type not accepted";
			}
			
			if(!valid) {
				setForeground(Color.red);
				setToolTipText(toolTip);
			} else {
				setForeground(SystemColor.textText);
				setToolTipText(f.getAbsolutePath());
			}
		}
	}
	
	public void setMode(SelectionMode mode) {
		this.mode = mode;
	}
	
	public SelectionMode getMode() {
		return this.mode;
	}
	
	public FileFilter getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	public String getText() {
		return textField.getText();
	}
	
	public void setText(String text) {
		textField.setText(text);
	}
	
	public PromptedTextField getTextField() {
		return this.textField;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		textField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}
	
	/**
	 * Open browse dialog.
	 */
	public void onBrowse() {
		String path = null;
		final File f = getSelectedFile();
		String parentPath = null;
		if(f != null) {
			parentPath = f.getParent();
		}
		
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setRunAsync(false);
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(mode == SelectionMode.FOLDERS);
		props.setCanChooseFiles(mode == SelectionMode.FILES);
		props.setCanCreateDirectories(true);
		if(getFileFilter() != null)
			props.setFileFilter(getFileFilter());
		
		final List<String> selections = NativeDialogs.showOpenDialog(props);
		if(selections != null && selections.size() == 1) {
			path = selections.get(0);
		}

		if(path != null) {
			textField.setState(FieldState.INPUT);
			setFile(new File(path));
		}
	}
	
//	/**
//	 * Listener to keep browse button at end of component
//	 */
//	private final ComponentListener resizeListener = new ComponentListener() {
//		
//		@Override
//		public void componentShown(ComponentEvent arg0) {
//			
//		}
//		
//		@Override
//		public void componentResized(ComponentEvent arg0) {
//			Runnable moveBtn = new Runnable() {
//				
//				@Override
//				public void run() {
//					browseButton.setBounds(
//							getWidth()-browseButton.getPreferredSize().width, 0,
//							browseButton.getPreferredSize().width, getHeight());
//				}
//			};
//			SwingUtilities.invokeLater(moveBtn);
//		}
//		
//		@Override
//		public void componentMoved(ComponentEvent arg0) {
//		}
//		
//		@Override
//		public void componentHidden(ComponentEvent arg0) {
//		}
//	};
	
	/**
	 * Document listener
	 */
	private final DocumentListener validationListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent arg0) {
			checkPath();
		}
		
		@Override
		public void insertUpdate(DocumentEvent arg0) {
			final Document doc = arg0.getDocument();
			checkPath();
		}
		
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			
		}
	};
	
//	private final KeyListener keyListener = new KeyListener() {
//		
//		@Override
//		public void keyTyped(KeyEvent e) {
//		}
//		
//		@Override
//		public void keyReleased(KeyEvent e) {
//			final PathExpander pe = new PathExpander();
//			String path = pe.expandPath(getText());
//			final File file = (path.length() > 0 ? new File(path) : null);
//			firePropertyChange(FILE_PROP, lastSelectedFile, file);
//			lastSelectedFile = file;
//		}
//		
//		@Override
//		public void keyPressed(KeyEvent e) {
//		}
//	};
//	
	/**
	 * Focus listener
	 */
	private final FocusListener focusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent arg0) {
			final File f = FileSelectionField.this.getSelectedFile();
			setFile(f);
		}
		
		@Override
		public void focusGained(FocusEvent arg0) {
		}
	};
	
	private class FileSelectionTransferHandler extends FileTransferHandler {

		private static final long serialVersionUID = 6799990443658389742L;

		@Override
		public boolean importData(JComponent comp, Transferable transferable) {
			File file = null;
			try {
				file = getFile(transferable);
			} catch (IOException e) {
				return false;
			}
			
			if(file != null) {
				if(getFileFilter() != null && !getFileFilter().accept(file)) return false;
				setFile(file);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public File getFile(Transferable transferable) throws IOException {
			File retVal = super.getFile(transferable);
			final FileFilter filter = getFileFilter();
			if(filter != null && !filter.accept(retVal)) {
				retVal = null;
			}
			return retVal;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			return super.createTransferable(c);
		}
		
	}
}
