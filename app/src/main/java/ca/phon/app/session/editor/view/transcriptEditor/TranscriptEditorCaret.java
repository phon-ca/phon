package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import java.awt.*;

/**
 * Custom caret implementation for {@link TranscriptEditor}
 */
public class TranscriptEditorCaret extends DefaultCaret {

    enum Mode {
        DEFAULT,
        BOX
    }

    private Mode mode;

    public TranscriptEditorCaret() {
        super();
        setBlinkRate(500);
        this.mode = Mode.DEFAULT;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private boolean _contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;
        if ((w | h | W | H) < 0) {
            // At least one of the dimensions is negative...
            return false;
        }
        // Note: if any dimension is zero, tests below must return false...
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        if (W > 0) {
            w += x;
            W += X;
            if (W <= X) {
                // X+W overflowed or W was zero, return false if...
                // either original w or W was zero or
                // x+w did not overflow or
                // the overflowed x+w is smaller than the overflowed X+W
                if (w >= x || W > w) return false;
            } else {
                // X+W did not overflow and W was not zero, return false if...
                // original w was zero or
                // x+w did not overflow and x+w is smaller than X+W
                if (w >= x && W > w) return false;
            }
        }
        else if ((x + w) < X) {
            return false;
        }
        if (H > 0) {
            h += y;
            H += Y;
            if (H <= Y) {
                if (h >= y || H > h) return false;
            } else {
                if (h >= y && H > h) return false;
            }
        }
        else if ((y + h) < Y) {
            return false;
        }
        return true;
    }

    @Override
    public void install(JTextComponent c) {
        if(!(c instanceof TranscriptEditor))
            throw new IllegalArgumentException("Invalid Text component type");
        super.install(c);
    }

    public void paint(Graphics g) {
        if(isVisible()) {
            final TranscriptEditor component = (TranscriptEditor) getComponent();
            try {
                TextUI mapper = component.getUI();
                Rectangle r = mapper.modelToView(component, getDot(), getDotBias());
                Rectangle pr = mapper.modelToView(component, getDot()-1, getDotBias());
                if ((r == null) || ((r.width == 0) && (r.height == 0))) {
                    return;
                }
                if (width > 0 && height > 0 &&
                        !this._contains(r.x, r.y, r.width, r.height)) {
                    // We seem to have gotten out of sync and no longer
                    // contain the right location, adjust accordingly.
                    Rectangle clip = g.getClipBounds();

                    if (clip != null && !clip.contains(this)) {
                        // Clip doesn't contain the old location, force it
                        // to be repainted lest we leave a caret around.
                        repaint();
                    }
                    // This will potentially cause a repaint of something
                    // we're already repainting, but without changing the
                    // semantics of damage we can't really get around this.
                    damage(r);
                }
                Element ele = component.getTranscriptDocument().getCharacterElement(getDot());
                float actualLineHeight = g.getFontMetrics().getHeight();
                if(ele != null) {
                    final AttributeSet attrs = ele.getAttributes();
                    if(StyleConstants.getFontFamily(attrs) != null) {
                        int style = (StyleConstants.isBold(attrs) ? Font.BOLD : 0) |
                                (StyleConstants.isItalic(attrs) ? Font.ITALIC : 0);
                        final Font f = new Font(StyleConstants.getFontFamily(attrs), style, StyleConstants.getFontSize(attrs));
                        actualLineHeight = g.getFontMetrics(f).getHeight();
                    }
                }
                r.height = (int) actualLineHeight;
                g.setColor(component.getCaretColor());
                if(getMode() == Mode.DEFAULT) {
                    int paintWidth = 1;
                    r.x -= paintWidth >> 1;
                    g.fillRect(r.x, r.y, paintWidth, r.height);
                } else if(getMode() == Mode.BOX) {
                    final Rectangle boxRect = new Rectangle(pr.x, r.y, r.x-pr.x + 1, r.height);
                    damage(boxRect);
                    g.drawRect(boxRect.x, boxRect.y, boxRect.width, boxRect.height);
                }
            } catch (BadLocationException e) {
                // can't render I guess
                //System.err.println("Can't render cursor");
            }
        }
    }

}
