/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.pushingpixels.substance.api.SubstanceConstants.Side;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * Utility methods for creating segmented buttons.
 * 
 * Methods copied from Apple Technical Note TN2196
 */
public class SegmentedButtonBuilder {
	
	// Create a Layout component that will ensure the buttons abut each other
	public static JComponent createLayoutComponent(List<JButton> segmentButtons) {
	  Box layoutBox = Box.createHorizontalBox();
	  for(JButton button : segmentButtons) {
	    layoutBox.add(button);
	  }
	  return layoutBox;
	}

	public static JButton createSegmentButton(String style, String position, ButtonGroup buttonGrp) {
	  JButton button = new JButton();
	  
	  // client properties for Mac OS X
	  button.putClientProperty("JButton.buttonType", style);
	  button.putClientProperty("JButton.segmentPosition", position);
	  
	  // client properties for Substance L&F
	  Set<Side> openSides = new LinkedHashSet<Side>();
	  if(position.equals("first")) {
		  openSides.add(Side.RIGHT);
	  } else if(position.equals("middle")) {
		  openSides.add(Side.LEFT);
		  openSides.add(Side.RIGHT);
	  } else if(position.equals("last")) {
		  openSides.add(Side.LEFT);
	  }
	  
	  button.putClientProperty(SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
			  openSides);
	  
	  buttonGrp.add(button);
	  return button;
	}

	// Bottleneck for creating the buttons for the button group
	public static List<JButton> createSegmentButtonsWithStyle(int numButtons, ButtonGroup buttonGrp, String style){
	  // Allocate a list of JButtons
	  List<JButton> buttons = new ArrayList<JButton>();
	  if(numButtons == 1) {
	    // If 1 button is requested, then it gets the "only" segment position
	    buttons.add(createSegmentButton(style, "only", buttonGrp));
	  } else {
	    // If more than 1 button is requested, then
	    // the first one gets "first" the last one gets "last" and the rest get "middle"
	    buttons.add(createSegmentButton(style, "first", buttonGrp));
	    for(int i = 1; i < numButtons - 1; i++) {
	      buttons.add(createSegmentButton(style, "middle", buttonGrp));
	    }
	    buttons.add(createSegmentButton(style, "last", buttonGrp));
	  }
	  return buttons;
	}

	// Convenience methods that pass in the correct button style for each segmented button style
	public static List<JButton> createSegmentedButtons(int numButtons, ButtonGroup buttonGroup) {
	  return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmented");
	}

	public static List<JButton> createSegmentedRoundRectButtons(int numButtons, ButtonGroup buttonGroup) {
	  return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmentedRoundRect");
	}

	public static List<JButton> createSegmentedCapsuleButtons(int numButtons, ButtonGroup buttonGroup) {
	  return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmentedCapsule");
	}

	public static List<JButton> createSegmentedTexturedButtons(int numButtons, ButtonGroup buttonGroup) {
	  return createSegmentButtonsWithStyle(numButtons, buttonGroup, "segmentedTextured");
	}

}
