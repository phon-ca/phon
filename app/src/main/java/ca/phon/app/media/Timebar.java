package ca.phon.app.media;

import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class Timebar extends TimeComponent {

	public final static int DEFAULT_MINOR_TICK_HEIGHT = 5;
	public final static int DEFUALT_MAJOR_TICK_HEIGHT = 8;
	
	private int minorTickHeight = DEFAULT_MINOR_TICK_HEIGHT;
	private int majorTickHeight = DEFUALT_MAJOR_TICK_HEIGHT;
	
	private final static String uiClassId = "TimebarUI";
	
	public Timebar() {
		this(new TimeUIModel());
	}
	
	public Timebar(TimeUIModel model) {
		super(model);
		
		setFont(UIManager.getFont("Label.font"));
		updateUI();
	}
	
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	public void updateUI() {
		setUI(new DefaultTimebarUI());
	}

	public DefaultTimebarUI getUI() {
		return (DefaultTimebarUI)ui;
	}
	
	public int getMinorTickHeight() {
		return minorTickHeight;
	}

	public void setMinorTickHeight(int minorTickHeight) {
		var oldVal = this.minorTickHeight;
		this.minorTickHeight = minorTickHeight;
		super.firePropertyChange("minorTickHeight", oldVal, minorTickHeight);
	}

	public int getMajorTickHeight() {
		return majorTickHeight;
	}

	public void setMajorTickHeight(int majorTickHeight) {
		var oldVal = this.majorTickHeight;
		this.majorTickHeight = majorTickHeight;
		super.firePropertyChange("majorTickHeight", oldVal, majorTickHeight);
	}
	
}
