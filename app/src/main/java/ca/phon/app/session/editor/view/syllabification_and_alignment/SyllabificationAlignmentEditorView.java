package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
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
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayout;
import ca.phon.app.session.editor.view.common.TierDataLayoutButtons;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SyllabificationAlignmentEditorView extends EditorView {

	private static final long serialVersionUID = -1757697054252181347L;

	private final static String VIEW_NAME = "Syllabification & Alignment";
	
	private JPanel topPanel;
	private JCheckBox targetIPABox;
	private JCheckBox actualIPABox;
	private JCheckBox alignmentBox;
	private JCheckBox colorInAlignmentBox;
	
	private JPanel contentPane;
	private JScrollPane scroller;
	
	// components
	private final List<SyllabificationDisplay> targetDisplays = new ArrayList<SyllabificationDisplay>();
	private final List<SyllabificationDisplay> actualDisplays = new ArrayList<SyllabificationDisplay>();
	private final List<PhoneMapDisplay> alignmentDisplayus = new ArrayList<PhoneMapDisplay>();
	
	public SyllabificationAlignmentEditorView(SessionEditor editor) {
		super(editor);
		init();
		setupEditorActions();
	}
	
	private void init() {
		// tier content
		contentPane = new JPanel();
		final TierDataLayout layout = new TierDataLayout();
		contentPane.setLayout(layout);
		contentPane.setBackground(Color.white);
		
		// top panel
		final FormLayout topLayout = new FormLayout(
				"pref, pref, pref, pref, fill:pref:grow, right:pref", "pref");
		topPanel = new JPanel(topLayout);
		
		final PhonUIAction toggleTargetAct = new PhonUIAction(this, "toggleCheckbox");
		toggleTargetAct.putValue(PhonUIAction.NAME, SystemTierType.TargetSyllables.getName());
		toggleTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle target syllables");
		toggleTargetAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
		targetIPABox = new JCheckBox(toggleTargetAct);
		
		final PhonUIAction toggleActualAct = new PhonUIAction(this, "toggleCheckbox");
		toggleActualAct.putValue(PhonUIAction.NAME, SystemTierType.ActualSyllables.getName());
		toggleActualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle actual syllables");
		toggleActualAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
		actualIPABox = new JCheckBox(toggleActualAct);
		
		final PhonUIAction toggleAlignmentAct = new PhonUIAction(this, "toggleCheckbox");
		toggleAlignmentAct.putValue(PhonUIAction.NAME, SystemTierType.SyllableAlignment.getName());
		toggleAlignmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle alignment");
		toggleAlignmentAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
		alignmentBox = new JCheckBox(toggleAlignmentAct);
		
		final PhonUIAction toggleAlignmentColorAct = new PhonUIAction(this, "toggleCheckbox");
		toggleAlignmentColorAct.putValue(PhonUIAction.NAME, "Color in alignment");
		toggleAlignmentColorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle color in alignment");
		toggleAlignmentColorAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.FALSE);
		colorInAlignmentBox = new JCheckBox(toggleAlignmentColorAct);
		
		final TierDataLayoutButtons tdlb = new TierDataLayoutButtons(contentPane, layout);
		
		final CellConstraints cc = new CellConstraints();
		topPanel.add(targetIPABox, cc.xy(1,1));
		topPanel.add(actualIPABox, cc.xy(2,1));
		topPanel.add(alignmentBox, cc.xy(3,1));
		topPanel.add(colorInAlignmentBox, cc.xy(4,1));
		topPanel.add(tdlb, cc.xy(6,1));
		
		setLayout(new BorderLayout());
		scroller = new JScrollPane(contentPane);
		scroller.setBackground(Color.white);
		scroller.setOpaque(true);
		add(topPanel, BorderLayout.NORTH);
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
		final boolean showTarget = targetIPABox.isSelected();
		final boolean showActual = actualIPABox.isSelected();
		final boolean showAlignment = alignmentBox.isSelected();
		final boolean colorInAlignment = colorInAlignmentBox.isSelected();
		
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		
		int maxExtra = Math.max(targetDisplays.size(), actualDisplays.size());
		maxExtra = Math.max(maxExtra, alignmentDisplayus.size());
		contentPane.removeAll();
		
		if(showTarget) {
			final JLabel targetSyllLbl = new JLabel(SystemTierType.TargetSyllables.getName());
			final TierDataConstraint targetConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 0);
			contentPane.add(targetSyllLbl, targetConstraint);
		}
		
		if(showActual) {
			final JLabel actualSyllLbl = new JLabel(SystemTierType.ActualSyllables.getName());
			final TierDataConstraint actualConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 1);
			contentPane.add(actualSyllLbl, actualConstraint);
		}
		
		if(showAlignment) {
			final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
//			separator.setPreferredSize(new Dimension(0, 1));
			separator.setForeground(Color.lightGray);
			final TierDataConstraint sepConstraint = new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, 2);
			contentPane.add(separator, sepConstraint);
			
			final JLabel alignSyllLbl = new JLabel(SystemTierType.SyllableAlignment.getName());
			final TierDataConstraint alignConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 3);
			contentPane.add(alignSyllLbl, alignConstraint);
		}
		
		final TierDataLayout layout = TierDataLayout.class.cast(contentPane.getLayout());		
		for(int gIndex = 0; gIndex < record.numberOfGroups(); gIndex++) {
			final Group group = record.getGroup(gIndex);
			
			if(showTarget) {
				// target
				final IPATranscript ipaTarget = group.getIPATarget();
				final SyllabificationDisplay ipaTargetDisplay = getIPATargetDisplay(gIndex);
				ipaTargetDisplay.setPhonesForGroup(0, ipaTarget.toList());
				
				if(!layout.hasLayoutComponent(ipaTargetDisplay)) {
					final TierDataConstraint ipaTargetConstraint = 
							new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 0);
					contentPane.add(ipaTargetDisplay, ipaTargetConstraint);
				}
			}
			
			if(showActual) {
				// actual
				final IPATranscript ipaActual = group.getIPAActual();
				final SyllabificationDisplay ipaActualDisplay = getIPAActualDisplay(gIndex);
				ipaActualDisplay.setPhonesForGroup(0, ipaActual.toList());
				
				if(!layout.hasLayoutComponent(ipaActualDisplay)) {
					final TierDataConstraint ipaActualConstraint = 
							new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 1);
					contentPane.add(ipaActualDisplay, ipaActualConstraint);
				}
			}
			
			if(showAlignment) {
				// alignment
				final PhoneMap pm = group.getPhoneAlignment();
				final PhoneMapDisplay pmDisplay = getAlignmentDisplay(gIndex);
				pmDisplay.setPhoneMapForGroup(0, pm);
				pmDisplay.setPaintPhoneBackground(colorInAlignment);
				
//				pmDisplay.addPropertyChangeListener(new PropertyChangeListener() {
//					
//					@Override
//					public void propertyChange(PropertyChangeEvent arg0) {
//						if(arg0.getPropertyName().equals(PhoneMapDisplay.ALIGNMENT_CHANGE_PROP)
//								|| arg0.getPropertyName().equals(PhoneMapDisplay.TEMP_ALIGNMENT_CHANGE_PROP)) 
//						layout.layoutContainer(contentPane);
//					}
//				});
				
				if(!layout.hasLayoutComponent(pmDisplay)) {
					final TierDataConstraint pmConstraint = 
							new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 3);
					contentPane.add(pmDisplay, pmConstraint);
				}
			}
		}
		contentPane.revalidate();
	}

	public void toggleCheckbox() {
		update();
		repaint();
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
		return IconManager.getInstance().getIcon("misc/syllabification", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		return null;
	}
	
}
