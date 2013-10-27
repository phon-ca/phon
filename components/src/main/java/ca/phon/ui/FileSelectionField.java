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
package ca.phon.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PathExpander;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Special text field for selecting file/folders.
 */
public class FileSelectionField extends PromptedTextField {
	
	private static final long serialVersionUID = 7059011387085702827L;

	/**
	 * Property for changes to the selected file
	 */
	public final static String FILE_PROP = "_selected_file_";
	
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
	private FileFilter fileFilter = FileFilter.allFilesFilter;
	
	/**
	 * Constructor
	 */
	public FileSelectionField() {
		super();
		init();
		getDocument().addDocumentListener(validationListener);
		addFocusListener(focusListener);
	}
	
	private void init() {
		final ImageIcon browseIcon = 
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		final PhonUIAction browseAct = 
				new PhonUIAction(this, "onBrowse");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse...");
		browseAct.putValue(PhonUIAction.SMALL_ICON, browseIcon);
		browseButton = new JButton(browseAct);
		browseButton.putClientProperty("JButton.buttonType", "square");
		browseButton.setCursor(Cursor.getDefaultCursor());
		
//		addKeyListener(keyListener);
		add(browseButton);
		
		// setup empty border with space for the button
		final int leftSpace = 0;
		final int rightSpace = browseButton.getPreferredSize().width;
		final int topSpace = 0;
		final int btmSpace = 0;
		
		final Border emptyBorder =
				BorderFactory.createEmptyBorder(topSpace, leftSpace, btmSpace, rightSpace);
		final Border border = 
				BorderFactory.createCompoundBorder(getBorder(), emptyBorder);
		setBorder(border);

		addComponentListener(resizeListener);
		
		addActionListener(new ActionListener() {
			
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
		
		if(getState() != FieldState.PROMPT) {
			String txt = getText();
			txt = (new PathExpander()).expandPath(txt);
			retVal = new File(txt);
		}
		
		return retVal;
	}

	/**
	 * Set the current file
	 * 
	 * @param file
	 */
	private File lastSelectedFile = null;
	public void setFile(File f) {
		if(f == null) {
			setText("");
		} else {
			setState(FieldState.INPUT);
			
			String path = f.getAbsolutePath();
			String collapsedPath = (new PathExpander()).compressPath(path);
			setText(collapsedPath);
			
			super.firePropertyChange(FILE_PROP, lastSelectedFile, f);
			lastSelectedFile = f;
		}
	}
	
	private void checkPath() {
		if(getState() == FieldState.PROMPT || !validatePath) return;
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
			
			if(!fileFilter.accept(f)) {
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
		
		if(mode == SelectionMode.FILES) {
			path = NativeDialogs.browseForFileBlocking(CommonModuleFrame.getCurrentFrame(), parentPath, 
					"*", new FileFilter[]{ fileFilter }, null);
		} else {
			path = 
					NativeDialogs.browseForDirectoryBlocking(
							CommonModuleFrame.getCurrentFrame(), null, null);
		}
		if(path != null) {
			setState(FieldState.INPUT);
			setFile(new File(path));
		}
	}
	
	/**
	 * Listener to keep browse button at end of component
	 */
	private final ComponentListener resizeListener = new ComponentListener() {
		
		@Override
		public void componentShown(ComponentEvent arg0) {
			
		}
		
		@Override
		public void componentResized(ComponentEvent arg0) {
			Runnable moveBtn = new Runnable() {
				
				@Override
				public void run() {
					browseButton.setBounds(
							getWidth()-browseButton.getPreferredSize().width, 0,
							browseButton.getPreferredSize().width, getHeight());
				}
			};
			SwingUtilities.invokeLater(moveBtn);
		}
		
		@Override
		public void componentMoved(ComponentEvent arg0) {
		}
		
		@Override
		public void componentHidden(ComponentEvent arg0) {
		}
	};
	
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
	
	/**
	 * Focus listener
	 */
	private final FocusListener focusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent arg0) {
			final File f = FileSelectionField.this.getSelectedFile();
			if(f != null) {
				final String path = f.getPath();
				final PathExpander pe = new PathExpander();
				final String compressedPath = pe.compressPath(path);
				
				FileSelectionField.this.setText(compressedPath);
			} else {
				setState(FieldState.PROMPT);
			}
			firePropertyChange(FILE_PROP, lastSelectedFile, f);
			lastSelectedFile = f;
		}
		
		@Override
		public void focusGained(FocusEvent arg0) {
		}
	};
}
