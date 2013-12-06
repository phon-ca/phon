package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

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
	
	// components
	private final List<SyllabificationDisplay> targetDisplays = new ArrayList<>();
	private final List<SyllabificationDisplay> actualDisplays = new ArrayList<>();
	private final List<PhoneMapDisplay> alignmentDisplayus = new ArrayList<>();
	
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
	
	private SyllabificationDisplay getIPATargetDisplay(int group) {
		SyllabificationDisplay retVal = null;
		if(group < targetDisplays.size()) {
			retVal = targetDisplays.get(group);
		} else {
			retVal = new SyllabificationDisplay();
			targetDisplays.add(retVal);
		}
		return retVal;
	}
	
	private SyllabificationDisplay getIPAActualDisplay(int group) {
		SyllabificationDisplay retVal = null;
		if(group < actualDisplays.size()) {
			retVal = actualDisplays.get(group);
		} else {
			retVal = new SyllabificationDisplay();
			actualDisplays.add(retVal);
		}
		return retVal;
	}
	
	private PhoneMapDisplay getAlignmentDisplay(int group) {
		PhoneMapDisplay retVal = null;
		if(group < alignmentDisplayus.size()) {
			retVal = alignmentDisplayus.get(group);
		} else {
			retVal = new PhoneMapDisplay();
			alignmentDisplayus.add(retVal);
		}
		return retVal;
	}
	
	public void update() {
//		for(SyllabificationDisplay targetDisplay:targetDisplays)
//			contentPane.remove(targetDisplay);
//		for(SyllabificationDisplay actualDisplay:actualDisplays)
//			contentPane.remove(actualDisplay);
//		for(PhoneMapDisplay alignmentDisplay:alignmentDisplayus) {
//			contentPane.remove(alignmentDisplay);
//		}
		
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		
		int maxExtra = Math.max(targetDisplays.size(), actualDisplays.size());
		maxExtra = Math.max(maxExtra, alignmentDisplayus.size());
		
		for(int i = record.numberOfGroups(); i < maxExtra; i++) {
			if(i < targetDisplays.size()) {
				final SyllabificationDisplay targetDisplay = targetDisplays.get(i);
				if(targetDisplay != null) contentPane.remove(targetDisplay);
			}
			if(i < actualDisplays.size()) {
				final SyllabificationDisplay actualDisplay = actualDisplays.get(i);
				if(actualDisplay != null) contentPane.remove(actualDisplay);
			}
			if(i < alignmentDisplayus.size()) {
				final PhoneMapDisplay alignmentDisplay = alignmentDisplayus.get(i);
				if(alignmentDisplay != null) contentPane.remove(alignmentDisplay);
			}
		}
		
		final TierDataLayout layout = TierDataLayout.class.cast(contentPane.getLayout());		
		for(int gIndex = 0; gIndex < record.numberOfGroups(); gIndex++) {
			final Group group = record.getGroup(gIndex);
			
			// target
			final IPATranscript ipaTarget = group.getIPATarget();
			final SyllabificationDisplay ipaTargetDisplay = getIPATargetDisplay(gIndex);
			ipaTargetDisplay.setPhonesForGroup(0, ipaTarget);
			
			if(!layout.hasLayoutComponent(ipaTargetDisplay)) {
				final TierDataConstraint ipaTargetConstraint = 
						new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 0);
				contentPane.add(ipaTargetDisplay, ipaTargetConstraint);
			}
			
			// actual
			final IPATranscript ipaActual = group.getIPAActual();
			final SyllabificationDisplay ipaActualDisplay = getIPAActualDisplay(gIndex);
			ipaActualDisplay.setPhonesForGroup(0, ipaActual);
			
			if(!layout.hasLayoutComponent(ipaActualDisplay)) {
				final TierDataConstraint ipaActualConstraint = 
						new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 1);
				contentPane.add(ipaActualDisplay, ipaActualConstraint);
			}
			
			// alignment
			final PhoneMap pm = group.getPhoneAlignment();
			final PhoneMapDisplay pmDisplay = getAlignmentDisplay(gIndex);
			pmDisplay.setPhoneMapForGroup(0, pm);
			
			pmDisplay.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					if(arg0.getPropertyName().equals(PhoneMapDisplay.ALIGNMENT_CHANGE_PROP)
							|| arg0.getPropertyName().equals(PhoneMapDisplay.TEMP_ALIGNMENT_CHANGE_PROP)) 
					layout.layoutContainer(contentPane);
				}
			});
			
			if(!layout.hasLayoutComponent(pmDisplay)) {
				final TierDataConstraint pmConstraint = 
						new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 3);
				contentPane.add(pmDisplay, pmConstraint);
			}
		}
		contentPane.revalidate();
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
