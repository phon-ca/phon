/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.util.icons.*;

/**
 * A 'on click' button which includes the following features:
 * 
 *  - support for multiple actions.  Each action is displayed as
 *  a button on mouse-over.  To display actions all the time, use
 *  <code>setAlwaysDisplayActions(true)</code>.
 *  
 *  - suppot for custom components to be displayed as the buttons 'content'.
 *  Display pane is available using method <code>getDisplayPane()</code>
 *  
 *  - since component is based on swingx, supports background painter
 *  See <code>setBackgroundPainter()</code>.
 *  
 */
public class MultiActionButton extends JXPanel implements Scrollable {
	
	private static final long serialVersionUID = 1500039901442454318L;

	/** Display actions at all times? */
	private boolean alwaysDisplayActions = false;
	
	private boolean displayActions = false;
	
	/**
	 * Display default action in list
	 */
	private boolean displayDefaultAction = false;
	
	/** Actions */
	private List<Action> otherActions = new ArrayList<Action>();
	private Map<Action, Shape> actionShapes = new HashMap<Action, Shape>();
	// currently moused-over shape
	private Shape outlineShape = null;
	
	/** Default action - performed on mouse click */
	private Action defaultAction;
	
	/** Top label */
	private JXLabel topLabel;
	
	/** Btm label */
	private JXLabel btmLabel;
	
	private FgActionPainter actionPainter = new FgActionPainter();
	
	/**
	 * Constructor
	 */
	public MultiActionButton() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		topLabel = new JXLabel();
		btmLabel = new JXLabel();
		
		add(topLabel, BorderLayout.NORTH);
		add(btmLabel, BorderLayout.SOUTH);
		
		MultiActionMouseHandler mouseHandler = new MultiActionMouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		
		super.setFocusable(true);
	}

	public void addAction(Action act) {
		otherActions.add(act);
		revalidate();
	}
	
	public JXLabel getTopLabel() {
		return this.topLabel;
	}
	
	public JXLabel getBottomLabel() {
		return this.btmLabel;
	}
	
	public void setTopLabelText(String txt) {
		getTopLabel().setText(txt);
	}
	
	public void setBottomLabelText(String txt) {
		getBottomLabel().setText(txt);
	}
	
	/**
	 * Set the default action.  This also sets tooltip for the
	 * component.
	 * @param act
	 */
	public void setDefaultAction(Action act) {
		this.defaultAction = act;
		
		if(act.getValue(Action.SHORT_DESCRIPTION) != null) {
			setToolTipText((String)act.getValue(Action.SHORT_DESCRIPTION));
		} else {
			setToolTipText("");
		}
	}
	
	public void removeAction(Action act) {
		otherActions.remove(act);
		revalidate();
	}
	
	public void clearActions() {
		otherActions.clear();
		revalidate();
	}
	
	protected JXButton getActionButton(Action act) {
		JXButton retVal = new JXButton();
		
		retVal.setAction(act);
		retVal.setBorderPainted(false);
		retVal.setVisible(displayActions);
		
		return retVal;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		actionPainter.paint((Graphics2D)g, this, getWidth(), getHeight());
	}
	
	@Override
	public Insets getInsets() {
		Insets retVal = super.getInsets();
		
		retVal.top += 5;
		retVal.bottom += 5;
		retVal.left += 5;
		retVal.right += 5;
		
		return retVal;
	}
	
	/**
	 * Display context menu 
	 */
	private void displayContextMenu(Point p) {
		JPopupMenu popupMenu = new JPopupMenu();
		
		if(defaultAction != null) {
			JMenuItem defaultItem = new JMenuItem(defaultAction);
			popupMenu.add(defaultItem);
		}
		
		for(Action act:otherActions) {
			JMenuItem itm = new JMenuItem(act);
			popupMenu.add(itm);
		}
		
		if(popupMenu.getComponentCount() > 0) 
			popupMenu.show(this, p.x, p.y);
	}
	
	public boolean isAlwaysDisplayActions() {
		return this.alwaysDisplayActions;
	}
	
	public void setAlwaysDisplayActions(boolean v) {
		this.alwaysDisplayActions = v;
	}
	
	public boolean isDisplayDefaultAction() {
		return this.displayDefaultAction;
	}
	
	public void setDisplayDefaultAction(boolean displayDefaultAction) {
		this.displayDefaultAction = displayDefaultAction;
	}
	
	/**
	 * General mouse handler
	 */
	private class MultiActionMouseHandler extends MouseInputAdapter {
		@Override
		public void mouseEntered(MouseEvent me) {
			displayActions = true;
			repaint();
		}
		
		@Override
		public void mouseExited(MouseEvent me) {
			displayActions = false;
			repaint();
		}
		
		@Override
		public void mouseClicked(MouseEvent me) {
			
			if(me.getButton() == MouseEvent.BUTTON1) {
				boolean insideActionBtn = false;
				for(Action act:otherActions) {
					Shape actionShape = actionShapes.get(act);
					
					if(actionShape != null) {
						if(actionShape.contains(me.getPoint())) {
							
							ActionEvent ae = new ActionEvent(me.getSource(), me.getID(), 
									(String)act.getValue(Action.ACTION_COMMAND_KEY));
							act.actionPerformed(ae);
							
							insideActionBtn = true;
							break;
						}
					}
				}
				
				if(!insideActionBtn && defaultAction != null) {
					ActionEvent ae = new ActionEvent(me.getSource(), me.getID(), 
							(String)defaultAction.getValue(Action.ACTION_COMMAND_KEY));
					defaultAction.actionPerformed(ae);
					
					
				}
			}
		}
		
		private Cursor prevCursor;
		
		@Override
		public void mouseMoved(MouseEvent me) {
			boolean insideActionBtn = false;
			for(Action act:otherActions) {
				Shape actionShape = actionShapes.get(act);
				
				if(actionShape != null) {
					if(actionShape.contains(me.getPoint())) {
						setToolTipText((String)act.getValue(Action.SHORT_DESCRIPTION));
						insideActionBtn = true;
						outlineShape = actionShape;
						repaint();
						break;
					}
				}
			}
			
			if(!insideActionBtn) {
				if(prevCursor != null) {
					setCursor(prevCursor);
					prevCursor = null;
				}
				if(defaultAction != null && defaultAction.getValue(Action.SHORT_DESCRIPTION) != null) {
					setToolTipText((String)defaultAction.getValue(Action.SHORT_DESCRIPTION));
				} else {
					setToolTipText("");
				}
			} else {
				if(prevCursor == null) {
					prevCursor = getCursor();
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}
			
			if(!insideActionBtn && outlineShape != null) {
				
				outlineShape = null;
				repaint();
			}
		}
		
		@Override
		public void mousePressed(MouseEvent me) {
			MultiActionButton.this.requestFocus();
			if(me.isPopupTrigger()) {
				displayContextMenu(me.getPoint());
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent me) {
			if(me.isPopupTrigger()) {
				displayContextMenu(me.getPoint());
			}
		}
	}
	
	/**
	 * Foreground *action button* painter
	 */
	private class FgActionPainter implements Painter<JXPanel> {
		
		public FgActionPainter() {
		}
		
		@Override
		public void paint(Graphics2D g, JXPanel object, int width, int height) {
			// paint buttons in upper-right hand corner
			// use clipbounds so that actions are always visible
			int rIdx =
					object.getVisibleRect().x + object.getVisibleRect().width;
			int btnSpace = 5;
			if(isDisplayDefaultAction()) {
				ImageIcon icn = (ImageIcon)defaultAction.getValue(Action.LARGE_ICON_KEY);
				if(icn == null) {
					icn = IconManager.getInstance().getIcon("blank", IconSize.LARGE);
				}
				// define bounding rect
				Rectangle2D.Double rect2D = new Rectangle2D.Double();
				rect2D.x = rIdx - icn.getIconWidth() - btnSpace;
				rect2D.y = btnSpace;
				rect2D.width = icn.getIconWidth();
				rect2D.height = icn.getIconHeight();
				
				actionShapes.put(defaultAction, rect2D);
				
				g.drawImage(icn.getImage(), (int)rect2D.x, (int)rect2D.y, object);
				rIdx -= (rect2D.width + btnSpace);
			}
			if(alwaysDisplayActions || displayActions) {

				for(Action act:otherActions) {
					ImageIcon icn = (ImageIcon)act.getValue(Action.LARGE_ICON_KEY);
					if(icn == null) {
						icn = IconManager.getInstance().getIcon("blank", IconSize.LARGE);
					}
					
					// define bounding rect
					Rectangle2D.Double rect2D = new Rectangle2D.Double();
					rect2D.x = rIdx - icn.getIconWidth() - btnSpace;
					rect2D.y = btnSpace;
					rect2D.width = icn.getIconWidth();
					rect2D.height = icn.getIconHeight();
					
					actionShapes.put(act, rect2D);
					
					g.drawImage(icn.getImage(), (int)rect2D.x, (int)rect2D.y, object);
					
					rIdx -= (rect2D.width + btnSpace);
				}
				
				// draw highlight for mouse-over shape
				if(outlineShape != null) {
					GlowPathEffect gpe = new GlowPathEffect();
					gpe.setRenderInsideShape(true);
					gpe.setBrushColor(Color.yellow);
					
					gpe.apply(g, outlineShape, outlineShape.getBounds().width, outlineShape.getBounds().height);
				}
			}
		}
		
	}
}
