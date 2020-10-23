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
package ca.phon.app.session.editor.view.common;

import java.awt.*;

import ca.phon.ui.*;

/**
 * <p>Background painter for components using the {@link TierDataLayout} provider.</p>
 * 
 */
public class TierDataLayoutBgPainter {

	private Color bgColor;
	
	private Color labelBgColor;
	
	private Color primaryStripeColor;
	
	private Color secondaryStripeColor;
	
	public TierDataLayoutBgPainter() {
		super();
	}

	public Color getBgColor() {
		return bgColor;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public Color getLabelBgColor() {
		return labelBgColor;
	}

	public void setLabelBgColor(Color labelBgColor) {
		this.labelBgColor = labelBgColor;
	}

	public Color getPrimaryStripeColor() {
		return primaryStripeColor;
	}

	public void setPrimaryStripeColor(Color primaryStripeColor) {
		this.primaryStripeColor = primaryStripeColor;
	}

	public Color getSecondaryStripeColor() {
		return secondaryStripeColor;
	}

	public void setSecondaryStripeColor(Color secondaryStripeColor) {
		this.secondaryStripeColor = secondaryStripeColor;
	}
	
	/**
	 * Paint the component with the given {@link TierDataLayout} and
	 * {@link Graphics2D}
	 * 
	 * @param c
	 * @param g2
	 * @param layout
	 */
	public void paintComponent(Component c, Graphics2D g2, TierDataLayout layout) {
		final Color origColor = g2.getColor();
		
		Color bgColor = getBgColor();
		if(bgColor == null) bgColor = c.getBackground();
		
		// fill background
		final Dimension compSize = c.getSize();
		final Rectangle compRect = new Rectangle(0, 0, compSize.width, compSize.height);
		g2.setColor(bgColor);
		g2.fill(compRect);
		
		// label area
		Color labelBgColor = getLabelBgColor();
		if(labelBgColor == null) labelBgColor = PhonGuiConstants.PHON_UI_STRIP_COLOR;
		final Rectangle labelBgRect = new Rectangle(0, 0, layout.getTierLabelWidth() + layout.getHorizontalGap() / 2, compSize.height);
		g2.setColor(labelBgColor);
		g2.fill(labelBgRect);
		
		// tier striping
		Color primaryStripeColor = getPrimaryStripeColor();
		if(primaryStripeColor == null) primaryStripeColor = bgColor;
		Color secondaryStripeColor = getSecondaryStripeColor();
		if(secondaryStripeColor == null) secondaryStripeColor = PhonGuiConstants.PHON_UI_STRIP_COLOR;
		
		for(int i = 0; i < layout.getRowCount(); i++) {
			final Rectangle rowRect = layout.getLayoutProvider().rowRect((Container)c, layout, i);
			rowRect.x += layout.getHorizontalGap() / 2;
			rowRect.width -= layout.getHorizontalGap() - 1;
			rowRect.y -= layout.getVerticalGap() / 2;
			rowRect.height += layout.getVerticalGap();
			final Color stripeColor = (i % 2 == 0 ? primaryStripeColor : secondaryStripeColor);
			g2.setColor(stripeColor);
			g2.fill(rowRect);
		}
		
		g2.setColor(origColor);
	}
	
}
