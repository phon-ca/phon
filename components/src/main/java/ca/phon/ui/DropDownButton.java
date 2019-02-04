package ca.phon.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.phon.util.icons.IconManager;

public class DropDownButton extends JButton {
	
	private static enum IconState {
		NORMAL,
		PRESSED,
		SELECTED,
		DISABLED,
		DISABLED_SELECTED,
		ROLLOVER,
		ROLLOVER_SELECTED,
		ROLLOVER_ARROW,
		ROLLOVER_ARROW_SELECTED
	};
	
	/**
	 * Property key for arrow icon.  If <code>null</code> the
	 * default arrow icon (small triangular down arrow) will be
	 * used.
	 */
	public static String ARROW_ICON = "arrowIcon";

	/**
	 * Property key for position of arrow icon.
	 * Value show be one of SwingConstants.TOP/CENTER/BOTTOM
	 * This property is not required.
	 */
	public static String ARROW_ICON_POSITION = "arrowIconPosition";
	
	/**
	 * Property key for the button popup object.
	 */
	public static String BUTTON_POPUP = "buttonPopup";
	
	private Map<IconState, Icon> iconMap = new HashMap<>();
	private Map<IconState, Icon> arrowIconMap = new HashMap<>();
	
	private DropDownButtonPopup buttonPopup;
	private PropertyChangeListener buttonPopupListener = (e) -> {
		if(!e.getPropertyName().equals(DropDownButtonPopup.POPUP_VISIBLE)) return;
		
		if(!Boolean.parseBoolean(e.getNewValue().toString())) {
			resetIcons();
			if(getModel() instanceof DropDownButtonModel) {
				((DropDownButtonModel)getModel())._release();
			}
		}
	};
	
	private boolean mouseInButton = false;
	private boolean mouseInArrowArea = false;
	
	public DropDownButton(Icon icon, JComponent popup) {
		this(icon, new DropDownButtonPopup(popup));
	}
	
	public DropDownButton(Icon icon, JPopupMenu popupMenu) {
		this(icon, new DropDownButtonPopup(popupMenu));
	}

	public DropDownButton(Icon icon, DropDownButtonPopup buttonPopup) {
		super();
		
		setButtonPopup(buttonPopup);
		
		setIcon(icon);
		resetIcons();
		
		installListeners();
		setModel(new DropDownButtonModel());
	}

	public DropDownButton(Action action) {
		super();
		
		// ensure proper configuration of provided action
		if(action.getValue(Action.SMALL_ICON) == null)
			throw new IllegalArgumentException("Action must include the " + Action.SMALL_ICON + " property");
//		if(action.getValue(Action.NAME) != null)
//			throw new IllegalArgumentException("Action must not include the " + Action.NAME + " property");

		Object popupObj = action.getValue(BUTTON_POPUP);
		DropDownButtonPopup buttonPopup  =null;
		if(popupObj == null)
			throw new IllegalArgumentException("Action must include the " + BUTTON_POPUP + " property");
		if(popupObj instanceof DropDownButtonPopup)
			buttonPopup = (DropDownButtonPopup)popupObj;
		else if(popupObj instanceof JPopupMenu)
			buttonPopup = new DropDownButtonPopup((JPopupMenu) popupObj);
		else if(popupObj instanceof JComponent) 
			buttonPopup = new DropDownButtonPopup((JComponent)popupObj);
		else
			throw new IllegalArgumentException("Invalid popup type " + popupObj.getClass().getName());
		setButtonPopup(buttonPopup);
		
		setAction(action);
		resetIcons();
		
		installListeners();
		setModel(new DropDownButtonModel());
	}
	
	private void installListeners() {
		addMouseListener(new MouseAdapter() {
			private boolean popupMenuOperation = false;
	            
            @Override
            public void mousePressed( MouseEvent e ) {
                popupMenuOperation = false;
                if ( buttonPopup != null && getModel() instanceof DropDownButtonModel ) {
                    DropDownButtonModel model = (DropDownButtonModel) getModel();
                    if ( !model._isPressed() ) {
                        if( isInArrowArea( e.getPoint() ) ) {
                            model._press();
                            buttonPopup.show(DropDownButton.this);
                            popupMenuOperation = true;
                        }
                    } else {
                        model._release();
                        popupMenuOperation = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (popupMenuOperation) {
                    popupMenuOperation = false;
                    e.consume();
                }
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                mouseInButton = true;
                mouseInArrowArea = isInArrowArea( e.getPoint() );
                updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                repaint();
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                mouseInButton = false;
                mouseInArrowArea = false;
                updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
            }
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseInArrowArea = isInArrowArea( e.getPoint() );
                updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				
			}
			
		});
	}
	
	public boolean isInArrowArea(Point p) {
		Icon icn = getIcon();
		Rectangle iconRect = new Rectangle(0, 0, icn.getIconWidth() + getInsets().left + getInsets().right, getHeight());
		if(icn instanceof DropDownIcon && iconRect.contains(p)) {
			DropDownIcon icon = (DropDownIcon)icn;
			
			Rectangle arrowRect = icon.getArrowRect();
			arrowRect.translate(getInsets().left, 0);
			arrowRect.width += getInsets().right;
			
			return arrowRect.contains(p);
		} else {
			return false;
		}
	}
	
	public DropDownButtonPopup getButtonPopup() {
		return this.buttonPopup;
	}
	
	public void setButtonPopup(DropDownButtonPopup popup) {
		if(this.buttonPopup != null)
			this.buttonPopup.removePropertyChangeListener(buttonPopupListener);
		this.buttonPopup = popup;
		this.buttonPopup.addPropertyChangeListener(DropDownButton.DropDownButtonPopup.POPUP_VISIBLE, buttonPopupListener);
	}
	
	@Override
	public void setIcon(Icon icon) {
		Icon arrowIcn = updateIconForState(icon, IconState.NORMAL);
		// clear rollover icons
		arrowIconMap.remove(IconState.ROLLOVER);
		arrowIconMap.remove(IconState.ROLLOVER_SELECTED);
		arrowIconMap.remove(IconState.ROLLOVER_ARROW);
		arrowIconMap.remove(IconState.ROLLOVER_ARROW_SELECTED);		
        super.setIcon(arrowIcn);
        updateRollover(_getRolloverIcon(), _getRolloverSelectedIcon());
	}
	
	private Icon updateIconForState(Icon icon, IconState state) {
		if(icon == null) {
			iconMap.remove(state);
			arrowIconMap.remove(state);
			return null;
		} else {
			iconMap.put(state, icon);
			Icon arrowIcon = new DropDownIcon(icon, 6, SwingConstants.CENTER, false);
			arrowIconMap.put(state, arrowIcon);
			return arrowIcon;
		}
	}
	
	private void updateRollover(Icon rollover, Icon rolloverSelected) {
        super.setRolloverIcon(rollover);
        super.setRolloverSelectedIcon(rolloverSelected);
	}
	
	private void resetIcons() {
        var icon = iconMap.get(IconState.NORMAL);
        if(icon != null)
            setIcon( icon );
        
        icon = iconMap.get(IconState.PRESSED);
        if(icon != null)
            setPressedIcon(icon);

        icon = iconMap.get(IconState.SELECTED);
        if(icon != null)
        	setSelectedIcon(icon);
        
        icon = iconMap.get(IconState.DISABLED);
        if(icon != null)
        	setDisabledIcon(icon);
        
        icon = iconMap.get(IconState.DISABLED_SELECTED);
        if(icon != null)
        	setDisabledSelectedIcon(icon);

        icon = iconMap.get(IconState.ROLLOVER);
        if(icon != null)
            setRolloverIcon(icon);
        
        icon = iconMap.get(IconState.ROLLOVER_SELECTED);
        if(icon != null)
            setRolloverSelectedIcon(icon);
	}
	
	@Override
	public void setPressedIcon(Icon icon) {
	    Icon arrowIcon = updateIconForState(icon, IconState.PRESSED);
	    super.setPressedIcon(arrowIcon);
	}
	
	@Override
	public void setSelectedIcon(Icon icon) {
		Icon arrowIcon = updateIconForState(icon, IconState.SELECTED);
	    super.setSelectedIcon(arrowIcon);
	}
	
	@Override
	public void setRolloverIcon(Icon icon) {
		Icon arrowIcon = updateIconForState(icon, IconState.ROLLOVER);
		arrowIconMap.remove(IconState.ROLLOVER_SELECTED);
		arrowIconMap.remove(IconState.ROLLOVER_ARROW_SELECTED);
	    super.setRolloverIcon(arrowIcon);
	}
	
	@Override
	public void setRolloverSelectedIcon(Icon icon) {
		Icon arrowIcon = updateIconForState(icon, IconState.ROLLOVER_SELECTED);
	    arrowIconMap.remove(IconState.ROLLOVER_ARROW_SELECTED);
	    super.setRolloverSelectedIcon(arrowIcon);
	}
	
	@Override
	public void setDisabledIcon(Icon icon) {
	    // TODO use 'disabled' arrow icon
		Icon arrowIcon = updateIconForState(icon, IconState.DISABLED);
	    super.setDisabledIcon(arrowIcon);
	}
	
	@Override
	public void setDisabledSelectedIcon(Icon icon) {
	    // TODO use 'disabled' arrow icon
		Icon arrowIcon = updateIconForState(icon, IconState.DISABLED_SELECTED);
	    super.setDisabledSelectedIcon(arrowIcon);
	}
	
	private Icon _getRolloverIcon() {
        Icon icon = null;
        icon = arrowIconMap.get( mouseInArrowArea ? IconState.ROLLOVER_ARROW : IconState.ROLLOVER );
        if(icon == null) {
            Icon orig = iconMap.get(IconState.ROLLOVER);
            if(orig == null)
                orig = iconMap.get(IconState.NORMAL);
            icon = new DropDownIcon(orig, 6, SwingConstants.CENTER, mouseInArrowArea);
            arrowIconMap.put( mouseInArrowArea ? IconState.ROLLOVER_ARROW : IconState.ROLLOVER, icon );
        }
        return icon;
    }
    
    private Icon _getRolloverSelectedIcon() {
        Icon icon = null;
        icon = arrowIconMap.get( mouseInArrowArea ? IconState.ROLLOVER_ARROW_SELECTED : IconState.ROLLOVER_SELECTED );
        if(icon == null) {
            Icon orig = iconMap.get(IconState.ROLLOVER_SELECTED);
            if(orig == null)
                orig = iconMap.get(IconState.ROLLOVER);
            if(orig == null)
                orig = iconMap.get(IconState.NORMAL);
            icon = new DropDownIcon(orig, 6, SwingConstants.CENTER, mouseInArrowArea);
            arrowIconMap.put( mouseInArrowArea ? IconState.ROLLOVER_ARROW_SELECTED : IconState.ROLLOVER_SELECTED, icon );
        }
        return icon;
    }
	
	private class DropDownButtonModel extends DefaultButtonModel {
		private boolean _pressed = false;
        
        @Override
        public void setPressed(boolean b) {
            if( mouseInArrowArea || _pressed )
                return;
            super.setPressed( b );
        }
    
        public void _press() {
            if((isPressed()) || !isEnabled()) {
                return;
            }

            stateMask |= PRESSED + ARMED;

            fireStateChanged();
            _pressed = true;
        }
        
        public void _release() {
            _pressed = false;
            mouseInArrowArea = false;
            setArmed( false );
            setPressed( false );
            setRollover( false );
            setSelected( false );
        }

        public boolean _isPressed() {
            return _pressed;
        }
        
        @Override
        protected void fireStateChanged() {
            if( _pressed )
                return;
            super.fireStateChanged();
        }

        @Override
        public void setArmed(boolean b) {
            if( _pressed )
                return;
            super.setArmed(b);
        }

        @Override
        public void setEnabled(boolean b) {
            if( _pressed )
                return;
            super.setEnabled(b);
        }

        @Override
        public void setSelected(boolean b) {
            if( _pressed )
                return;
            super.setSelected(b);
        }

        @Override
        public void setRollover(boolean b) {
            if( _pressed )
                return;
            super.setRollover(b);
        }
	}
	
	public static class DropDownButtonPopup {
		
		/**
		 * Property key for popup visibility
		 */
		public final static String POPUP_VISIBLE = "popupVisible";
		
		private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
		
		private WeakReference<Object> popupRef;
		
		public DropDownButtonPopup(JComponent c) {
			popupRef = new WeakReference<Object>(c);
		}
		
		public DropDownButtonPopup(JPopupMenu popupMenu) {
			popupRef = new WeakReference<Object>(popupMenu);
		}
		
		public Object getPopupObj() {
			return popupRef.get();
		}
		
		public void show(JComponent c) {
			Object popupObj = getPopupObj();
			if(popupObj == null) return;

			propSupport.firePropertyChange(POPUP_VISIBLE, false, true);
			if(popupObj instanceof JPopupMenu) {
				JPopupMenu menu = (JPopupMenu)popupObj;
				menu.show(c, 0, c.getHeight());
				
				menu.addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
						propSupport.firePropertyChange(POPUP_VISIBLE, true, false);
						menu.removePopupMenuListener(this);
					}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
						
					}
					
				});
			} else {
				JComponent comp = (JComponent)popupObj;
				
				Popup popup = PopupFactory.getSharedInstance().getPopup(c, comp, 
						c.getLocationOnScreen().x, c.getLocationOnScreen().y + c.getHeight());
				
				comp.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						propSupport.firePropertyChange(BUTTON_POPUP, true, false);
						comp.removeFocusListener(this);
						popup.hide();
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						
					}
					
				});
				
				popup.show();
			}
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propSupport.removePropertyChangeListener(listener);
		}

		public PropertyChangeListener[] getPropertyChangeListeners() {
			return propSupport.getPropertyChangeListeners();
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propSupport.removePropertyChangeListener(propertyName, listener);
		}

		public boolean hasListeners(String propertyName) {
			return propSupport.hasListeners(propertyName);
		}
		
	}
	
}
