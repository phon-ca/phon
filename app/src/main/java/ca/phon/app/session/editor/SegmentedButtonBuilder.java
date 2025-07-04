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
package ca.phon.app.session.editor;

import org.pushingpixels.substance.api.SubstanceConstants.Side;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

import javax.swing.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Utility methods for creating segmented buttons.
 * 
 * Methods modified from Apple Technical Note TN2196
 */
public class SegmentedButtonBuilder<T extends AbstractButton> {

	private Supplier<T> supplier;

	public SegmentedButtonBuilder(Supplier<T> supplier) {
		super();
		this.supplier = supplier;
	}

	// Create a Layout component that will ensure the buttons abut each other
	public JComponent createLayoutComponent(List<T> segmentButtons) {
		Box layoutBox = Box.createHorizontalBox();
		for (T button : segmentButtons) {
			layoutBox.add(button);
		}
		return layoutBox;
	}

	public T createSegmentButton(String style, String position, ButtonGroup buttonGrp) {
		T button = supplier.get();

		// client properties for Mac OS X
		button.putClientProperty("JButton.buttonType", style);
		button.putClientProperty("JButton.segmentPosition", position);

		// client properties for Substance L&F
		Set<Side> openSides = new LinkedHashSet<Side>();
		if (position.equals("first")) {
			openSides.add(Side.RIGHT);
		} else if (position.equals("middle")) {
			openSides.add(Side.LEFT);
			openSides.add(Side.RIGHT);
		} else if (position.equals("last")) {
			openSides.add(Side.LEFT);
		}

		button.putClientProperty(SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY, openSides);

		buttonGrp.add(button);
		return button;
	}

	// Bottleneck for creating the buttons for the button group
	public List<T> createSegmentButtonsWithStyle(int numButtons, ButtonGroup buttonGrp, String style) {
		// Allocate a list of JButtons
		List<T> buttons = new ArrayList<>();
		if (numButtons == 1) {
			// If 1 button is requested, then it gets the "only" segment
			// position
			buttons.add(createSegmentButton(style, "only", buttonGrp));
		} else {
			// If more than 1 button is requested, then
			// the first one gets "first" the last one gets "last" and the rest
			// get "middle"
			buttons.add(createSegmentButton(style, "first", buttonGrp));
			for (int i = 1; i < numButtons - 1; i++) {
				buttons.add(createSegmentButton(style, "middle", buttonGrp));
			}
			buttons.add(createSegmentButton(style, "last", buttonGrp));
		}
		return buttons;
	}

	// Convenience methods that pass in the correct button style for each
	// segmented button style
	public List<T> createSegmentedButtons(int numButtons, ButtonGroup buttonGroup) {
		return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmented");
	}

	public List<T> createSegmentedRoundRectButtons(int numButtons, ButtonGroup buttonGroup) {
		return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmentedRoundRect");
	}

	public List<T> createSegmentedCapsuleButtons(int numButtons, ButtonGroup buttonGroup) {
		return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmentedCapsule");
	}

	public List<T> createSegmentedTexturedButtons(int numButtons, ButtonGroup buttonGroup) {
		return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmentedTextured");
	}

}
