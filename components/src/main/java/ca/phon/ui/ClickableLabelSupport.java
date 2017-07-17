package ca.phon.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

/**
 * Add an action to {@link JLabel} components.
 *
 */
public class ClickableLabelSupport {
	
	private boolean doubleClick = false;
	
	private JLabel label;
	
	private Action action;
	
	public ClickableLabelSupport() {
		super();
	}
	
	public ClickableLabelSupport(JLabel label) {
		super();
		
		install(label);
	}
	
	public boolean isDoubleClick() {
		return this.doubleClick;
	}
	
	public void setDoubleClick(boolean doubleClick) {
		this.doubleClick = doubleClick;
	}
	
	public void setAction(Action action) {
		if(label != null && action.getValue(Action.SHORT_DESCRIPTION) != null) {
			label.setToolTipText(action.getValue(Action.SHORT_DESCRIPTION).toString());
		}
	}
	
	public void install(JLabel label) {
		if(this.label != null) {
			this.label.removeMouseListener(mouseListener);
		}
		if(label != null) {
			label.addMouseListener(mouseListener);
			label.setForeground(Color.BLUE);
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			if(action != null) {
				label.setToolTipText(action.getValue(Action.SHORT_DESCRIPTION).toString());
			}
		}
		
		this.label = label;
	}
	
	private final MouseInputAdapter mouseListener = new MouseInputAdapter() {
		
		@Override
		public void mouseClicked(MouseEvent me) {
			if(action != null) {
				action.actionPerformed(new ActionEvent(label, -1, "onClick"));
			}
		}
		
	};
	
}
