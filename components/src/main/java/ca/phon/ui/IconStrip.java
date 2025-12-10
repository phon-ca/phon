package ca.phon.ui;

import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An icon strip for displaying FlatButton actions in a row/column.
 * Direction is determined by component orientation.
 * The icon strip will contain three sections:
 * - left/top
 * - center
 * - right/bottom
 *
 */
public class IconStrip extends JPanel {

    public enum IconStripPosition {
        LEFT,
        CENTER,
        RIGHT
    };

    public IconStrip() {
        this(SwingConstants.HORIZONTAL);
    }

    public IconStrip(int layout) {
        super(new LayoutManager(layout));
    }

    public FlatButton createButton(Action action) {
        final FlatButton retVal = new FlatButton(action);
        retVal.setAction(action);
        retVal.setPadding(0);
        retVal.setBorderPainted(false);
        retVal.setFocusPainted(false);
        retVal.setContentAreaFilled(false);
        retVal.setRolloverEnabled(true);
        retVal.setIconColor(UIManager.getColor(IconStripUIProps.ICON_STRIP_ICON_COLOR));
        retVal.setIconHoverColor(UIManager.getColor(IconStripUIProps.ICON_STRIP_HOVER_COLOR));
        retVal.setBgSelectedColor(UIManager.getColor(IconStripUIProps.ICON_STRIP_ICON_SELECTED_BACKGROUND));
        retVal.setBgPressedColor(UIManager.getColor(IconStripUIProps.ICON_STRIP_ICON_PRESSED_BACKGROUND));
        retVal.setIconSelectedColor(UIManager.getColor(IconStripUIProps.ICON_STRIP_ICON_SELECTED_COLOR));
        retVal.setFont(FontPreferences.getTitleFont());
        retVal.setPopupLocation(SwingConstants.EAST);
        return retVal;
    }

    public FlatButton add(Action action, IconStripPosition position) {
        final FlatButton retVal = createButton(action);
        add(retVal, position);
        return retVal;
    }

    public void addSeparator(IconStripPosition position) {
        final int orientation = ((LayoutManager)getLayout()).layout;
        final JSeparator separator = new JSeparator(orientation);
        add(separator, position);
    }

    /**
     * Custom layout manager for icon strip
     */
    private static class LayoutManager implements LayoutManager2 {

        /**
         * Icon strip layout - horizontal or vertical
         */
        private int layout;

        private LinkedHashMap<Component, IconStripPosition> componentMap = new LinkedHashMap<>();

        public LayoutManager(int layout) {
            this.layout = layout;
        }

        public LayoutManager() {
            this(SwingConstants.HORIZONTAL);
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            if(constraints instanceof IconStripPosition) {
                componentMap.put(comp, (IconStripPosition)constraints);
            } else {
                throw new IllegalArgumentException("constraints must be one of " + IconStripPosition.values());
            }
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            if(target instanceof IconStrip iconStrip) {
                final Insets insets = target.getInsets();
                final int insetWidth = insets.left + insets.right;
                final int insetHeight = insets.top + insets.bottom;
                int maxWidth = insetWidth;
                int maxHeight = insetHeight;
                for(Component comp:componentMap.keySet()) {
                    if(!comp.isVisible()) continue;
                    final Dimension prefSize = comp.getPreferredSize();
                    if(layout == SwingConstants.HORIZONTAL) {
                        maxWidth += prefSize.width;
                        maxHeight = Math.max(maxHeight, prefSize.height);
                    } else {
                        maxWidth = Math.max(maxWidth, prefSize.width);
                        maxHeight += prefSize.height;
                    }
                }
                if(layout == SwingConstants.HORIZONTAL) {
                    maxWidth = Integer.MAX_VALUE;
                } else {
                    maxHeight += Integer.MAX_VALUE;
                }
                return new Dimension(maxWidth, maxHeight);
            } else {
                throw new IllegalArgumentException("target must be an IconStrip");
            }
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0;
        }

        @Override
        public void invalidateLayout(Container target) {
            // ignore
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            // ignore
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            componentMap.remove(comp);
        }

        private int calculateRows(int availableWidth, Insets insets) {
            if(layout != SwingConstants.HORIZONTAL) return 1;

            int maxWidth = availableWidth - insets.left - insets.right;
            int rows = 1;
            int currentWidth = 0;

            // Process all components in order, checking if they fit on current row
            for(Map.Entry<Component, IconStripPosition> entry : componentMap.entrySet()) {
                if(!entry.getKey().isVisible()) continue;

                final Dimension prefSize = entry.getKey().getPreferredSize();

                if(currentWidth + prefSize.width > maxWidth && currentWidth > 0) {
                    rows++;
                    currentWidth = prefSize.width;
                } else {
                    currentWidth += prefSize.width;
                }
            }

            return rows;
        }

        private int calculateRowsForPosition(IconStripPosition position, int availableWidth, Insets insets) {
            // No longer used - keeping for compatibility
            return 1;
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            if(parent instanceof IconStrip iconStrip) {
                final Insets insets = parent.getInsets();
                final int insetWidth = insets.left + insets.right;
                final int insetHeight = insets.top + insets.bottom;

                if(layout == SwingConstants.HORIZONTAL) {
                    // Calculate wrapping for horizontal layout
                    int availableWidth = parent.getWidth();
                    if(availableWidth == 0) {
                        // If no width set yet, calculate single-line preferred size
                        int prefWidth = 0;
                        int prefHeight = 0;
                        for(Component comp:componentMap.keySet()) {
                            if(!comp.isVisible()) continue;
                            final Dimension prefSize = comp.getPreferredSize();
                            prefWidth += prefSize.width;
                            prefHeight = Math.max(prefHeight, prefSize.height);
                        }
                        return new Dimension(prefWidth + insetWidth, prefHeight + insetHeight);
                    }

                    // Calculate height based on number of rows needed
                    int maxRowHeight = 0;

                    for(Component comp:componentMap.keySet()) {
                        if(!comp.isVisible()) continue;
                        final Dimension prefSize = comp.getPreferredSize();
                        maxRowHeight = Math.max(maxRowHeight, prefSize.height);
                    }

                    int totalRows = calculateRows(availableWidth, insets);
                    int totalHeight = maxRowHeight * totalRows;

                    return new Dimension(availableWidth, totalHeight + insetHeight);
                } else {
                    // Vertical layout - no wrapping
                    int prefWidth = 0;
                    int prefHeight = 0;
                    for(Component comp:componentMap.keySet()) {
                        if(!comp.isVisible()) continue;
                        final Dimension prefSize = comp.getPreferredSize();
                        prefWidth = Math.max(prefWidth, prefSize.width);
                        prefHeight += prefSize.height;
                    }
                    return new Dimension(prefWidth + insetWidth, prefHeight + insetHeight);
                }
            } else {
                throw new IllegalArgumentException("target must be an IconStrip");
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            if(parent instanceof IconStrip iconStrip) {
                final Insets insets = parent.getInsets();
                final int insetWidth = insets.left + insets.right;
                final int insetHeight = insets.top + insets.bottom;
                int prefWidth = 0;
                int prefHeight = 0;
                for(Component comp:componentMap.keySet()) {
                    if(!comp.isVisible()) continue;
                    final Dimension prefSize = comp.getPreferredSize();
                    if(layout == SwingConstants.HORIZONTAL) {
                        prefWidth = Math.max(prefWidth, prefSize.width);
                        prefHeight = Math.max(prefHeight, prefSize.height);
                    } else {
                        prefWidth = Math.max(prefWidth, prefSize.width);
                        prefHeight += prefSize.height;
                    }
                }
                return new Dimension(prefWidth + insetWidth, prefHeight + insetHeight);
            } else {
                throw new IllegalArgumentException("target must be an IconStrip");
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            if(parent instanceof IconStrip iconStrip) {
                final Dimension size = parent.getSize();
                final Insets insets = parent.getInsets();

                if(layout == SwingConstants.HORIZONTAL) {
                    layoutHorizontalWrapped(parent, size, insets);
                } else {
                    layoutVertical(parent, size, insets);
                }
            } else {
                throw new IllegalArgumentException("target must be an IconStrip");
            }
        }

        private void layoutHorizontalWrapped(Container parent, Dimension size, Insets insets) {
            int availableWidth = size.width - insets.left - insets.right;

            // Calculate max component height for vertical centering in rows
            int rowHeight = 0;
            for(Component comp : componentMap.keySet()) {
                if(!comp.isVisible()) continue;
                final Dimension prefSize = comp.getPreferredSize();
                rowHeight = Math.max(rowHeight, prefSize.height);
            }

            // Group all components into rows by position
            List<RowLayout> rows = createRowLayouts(availableWidth);

            // Layout each row
            int currentY = insets.top;
            for(RowLayout rowLayout : rows) {
                layoutRowWithPositions(rowLayout, currentY, availableWidth, rowHeight, insets);
                currentY += rowHeight;
            }
        }

        private static class RowLayout {
            List<Component> leftComponents = new ArrayList<>();
            List<Component> centerComponents = new ArrayList<>();
            List<Component> rightComponents = new ArrayList<>();
        }

        private List<RowLayout> createRowLayouts(int availableWidth) {
            List<RowLayout> rows = new ArrayList<>();
            RowLayout currentRow = new RowLayout();
            rows.add(currentRow);

            int currentRowWidth = 0;

            for(Map.Entry<Component, IconStripPosition> entry : componentMap.entrySet()) {
                Component comp = entry.getKey();
                if(!comp.isVisible()) continue;

                IconStripPosition position = entry.getValue();
                final Dimension prefSize = comp.getPreferredSize();

                // Check if we need to start a new row
                if(currentRowWidth + prefSize.width > availableWidth && currentRowWidth > 0) {
                    currentRow = new RowLayout();
                    rows.add(currentRow);
                    currentRowWidth = 0;
                }

                // Add component to appropriate position in current row
                switch(position) {
                    case LEFT:
                        currentRow.leftComponents.add(comp);
                        break;
                    case CENTER:
                        currentRow.centerComponents.add(comp);
                        break;
                    case RIGHT:
                        currentRow.rightComponents.add(comp);
                        break;
                }

                currentRowWidth += prefSize.width;
            }

            return rows;
        }

        private void layoutRowWithPositions(RowLayout rowLayout, int y, int availableWidth, int rowHeight, Insets insets) {
            int leftX = insets.left;

            // Layout left-aligned components
            for(Component comp : rowLayout.leftComponents) {
                final Dimension prefSize = comp.getPreferredSize();
                int centeredY = y + (rowHeight - prefSize.height) / 2;
                comp.setBounds(leftX, centeredY, prefSize.width, prefSize.height);
                leftX += prefSize.width;
            }

            // Calculate center components total width
            int centerWidth = 0;
            for(Component comp : rowLayout.centerComponents) {
                centerWidth += comp.getPreferredSize().width;
            }

            // Layout center-aligned components
            int centerX = insets.left + (availableWidth - centerWidth) / 2;
            for(Component comp : rowLayout.centerComponents) {
                final Dimension prefSize = comp.getPreferredSize();
                int centeredY = y + (rowHeight - prefSize.height) / 2;
                comp.setBounds(centerX, centeredY, prefSize.width, prefSize.height);
                centerX += prefSize.width;
            }

            // Calculate right components total width
            int rightWidth = 0;
            for(Component comp : rowLayout.rightComponents) {
                rightWidth += comp.getPreferredSize().width;
            }

            // Layout right-aligned components
            int rightX = insets.left + availableWidth - rightWidth;
            for(Component comp : rowLayout.rightComponents) {
                final Dimension prefSize = comp.getPreferredSize();
                int centeredY = y + (rowHeight - prefSize.height) / 2;
                comp.setBounds(rightX, centeredY, prefSize.width, prefSize.height);
                rightX += prefSize.width;
            }
        }

        private void layoutVertical(Container parent, Dimension size, Insets insets) {
            int currentX = insets.left;
            int currentY = insets.top;

            // layout left/top components
            for(Component comp:componentMap.keySet()) {
                if(!comp.isVisible()) continue;
                final IconStripPosition position = componentMap.get(comp);
                if(position == IconStripPosition.LEFT) {
                    final Dimension prefSize = comp.getPreferredSize();
                    currentX = (size.width/2 - prefSize.width/2) + insets.left;
                    comp.setBounds(currentX, currentY, prefSize.width, prefSize.height);
                    currentY += prefSize.height;
                }
            }

            // layout center components
            currentY = size.height/2;
            for(Component comp:componentMap.keySet()) {
                if(!comp.isVisible()) continue;
                final IconStripPosition position = componentMap.get(comp);
                if(position == IconStripPosition.CENTER) {
                    final Dimension prefSize = comp.getPreferredSize();
                    currentY -= prefSize.height/2;
                }
            }

            for(Component comp:componentMap.keySet()) {
                if(!comp.isVisible()) continue;
                final IconStripPosition position = componentMap.get(comp);
                if(position == IconStripPosition.CENTER) {
                    final Dimension prefSize = comp.getPreferredSize();
                    currentX = (size.width/2 - prefSize.width/2) + insets.left;
                    comp.setBounds(currentX, currentY, prefSize.width, prefSize.height);
                    currentY += prefSize.height;
                }
            }

            // layout right/bottom components
            currentY = size.height - insets.bottom;
            for(Component comp:componentMap.keySet()) {
                if(!comp.isVisible()) continue;
                final IconStripPosition position = componentMap.get(comp);
                if(position == IconStripPosition.RIGHT) {
                    final Dimension prefSize = comp.getPreferredSize();
                    currentY -= prefSize.height;
                }
            }

            for(Component comp:componentMap.keySet()) {
                if(!comp.isVisible()) continue;
                final IconStripPosition position = componentMap.get(comp);
                if(position == IconStripPosition.RIGHT) {
                    final Dimension prefSize = comp.getPreferredSize();
                    currentX = (size.width/2 - prefSize.width/2) + insets.left;
                    comp.setBounds(currentX, currentY, prefSize.width, prefSize.height);
                    currentY += prefSize.height;
                }
            }
        }
    }

}
