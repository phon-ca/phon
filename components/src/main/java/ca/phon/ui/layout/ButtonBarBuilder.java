/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ui.layout;

import java.lang.ref.*;
import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.util.*;

/**
 * Utility class for building dialog button bars.  The layout
 * defines three sections: right, left, and center.  Buttons
 * are added to one of these three regions and displayed in
 * the order added.
 * 
 *
 */
public class ButtonBarBuilder {

	// list of right-aligned components in order added
	private final List<WeakReference<JComponent>> rightAlignedComponents 
		= Collections.synchronizedList(new ArrayList<WeakReference<JComponent>>());
	
	private final List<WeakReference<JComponent>> leftAlignedComponents 
		= Collections.synchronizedList(new ArrayList<WeakReference<JComponent>>());

	private final List<WeakReference<JComponent>> centerAlignedComponents 
		= Collections.synchronizedList(new ArrayList<WeakReference<JComponent>>());
	
	private WeakReference<JComponent> leftFillComponent;
	
	private WeakReference<JComponent> rightFillComponent;
	
	public ButtonBarBuilder addComponentRight(JComponent btn) {
		final WeakReference<JComponent> compRef = new WeakReference<JComponent>(btn);
		rightAlignedComponents.add(compRef);
		return this;
	}
	
	public ButtonBarBuilder addComponentLeft(JComponent btn) {
		final WeakReference<JComponent> compRef = new WeakReference<JComponent>(btn);
		leftAlignedComponents.add(compRef);
		return this;
	}
	
	public ButtonBarBuilder addComponentCenter(JComponent btn) {
		final WeakReference<JComponent> compRef = new WeakReference<JComponent>(btn);
		centerAlignedComponents.add(compRef);
		return this;
	}
	
	public ButtonBarBuilder setLeftFillComponent(JComponent comp) {
		final WeakReference<JComponent> compRef = new WeakReference<JComponent>(comp);
		leftFillComponent = compRef;
		return this;
	}
	
	public ButtonBarBuilder setRightFillComponent(JComponent comp) {
		final WeakReference<JComponent> compRef = new WeakReference<JComponent>(comp);
		rightFillComponent = compRef;
		return this;
	}
	
	private String createColumnSchema() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append("left:pref");
		for(int i = 1; i < leftAlignedComponents.size(); i++) {
			sb.append(",left:pref");
		}
		sb.append(",fill:pref:grow");
		
		sb.append(",center:pref");
		for(int i = 1; i < centerAlignedComponents.size(); i++) {
			sb.append(",center:pref");
		}
		sb.append(",fill:pref:grow");
		
		sb.append(",right:pref");
		for(int i = 1; i < rightAlignedComponents.size(); i++) {
			sb.append(",right:pref");
		}
		
		return sb.toString();
	}
	
	public JComponent build() {
		final CellConstraints cc = new CellConstraints();
		final FormLayout layout = new FormLayout(createColumnSchema(), "pref");
		
		final JPanel retVal = new JPanel(layout);
		
		int colIdx = 1;
		
		for(int i = 0; i < leftAlignedComponents.size(); i++) {
			final WeakReference<JComponent> buttonRef = leftAlignedComponents.get(i);
			final JComponent button = buttonRef.get();
			
			retVal.add(button, cc.xy(colIdx, 1));
			++colIdx;
		}
		if(colIdx == 1) ++colIdx;
		if(leftFillComponent != null) {
			retVal.add(leftFillComponent.get(), cc.xy(colIdx, 1));
		}
		++colIdx;
		
		int oldIdx = colIdx;
		for(int i = 0; i < centerAlignedComponents.size(); i++) {
			final WeakReference<JComponent> buttonRef = centerAlignedComponents.get(i);
			final JComponent button = buttonRef.get();
			
			retVal.add(button, cc.xy(colIdx, 1));
			++colIdx;
		}
		if(colIdx == oldIdx) ++colIdx;
		if(rightFillComponent != null) {
			retVal.add(rightFillComponent.get(), cc.xy(colIdx, 1));
		}
		++colIdx;
		
		for(int i = 0; i < rightAlignedComponents.size(); i++) {
			final WeakReference<JComponent> buttonRef = rightAlignedComponents.get(i);
			final JComponent button = buttonRef.get();
			
			retVal.add(button, cc.xy(colIdx, 1));
			++colIdx;
		}
		
		return retVal;
	}
	
	/* Helper methods */
	
	public static JComponent buildOkBar(JComponent okButton) {
		final ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addComponentRight(okButton);
		return builder.build();
	}
	
	public static JComponent buildOkCancelBar(JComponent okButton, JComponent cancelButton) {
		return buildOkCancelBar(okButton, cancelButton, new JComponent[0]);
	}
	
	public static JComponent buildOkCancelBar(JComponent okButton, JComponent cancelButton, JComponent ... otherBtns) {
		final ButtonBarBuilder builder = new ButtonBarBuilder();
		
		if(OSInfo.isMacOs()) {
			builder.addComponentRight(cancelButton);
			builder.addComponentRight(okButton);
		} else {
			builder.addComponentRight(okButton);
			builder.addComponentRight(cancelButton);
		}
		
		for(JComponent btn:otherBtns) {
			builder.addComponentLeft(btn);
		}
		
		return builder.build();
	}
	
}
