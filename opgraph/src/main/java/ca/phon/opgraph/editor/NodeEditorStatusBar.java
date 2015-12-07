package ca.phon.opgraph.editor;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXStatusBar;

import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.OSInfo;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class NodeEditorStatusBar extends JXStatusBar {
	
	private static final long serialVersionUID = 8619512371093438605L;

	private JProgressBar progressBar;
	
	private JLabel progressLabel;
	
	public NodeEditorStatusBar() {
		super();
		
		init();
	}
	
	private void init() {
		JComponent pbar = new JPanel(new FormLayout("pref", 
				(OSInfo.isMacOs() ? "10px" : "pref")));
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		pbar.add(progressBar, (new CellConstraints()).xy(1, 1));
		
		progressLabel = new JLabel();
		progressLabel.setFont(FontPreferences.getSmallFont());
		
		add(progressLabel, new JXStatusBar.Constraint(200));
		add(pbar, new JXStatusBar.Constraint(120));
		add(new JLabel(), new JXStatusBar.Constraint(5));
	}
	
	public JProgressBar getProgressBar() {
		return this.progressBar;
	}
	
	public JLabel getProgressLabel() {
		return this.progressLabel;
	}

}
