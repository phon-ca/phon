package ca.phon.ui;

/**
 * Interface for components which have an icon strip that needs to be exposed
 * to other components.
 */
@FunctionalInterface
public interface HasIconStrip {

    public IconStrip getIconStrip();

}
