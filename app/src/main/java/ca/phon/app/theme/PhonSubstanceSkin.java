/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.theme;

import java.awt.Color;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.ComponentStateFacet;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceColorSchemeBundle;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.colorscheme.BaseColorScheme;
import org.pushingpixels.substance.api.colorscheme.SteelBlueColorScheme;
import org.pushingpixels.substance.api.painter.border.GlassBorderPainter;
import org.pushingpixels.substance.api.painter.decoration.ArcDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.ClassicFillPainter;
import org.pushingpixels.substance.api.painter.highlight.GlassHighlightPainter;
import org.pushingpixels.substance.api.painter.overlay.BottomLineOverlayPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;
import org.pushingpixels.substance.internal.colorscheme.BlendBiColorScheme;

public class PhonSubstanceSkin extends SubstanceSkin {
	/**
     * Display name for <code>this</code> skin.
     */
    public static final String NAME = "Phon";


    /**
     * Overlay painter to paint separator lines on some decoration areas.
     */
    private BottomLineOverlayPainter bottomLineOverlayPainter;

    public PhonSubstanceSkin() {
        super();

        ColorSchemes schemes = SubstanceSkin
                .getColorSchemes("ca/phon/app/theme/phon.colorschemes");

        SubstanceColorScheme activeScheme = schemes.get("Phon Active");
        SubstanceColorScheme enabledScheme = schemes.get("Phon Enabled").saturate(-0.9);
        SubstanceColorScheme rolloverUnselectedScheme = schemes
                .get("Phon Rollover Unselected");
        final SubstanceColorScheme pressedScheme = schemes.get("Phon Pressed");
        SubstanceColorScheme rolloverSelectedScheme = schemes
                .get("Phon Rollover Selected");
        SubstanceColorScheme disabledScheme = schemes.get("Phon Disabled").saturate(-0.9);

        SubstanceColorSchemeBundle defaultSchemeBundle = new SubstanceColorSchemeBundle(
                activeScheme, enabledScheme, disabledScheme);

        CopyMutableColorScheme steelBlue= new CopyMutableColorScheme("Phon Hover", new SteelBlueColorScheme().tint(0.4));
        steelBlue.setForegroundColor(enabledScheme.getForegroundColor());

        double saturate = 0.1;
        double tint = 0.4;
        double shade = tint/4;
        CopyMutableColorScheme pressed = new CopyMutableColorScheme("Phon Pressed", steelBlue.saturate(saturate).shade(shade));
        //pressed.setForegroundColor(pressedScheme.getForegroundColor());
        defaultSchemeBundle.registerColorScheme(pressed,
                ComponentState.PRESSED_SELECTED, ComponentState.PRESSED_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(new BlendBiColorScheme(
                        steelBlue, disabledScheme, 0.25),
                ComponentState.DISABLED_SELECTED);
        defaultSchemeBundle.registerColorScheme(
                steelBlue.tint(tint).saturate(saturate),
                ComponentState.SELECTED);
        defaultSchemeBundle.registerColorScheme(
                steelBlue.shade(shade / 2).saturate(saturate/2),
                ComponentState.ROLLOVER_SELECTED);
        defaultSchemeBundle.registerColorScheme(
                steelBlue.tint(tint / 2).saturate(saturate/2),
                ComponentState.ROLLOVER_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(steelBlue.shade(0.5),
                ColorSchemeAssociationKind.MARK, ComponentState.getActiveStates());
        defaultSchemeBundle.registerColorScheme(steelBlue,
                ColorSchemeAssociationKind.BORDER, ComponentState.getActiveStates());

        // for progress bars
        ComponentState determinateState = new ComponentState(
                "determinate enabled", new ComponentStateFacet[] {
                        ComponentStateFacet.ENABLE,
                        ComponentStateFacet.DETERMINATE,
                        ComponentStateFacet.SELECTION }, null);
        ComponentState determinateDisabledState = new ComponentState(
                "determinate disabled", new ComponentStateFacet[] {
                        ComponentStateFacet.DETERMINATE,
                        ComponentStateFacet.SELECTION },
                new ComponentStateFacet[] { ComponentStateFacet.ENABLE });
        ComponentState indeterminateState = new ComponentState(
                "indeterminate enabled",
                new ComponentStateFacet[] { ComponentStateFacet.ENABLE,
                        ComponentStateFacet.SELECTION },
                new ComponentStateFacet[] { ComponentStateFacet.DETERMINATE });
        ComponentState indeterminateDisabledState = new ComponentState(
                "indeterminate disabled", null, new ComponentStateFacet[] {
                        ComponentStateFacet.DETERMINATE,
                        ComponentStateFacet.ENABLE, ComponentStateFacet.SELECTION });
        defaultSchemeBundle.registerColorScheme(rolloverSelectedScheme,
                determinateState, indeterminateState);
        defaultSchemeBundle.registerColorScheme(rolloverSelectedScheme,
                ColorSchemeAssociationKind.BORDER,
                determinateState, indeterminateState);
        defaultSchemeBundle.registerColorScheme(disabledScheme,
                determinateDisabledState, indeterminateDisabledState);
        defaultSchemeBundle.registerColorScheme(disabledScheme,
                ColorSchemeAssociationKind.BORDER,
                determinateDisabledState, indeterminateDisabledState);

        // for uneditable fields
        ComponentState editable = new ComponentState("editable",
                new ComponentStateFacet[] {ComponentStateFacet.ENABLE, ComponentStateFacet.EDITABLE},
                null);
        ComponentState uneditable = new ComponentState("uneditable",
                editable, new ComponentStateFacet[] {ComponentStateFacet.ENABLE},
                new ComponentStateFacet[] {ComponentStateFacet.EDITABLE});
        defaultSchemeBundle.registerColorScheme(
                defaultSchemeBundle.getColorScheme(editable),
                ColorSchemeAssociationKind.FILL, uneditable
        );

        // for text highlight
        defaultSchemeBundle.registerHighlightColorScheme(steelBlue);

        registerDecorationAreaSchemeBundle(defaultSchemeBundle,
                DecorationAreaType.NONE);

        CopyMutableColorScheme chrome = new CopyMutableColorScheme("Phon Chrome", pressedScheme);
        chrome.setUltraDarkColor(chrome.getExtraLightColor());
        registerDecorationAreaSchemeBundle(new SubstanceColorSchemeBundle(pressedScheme, pressedScheme, disabledScheme), chrome,
                DecorationAreaType.PRIMARY_TITLE_PANE,
                DecorationAreaType.SECONDARY_TITLE_PANE);


        this.registerAsDecorationArea(enabledScheme,
                DecorationAreaType.PRIMARY_TITLE_PANE_INACTIVE,
                DecorationAreaType.SECONDARY_TITLE_PANE_INACTIVE);


        registerAsDecorationArea(activeScheme.saturate(-0.75),
                DecorationAreaType.HEADER, DecorationAreaType.FOOTER,
                DecorationAreaType.GENERAL);



        this.buttonShaper = new ClassicButtonShaper();
        this.fillPainter = new ClassicFillPainter();

        this.decorationPainter = new ArcDecorationPainter();

        this.highlightPainter = new GlassHighlightPainter();
        this.borderPainter = new GlassBorderPainter();
    }

    /*
      * (non-Javadoc)
      *
      * @see org.pushingpixels.substance.skin.SubstanceSkin#getDisplayName()
      */
    @Override
    public String getDisplayName() {
        return NAME;
    }
}

class CopyMutableColorScheme extends BaseColorScheme {

    Color foregroundColor;
    Color ultraLightColor;
    Color extraLightColor;
    Color lightColor;
    Color midColor;
    Color darkColor;
    Color ultraDarkColor;

    public CopyMutableColorScheme(String name, SubstanceColorScheme copy) {
        super(name, copy.isDark());
        foregroundColor = copy.getForegroundColor();
        ultraLightColor = copy.getUltraLightColor();
        extraLightColor = copy.getExtraLightColor();
        lightColor = copy.getLightColor();
        midColor = copy.getMidColor();
        darkColor = copy.getDarkColor();
        ultraDarkColor = copy.getUltraDarkColor();
    }

    public void setDark(boolean isDark) {
        this.isDark = isDark;
    }

    @Override
    public Color getDarkColor() {
        return darkColor;
    }

    public void setDarkColor(Color darkColor) {
        this.darkColor = darkColor;
    }

    @Override
    public Color getExtraLightColor() {
        return extraLightColor;
    }

    public void setExtraLightColor(Color extraLightColor) {
        this.extraLightColor = extraLightColor;
    }

    @Override
    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Override
    public Color getLightColor() {
        return lightColor;
    }

    public void setLightColor(Color lightColor) {
        this.lightColor = lightColor;
    }

    @Override
    public Color getMidColor() {
        return midColor;
    }

    public void setMidColor(Color midColor) {
        this.midColor = midColor;
    }

    @Override
    public Color getUltraDarkColor() {
        return ultraDarkColor;
    }

    public void setUltraDarkColor(Color ultraDarkColor) {
        this.ultraDarkColor = ultraDarkColor;
    }

    @Override
    public Color getUltraLightColor() {
        return ultraLightColor;
    }

    public void setUltraLightColor(Color ultraLightColor) {
        this.ultraLightColor = ultraLightColor;
    }
}
