package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.phon.app.media.Timebar;

public abstract class TimeGridTier extends JComponent {

	private static final long serialVersionUID = 1L;

	public WeakReference<TimeGridView> parentViewRef;
	
	private Timebar timebar;
	
	private JPanel contentPane;
	
	public TimeGridTier(TimeGridView parent) {
		super();
		
		this.parentViewRef = new WeakReference<TimeGridView>(parent);
		init();
	}
	
	private void init() {
		timebar = new Timebar(getParentView().getTimebarModel());
		timebar.setBackground(Color.WHITE);
		timebar.setOpaque(true);
		
		setLayout(new BorderLayout());
		add(timebar, BorderLayout.NORTH);
		
		contentPane = new JPanel();
		add(contentPane, BorderLayout.CENTER);
	}
	
	public Timebar getTimebar() {
		return this.timebar;
	}
	
	public JPanel getContentPane() {
		return this.contentPane;
	}
	
	public TimeGridView getParentView() {
		return this.parentViewRef.get();
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();
		Dimension timePref = timebar.getPreferredSize();
		return new Dimension((int)timePref.getWidth(), (int)retVal.getHeight());
	}
	
}
