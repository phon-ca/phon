/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ui.text;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import ca.phon.ui.action.*;
import ca.phon.ui.dnd.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.PromptedTextField.*;
import ca.phon.util.icons.*;

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
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1.0;
		setLayout(layout);
		
		textField = new PromptedTextField();
		gbc.weightx = 1.0;
		add(textField, gbc);
		
		final ImageIcon browseIcon = 
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		final PhonUIAction browseAct = 
				new PhonUIAction(this, "onBrowse");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse...");
		browseAct.putValue(PhonUIAction.SMALL_ICON, browseIcon);
		browseButton = new JButton(browseAct);
		browseButton.putClientProperty("JButton.buttonType", "square");
		browseButton.setCursor(Cursor.getDefaultCursor());
		gbc.gridx++;
		gbc.weightx = 0.0;
		add(browseButton, gbc);
		
		textField.addActionListener( (e) -> {
			final File f = getSelectedFile();
			setFile(f);
		});
	}
	
	public JButton getBrowseButton() {
		return this.browseButton;
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
