package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.phon.app.media.TimeComponent;
import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.EditorView;

public abstract class TimeGridTier extends TimeComponent {

	private static final long serialVersionUID = 1L;

	public WeakReference<TimeGridView> parentViewRef;
	
	private Timebar timebar;
	
	private JPanel contentPane;
	
	public TimeGridTier(TimeGridView parent) {
		super(parent.getTimeModel());
		
		this.parentViewRef = new WeakReference<TimeGridView>(parent);
		init();
	}
	
	private void init() {
		timebar = new Timebar(getTimeModel());
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
	
	public void setContentPane(JPanel contentPane) {
		var oldVal = this.contentPane;
		remove(this.contentPane);
		this.contentPane = contentPane;
		if(contentPane != null)
			add(contentPane, BorderLayout.CENTER);
		revalidate();
		super.firePropertyChange("contentPane", oldVal, contentPane);
	}
	
	public TimeGridView getParentView() {
		return this.parentViewRef.get();
	}
	
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();
		retVal.width = timebar.getPreferredSize().width;
		return retVal;
	}
	
	public boolean isResizeable() {
		return true;
	}
	
	/**
	 * Called when the {@link EditorView} is closed
	 * 
	 */
	public void onClose() {
		
	}
	
}
