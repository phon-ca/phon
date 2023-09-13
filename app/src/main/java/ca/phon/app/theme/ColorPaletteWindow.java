package ca.phon.app.theme;

import ca.phon.ui.CommonModuleFrame;

import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.IOException;

public class ColorPaletteWindow extends CommonModuleFrame {

    private final static String TITLE = "Color Palette Designer";

    private ColorPaletteDesigner designer;

    public ColorPaletteWindow() {
        super(TITLE);

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        designer = new ColorPaletteDesigner();
        designer.addPropertyChangeListener("modified", e -> {
            setModified(designer.isModified());
        });
        putExtension(UndoManager.class, designer.getUndoManager());

        add(designer, BorderLayout.CENTER);
    }

    @Override
    public boolean saveData() throws IOException {
        return super.saveData();
    }

}
