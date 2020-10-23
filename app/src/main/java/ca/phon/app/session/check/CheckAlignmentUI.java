package ca.phon.app.session.check;

import java.awt.*;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.session.check.*;

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
