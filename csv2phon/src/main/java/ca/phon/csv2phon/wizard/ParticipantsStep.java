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
package ca.phon.csv2phon.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import ca.phon.application.transcript.IParticipant;
import ca.phon.csv2phon.io.ImportDescriptionType;
import ca.phon.csv2phon.io.ObjectFactory;
import ca.phon.csv2phon.io.ParticipantType;
import ca.phon.gui.CommonModuleFrame;
import ca.phon.gui.DialogHeader;
import ca.phon.gui.recordeditor.ParticipantEditor;
import ca.phon.util.PhonDateFormat;
import ca.phon.util.iconManager.IconManager;
import ca.phon.util.iconManager.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Setup participants for all imported sessions/
 *
 */
public class ParticipantsStep extends CSVImportStep {
	
	private DialogHeader header;
	
	/**
	 * Participant Table
	 * 
	 */
	private JXTable participantTable;
	private JButton editParticipantButton;
	private JButton addParticipantButton;
	
	/**
	 * Constructor
	 */
	public ParticipantsStep() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("CSV Import", "Set up participants.");
		add(header, BorderLayout.NORTH);
		
		FormLayout participantLayout = new FormLayout(
				"fill:pref:grow, 1dlu, pref",
				"pref, 3dlu, pref, 3dlu, pref, fill:pref:grow");
		CellConstraints cc = new CellConstraints();
		JPanel participantPanel = new JPanel(participantLayout);
		participantPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
		
		JLabel infoLabel = new JLabel(
				"<html><body><p>(Optional) Set up participants which will be added to each imported session.</p></body></html>");
		participantTable = new JXTable();
		
		ImageIcon addIcon = 
			IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
		addParticipantButton = new JButton(addIcon);
		addParticipantButton.setFocusable(false);
		addParticipantButton.setToolTipText("Add participant");
		addParticipantButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newParticipant();
			}
			
		});
		
		ImageIcon editIcon = 
			IconManager.getInstance().getIcon("actions/edit", IconSize.XSMALL);
		editParticipantButton = new JButton(editIcon);
		editParticipantButton.setFocusable(false);
		editParticipantButton.setToolTipText("Edit participant");
		editParticipantButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editParticipant();
			}
			
		});
		
		Action deleteParticipantAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteParticipant();
			}
			
		};
		ActionMap participantActionMap = participantTable.getActionMap();
		participantActionMap.put("DELETE_PARTICIPANT", deleteParticipantAction);
		InputMap participantInputMap = participantTable.getInputMap();
		participantInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_PARTICIPANT");
		participantInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_PARTICIPANT");
		
		participantTable.setActionMap(participantActionMap);
		participantTable.setInputMap(JComponent.WHEN_FOCUSED, participantInputMap);
		
		participantPanel.add(infoLabel, cc.xy(1,1));
		participantPanel.add(new JScrollPane(participantTable), cc.xywh(1, 3, 1, 4));
		participantPanel.add(addParticipantButton, cc.xy(3, 3));
		participantPanel.add(editParticipantButton, cc.xy(3, 5));
		
		add(participantPanel, BorderLayout.CENTER);
	}
	
	private void newParticipant() {
//		IPhonFactory factory = IPhonFactory.getDefaultFactory();
//		IParticipant part = factory.createParticipant();
		ObjectFactory factory = new ObjectFactory();
		ParticipantType pt = factory.createParticipantType();
		IParticipant part = new CSVImportParticipant(pt);
		
		boolean canceled = ParticipantEditor.editParticipant(CommonModuleFrame.getCurrentFrame(), part);
		
		if(!canceled) {
//			IParticipant newPart = model.getSession().newParticipant();
//			if(part.getName() != null)
//				newPart.setName(part.getName());
//			if(part.getBirthDate() != null)
//				newPart.setBirthDate(part.getBirthDate());
//			if(part.getEducation() != null)
//				newPart.setEducation(part.getEducation());
//			if(part.getGroup() != null)
//				newPart.setGroup(part.getGroup());
//			if(part.getLanguage() != null)
//				newPart.setLanguage(part.getLanguage());
//			if(part.getRole() != null)
//				newPart.setRole(part.getRole());
//			if(part.getSES() != null)
//				newPart.setSES(part.getSES());
//			if(part.getSex() != null)
//				newPart.setSex(part.getSex());
//			
//			ParticipantListEdit edit = 
//				new ParticipantListEdit(ParticipantListEdit.ParticipantEditType.Insertion,
//						model.getSession(), part);
//			model.getUndoSupport().postEdit(edit);
//			
			getSettings().getParticipant().add(pt);
			((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
//			model.fireRecordEditorEvent(PARTICIPANT_LIST_CHANGED, this);
		}
	}
	
	private void editParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		if(selectedRow >= 0 && selectedRow < getParticipants().size()) {
			IParticipant part = getParticipants().get(selectedRow);
			
			ParticipantEditor.editParticipant(CommonModuleFrame.getCurrentFrame(), part);
		
			((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
		}
	}
	
	private void deleteParticipant() {
		int selectedRow = participantTable.getSelectedRow();
		if(selectedRow < 0) return;
		
		selectedRow = participantTable.convertRowIndexToModel(selectedRow);
		if(selectedRow >= 0 && selectedRow < getParticipants().size()) {
			if(getSettings() != null) {
				getSettings().getParticipant().remove(selectedRow);
				((ParticipantsTableModel)participantTable.getModel()).fireTableDataChanged();
			}
		}
	}
	
	
	public List<IParticipant> getParticipants() {
		List<IParticipant> retVal = new ArrayList<IParticipant>();
		
		if(getSettings() != null) {
			for(ParticipantType pt:getSettings().getParticipant()) {
				retVal.add(new CSVImportParticipant(pt));
			}
		}
		
		return retVal;
	}
	
	
	
	
	@Override
	public void setSettings(ImportDescriptionType settings) {
		super.setSettings(settings);
		
		participantTable.setModel(new ParticipantsTableModel());
	}




	private class ParticipantsTableModel extends AbstractTableModel {
		
		public ParticipantsTableModel() {
			super();
			
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return getParticipants().size();
		}
		
		@Override
		public String getColumnName(int col) {
			String retVal = "";
			
			if(col == 0) retVal = "Name";
			else if(col == 1) retVal = "Birthday";
			
			return retVal;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object retVal = new String();
			IParticipant p = getParticipants().get(rowIndex);
			if(columnIndex == 0) {
				if(p.getName() == null) return p.getId();
				retVal = p.getName();
			} else if(columnIndex == 1) {
				if(p.getBirthDate() != null) {
					PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
					retVal = pdf.format(p.getBirthDate());
				}
			} 
			return retVal;
		}

	}

}
