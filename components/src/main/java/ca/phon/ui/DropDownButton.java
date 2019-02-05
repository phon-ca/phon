package ca.phon.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

/**
 * {@link JButton} with a drop-down component.
 */
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
	 * Property key for arrow icon gap.
	 */
	public static String ARROW_ICON_GAP = "arrowIconGap";
	
	/**
	 * Property key for the button popup object.
	 */
	public static String BUTTON_POPUP = "buttonPopup";
	
	private Map<IconState, Icon> iconMap = new HashMap<>();
	private Map<IconState, Icon> arrowIconMap = new HashMap<>();
	
	private ButtonPopup buttonPopup;
	private PropertyChangeListener buttonPopupListener = (e) -> {
		if(!e.getPropertyName().equals(ButtonPopup.POPUP_VISIBLE)) return;
		
		if(!Boolean.parseBoolean(e.getNewValue().toString())) {
			resetIcons();
			if(getModel() instanceof DropDownButtonModel) {
				((DropDownButtonModel)getModel())._release();
			}
		}
		popupVisible = Boolean.parseBoolean(e.getNewValue().toString());
	};
	
	private int arrowIconPosition = DropDownIcon.DEFAULT_ICON_POSITION;
	private int arrowIconGap = DropDownIcon.DEFAULT_GAP;
	private Icon arrowIcon = null;
	
	private boolean popupVisible = false;
	private boolean onlyPopup = false;
	private boolean mouseInArrowArea = false;
	
	
	public DropDownButton(Icon icon, JComponent popup) {
		this(icon, new ButtonPopup(popup));
	}
	
	public DropDownButton(Icon icon, JPopupMenu popupMenu) {
		this(icon, new ButtonPopup(popupMenu));
	}

	public DropDownButton(Icon icon, ButtonPopup buttonPopup) {
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
		ButtonPopup buttonPopup  =null;
		if(popupObj == null)
			throw new IllegalArgumentException("Action must include the " + BUTTON_POPUP + " property");
		if(popupObj instanceof ButtonPopup)
			buttonPopup = (ButtonPopup)popupObj;
		else if(popupObj instanceof JPopupMenu)
			buttonPopup = new ButtonPopup((JPopupMenu) popupObj);
		else if(popupObj instanceof JComponent) 
			buttonPopup = new ButtonPopup((JComponent)popupObj);
		else
			throw new IllegalArgumentException("Invalid popup type " + popupObj.getClass().getName());
		setButtonPopup(buttonPopup);
		
		if(action.getValue(ARROW_ICON) != null)
			this.arrowIcon = (Icon)action.getValue(ARROW_ICON);
		
		if(action.getValue(ARROW_ICON_GAP) != null)
			this.arrowIconGap = (Integer)action.getValue(ARROW_ICON_GAP);
		
		if(action.getValue(ARROW_ICON_POSITION) != null)
			this.arrowIconPosition = (Integer)action.getValue(ARROW_ICON_POSITION);
		
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
                        if( onlyPopup || isInArrowArea( e.getPoint() ) ) {
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
            	if (popupMenuOperation || onlyPopup) {
                    popupMenuOperation = false;
                    e.consume();
                }
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
            	if(!onlyPopup) {
	                mouseInArrowArea = isInArrowArea( e.getPoint() );
	                updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
	                repaint();
            	}
            }

            @Override
            public void mouseExited( MouseEvent e ) {
            	if(!onlyPopup) {
	                mouseInArrowArea = false;
	                updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
            	}
            }
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if(!onlyPopup) {
					mouseInArrowArea = isInArrowArea( e.getPoint() );
	                updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
				}
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
	
	public ButtonPopup getButtonPopup() {
		return this.buttonPopup;
	}
	
	public void setButtonPopup(ButtonPopup popup) {
		if(this.buttonPopup != null)
			this.buttonPopup.removePropertyChangeListener(buttonPopupListener);
		this.buttonPopup = popup;
		this.buttonPopup.addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, buttonPopupListener);
	}
	
	public boolean isPopupVisible() {
		return this.popupVisible;
	}
	
	public boolean isOnlyPopup() {
		return this.onlyPopup;
	}
	
	public void setOnlyPopup(boolean onlyPopup) {
		this.onlyPopup = onlyPopup;
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
			Icon arrowIcon = new DropDownIcon(icon, this.arrowIcon, arrowIconGap, arrowIconPosition, false);
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
            icon = new DropDownIcon(orig, arrowIcon, arrowIconGap, arrowIconPosition, mouseInArrowArea);
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
            icon = new DropDownIcon(orig, arrowIcon, arrowIconGap, arrowIconPosition, mouseInArrowArea);
            arrowIconMap.put( mouseInArrowArea ? IconState.ROLLOVER_ARROW_SELECTED : IconState.ROLLOVER_SELECTED, icon );
        }
        return icon;
    }
	
	private class DropDownButtonModel extends DefaultButtonModel {
		private boolean _pressed = false;
        
        @Override
        public void setPressed(boolean b) {
            if( onlyPopup || mouseInArrowArea || _pressed )
                return;
            super.setPressed( b );
        }
    
        public void _press() {
            if((isPressed()) || !isEnabled()) {
                return;
            }

            super.setPressed(true);
            super.setArmed(true);

            fireStateChanged();
            _pressed = true;
        }
        
        public void _release() {
            _pressed = false;
            mouseInArrowArea = false;
            
            super.setArmed( false );
            super.setPressed( false );
            super.setRollover( false );
            super.setSelected( false );
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
	
}
