/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.RectanglePainter;

import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * A panel to display some information to the user.  The panel
 * includes a close icon and save the preference for the 
 * visibility of the panel.
 */
public class HidablePanel extends MultiActionButton {
	
	private static final long serialVersionUID = 360940577329250637L;

	private final static Logger LOGGER = Logger.getLogger(HidablePanel.class.getName());
	
	/**
	 * Location of hidable panel properties
	 */
	private final static String PANEL_PROPS = "hidden_panels";
	
	private static Preferences getPanelPrefs() {
		final Preferences userPrefs = PrefHelper.getUserPreferences();
		return userPrefs.node(PANEL_PROPS);
	}
	
	public static boolean clearSavePanelProps() {
		try {
			getPanelPrefs().clear();
			return true;
		} catch (BackingStoreException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
	
	private void init() {
		setOpaque(false);
		
//		label = new JLabel("Hide");
//		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//		label.setFont(label.getFont().deriveFont(10.0f));
//		
//		label.addMouseListener(new MouseInputAdapter() {
//
//			@Override
//			public void mousePressed(MouseEvent arg0) {
//				hideComponent();
//			}
//			
//		});
		
//		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		topPanel.setOpaque(false);
//		topPanel.add(label);
//		
//		setLayout(new BorderLayout());
//		
//		add(topPanel, BorderLayout.NORTH);
		PhonUIAction onHideAct = new PhonUIAction(this, "onHide");
		ImageIcon hideIcn = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.XSMALL);
		onHideAct.putValue(Action.SHORT_DESCRIPTION, "Hide and don't show again");
		onHideAct.putValue(Action.NAME, "Hide message");
		onHideAct.putValue(Action.SMALL_ICON, hideIcn);
		onHideAct.putValue(Action.LARGE_ICON_KEY, hideIcn);
		super.addAction(onHideAct);

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
