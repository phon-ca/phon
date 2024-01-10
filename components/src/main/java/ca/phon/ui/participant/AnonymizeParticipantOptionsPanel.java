/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui.participant;

import ca.phon.util.PrefHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Options for anonymizing participant data.
 */
public class AnonymizeParticipantOptionsPanel extends JPanel {

	private static final long serialVersionUID = 2621413046319572275L;

	private final static String ANON_ID_PROP = 
			AnonymizeParticipantOptionsPanel.class.getName() + "anonId";
	private JCheckBox anonIdBox;
	
	private final static String ANON_NAME_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonName";
	private JCheckBox anonNameBox;
	
	private final static String ANON_BDAY_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonBday";
	private JCheckBox anonBdayBox;
	
	private final static String ANON_SEX_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonSex";
	private JCheckBox anonSexBox;
	
	private final static String ANON_AGE_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonAge";
	private JCheckBox anonAgeBox;
	
	private final static String ANON_LANG_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonLang";
	private JCheckBox anonLangBox;

	private final static String ANON_FIRSTLANG_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonFirstLang";
	private JCheckBox anonFirstLangBox;

	
	private final static String ANON_GROUP_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonGroup";
	private JCheckBox anonGroupBox;
	
	private final static String ANON_EDU_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonEdu";
	private JCheckBox anonEduBox;
	
	private final static String ANON_SES_PROP = 
			AnonymizeParticipantOptionsPanel.class.getName() + "anonSes";
	private JCheckBox anonSesBox;

	private final static String ANON_OTHER_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonOther";
	private JCheckBox anonOtherBox;

	private final static String ANON_BIRTHPLACE_PROP =
			AnonymizeParticipantOptionsPanel.class.getName() + "anonBirthplace";
	private JCheckBox anonBirthplaceBox;
	
	public AnonymizeParticipantOptionsPanel() {
		super();
		
		init();
	}
	
	public boolean isAssignId() {
		return anonIdBox.isSelected();
	}
	
	public boolean isAnonName() {
		return anonNameBox.isSelected();
	}
	
	public boolean isAnonBday() {
		return anonBdayBox.isSelected();
	}
	
	public boolean isAnonAge() {
		return anonAgeBox.isSelected();
	}
	
	public boolean isAnonSex() {
		return anonSexBox.isSelected();
	}
	
	public boolean isAnonLang() {
		return anonLangBox.isSelected();
	}

	public boolean isAnonGroup() {
		return anonGroupBox.isSelected();
	}
	
	public boolean isAnonEdu() {
		return anonEduBox.isSelected();
	}
	
	public boolean isAnonSes() {
		return anonSesBox.isSelected();
	}

	public boolean isAnonOther() {
		return anonOtherBox.isSelected();
	}

	public boolean isAnonBirthplace() {
		return anonBirthplaceBox.isSelected();
	}

	public boolean isAnonFirstLang() {
		return anonFirstLangBox.isSelected();
	}

	private void init() {
		anonIdBox = new JCheckBox("Assign ID from role");
		anonIdBox.setSelected(PrefHelper.getBoolean(ANON_ID_PROP, true));
		anonIdBox.addActionListener(new AnonymizeItemListener(ANON_ID_PROP, anonIdBox));
		
		anonNameBox = new JCheckBox("Name");
		anonNameBox.setSelected(PrefHelper.getBoolean(ANON_NAME_PROP, true));
		anonNameBox.addActionListener(new AnonymizeItemListener(ANON_NAME_PROP, anonNameBox));
		
		anonBdayBox = new JCheckBox("Birthday");
		anonBdayBox.setSelected(PrefHelper.getBoolean(ANON_BDAY_PROP, true));
		anonBdayBox.addActionListener(new AnonymizeItemListener(ANON_BDAY_PROP, anonBdayBox));
		
		anonAgeBox = new JCheckBox("Age");
		anonAgeBox.setSelected(PrefHelper.getBoolean(ANON_AGE_PROP, true));
		anonAgeBox.addActionListener(new AnonymizeItemListener(ANON_AGE_PROP, anonAgeBox));
		
		anonSexBox = new JCheckBox("Sex");
		anonSexBox.setSelected(PrefHelper.getBoolean(ANON_SEX_PROP, true));
		anonSexBox.addActionListener(new AnonymizeItemListener(ANON_SEX_PROP, anonSexBox));
		
		anonLangBox = new JCheckBox("Language");
		anonLangBox.setSelected(PrefHelper.getBoolean(ANON_LANG_PROP, true));
		anonLangBox.addActionListener(new AnonymizeItemListener(ANON_LANG_PROP, anonLangBox));

		anonFirstLangBox = new JCheckBox("First Language");
		anonFirstLangBox.setSelected(PrefHelper.getBoolean(ANON_FIRSTLANG_PROP, true));
		anonFirstLangBox.addActionListener(new AnonymizeItemListener(ANON_FIRSTLANG_PROP, anonFirstLangBox));
		
		anonEduBox = new JCheckBox("Education");
		anonEduBox.setSelected(PrefHelper.getBoolean(ANON_EDU_PROP, true));
		anonEduBox.addActionListener(new AnonymizeItemListener(ANON_EDU_PROP, anonEduBox));
		
		anonGroupBox = new JCheckBox("Group");
		anonGroupBox.setSelected(PrefHelper.getBoolean(ANON_GROUP_PROP, true));
		anonGroupBox.addActionListener(new AnonymizeItemListener(ANON_GROUP_PROP, anonGroupBox));
		
		anonSesBox = new JCheckBox("SES");
		anonSesBox.setSelected(PrefHelper.getBoolean(ANON_SES_PROP, true));
		anonSesBox.addActionListener(new AnonymizeItemListener(ANON_SES_PROP, anonSesBox));

		anonBirthplaceBox = new JCheckBox("Birthplace");
		anonBirthplaceBox.setSelected(PrefHelper.getBoolean(ANON_BIRTHPLACE_PROP, true));
		anonBirthplaceBox.addActionListener(new AnonymizeItemListener(ANON_BIRTHPLACE_PROP, anonBirthplaceBox));

		anonOtherBox = new JCheckBox("Other");
		anonOtherBox.setSelected(PrefHelper.getBoolean(ANON_OTHER_PROP, true));
		anonOtherBox.addActionListener(new AnonymizeItemListener(ANON_OTHER_PROP, anonOtherBox));
		
		setLayout(new GridLayout(0, 3));
		add(anonIdBox);
		add(anonNameBox);
		add(anonSexBox);
		add(anonBdayBox);
		add(anonAgeBox);
		add(anonLangBox);
		add(anonFirstLangBox);
		add(anonEduBox);
		add(anonGroupBox);
		add(anonSesBox);
		add(anonBirthplaceBox);
		add(anonOtherBox);
	}

	private class AnonymizeItemListener implements ActionListener {

		private final String id;
		
		private final JCheckBox checkBox;
		
		public AnonymizeItemListener(String id, JCheckBox checkBox) {
			this.id = id;
			this.checkBox = checkBox;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			PrefHelper.getUserPreferences().putBoolean(id, checkBox.isSelected());
		}
		
	}
	
}
