package ca.phon.ui;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;

/**
 * Flat button component with named icons from FontAwesome or Google Material Icons.
 * See {@link ca.phon.util.icons.IconManager#buildFontIcon(String, String, IconSize, Color)} for more information.
 */
public class FlatButton extends JButton {

    public static final String ICON_SIZE_PROP = FlatButton.class.getName() + ".iconSize";
    private IconSize iconSize;

    public static final String ICON_NAME_PROP = FlatButton.class.getName() + ".iconName";
    private String iconName;

    public static final String ICON_FONT_NAME_PROP = FlatButton.class.getName() + ".iconFontName";
    private String iconFontName;

    private Color iconColor;

    private Color iconHoverColor;

    private Color iconPressedColor;

    private Color iconDisabledColor;

    private Color iconSelectedColor;

    private Color bgColor;

    private Color bgHoverColor;

    private Color bgPressedColor;

    private Color bgDisabledColor;

    private Color bgSelectedColor;

    private int borderRadius = 10;

    private int borderWidth = 1;

    private Color borderColor;

    private Color borderHoverColor;

    private Color borderPressedColor;

    private Color borderDisabledColor;

    private Color borderSelectedColor;

    private int padding = 5;

    private int iconTextGap = 5;

    /**
     * Custom popup text which will be shown when the mouse hovers over the button
     */
    private String popupText = null;

    /**
     * Location of popup text one of
     * <ul>
     *     <li>{@link SwingConstants#NORTH}</li>
     *     <li>{@link SwingConstants#SOUTH}</li>
     *     <li>{@link SwingConstants#EAST}</li>
     *     <li>{@link SwingConstants#WEST}</li>
     * </ul>
     */
    private int popupLocation = SwingConstants.SOUTH;

    public FlatButton() {
        super();
        init();
    }

    public FlatButton(String iconFontName, String iconName) {
        this(iconFontName, iconName, IconSize.SMALL);
    }

    public FlatButton(String iconFontName, String iconName, IconSize size) {
        super();
        this.iconFontName = iconFontName;
        this.iconName = iconName;
        this.iconSize = size;
        init();
    }

    public FlatButton(Action a) {
        super(a);
        iconFontName = (String)a.getValue(ICON_FONT_NAME_PROP);
        if(iconFontName == null) {
            iconFontName = IconManager.FontAwesomeFontName;
        }
        iconName = (String)a.getValue(ICON_NAME_PROP);
        if(iconName == null) {
            iconName = "question-circle";
        }
        iconSize = (IconSize)a.getValue(ICON_SIZE_PROP);
        if(iconSize == null) {
            iconSize = IconSize.SMALL;
        }
        init();
    }

    private void init() {
        setUI(new FlatButtonUI());

        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
    }

    public IconSize getIconSize() {
        return iconSize;
    }

    public void setIconSize(IconSize iconSize) {
        var oldVal = this.iconSize;
        this.iconSize = iconSize;
        firePropertyChange("iconSize", oldVal, iconSize);
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        var oldVal = this.iconName;
        this.iconName = iconName;
        firePropertyChange("iconName", oldVal, iconName);
    }

    public String getIconFontName() {
        return iconFontName;
    }

    public void setIconFontName(String iconFontName) {
        var oldVal = this.iconFontName;
        this.iconFontName = iconFontName;
        firePropertyChange("iconFontName", oldVal, iconFontName);
    }

    public Color getIconColor() {
        return iconColor != null ? iconColor : UIManager.getColor(FlatButtonUIProps.ICON_COLOR_PROP);
    }

    public void setIconColor(Color iconColor) {
        var oldVal = this.iconColor;
        this.iconColor = iconColor;
        firePropertyChange("iconColor", oldVal, iconColor);
    }

    public Color getIconHoverColor() {
        return iconHoverColor != null ? iconHoverColor : UIManager.getColor(FlatButtonUIProps.ICON_HOVER_COLOR_PROP);
    }

    public void setIconHoverColor(Color iconHoverColor) {
        var oldVal = this.iconHoverColor;
        this.iconHoverColor = iconHoverColor;
        firePropertyChange("iconHoverColor", oldVal, iconHoverColor);
    }

    public Color getIconPressedColor() {
        return iconPressedColor != null ? iconPressedColor : UIManager.getColor(FlatButtonUIProps.ICON_PRESSED_COLOR_PROP);
    }

    public void setIconPressedColor(Color iconPressedColor) {
        var oldVal = this.iconPressedColor;
        this.iconPressedColor = iconPressedColor;
        firePropertyChange("iconPressedColor", oldVal, iconPressedColor);
    }

    public Color getIconDisabledColor() {
        return iconDisabledColor != null ? iconDisabledColor : UIManager.getColor(FlatButtonUIProps.ICON_DISABLED_COLOR_PROP);
    }

    public void setIconDisabledColor(Color iconDisabledColor) {
        var oldVal = this.iconDisabledColor;
        this.iconDisabledColor = iconDisabledColor;
        firePropertyChange("iconDisabledColor", oldVal, iconDisabledColor);
    }

    public Color getIconSelectedColor() {
        return iconSelectedColor != null ? iconSelectedColor : UIManager.getColor(FlatButtonUIProps.ICON_SELECTED_COLOR_PROP);
    }

    public void setIconSelectedColor(Color iconSelectedColor) {
        var oldVal = this.iconSelectedColor;
        this.iconSelectedColor = iconSelectedColor;
        firePropertyChange("iconSelectedColor", oldVal, iconSelectedColor);
    }

    public Color getBgColor() {
        return bgColor != null ? bgColor : UIManager.getColor(FlatButtonUIProps.BG_COLOR_PROP);
    }

    public void setBgColor(Color bgColor) {
        var oldVal = this.bgColor;
        this.bgColor = bgColor;
        firePropertyChange("bgColor", oldVal, bgColor);
    }

    public Color getBgHoverColor() {
        return bgHoverColor != null ? bgHoverColor : UIManager.getColor(FlatButtonUIProps.BG_HOVER_COLOR_PROP);
    }

    public void setBgHoverColor(Color bgHoverColor) {
        var oldVal = this.bgHoverColor;
        this.bgHoverColor = bgHoverColor;
        firePropertyChange("bgHoverColor", oldVal, bgHoverColor);
    }

    public Color getBgPressedColor() {
        return bgPressedColor != null ? bgPressedColor : UIManager.getColor(FlatButtonUIProps.BG_PRESSED_COLOR_PROP);
    }

    public void setBgPressedColor(Color bgPressedColor) {
        var oldVal = this.bgPressedColor;
        this.bgPressedColor = bgPressedColor;
        firePropertyChange("bgPressedColor", oldVal, bgPressedColor);
    }

    public Color getBgDisabledColor() {
        return bgDisabledColor != null ? bgDisabledColor : UIManager.getColor(FlatButtonUIProps.BG_DISABLED_COLOR_PROP);
    }

    public void setBgDisabledColor(Color bgDisabledColor) {
        var oldVal = this.bgDisabledColor;
        this.bgDisabledColor = bgDisabledColor;
        firePropertyChange("bgDisabledColor", oldVal, bgDisabledColor);
    }

    public Color getBgSelectedColor() {
        return bgSelectedColor != null ? bgSelectedColor : UIManager.getColor(FlatButtonUIProps.BG_SELECTED_COLOR_PROP);
    }

    public void setBgSelectedColor(Color bgSelectedColor) {
        var oldVal = this.bgSelectedColor;
        this.bgSelectedColor = bgSelectedColor;
        firePropertyChange("bgSelectedColor", oldVal, bgSelectedColor);
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(int borderRadius) {
        var oldVal = this.borderRadius;
        this.borderRadius = borderRadius;
        firePropertyChange("borderRadius", oldVal, borderRadius);
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        var oldVal = this.borderWidth;
        this.borderWidth = borderWidth;
        firePropertyChange("borderWidth", oldVal, borderWidth);
    }

    public Color getBorderColor() {
        return borderColor != null ? borderColor : UIManager.getColor(FlatButtonUIProps.BORDER_COLOR_PROP);
    }

    public void setBorderColor(Color borderColor) {
        var oldVal = this.borderColor;
        this.borderColor = borderColor;
        firePropertyChange("borderColor", oldVal, borderColor);
    }

    public Color getBorderHoverColor() {
        return borderHoverColor != null ? borderHoverColor : UIManager.getColor(FlatButtonUIProps.BORDER_HOVER_COLOR_PROP);
    }

    public void setBorderHoverColor(Color borderHoverColor) {
        var oldVal = this.borderHoverColor;
        this.borderHoverColor = borderHoverColor;
        firePropertyChange("borderHoverColor", oldVal, borderHoverColor);
    }

    public Color getBorderPressedColor() {
        return borderPressedColor != null ? borderPressedColor : UIManager.getColor(FlatButtonUIProps.BORDER_PRESSED_COLOR_PROP);
    }

    public void setBorderPressedColor(Color borderPressedColor) {
        var oldVal = this.borderPressedColor;
        this.borderPressedColor = borderPressedColor;
        firePropertyChange("borderPressedColor", oldVal, borderPressedColor);
    }

    public Color getBorderDisabledColor() {
        return borderDisabledColor != null ? borderDisabledColor : UIManager.getColor(FlatButtonUIProps.BORDER_DISABLED_COLOR_PROP);
    }

    public void setBorderDisabledColor(Color borderDisabledColor) {
        var oldVal = this.borderDisabledColor;
        this.borderDisabledColor = borderDisabledColor;
        firePropertyChange("borderDisabledColor", oldVal, borderDisabledColor);
    }

    public Color getBorderSelectedColor() {
        return borderSelectedColor != null ? borderSelectedColor : UIManager.getColor(FlatButtonUIProps.BORDER_SELECTED_COLOR_PROP);
    }

    public void setBorderSelectedColor(Color borderSelectedColor) {
        var oldVal = this.borderSelectedColor;
        this.borderSelectedColor = borderSelectedColor;
        firePropertyChange("borderSelectedColor", oldVal, borderSelectedColor);
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        var oldVal = this.padding;
        this.padding = padding;
        firePropertyChange("padding", oldVal, padding);
    }

    public int getIconTextGap() {
        return iconTextGap;
    }

    public void setIconTextGap(int iconTextGap) {
        var oldVal = this.iconTextGap;
        this.iconTextGap = iconTextGap;
        firePropertyChange("iconTextGap", oldVal, iconTextGap);
    }

    public String getPopupText() {
        return popupText;
    }

    public void setPopupText(String popupText) {
        var oldVal = this.popupText;
        this.popupText = popupText;
        firePropertyChange("popupText", oldVal, popupText);
    }

    public int getPopupLocation() {
        return popupLocation;
    }

    public void setPopupLocation(int popupLocation) {
        var oldVal = this.popupLocation;
        this.popupLocation = popupLocation;
        firePropertyChange("popupLocation", oldVal, popupLocation);
    }

}
