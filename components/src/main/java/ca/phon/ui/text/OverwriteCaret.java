/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui.text;

import java.awt.*;

import javax.swing.text.*;

public class OverwriteCaret extends DefaultCaret {

	private static final long serialVersionUID = -3921804104413957955L;

	/* (non-Javadoc)
     * @see javax.swing.text.DefaultCaret#damage(java.awt.Rectangle)
     */
    @Override
    protected synchronized void damage(Rectangle r) {
        if (r == null)
            return;

        // give values to x,y,width,height (inherited from java.awt.Rectangle)
        x = r.x;
        y = r.y;
        height = r.height;
        
        if (width <= 0)
            width = getComponent().getWidth();
        
        repaint();
        repaint();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.DefaultCaret#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) {
        final JTextComponent textComp = 
        		(JTextComponent)getComponent();

        final int dot = getDot();
        Rectangle r = null;
        char dotChar;
        try {
            r = textComp.modelToView(dot);
            if (r == null)
                return;
            dotChar = textComp.getText(dot, 1).charAt(0);
        } catch (BadLocationException e) {
            return;
        }

        if(Character.isWhitespace(dotChar)) dotChar = '_';

        if ((x != r.x) || (y != r.y)) {
            damage(r);
            return;
        }

        g.setColor(textComp.getCaretColor());
        g.setXORMode(textComp.getBackground()); 

        width = g.getFontMetrics().charWidth(dotChar);
        if (isVisible())
            g.fillRect(r.x, r.y, width, r.height);
    }

}
