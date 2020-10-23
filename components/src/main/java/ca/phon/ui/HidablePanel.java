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

import java.awt.*;
import java.util.prefs.*;

import javax.swing.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.*;

import ca.phon.ui.action.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * A panel to display some information to the user.  The panel
 * includes a close icon and save the preference for the 
 * visibility of the panel.
 */
public class HidablePanel extends MultiActionButton {
	
	private static final long serialVersionUID = 360940577329250637L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(HidablePanel.class.getName());
	
	/**
	 * Location of hidable panel properties
	 */
	private final static String PANEL_PROPS = "hidden_panels";
	
	private Action hideAct;
	
	private static Preferences getPanelPrefs() {
		final Preferences userPrefs = PrefHelper.getUserPreferences();
		return userPrefs.node(PANEL_PROPS);
	}
	
	public static boolean clearSavePanelProps() {
		try {
			getPanelPrefs().clear();
			return true;
		} catch (BackingStoreException e) {
			LOGGER.error( e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * Property name for this panel
	 */
	private String panelProp;
	
	/**
	 * The clickable label to hide panel
	 */
	private JLabel label;
	
	/**
	 * Constructor
	 * 
	 * @param panelProp the property name for the panel
	 */
	public HidablePanel(String panelProp) {
		this.panelProp = panelProp;
		
		init();
	}
	
	@Override
	public void clearActions() {
		super.clearActions();
		addAction(hideAct);
	}
	
	private void init() {
		setOpaque(false);
		
		hideAct = new PhonUIAction(this, "onHide");
		ImageIcon hideIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.XSMALL);
		hideAct.putValue(Action.SHORT_DESCRIPTION, "Hide and don't show again");
		hideAct.putValue(Action.NAME, "Hide message");
		hideAct.putValue(Action.SMALL_ICON, hideIcn);
		hideAct.putValue(Action.LARGE_ICON_KEY, hideIcn);
		super.addAction(hideAct);

		MattePainter matte = new MattePainter(UIManager.getColor("control"));
		RectanglePainter rectPainter = new RectanglePainter(1, 1, 1, 1);
		rectPainter.setFillPaint(PhonGuiConstants.PHON_SHADED);
		CompoundPainter<JXLabel> cmpPainter = new CompoundPainter<JXLabel>(matte, rectPainter);
		super.setBackgroundPainter(cmpPainter);
	}
	
//	/**
//	 * Set the content for the panel
//	 * 
//	 * @param comp
//	 */
//	public void setContent(JComponent comp) {
//		add(comp, BorderLayout.CENTER);
//	}

	/**
	 * Has this panel been turned off by the user?
	 * 
	 * @return true if the panel should be visible, false otherwise
	 */
	public boolean isUserHidden() {
		return getPanelPrefs().getBoolean(panelProp, false);
	}
	
	@Override
	public boolean isVisible() {
		return super.isVisible() && !isUserHidden();
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = new Dimension(0, 0);
		
		if(isVisible())
			retVal = super.getPreferredSize();
		
		return retVal;
	}
	
	@Override
	public Insets getInsets() {
		Insets insets = new Insets(5, 5, 10, 10);
		return insets;
	}

//	@Override
//	protected void paintComponent(Graphics g) {
//		// paint bg
//		Dimension size = getSize();
//		// fill background
//		g.setColor(PhonGuiConstants.PHON_SHADED);
//		g.fillRoundRect(0, 0, size.width, size.height, 10, 10);
//		
//		g.setColor(PhonGuiConstants.PHON_UI_STRIP_COLOR);
//		g.drawRoundRect(0, 0, size.width, size.height, 10, 10);
//		
//		super.paintComponent(g);
//	}
	
	/**
	 * 
	 */
	public void onHide(PhonActionEvent pae) {
		final Preferences prefs = getPanelPrefs();
		prefs.putBoolean(panelProp, true);
		
		super.getParent().invalidate();
		super.getParent().validate();
	}
	
}
