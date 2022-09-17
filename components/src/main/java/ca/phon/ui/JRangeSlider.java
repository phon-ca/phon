/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui;

import ca.phon.util.MsFormat;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.event.*;
import java.text.Format;

/**
 * A component that lets the user graphically select a contiguous range of values by
 * sliding knobs withing a bounded interval.  Like the regular swing JSlider,
 * the component can show both major ticks marks and minor tick marks between
 * them.
 * 
 * @beaninfo
 * 		attribute: isContainer false
 *		description: A component that supports selecting a range of values from within a range.
 *
 * @version 1.0 2007-04-22
 *
 */
public class JRangeSlider extends JComponent implements SwingConstants {
	
	private static final long serialVersionUID = -2447438292052903645L;

	/** uiClassID */
	private static final String uiClassID = "RangeSliderUI";
		
	/** The model */
	private BoundedRangeModel _model;
	
	/** The number of values between major ticks. */
	protected int majorTickSpacing;
	
	/** The number of values between minor ticks. */
	protected int minorTickSpacing;
	
	/** Snap to ticks? */
	protected boolean snapToTicks = false;
	
	/** Snap to value? */
	protected boolean snapToValue = true;
	
	/** Paint the sliding value label? */
	protected boolean paintSlidingLabel = false;
	
	/** Component orientation */
	protected int orientation;
	
	/** The default change listener for the model. */
	protected ChangeListener changeListener = 
		makeChangeListener();
	
	/** The formatter */
	protected Format labelFormat;
	
	/** Keep one <CODE>ChangeEvent</CODE> per instance */
	protected transient ChangeEvent changeEvent = null;
	
	/** 
	 * Default Constructor.
	 * 
	 * Creates a horizontal slider with range[0-100] and selected
	 * range [20-80].
	 */
	public JRangeSlider() {
		this(HORIZONTAL, 0, 100, 20, 60);
	}
	
	

	/**
	 * Creates a slider with the specified orientation with range [0-100]
	 * and selected range [20-80].
	 */
	public JRangeSlider(int orientation) {
		this(orientation, 0, 100, 20, 60);
	}
	
	/**
	 * Creates a horizontal slider with the specified range 
	 * and selected range.
	 * 
	 * @param min
	 * @param max
	 * @param start
	 * @param length
	 */
	public JRangeSlider(int min, int max, int start, int length) {
		this(HORIZONTAL, min, max, start, length);
	}
	
	/**
	 * Creates a slider with the spectified orientation, range, 
	 * and selected range.
	 * 
	 * @param orientation
	 * @param min
	 * @param max
	 * @param start
	 * @param length
	 */
	public JRangeSlider(int orientation, int min, int max,
			int start, int length) {
		super();
		checkOrientation(orientation);
		this.orientation = orientation;
		_model = new DefaultBoundedRangeModel(start, length, min, max);
		_model.addChangeListener(changeListener);
		updateUI();
	}
	
	/**
	 * Get the UI object.
	 */
	public RangeSliderUI getUI() {
		return (RangeSliderUI)ui;
	}
	
	/**
	 * Set the component UI.
	 */
	public void setUI(RangeSliderUI ui) {
		super.setUI(ui);
	}
	
	/**
	 * Update UI
	 */
	@Override
	public void updateUI() {
		setUI(new DefaultRangeSliderUI(this));
	}
	
	/**
	 * Get UI classname.
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}
	
	private void checkOrientation(int orientation) {
		if(orientation != HORIZONTAL &&
				orientation != VERTICAL) {
			throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL");
		}
	}
	
	private ChangeListener makeChangeListener() {
		return new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				fireStateChange();
			}
			
		};
	}
	
	/**
	 * Add a change listener.
	 */
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}
	
	/**
	 * Remote a change listener.
	 */
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}
	
	/**
	 * Get the change listeners.
	 */
	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(
				ChangeListener.class);
	}
	
	/**
	 * Fire state change.
	 */
	protected void fireStateChange() {
		if(changeEvent == null)
			changeEvent = new ChangeEvent(this);
		
		for(ChangeListener changeListener:getChangeListeners()) {
			changeListener.stateChanged(changeEvent);
		}
	}
	
	/**
	 * The data model that handles the components four main attributes:
	 * min, max, start, length
	 * 
	 */
	public BoundedRangeModel getModel() {
		return _model;
	}
	
	/**
	 * Set the model.
	 */
	public void setModel(BoundedRangeModel model) {
		BoundedRangeModel oldModel = _model;
		
		if(oldModel != null)
			oldModel.removeChangeListener(changeListener);
		
		_model = model;
		
		if(_model != null) 
			_model.addChangeListener(changeListener);
		
		firePropertyChange("model", oldModel, model);
	}
	
	/**
	 * Return the start value
	 */
	public int getStart() {
		return getModel().getValue();
	}
	
	/**
	 * Set the start value
	 * @param value
	 */
	public void setStart(int value) {
		int oldValue = getModel().getValue();
		if(getModel().getValue() == value)
			return;
		
		getModel().setValue(value);
		firePropertyChange("value", oldValue, value);
	}
	
	/**
	 * Return the length value.
	 */
	public int getLength() {
		return getModel().getExtent();
	}
	
	/**
	 * Set the length
	 * @param length
	 */
	public void setLength(int length) {
		int oldValue = getModel().getExtent();
		if(getModel().getExtent() == length)
			return;
		
		getModel().setExtent(length);
		firePropertyChange("extent", oldValue, length);
	}
	
	/**
	 * Return the minimum
	 */
	public int getMinimum() {
		return getModel().getMinimum();
	}
	
	/**
	 * Set the minimum.
	 * 
	 * @param min
	 */
	public void setMinimum(int min) {
		int oldValue = getModel().getMinimum();
		if(getModel().getMinimum() == min)
			return;
		
		getModel().setMinimum(min);
		firePropertyChange("minimum", oldValue, min);
	}
	
	/**
	 * Return the maximum.
	 */
	public int getMaximum() {
		return getModel().getMaximum();
	}
	
	/**
	 * Set the maximum
	 */
	public void setMaximum(int max) {
		int oldValue = getModel().getMaximum();
		if(getModel().getMaximum() == max)
			return;
		
		getModel().setMaximum(max);
		firePropertyChange("maximum", oldValue, max);
	}
	
	/**
	 * Is one of the knobs being dragged?
	 */
	public boolean getValueIsAdjusting() {
		return getModel().getValueIsAdjusting();
	}
	
	/**
	 * Sets the valueIsAdjusting property.
	 */
	public void setValueIsAdjusting(boolean adjusting) {
		boolean oldValue = getModel().getValueIsAdjusting();
		if(getModel().getValueIsAdjusting() == adjusting)
			return;
		getModel().setValueIsAdjusting(adjusting);
		firePropertyChange("valueIsAdjusting", oldValue, adjusting);
	}
	
	/**
	 * The component orientation.
	 */
	public int getOrientation() {
		return orientation;
	}
	
	/**
	 * Set the component orientaiton.
	 */
	public void setOrientation(int orientation) {
		checkOrientation(orientation);
		
		int oldValue = this.orientation;
		if(oldValue == orientation)
			return;
		
		this.orientation = orientation;
		firePropertyChange("orientation", oldValue, orientation);
		revalidate();
	}
	
	public static void main(String[] args) {
		JRangeSlider slider = new JRangeSlider(1000, 10000, 1000, 3000);
		slider.setLabelFormat(new MsFormat());
		slider.setPaintSlidingLabel(true);
		
		JFrame f = new JFrame("Test Slider");
		
		FormLayout layout = new FormLayout(
				"3dlu, fill:pref:grow, 3dlu",
				"3dlu, pref, 3dlu");
		
		CellConstraints cc = new CellConstraints();
		f.add(slider);
		f.pack();
		f.setVisible(true);
	}

	public Format getLabelFormat() {
		return labelFormat;
	}



	public void setLabelFormat(Format labelFormat) {
		Format oldValue = this.labelFormat;
		if(this.labelFormat == labelFormat)
			return;
		
		this.labelFormat = labelFormat;
		firePropertyChange("labelformat", oldValue, labelFormat);
	}



	public boolean isPaintSlidingLabel() {
		return paintSlidingLabel;
	}

	public void setPaintSlidingLabel(boolean paintSlidingLabel) {
		this.paintSlidingLabel = paintSlidingLabel;
	}
}
