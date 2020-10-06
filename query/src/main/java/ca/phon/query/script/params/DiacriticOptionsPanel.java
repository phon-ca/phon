package ca.phon.query.script.params;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import ca.phon.ipamap2.DiacriticSelector;
import ca.phon.ipamap2.IPAMapCellSelectionListener;
import ca.phon.ipamap2.IPAMapGrid;
import ca.phon.ipamap2.IPAMapGridContainer;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
import ca.phon.script.params.ScriptParam;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DiacriticOptionsPanel extends JPanel {
	
	private JCheckBox ignoreDiacriticsBox;
	
	private DropDownButton dropDownButton;
	private DiacriticSelector diacriticSelector;
	
	private JComboBox<SelectionMode> modeBox;
	
	private DiacriticOptionsScriptParam diacriticOptionsParam;
	
	private IPAMapGrid selectedGridMap;
	
	public DiacriticOptionsPanel() {
		this(new DiacriticOptionsScriptParam("", "", false, List.of()));
	}
	
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
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		
		ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
		ignoreDiacriticsBox.setSelected(diacriticOptionsParam.isIgnoreDiacritics());
		ignoreDiacriticsBox.setToolTipText("Select to ignore diacritics");
		ignoreDiacriticsBox.addChangeListener((e) -> {
			updateButtons();
		});
		final ChangeListener ignoreDiacriticsChangeListener = (e) -> {
			diacriticOptionsParam.setIgnoreDiacritics(ignoreDiacriticsBox.isSelected());
		};
		ignoreDiacriticsBox.addChangeListener(ignoreDiacriticsChangeListener);
		diacriticOptionsParam.addPropertyChangeListener(diacriticOptionsParam.getIgnoreDiacriticsParamId(), (e) -> {
			ignoreDiacriticsBox.removeChangeListener(ignoreDiacriticsChangeListener);
			ignoreDiacriticsBox.setSelected(diacriticOptionsParam.isIgnoreDiacritics());
			ignoreDiacriticsBox.addChangeListener(ignoreDiacriticsChangeListener);
		});
		add(ignoreDiacriticsBox, gbc);
		
		++gbc.gridx;
		modeBox = new JComboBox<>(SelectionMode.values());
		modeBox.setToolTipText("Diacritic selection mode");
		modeBox.setSelectedItem(diacriticOptionsParam.getSelectionMode());
		final ItemListener modeListener = (e) -> {
			diacriticOptionsParam.setSelectionMode((SelectionMode)modeBox.getSelectedItem());
		};
		modeBox.addItemListener(modeListener);
		diacriticOptionsParam.addPropertyChangeListener(diacriticOptionsParam.getSelectionModeParamId(), (e) -> {
			modeBox.removeItemListener(modeListener);
			modeBox.setSelectedItem(diacriticOptionsParam.getSelectionMode());
			modeBox.addItemListener(modeListener);
		});
		add(modeBox, gbc);
		
		++gbc.gridx;
		final CountDownLatch latch = new CountDownLatch(1);
		diacriticSelector = new DiacriticSelector();
		diacriticSelector.setSelectedDiacritics(diacriticOptionsParam.getSelectedDiacritics());
		final PropertyChangeListener diacriticListener = (e) -> {
			diacriticOptionsParam.setSelectedDiacritics(diacriticSelector.getSelectedDiacritics());
		};
		diacriticSelector.addPropertyChangeListener("selected", diacriticListener);
		diacriticOptionsParam.addPropertyChangeListener(diacriticOptionsParam.getSelectedDiacriticsParamId(), (e) -> {
			diacriticSelector.removePropertyChangeListener("selected", diacriticListener);
			diacriticSelector.setSelectedDiacritics(diacriticOptionsParam.getSelectedDiacritics());
			diacriticSelector.addPropertyChangeListener("selected", diacriticListener);
		});
		
		latch.countDown();
		
		PhonUIAction dropDownAct = new PhonUIAction(this, "noop");
		dropDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Click to show diacritic selector");
		dropDownAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL));
		dropDownAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		dropDownAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		dropDownAct.putValue(DropDownButton.BUTTON_POPUP, diacriticSelector);
		dropDownAct.putValue(PhonUIAction.NAME, "Select diacritics");
		dropDownButton = new DropDownButton(dropDownAct);
		dropDownButton.setOnlyPopup(true);
		add(dropDownButton, gbc);

		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		++gbc.gridx;
		add(Box.createHorizontalGlue(), gbc);
		
		gbc.gridx = 0;
		++gbc.gridy;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		
		IPAMapGridContainer container = new IPAMapGridContainer();
		var selectedGrid = diacriticSelector.getSelectedGrid();
		selectedGrid.setName("Selected");
		var gridTuple = container.addGrid(diacriticSelector.getSelectedGrid());
		selectedGridMap = gridTuple.getObj2();
		add(container, gbc);
		
		diacriticOptionsParam.addPropertyChangeListener(ScriptParam.ENABLED_PROP, (e) -> {
			ignoreDiacriticsBox.setEnabled(diacriticOptionsParam.isEnabled());
			dropDownButton.setEnabled(diacriticOptionsParam.isEnabled() && ignoreDiacriticsBox.isSelected());
			modeBox.setEnabled(diacriticOptionsParam.isEnabled() && ignoreDiacriticsBox.isSelected());
			selectedGridMap.setEnabled(diacriticOptionsParam.isEnabled() && ignoreDiacriticsBox.isSelected());
		});
		
		diacriticSelector.getMapGridContainer().addCellSelectionListener(new IPAMapCellSelectionListener() {
			
			@Override
			public void cellSelectionChanged(IPAMapGrid mapGrid, int cellIdx, boolean selected) {
				container.revalidate();
				gridTuple.getObj2().revalidate();
				container.repaint();
				
				diacriticSelector.getMapGridContainer().repaint();
			}
			
		});
		
		updateButtons();
	}
	
	public JCheckBox getIgnoreDiacriticsBox() {
		return this.ignoreDiacriticsBox;
	}
	
	public DiacriticSelector getDiacriticSelector() {
		return this.diacriticSelector;
	}
	
	public JComboBox<SelectionMode> getSelectionModeBox() {
		return this.modeBox;
	}
	
	public DiacriticOptionsScriptParam getDiacriticOptions() {
		return this.diacriticOptionsParam;
	}
	
	private void updateButtons() {
		modeBox.setEnabled(ignoreDiacriticsBox.isSelected());
		dropDownButton.setEnabled(ignoreDiacriticsBox.isSelected());
		selectedGridMap.setVisible(ignoreDiacriticsBox.isSelected());
	}
	
}
