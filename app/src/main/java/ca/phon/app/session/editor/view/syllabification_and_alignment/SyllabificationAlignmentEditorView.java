/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayout;
import ca.phon.app.session.editor.view.common.TierDataLayoutButtons;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.app.session.editor.view.syllabification_and_alignment.actions.ResetAlignmentCommand;
import ca.phon.app.session.editor.view.syllabification_and_alignment.actions.ResetSyllabificationCommand;
import ca.phon.app.session.editor.view.syllabification_and_alignment.actions.SyllabificationSettingsCommand;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.PhoneMapDisplay.AlignmentChangeData;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay.SyllabificationChangeData;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SyllabificationAlignmentEditorView extends EditorView {
	
	public final static String SC_EDIT = EditorEventType.MODIFICATION_EVENT + "_SC_TYPE_";

	private static final long serialVersionUID = -1757697054252181347L;

	private final static String VIEW_NAME = "Syllabification & Alignment";
	
	private JPanel topPanel;
	private JButton settingsBtn;
	private JCheckBox targetIPABox;
	private JCheckBox actualIPABox;
	private JCheckBox alignmentBox;
	private JCheckBox colorInAlignmentBox;
	
	private TierDataLayoutPanel contentPane;
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
		contentPane = new TierDataLayoutPanel();
		
		// top panel
		final FormLayout topLayout = new FormLayout(
				"pref, pref, pref, pref, pref, fill:pref:grow, right:pref", "pref");
		topPanel = new JPanel(topLayout);
		
		final SyllabificationSettingsCommand settingsAct = new SyllabificationSettingsCommand(getEditor(), this);
		settingsBtn = new JButton(settingsAct);
		
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
		
		final TierDataLayoutButtons tdlb = new TierDataLayoutButtons(contentPane, 
				(TierDataLayout)contentPane.getLayout());
		
		final CellConstraints cc = new CellConstraints();
		topPanel.add(settingsBtn, cc.xy(1,1));
		topPanel.add(targetIPABox, cc.xy(2,1));
		topPanel.add(actualIPABox, cc.xy(3,1));
		topPanel.add(alignmentBox, cc.xy(4,1));
		topPanel.add(colorInAlignmentBox, cc.xy(5,1));
		topPanel.add(tdlb, cc.xy(7,1));
		
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
		
		final EditorAction tierChangeAct =
				new DelegateEditorAction(this, "onTierChange");
		eventManager.registerActionForEvent(EditorEventType.TIER_CHANGE_EVT, tierChangeAct);
		
		final EditorAction tierChangedAct =
				new DelegateEditorAction(this, "onTierChanged");
		eventManager.registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, tierChangedAct);
		
		final EditorAction scChangeAct = 
				new DelegateEditorAction(this, "onScChange");
		eventManager.registerActionForEvent(SC_EDIT, scChangeAct);
	}
	
	private SyllabificationDisplay getIPATargetDisplay(int group) {
		SyllabificationDisplay retVal = null;
		if(group < targetDisplays.size()) {
			retVal = targetDisplays.get(group);
		} else {
			retVal = new SyllabificationDisplay();
			retVal.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, syllabificationDisplayListener);
			retVal.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, hiatusChangeListener);
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
			retVal.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, syllabificationDisplayListener);
			retVal.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, hiatusChangeListener);
			actualDisplays.add(retVal);
		}
		return retVal;
	}
	
	private final PropertyChangeListener syllabificationDisplayListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final SyllabificationChangeData newVal = (SyllabificationChangeData)evt.getNewValue();
			final SyllabificationDisplay display = (SyllabificationDisplay)evt.getSource();
			final ScTypeEdit edit = new ScTypeEdit(getEditor(), display.getTranscript(), newVal.getPosition(), newVal.getScType());
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};
	
	private final PropertyChangeListener hiatusChangeListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final SyllabificationDisplay display = (SyllabificationDisplay)evt.getSource();
			final ToggleDiphthongEdit edit = new ToggleDiphthongEdit(getEditor(), display.getTranscript(), (Integer)evt.getNewValue());
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};
	
	private PhoneMapDisplay getAlignmentDisplay(int group) {
		PhoneMapDisplay retVal = null;
		if(group < alignmentDisplayus.size()) {
			retVal = alignmentDisplayus.get(group);
		} else {
			retVal = new PhoneMapDisplay();
			retVal.addPropertyChangeListener(PhoneMapDisplay.ALIGNMENT_CHANGE_PROP, alignmentDisplayListener);
			alignmentDisplayus.add(retVal);
		}
		return retVal;
	}
	
	private final PropertyChangeListener alignmentDisplayListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final AlignmentChangeData newVal = (AlignmentChangeData)evt.getNewValue();
			final PhoneMapDisplay display = (PhoneMapDisplay)evt.getSource();

			final int gIdx = alignmentDisplayus.indexOf(display);
			
			final SyllabificationDisplay tDisplay = targetDisplays.get(gIdx);
			final SyllabificationDisplay aDisplay = actualDisplays.get(gIdx);
			final PhoneMap pm = new PhoneMap(tDisplay.getTranscript(), aDisplay.getTranscript());
			pm.setTopAlignment(newVal.getAlignment()[0]);
			pm.setBottomAlignment(newVal.getAlignment()[1]);
			
			final Record r = getEditor().currentRecord();
			final TierEdit<PhoneMap> edit = new TierEdit<PhoneMap>(getEditor(), r.getPhoneAlignment(), gIdx, pm);
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};
	
	public void update() {
		final boolean showTarget = targetIPABox.isSelected();
		final boolean showActual = actualIPABox.isSelected();
		final boolean showAlignment = alignmentBox.isSelected();
		final boolean colorInAlignment = colorInAlignmentBox.isSelected();
		
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		
		int maxExtra = Math.max(targetDisplays.size(), actualDisplays.size());
		maxExtra = Math.max(maxExtra, alignmentDisplayus.size());
		contentPane.removeAll();
		
		final ImageIcon reloadIcn = IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
		
		if(showTarget) {
			final ResetSyllabificationCommand resetTargetAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPATarget.getName());
			resetTargetAct.putValue(Action.NAME, null);
			resetTargetAct.putValue(Action.SMALL_ICON, reloadIcn);
			final JButton btn = new JButton(resetTargetAct);
			
			final JLabel targetSyllLbl = new JLabel(SystemTierType.TargetSyllables.getName());
			targetSyllLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			targetSyllLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
			
			final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			panel.setOpaque(false);
			panel.add(btn);
			panel.add(targetSyllLbl);
			
			final TierDataConstraint targetConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 0);
			contentPane.add(panel, targetConstraint);
		}
		
		if(showActual) {
			final ResetSyllabificationCommand resetActualAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPAActual.getName());
			resetActualAct.putValue(Action.NAME, null);
			resetActualAct.putValue(Action.SMALL_ICON, reloadIcn);
			final JButton btn = new JButton(resetActualAct);
			
			final JLabel actualSyllLbl = new JLabel(SystemTierType.ActualSyllables.getName());
			actualSyllLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			actualSyllLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
			
			final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			panel.setOpaque(false);
			panel.add(btn);
			panel.add(actualSyllLbl);
			
			final TierDataConstraint actualConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 1);
			contentPane.add(panel, actualConstraint);
		}
		
		if(showAlignment) {
			final ResetAlignmentCommand resetAct = new ResetAlignmentCommand(getEditor(), this);
			resetAct.putValue(Action.NAME, null);
			resetAct.putValue(Action.SMALL_ICON, reloadIcn);
			final JButton btn = new JButton(resetAct);
			
			final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
			separator.setForeground(Color.lightGray);
			final TierDataConstraint sepConstraint = new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, 2);
			contentPane.add(separator, sepConstraint);
			
			final JLabel alignSyllLbl = new JLabel(SystemTierType.SyllableAlignment.getName());
			alignSyllLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			alignSyllLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
			
			final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			panel.setOpaque(false);
			panel.add(btn);
			panel.add(alignSyllLbl);
			
			final TierDataConstraint alignConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 3);
			contentPane.add(panel, alignConstraint);
		}
		
		final TierDataLayout layout = TierDataLayout.class.cast(contentPane.getLayout());		
		for(int gIndex = 0; gIndex < record.numberOfGroups(); gIndex++) {
			final Group group = record.getGroup(gIndex);
			
			if(showTarget) {
				// target
				final IPATranscript ipaTarget = group.getIPATarget();
				final SyllabificationDisplay ipaTargetDisplay = getIPATargetDisplay(gIndex);
				ipaTargetDisplay.setFont(FontPreferences.getTierFont());
				ipaTargetDisplay.setTranscript(ipaTarget);
				
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
				ipaActualDisplay.setFont(FontPreferences.getTierFont());
				ipaActualDisplay.setTranscript(ipaActual);
				
				if(!layout.hasLayoutComponent(ipaActualDisplay)) {
					final TierDataConstraint ipaActualConstraint = 
							new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIndex, 1);
					contentPane.add(ipaActualDisplay, ipaActualConstraint);
				}
			}
			
			if(showAlignment) {
				// alignment
				final PhoneMap grp = group.getPhoneAlignment();
				PhoneMap pm = null;
				if(grp != null) {
					pm = new PhoneMap(grp.getTargetRep(), grp.getActualRep());
					pm.setTopAlignment(Arrays.copyOf(grp.getTopAlignment(), grp.getAlignmentLength()));
					pm.setBottomAlignment(Arrays.copyOf(grp.getBottomAlignment(), grp.getAlignmentLength()));
				}
				final PhoneMapDisplay pmDisplay = getAlignmentDisplay(gIndex);
				pmDisplay.setFont(FontPreferences.getTierFont());
				pmDisplay.setPhoneMapForGroup(0, pm);
				pmDisplay.setPaintPhoneBackground(colorInAlignment);
				
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
	@RunOnEDT
	public void onDataChanged(EditorEvent ee) {
		update();
		repaint();
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if(ee.getEventData() != null) {
			final String tierName = ee.getEventData().toString();
			if(SystemTierType.IPATarget.getName().equals(tierName) ||
					SystemTierType.IPAActual.getName().equals(tierName) ||
					SystemTierType.SyllableAlignment.getName().equals(tierName)) {
				update();
				repaint();
			}
		}
	}
	
	@RunOnEDT
	public void onTierChange(EditorEvent ee) {
		if(ee.getEventData() != null && ee.getSource() == getEditor().getUndoSupport()) {
			update();
			repaint();
		}
	}
	
	@RunOnEDT
	public void onScChange(EditorEvent ee) {
		if(ee.getEventData() != null && ee.getEventData() instanceof IPATranscript) {
			final IPATranscript ipa = (IPATranscript)ee.getEventData();
			final Record r = getEditor().currentRecord();
			for(int i = 0; i < r.numberOfGroups(); i++) {
				final SyllabificationDisplay targetDisplay = targetDisplays.get(i);
				final SyllabificationDisplay actualDisplay = actualDisplays.get(i);
				boolean found = false;
				if(targetDisplay.getTranscript() == ipa) {
					targetDisplay.repaint();
					found = true;
				} else if(actualDisplay.getTranscript() == ipa) {
					actualDisplay.repaint();
					found = true;
				}
				if(found) {
					alignmentDisplayus.get(i).repaint();
					return;
				}
			}
		}
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
		final JMenu retVal = new JMenu();
		
		retVal.add(new SyllabificationSettingsCommand(getEditor(), this));
		
		retVal.addSeparator();
		
		final ResetSyllabificationCommand resetIPATargetAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPATarget.getName());
		retVal.add(resetIPATargetAct);
		
		final ResetSyllabificationCommand resetIPAActualAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPAActual.getName());
		retVal.add(resetIPAActualAct);
		
		final ResetAlignmentCommand resetAlignmentAct = new ResetAlignmentCommand(getEditor(), this);
		retVal.add(resetAlignmentAct);
		
		return retVal;
	}
	
}
