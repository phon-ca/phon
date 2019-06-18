package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ca.phon.app.media.Timebar;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;

public class RecordGrid extends TimeGridTier {
	
	private List<String> includedTiers = new ArrayList<>();
	
	private Timebar timebar;
	
	public RecordGrid(TimeGridView parent) {
		super(parent);
		
		includedTiers.add(SystemTierType.Orthography.getName());
	
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
	
		
	}
	
	public Timebar getTimebar() {
		return this.timebar;
	}
	
}
