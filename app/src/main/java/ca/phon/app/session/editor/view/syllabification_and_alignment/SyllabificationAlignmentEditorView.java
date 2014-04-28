package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayout;
import ca.phon.app.session.editor.view.common.TierDataLayoutButtons;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierListener;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.ipa.PhoneMapDisplay;
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
		
		final PhonUIAction settingsAct = new PhonUIAction(this, "onShowSettings");
		settingsAct.putValue(PhonUIAction.NAME, "Syllabifier settings");
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
			targetSyllLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			targetSyllLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
			final TierDataConstraint targetConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 0);
			contentPane.add(targetSyllLbl, targetConstraint);
		}
		
		if(showActual) {
			final JLabel actualSyllLbl = new JLabel(SystemTierType.ActualSyllables.getName());
			actualSyllLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			actualSyllLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
			final TierDataConstraint actualConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 1);
			contentPane.add(actualSyllLbl, actualConstraint);
		}
		
		if(showAlignment) {
			final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
			separator.setForeground(Color.lightGray);
			final TierDataConstraint sepConstraint = new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, 2);
			contentPane.add(separator, sepConstraint);
			
			final JLabel alignSyllLbl = new JLabel(SystemTierType.SyllableAlignment.getName());
			alignSyllLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			alignSyllLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
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
				ipaActualDisplay.setTranscript(ipaActual);
				
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
	
	/**
	 * Show syllabifier settings dialog.
	 * 
	 */
	public void onShowSettings() {
		final JDialog settingsDialog = new JDialog(getEditor());
		settingsDialog.setModal(true);
		
		settingsDialog.setLayout(new BorderLayout());
		final DialogHeader header = new DialogHeader("Syllabifier settings", "Select syllabifier for IPA tiers.");
		settingsDialog.add(header, BorderLayout.NORTH);
		
		final SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		final SyllabificationSettingsPanel settingsPanel = new SyllabificationSettingsPanel(info);
		settingsDialog.add(settingsPanel, BorderLayout.CENTER);
		
		final AtomicBoolean wasCanceled = new AtomicBoolean(true);
		final JPanel btmPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton okBtn = new JButton("Ok");
		okBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wasCanceled.getAndSet(false);
				settingsDialog.setVisible(false);
			}
			
		});
		btmPanel.add(okBtn);
		settingsDialog.getRootPane().setDefaultButton(okBtn);
		
		settingsDialog.add(btmPanel, BorderLayout.SOUTH);
		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.setVisible(true);
		
		// wait
		
		if(!wasCanceled.get()) {
			info.setSyllabifierLanguageForTier(SystemTierType.IPATarget.getName(), settingsPanel.getSelectedTargetSyllabifier());
			info.setSyllabifierLanguageForTier(SystemTierType.IPAActual.getName(), settingsPanel.getSelectedActualSyllabifier());
			
			info.saveInfo(getEditor().getSession());
		}
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
		return null;
	}
	
}
