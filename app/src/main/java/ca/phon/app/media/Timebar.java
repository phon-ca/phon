package ca.phon.app.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class Timebar extends JComponent {

	public final static int DEFAULT_MINOR_TICK_HEIGHT = 5;
	public final static int DEFUALT_MAJOR_TICK_HEIGHT = 8;
	
	private int minorTickHeight = DEFAULT_MINOR_TICK_HEIGHT;
	private int majorTickHeight = DEFUALT_MAJOR_TICK_HEIGHT;
	
	private TimeUIModel model;
	
	private final static String uiClassId = "TimebarUI";
	
	public Timebar() {
		this(new TimeUIModel());
	}
	
	public Timebar(TimeUIModel model) {
		super();
		
		this.model = model;
		
		updateUI();
	}
	
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	protected void setUI(ComponentUI newUI) {
		super.setUI(newUI);
	}

	@Override
	public void updateUI() {
		setUI(new DefaultTimebarUI());
	}

	public DefaultTimebarUI getUI() {
		return (DefaultTimebarUI)ui;
	}
	
	public TimeUIModel getModel() {
		return this.model;
	}
	
	public void setModel(TimeUIModel model) {
		var oldModel = this.model;
		this.model = model;
		super.firePropertyChange("model", oldModel, model);
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
