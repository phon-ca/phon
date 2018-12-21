package ca.phon.app.session.check;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.session.check.CheckAlignment;

public class CheckAlignmentUI extends JPanel implements SessionCheckUI {

	private final CheckAlignment check;
	
	private JCheckBox resetAlignmentBox;
	
	public CheckAlignmentUI(CheckAlignment check) {
		super();
		this.check = check;
	
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		resetAlignmentBox = new JCheckBox("Reset alignments");
		resetAlignmentBox.setSelected(check.isResetAlignment());
		resetAlignmentBox.addActionListener( (e) -> {
			check.setResetAlignment(resetAlignmentBox.isSelected());
		});
		add(resetAlignmentBox);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		resetAlignmentBox.setEnabled(enabled);
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

}
