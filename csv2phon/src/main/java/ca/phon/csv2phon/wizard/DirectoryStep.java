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
package ca.phon.csv2phon.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.gui.DialogHeader;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PhonConstants;
import ca.phon.util.StringUtils;
import ca.phon.util.iconManager.IconManager;
import ca.phon.util.iconManager.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select directory containing csv files.
 *
 */
public class DirectoryStep extends CSVImportStep {
	
	/** UI */
	private DialogHeader header;
	private JLabel infoLbl;
	private JLabel csvDirLbl;
	private JButton csvDirBtn;
	
	private JComboBox charsetBox;
	
	private String base;
	
	private String charsetName = "UTF-8";
	
	public DirectoryStep() {
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		header = new DialogHeader("CSV Import", "Select folder containing csv files.");
		add(header, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createTitledBorder("Folder"));
		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, pref",
				"pref, 3dlu, pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		centerPanel.setLayout(layout);
		
		String lblTxt = "<html><body><p>Please select the folder containing the csv files for import." +
			"  <font color='red'>All csv files should have the same column structure and encoding</font>.</p></body></html>";
		infoLbl = new JLabel(lblTxt);
		centerPanel.add(infoLbl, cc.xyw(1,1,3));
		
		csvDirLbl = new JLabel();
		
		ImageIcon openIcon = 
			IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		csvDirBtn = new JButton(openIcon);
		csvDirBtn.addActionListener(new DirBrowseAction());
		csvDirBtn.setToolTipText("Browse for folder...");
		
		// setup charset chooser
		SortedMap<String, Charset> availableCharset = 
			Charset.availableCharsets();
		charsetBox = new JComboBox(availableCharset.keySet().toArray(new String[0]));
		charsetBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					charsetName = charsetBox.getSelectedItem().toString();
				}
			}
			
		});
		charsetBox.setSelectedItem("UTF-8");
		
		centerPanel.add(new JLabel("Folder:"), cc.xy(1,3));
		centerPanel.add(csvDirLbl, cc.xy(3,3));
		centerPanel.add(csvDirBtn, cc.xy(4,3));
		
		centerPanel.add(new JLabel("File encoding:"), cc.xy(1, 5));
		centerPanel.add(charsetBox, cc.xy(3, 5));
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	@Override
	public boolean validateStep() {
		boolean retVal = false;
		
		// make sure a directory is selected and exists
		if(getBase() != null) {
			File f = new File(getBase());
			retVal = f.exists() && f.isDirectory();
		}
		
		return retVal;
	}
	
	public String getBase() {
		return base;
	}

	public String getFileEncoding()  {
		return this.charsetName;
	}
	
	private class DirBrowseAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFrame parentFrame = 
				(JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, DirectoryStep.this);
			String selectedDir = 
				NativeDialogs.browseForDirectoryBlocking(parentFrame, 
						null, "Select folder");
			if(selectedDir != null) {
				base = selectedDir;
				
				csvDirLbl.setText(StringUtils.shortenStringUsingToken(
						new File(selectedDir).getAbsolutePath(), PhonConstants.ellipsis+"", 50));
			}
		}
		
	}
}
