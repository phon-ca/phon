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
package ca.phon.app.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FontDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel for editing session editor prefs.
 *
 */
public class EditorPrefsPanel extends PrefsPanel {
	
	/*
	 * UI
	 */
	private JComboBox cmbDictionaryLanguage;
	private JComboBox cmbSyllabifierLanguage;

	private JComboBox autosaveBox;
	private final Integer[] autosaveTimes = { 0, 5, 10, 15, 20, 30 }; // minutes
	
	private JLabel lblFont;
	private JLabel lblPreviewFont;
	private JButton btnChooseFont;
	
	public EditorPrefsPanel() {
		super("Session Editor");
		init();
	}
	
	private void init() {

		CellConstraints cc = new CellConstraints();
		
		final IPADictionaryLibrary dictLibrary = IPADictionaryLibrary.getInstance();
		
		final String dictLangPref = PrefHelper.get(PhonProperties.IPADICTIONARY_LANGUAGE,
				PhonProperties.DEFAULT_IPADICTIONARY_LANGUAGE);
		final Language dictLang = Language.fromString(dictLangPref);
		cmbDictionaryLanguage = new JComboBox(dictLibrary.availableLanguages().toArray(new Language[0]));
		cmbDictionaryLanguage.setSelectedItem(dictLang);
		cmbDictionaryLanguage.addItemListener(new DictionaryLanguageListener());
		
		JPanel jpanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpanel1.setBorder(new TitledBorder("Dictionary Language"));
		jpanel1.add(cmbDictionaryLanguage);
		
		final SyllabifierLibrary syllabifierLibrary = SyllabifierLibrary.getInstance();
		
		final List<String> sortedSyllabifiers = syllabifierLibrary.availableSyllabifierNames();
		Collections.sort(sortedSyllabifiers);
		
		final String syllLangPref = PrefHelper.get(PhonProperties.SYLLABIFIER_LANGUAGE, 
				PhonProperties.DEFAULT_SYLLABIFIER_LANGUAGE);
		final Language syllLang = Language.fromString(syllLangPref);
		cmbSyllabifierLanguage = new JComboBox(sortedSyllabifiers.toArray(new String[0]));
		cmbSyllabifierLanguage.setSelectedItem(syllLang.toString());
		cmbSyllabifierLanguage.addItemListener(new SyllabifierLanguageListener());
		
		JPanel jpanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpanel2.setBorder(new TitledBorder("Syllabifier Language"));
		jpanel2.add(cmbSyllabifierLanguage);
		
		Charset cs = Charset.forName("UTF-8");
		Font editorFont = PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, 
				Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT));
		lblFont = new JLabel(fontToString(editorFont));
		lblPreviewFont = new JLabel();
		lblPreviewFont.setVerticalAlignment(SwingConstants.TOP);
		
		btnChooseFont = new JButton("Select...");
		btnChooseFont.addActionListener(new EditorFontListener());
		
		JPanel jpanel3 = new JPanel(new FormLayout(
				"pref, 3dlu, left:pref:grow",
				"pref, 5dlu, pref, 5dlu, top:pref"));

		jpanel3.setBorder(new TitledBorder("Default Editor Font"));
		jpanel3.add(lblFont, cc.xy(1, 1));
		jpanel3.add(btnChooseFont, cc.xy(3, 1));
		
		JScrollPane fontScrollPane = new JScrollPane(lblPreviewFont);
		fontScrollPane.getViewport().setOpaque(false);
		fontScrollPane.setOpaque(false);
		fontScrollPane.setBorder(null);
		fontScrollPane.setPreferredSize(new Dimension(500, 100));
		jpanel3.add(fontScrollPane,	cc.xyw(1, 5, 3));
			
		autosaveBox = new JComboBox(autosaveTimes);
		
		final Integer autosavePref = PrefHelper.getInt(PhonProperties.AUTOSAVE_INTERVAL, PhonProperties.DEFAULT_AUTOSAVE_INTERVAL);
		autosaveBox.setSelectedItem((autosavePref/60));
		
		autosaveBox.addItemListener(new AutosaveTimeListener());
		autosaveBox.setRenderer(new AutosaveTimeRenderer());
		
		JPanel jpanel4 = new JPanel(new FormLayout(
				"pref",
				"pref"));
		
		jpanel4.add(autosaveBox, cc.xy(1,1));
		jpanel4.setBorder(BorderFactory.createTitledBorder("Autosave Sessions"));
		
		JPanel innerPanel = new JPanel();
		FormLayout layout = new FormLayout(
				"fill:pref:grow",
				"pref, pref, pref, pref");
		innerPanel.setLayout(layout);
		
		innerPanel.add(jpanel1, cc.xy(1,1));
		innerPanel.add(jpanel2, cc.xy(1,2));
		innerPanel.add(jpanel3, cc.xy(1,3));
		innerPanel.add(jpanel4, cc.xy(1, 4));
		
		setLayout(new BorderLayout());
		JScrollPane innerScroller = new JScrollPane(innerPanel);
		add(innerScroller, BorderLayout.CENTER);
	}
	
	private void updateDialogFonts() {
		Font font = PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, 
				Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT));
		lblPreviewFont.setFont(font);
	}
	
	/**
	 * Converts the specified font into a string that can be used by
	 * Font.decode.
	 * @param font  the Font to convert to a String
	 * @return      a String
	 */
	private String fontToString(Font font) {
		StringBuilder ret = new StringBuilder();
		ret.append(font.getFamily());
		ret.append("-");
		
		if(font.isBold()) {
			if(font.isItalic())
				ret.append("BOLDITALIC");
			else
				ret.append("BOLD");
		} else if(font.isItalic()) {
			ret.append("ITALIC");
		} else {
			ret.append("PLAIN");
		}
		ret.append("-");
		
		ret.append(font.getSize());
		
		return ret.toString();
	}
	
	private class EditorFontListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
//			JFontChooser fontChooser = new JFontChooser();
//			
//			Font editorFont = Font.decode((String)props.getProperty("editor_font"));
//			fontChooser.setSelectedFont(editorFont);
//			
//			int ret = fontChooser.showDialog(PropertiesDialog.this);
//			if(ret == JFontChooser.OK_OPTION) {
//				Font font = fontChooser.getSelectedFont();
//				lblFont.setText(fontToString(font));					
//				props.addProperty("editor_font", fontToString(font));
//				PhonUtilities.saveUserPrefs(props);
//				
//				updateDialogFonts();
//			}
//			Runnable run = new Runnable() {
//				@Override
//				public void run() {
					Font editorFont = PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, 
							Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT));
					
					final FontDialogProperties dialogProps = new FontDialogProperties();
					dialogProps.setFontName(editorFont.getName());
					dialogProps.setFontSize(editorFont.getSize());
					dialogProps.setBold(editorFont.isBold());
					dialogProps.setItalic(editorFont.isItalic());
					dialogProps.setTitle("Select Font");
					dialogProps.setParentWindow(CommonModuleFrame.getCurrentFrame());
					dialogProps.setRunAsync(false);
					
					Font newEditorFont = NativeDialogs.showFontDialog(dialogProps);
					
					if(newEditorFont != null) {
						lblFont.setText(fontToString(newEditorFont));
						PrefHelper.getUserPreferences().put(PhonProperties.IPA_TRANSCRIPT_FONT, fontToString(newEditorFont));
						
						updateDialogFonts();
					}
//				}
//			};
//			PhonWorker.getInstance().invokeLater(run);
			
		}
	}
	
	private class AutosaveTimeRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList arg0,
				Object arg1, int arg2, boolean arg3, boolean arg4) {
			JLabel retVal = 
				(JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
			
			Integer val = (Integer)arg1;
			if(val == 0) {
				retVal.setText("Never");
			} else {
				retVal.setText("Every " + val + " minutes");
			}
			
			return retVal;
		}
	}
	
	private class AutosaveTimeListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				Integer val = (Integer)autosaveBox.getSelectedItem();
				val = (val == null ? 0 : val);
				
				PrefHelper.getUserPreferences().putInt(PhonProperties.AUTOSAVE_INTERVAL, val);
			}
		}
		
	}
	
	private class DictionaryLanguageListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() != ItemEvent.SELECTED) return;
			
			Language dictLanguage = (Language)e.getItem();
			PrefHelper.getUserPreferences().put(PhonProperties.IPADICTIONARY_LANGUAGE, dictLanguage.toString());
		}
	}
	
//	private class LanguageCellRenderer extends DefaultListCellRenderer {
//
//		@Override
//		public Component getListCellRendererComponent(JList arg0,
//				Object arg1, int arg2, boolean arg3, boolean arg4) {
//			JLabel retVal = 
//				(JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
//			
//			String langId = arg1.toString();
//			if(langId.indexOf('-') > 0) {
//				langId = langId.split("-")[0];
//			}
//			Language le = LanguageParser.getInstance().getEntryById(langId);
//			if(le != null) {
//				retVal.setText(le.getName() + " (" + arg1.toString() + ")");
//			}
//			
//			return retVal;
//		}
//		
//	}
	
	private class SyllabifierLanguageListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() != ItemEvent.SELECTED) return;
			
			String sylLanguage = (String)e.getItem();
			PrefHelper.getUserPreferences().put(PhonProperties.SYLLABIFIER_LANGUAGE, sylLanguage);
		}
	}
	
	
}
