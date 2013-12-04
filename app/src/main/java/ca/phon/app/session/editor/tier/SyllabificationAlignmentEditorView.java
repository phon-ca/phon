package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

public class SyllabificationAlignmentEditorView extends EditorView {

	private static final long serialVersionUID = -1757697054252181347L;

	private final static String VIEW_NAME = "Syllabification & Alignment";
	
	private JPanel contentPane;
	private JScrollPane scroller;
	
	public SyllabificationAlignmentEditorView(SessionEditor editor) {
		super(editor);
		init();
		setupEditorActions();
	}
	
	private void init() {
		contentPane = new JPanel();
		final TierDataLayout layout = new TierDataLayout();
		contentPane.setLayout(layout);
		contentPane.setBackground(Color.white);
		
		setLayout(new BorderLayout());
		scroller = new JScrollPane(contentPane);
		scroller.setBackground(Color.white);
		scroller.setOpaque(true);
		add(scroller, BorderLayout.CENTER);
		
		update();
	}
	
	private void setupEditorActions() {
		final SessionEditor editor = getEditor();
		final EditorEventManager eventManager = editor.getEventManager();
		
		final EditorAction updateAct =
				new DelegateEditorAction(this, "onDataChanged");
		
		eventManager.registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, updateAct);
		eventManager.registerActionForEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, updateAct);
		eventManager.registerActionForEvent(EditorEventType.RECORD_REFRESH_EVT, updateAct);
	}
	
	public void update() {
		contentPane.removeAll();
		
		final JLabel targetSyllLbl = new JLabel(SystemTierType.TargetSyllables.getName());
		final TierDataConstraint targetConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 0);
		contentPane.add(targetSyllLbl, targetConstraint);
		
		final JLabel actualSyllLbl = new JLabel(SystemTierType.ActualSyllables.getName());
		final TierDataConstraint actualConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 1);
		contentPane.add(actualSyllLbl, actualConstraint);
		
		final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setForeground(Color.lightGray);
		final TierDataConstraint sepConstraint = new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, 2);
		contentPane.add(separator, sepConstraint);
		
		final JLabel alignSyllLbl = new JLabel(SystemTierType.SyllableAlignment.getName());
		final TierDataConstraint alignConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 3);
		contentPane.add(alignSyllLbl, alignConstraint);
		
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		for(int gIndex = 0; gIndex < record.numberOfGroups(); gIndex++) {
			final Group group = record.getGroup(gIndex);
			
			// target
			final IPATranscript ipaTarget = group.getIPATarget();
			final SyllabificationDisplay ipaTargetDisplay = new SyllabificationDisplay();
			ipaTargetDisplay.setPhonesForGroup(0, ipaTarget);
			final TierDataConstraint ipaTargetConstraint = 
					new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 0);
			contentPane.add(ipaTargetDisplay, ipaTargetConstraint);
			
			// actual
			final IPATranscript ipaActual = group.getIPAActual();
			final SyllabificationDisplay ipaActualDisplay = new SyllabificationDisplay();
			ipaActualDisplay.setPhonesForGroup(0, ipaActual);
			final TierDataConstraint ipaActualConstraint = 
					new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 1);
			contentPane.add(ipaActualDisplay, ipaActualConstraint);
			
			// alignment
			final PhoneMap pm = group.getPhoneAlignment();
			final PhoneMapDisplay pmDisplay = new PhoneMapDisplay();
			pmDisplay.setPhoneMapForGroup(0, pm);
			final TierDataConstraint pmConstraint = 
					new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 3);
			contentPane.add(pmDisplay, pmConstraint);
		}
		revalidate();
//		scroller.repaint();
	}

	/*---- Editor Actions -------------------*/
	public void onDataChanged(EditorEvent ee) {
		update();
		repaint();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public JMenu getMenu() {
		return null;
	}
	
}
