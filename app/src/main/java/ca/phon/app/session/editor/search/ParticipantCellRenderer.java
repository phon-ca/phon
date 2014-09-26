package ca.phon.app.session.editor.search;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.phon.session.Participant;

public class ParticipantCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 8649813359217446517L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel retVal = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		
		if(value instanceof Participant) {
			final Participant participant = (Participant)value;
			retVal.setText(participant.getName());
		}
		
		return retVal;
	}
	
	

}
