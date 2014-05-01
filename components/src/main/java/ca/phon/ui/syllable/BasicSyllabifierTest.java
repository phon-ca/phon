package ca.phon.ui.syllable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.basic.BasicSyllabifier;
import ca.phon.syllabifier.basic.BasicSyllabifierClassLoaderProvider;
import ca.phon.syllabifier.basic.BasicSyllabifier.SyllabifierStageResults;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.resources.ResourceLoader;

public class BasicSyllabifierTest extends JFrame {

	private final static Logger LOGGER = Logger
			.getLogger(BasicSyllabifierTest.class.getName());
	
	private JComboBox syllabifierBox;
 	
	private JTextField ipaField;
	
	private JPanel stagePanel;
	
	private ResourceLoader<Syllabifier> basicSyllabifierLoader = new ResourceLoader<Syllabifier>();
	
	public BasicSyllabifierTest() {
		super("Test Syllabifier");
		
		basicSyllabifierLoader.addHandler(new BasicSyllabifierClassLoaderProvider());
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final JPanel topPanel = new JPanel(new GridLayout(0, 1));
		ipaField = new JTextField();
		ipaField.setBorder(BorderFactory.createTitledBorder("Enter IPA:"));
		final Action act = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					syllabify();
				} catch (ParseException e1) {
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
//					final Toast t = ToastFactory.makeToast(e1.getLocalizedMessage());
//					t.start(ipaField);
				}
			}
			
		};
		act.putValue(Action.NAME, "Go");
		final JButton btn = new JButton(act);
		
		final List<Syllabifier> basicSyllabifiers = new ArrayList<Syllabifier>();
		final Iterator<Syllabifier> itr = basicSyllabifierLoader.iterator();
		while(itr.hasNext()) {
			basicSyllabifiers.add(itr.next());
		}
		syllabifierBox = new JComboBox(basicSyllabifiers.toArray(new Syllabifier[0]));
		syllabifierBox.setRenderer(new SyllabifierCellRenderer());
		syllabifierBox.setBorder(BorderFactory.createTitledBorder("Syllabifier:"));
		
		topPanel.add(ipaField);
		topPanel.add(btn);
		topPanel.add(syllabifierBox);
		
		stagePanel = new JPanel(new GridLayout(0, 1));
		final JScrollPane sp = new JScrollPane(stagePanel);
		sp.setBorder(BorderFactory.createTitledBorder("Stages:"));
		
		add(topPanel, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
	}

	private class SyllabifierCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1584799534042871645L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			final Syllabifier syllabifier = (Syllabifier)value;
			if(syllabifier != null)
				retVal.setText(syllabifier.getName() + " (" + syllabifier.getLanguage().toString() + ")");
			
			return retVal;
		}

	}
	
	private void syllabify() throws ParseException {
		final IPATranscript ipa = IPATranscript.parseIPATranscript(ipaField.getText());
		final BasicSyllabifier syllabifier = (BasicSyllabifier)syllabifierBox.getSelectedItem();
		syllabifier.syllabify(ipa.toList());
		
		stagePanel.removeAll();
		final BasicSyllabifier.SyllabifierStageResults results = syllabifier.getExtension(BasicSyllabifier.SyllabifierStageResults.class);
		for(String stageName:results.stages.keySet()) {
			final SyllabificationDisplay display = new SyllabificationDisplay();
			final IPATranscript stageIPA = IPATranscript.parseIPATranscript(results.stages.get(stageName));
			
			display.setTranscript(stageIPA);
			display.setBorder(BorderFactory.createTitledBorder(stageName));
			
			stagePanel.add(display);
		}
		
		stagePanel.revalidate();
		stagePanel.repaint();
	}
	
	public static void main(String[] args) {
		final BasicSyllabifierTest f = new BasicSyllabifierTest();
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
