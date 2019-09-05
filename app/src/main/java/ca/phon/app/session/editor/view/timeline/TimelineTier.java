package ca.phon.app.session.editor.view.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;

import ca.phon.app.media.TimeComponent;
import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.EditorView;
import ca.phon.ui.menu.MenuBuilder;

public abstract class TimelineTier extends TimeComponent {

	private static final long serialVersionUID = 1L;

	public WeakReference<TimelineView> parentViewRef;
	
	private Timebar timebar;
	
	private JPanel contentPane;
	
	public TimelineTier(TimelineView parent) {
		super(parent.getTimeModel());
		
		this.parentViewRef = new WeakReference<TimelineView>(parent);
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
	
	public TimelineView getParentView() {
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
	 * Setup context menu
	 */
	public void setupContextMenu(MouseEvent me, JMenu contextMenu) {
		
	}
	
	/**
	 * Called when the {@link EditorView} is closed
	 * 
	 */
	public void onClose() {
		
	}
	
}
