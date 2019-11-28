package ca.phon.query.script.params;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ca.phon.ipamap2.DiacriticSelector;
import ca.phon.ipamap2.IPAMapGrid;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DiacriticOptionsPanel extends JPanel {
	
	private JCheckBox ignoreDiacriticsBox;
	
	private DropDownButton dropDownButton;
	private DiacriticSelector diacriticSelector;
	private JTextField diacriticField;
	
	private JComboBox<SelectionMode> modeBox;
	
	private DiacriticOptionsScriptParam diacriticOptionsParam;
	
	public DiacriticOptionsPanel(DiacriticOptionsScriptParam diacriticOptionsParam) {
		super();
		this.diacriticOptionsParam = diacriticOptionsParam;
		
		init();
	}
	
	private void init() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		
		ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
		ignoreDiacriticsBox.setSelected(diacriticOptionsParam.isIgnoreDiacritics());
		ignoreDiacriticsBox.setToolTipText("Select to ignore diacritics");
		ignoreDiacriticsBox.addActionListener( (e) -> {
			updateButtons();
			diacriticOptionsParam.setIgnoreDiacritics(ignoreDiacriticsBox.isSelected());
		});
		add(ignoreDiacriticsBox, gbc);
		
		++gbc.gridx;
		modeBox = new JComboBox<>(SelectionMode.values());
		modeBox.setToolTipText("Diacritic selection mode");
		modeBox.setSelectedItem(diacriticOptionsParam.getSelectionMode());
		modeBox.addItemListener( (e) -> {
			diacriticOptionsParam.setSelectionMode((SelectionMode)modeBox.getSelectedItem());
		});
		add(modeBox, gbc);
		
		++gbc.gridx;
		final CountDownLatch latch = new CountDownLatch(1);
		diacriticSelector = new DiacriticSelector() {

			@Override
			public void cellSelectionChanged(IPAMapGrid mapGrid, int cellIdx, boolean selected) {
				if(latch.getCount() > 0) return;
				if(diacriticField != null) {
					String selectedTxt = diacriticSelector.getSelected().stream().collect(Collectors.joining(";"));
					diacriticField.setText(selectedTxt);
				}
				
				diacriticOptionsParam.setSelectedDiacritics(diacriticSelector.getSelectedDiacritics());
			}
		
			
		};
		diacriticSelector.setSelectedDiacritics(diacriticOptionsParam.getSelectedDiacritics());
		latch.countDown();
		
		PhonUIAction dropDownAct = new PhonUIAction(this, "noop");
		dropDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Click to show diacritic selector");
		dropDownAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL));
		dropDownAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		dropDownAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		dropDownAct.putValue(DropDownButton.BUTTON_POPUP, diacriticSelector);
		dropDownButton = new DropDownButton(dropDownAct);
		dropDownButton.setOnlyPopup(true);
		add(dropDownButton, gbc);
		
		++gbc.gridx;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		diacriticField = new JTextField();
		diacriticField.setFont(FontPreferences.getUIIpaFont());
		diacriticField.setEditable(false);
		
		String selectedTxt = diacriticSelector.getSelected().stream().collect(Collectors.joining(";"));
		diacriticField.setText(selectedTxt);
		add(diacriticField, gbc);
		
		updateButtons();
	}
	
	public DiacriticOptionsScriptParam getDiacriticOptions() {
		return this.diacriticOptionsParam;
	}
	
	private void updateButtons() {
		modeBox.setEnabled(ignoreDiacriticsBox.isSelected());
		dropDownButton.setEnabled(ignoreDiacriticsBox.isSelected());
		diacriticField.setEnabled(ignoreDiacriticsBox.isSelected());
	}
	
}
