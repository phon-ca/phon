/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.ui.ipa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.util.Tuple;

/**
 * Default UI for the phone map display.  This class
 * handles drawing and all input to the component.
 */
public class DefaultPhoneMapDisplayUI extends PhoneMapDisplayUI {

	/* Action IDs */
	private static final String FOCUS_PREVIOUS = "_FOCUS_PREV_PHONE_";
	private static final String FOCUS_NEXT = "_FOCUS_NEXT_PHONE_";
	private static final String MOVE_PHONE_RIGHT = "_MOVE_PHONE_RIGHT_";
	private static final String MOVE_PHONE_LEFT = "_MOVE_PHONE_LEFT_";
	private static final String TOGGLE_PHONE_COLOUR = "_TOGGLE_PHONE_COLOUR_";
	private static final String SELECT_TARGET_SIDE = "_SELECT_TARGET_SIDE_";
	private static final String SELECT_ACTUAL_SIDE = "_SELECT_ACTUAL_SIDE_";

	/** The display */
	private PhoneMapDisplay display;

	private static final int insetSize = 2;
	private Insets phoneBoxInsets = new Insets(insetSize, insetSize, insetSize, insetSize);
	private Dimension phoneBoxSize = new Dimension(18, 20);
	private static final int groupSpace = 5;

	private boolean drawPhoneLock = false;
	private Form lockSide = Form.Actual;

	// variable for controlling mouse drags
	private boolean isDragging = false;
	private int dragLeftEdge = -1;
	private int dragRightEdge = -1;


	public DefaultPhoneMapDisplayUI(PhoneMapDisplay c) {
		super();
		this.display = c;
//		installUI();
	}

	@Override
	public void installUI(JComponent c) {
		display = (PhoneMapDisplay)c;
//
		setupActions();
		installMouseListener();
		installKeyListener();

		display.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent fe) {
				display.repaint();
			}

			@Override
			public void focusLost(FocusEvent fe) {
				display.repaint();
			}
		});
//
		display.setRequestFocusEnabled(true);
	}

	/** Setup actions for component */
	private void setupActions() {
		ActionMap actionMap = display.getActionMap();
		InputMap inputMap = display.getInputMap(JComponent.WHEN_FOCUSED);

		PhonUIAction focusNextAct =
				new PhonUIAction(this, "focusNextPhone");
		actionMap.put(FOCUS_NEXT, focusNextAct);
		KeyStroke focusNextKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		inputMap.put(focusNextKs, FOCUS_NEXT);

		PhonUIAction moveRightAct =
				new PhonUIAction(this, "movePhoneRight");
		actionMap.put(MOVE_PHONE_RIGHT, moveRightAct);
		KeyStroke moveRightKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
					InputEvent.ALT_MASK);
		inputMap.put(moveRightKs, MOVE_PHONE_RIGHT);

		PhonUIAction focusPrevAct =
				new PhonUIAction(this, "focusPrevPhone");
		actionMap.put(FOCUS_PREVIOUS, focusPrevAct);
		KeyStroke focusPrevKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		inputMap.put(focusPrevKs, FOCUS_PREVIOUS);
		KeyStroke delKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		inputMap.put(delKs, FOCUS_PREVIOUS);

		PhonUIAction moveLeftAct =
				new PhonUIAction(this, "movePhoneLeft");
		actionMap.put(MOVE_PHONE_LEFT, moveLeftAct);
		KeyStroke moveLeftKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
					InputEvent.ALT_MASK);
		inputMap.put(moveLeftKs, MOVE_PHONE_LEFT);

		PhonUIAction toggleColourAct =
				new PhonUIAction(this, "togglePhoneColour");
		actionMap.put(TOGGLE_PHONE_COLOUR, toggleColourAct);
		KeyStroke toggleColourKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_C,
					InputEvent.ALT_MASK);
		inputMap.put(toggleColourKs, TOGGLE_PHONE_COLOUR);

		PhonUIAction selectTargetAct =
				new PhonUIAction(this, "setLockSide", Form.Target);
		actionMap.put(SELECT_TARGET_SIDE, selectTargetAct);
		KeyStroke selectTargetKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_UP,
					InputEvent.ALT_MASK);
		inputMap.put(selectTargetKs, SELECT_TARGET_SIDE);

		PhonUIAction selectActualAct =
				new PhonUIAction(this, "setLockSide", Form.Actual);
		actionMap.put(SELECT_ACTUAL_SIDE, selectActualAct);
		KeyStroke selectActualKs =
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
					InputEvent.ALT_MASK);
		inputMap.put(selectActualKs, SELECT_ACTUAL_SIDE);
	}

	/** Install mouse listener for component */
	private void installMouseListener() {
		MouseInputAdapter mouseListener =
			new AlignmentMouseHandler();
		display.addMouseListener(mouseListener);
		display.addMouseMotionListener(mouseListener);
	}

	private void installKeyListener() {
		KeyListener keyListener =
				new ChangeKeyListener();
		display.addKeyListener(keyListener);
	}

	/** UI Actions */
	public void focusNextPhone(PhonActionEvent pae) {
		int currentFocus = display.getFocusedPosition();
		int nextFocus = currentFocus+1;

		if(nextFocus < display.getNumberOfAlignmentPositions()) {
			display.setFocusedPosition(nextFocus);
		}
	}

	public void focusPrevPhone(PhonActionEvent pae) {
		int currentFocus = display.getFocusedPosition();
		int prevFocus = currentFocus-1;
		if(prevFocus >= 0) {
			display.setFocusedPosition(prevFocus);
		}
	}

	public void movePhoneRight(PhonActionEvent pae) {
		Tuple<Integer, Integer> alignmentPos =
				display.positionToGroupPos(display.getFocusedPosition());
		display.movePhoneRight(alignmentPos, lockSide);
	}

	public void movePhoneLeft(PhonActionEvent pae) {
		Tuple<Integer, Integer> alignmentPos =
				display.positionToGroupPos(display.getFocusedPosition());
		display.movePhoneLeft(alignmentPos, lockSide);
	}

	public void togglePhoneColour(PhonActionEvent pae) {
		display.togglePaintPhoneBackground();
	}

	public void setLockSide(PhonActionEvent pae) {
		lockSide = (Form)pae.getData();
		display.repaint();
	}

	private void paintPhone(Graphics2D g2d, Phone p,
			Area pArea, Rectangle pRect) {
		Font displayFont = display.getFont();
		if(displayFont == null)
			displayFont = UserPrefManager.getUITranscriptFont();
		
		if(display.isPaintPhoneBackground()) {
			Color grad_top = p.getScType().getColor().brighter();
			Color grad_btm = p.getScType().getColor().darker();
			GradientPaint gp =
					new GradientPaint(
						new Point(pRect.x, pRect.y), grad_top,
						new Point(pRect.x, pRect.y+pRect.height), grad_btm);
			Paint oldPaiont = g2d.getPaint();
			g2d.setPaint(gp);
//					g2d.setColor(p.getScType().getColor());
			g2d.fill(pArea);
			g2d.setPaint(oldPaiont);
		}

		// draw phone string
		Rectangle pBox =
			new Rectangle(pRect.x + phoneBoxInsets.left,
				pRect.y + phoneBoxInsets.top,
				phoneBoxSize.width, phoneBoxSize.height);

		Font f = displayFont;
		FontMetrics fm = g2d.getFontMetrics(f);
		Rectangle2D stringBounds =
				fm.getStringBounds(p.getPhoneString(), g2d);
		while(
				(stringBounds.getWidth() > pBox.width)
				|| (stringBounds.getHeight() > pBox.height)) {
			f = f.deriveFont(f.getSize2D()-0.2f);
			fm = g2d.getFontMetrics(f);
			stringBounds = fm.getStringBounds(p.getPhoneString(), g2d);
		}

		float phoneX =
				pBox.x + (pBox.width/2.0f) - ((float)stringBounds.getWidth()/2.0f);
		float phoneY =
				(float)pBox.y + (float)pBox.height - fm.getDescent();

		g2d.setFont(f);
		g2d.setColor(display.getForeground());
		g2d.drawString(p.getPhoneString(), phoneX, phoneY);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D)g;

		 // Enable antialiasing for shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        // Enable antialiasing for text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Dimension size = display.getSize();
		g2d.setColor(display.getBackground());
		g2d.fillRect(0, 0, size.width, size.height);

		// setup phone rect
		int pX = c.getInsets().left + insetSize;
		int pY = c.getInsets().top + insetSize;
		int pW = phoneBoxInsets.left + phoneBoxInsets.right
				+ phoneBoxSize.width;
		int pH = phoneBoxInsets.top + phoneBoxInsets.bottom
				+ phoneBoxSize.height;

		
//		FontMetrics fm = g.getFontMetrics(displayFont);

		Rectangle phoneRect =
				new Rectangle(pX, pY, pW, pH);

		double dArcLengthFill =
					Math.min(phoneRect.width/1.8, phoneRect.height/1.8);
		double dOffsetFill =
			dArcLengthFill / 2;

		g.setColor(Color.black);

		for(int gIdx = 0; gIdx < display.getNumberOfGroups(); gIdx++) {
			PhoneMap pm = display.getPhoneMapForGroup(gIdx);
			if(pm == null) continue;

			// holders for syllable and phone rects/areas
			List<Rectangle> targetSyllRects = new ArrayList<Rectangle>();
			List<Rectangle> actualSyllRects = new ArrayList<Rectangle>();

//			List<Area> targetPhoneAreas = new ArrayList<Area>();
//			List<Area> actualPhoneAreas = new ArrayList<Area>();
			Area[] targetPhoneAreas = new Area[pm.getAlignmentLength()];
			Area[] actualPhoneAreas = new Area[pm.getAlignmentLength()];

			// first create a list of target and actual syllables
			List<List<Phone>> targetSyllables = new ArrayList<List<Phone>>();
			List<List<Phone>> actualSyllables = new ArrayList<List<Phone>>();

			int lastIdx = -1;
			List<Phone> currentSyllable = null;
			for(Phone tp:pm.getTopElements()) {
				if(tp.getSyllableIndex() != lastIdx) {
					if(currentSyllable != null
							&& currentSyllable.size() > 0) {
						targetSyllables.add(currentSyllable);
					}
					currentSyllable = new ArrayList<Phone>();
					lastIdx = tp.getSyllableIndex();
				}
				currentSyllable.add(tp);
			}
			if(currentSyllable != null && currentSyllable.size() > 0)
				targetSyllables.add(currentSyllable);

			lastIdx = -1;
			currentSyllable = null;
			for(Phone ap:pm.getBottomElements()) {
				if(ap.getSyllableIndex() != lastIdx) {
					if(currentSyllable != null &&
							currentSyllable.size() > 0) {
						actualSyllables.add(currentSyllable);
					}
					currentSyllable = new ArrayList<Phone>();
					lastIdx = ap.getSyllableIndex();
				}
				currentSyllable.add(ap);
			}
			if(currentSyllable != null && currentSyllable.size() > 0) 
				actualSyllables.add(currentSyllable);

			// iterate through syllables, create syllable rects and
			// phone areas as needed
			Rectangle refRect = new Rectangle(phoneRect);
			for(List<Phone> targetSyll:targetSyllables) {

				Rectangle syllRect = null;

				for(int pIdx = 0; pIdx < targetSyll.size(); pIdx++) {
					Phone p = targetSyll.get(pIdx);
					int alignIdx = -1;

					for(int aIdx = 0; aIdx < pm.getAlignmentLength(); aIdx++) {
						Phone ap = pm.getTopAlignmentElements().get(aIdx);
						
						if(ap != null && ap == p) {
							alignIdx = aIdx;
							break;
						}
					}

					if(alignIdx >= 0) {
						int widthOffset =
								alignIdx * refRect.width;
						Rectangle pRect = new Rectangle(refRect);
						pRect.translate(widthOffset, 0);

						if(syllRect == null)
							syllRect = new Rectangle(pRect);
						else
							syllRect.add(pRect);

						Area pArea = null;
						if(pIdx == 0) {
							if(targetSyll.size() > 1)
								pArea = createAreaForLeftEdge(pRect);
							else
								pArea = createRoundRectArea(pRect);
						} else if(pIdx == targetSyll.size() -1) {
							pArea = createAreaForRightEdge(pRect);
						} else {
							pArea = createRectArea(pRect);
						}

						targetPhoneAreas[alignIdx] = pArea;

						paintPhone(g2d, p, pArea, pRect);
					}
				}

				if(syllRect != null)
					targetSyllRects.add(syllRect);

			}

			refRect.translate(0, refRect.height);
			for(List<Phone> actualSyll:actualSyllables) {

				Rectangle syllRect = null;

				for(int pIdx = 0; pIdx < actualSyll.size(); pIdx++) {
					Phone p = actualSyll.get(pIdx);
					int alignIdx = -1;

					for(int aIdx = 0; aIdx < pm.getAlignmentLength(); aIdx++) {
						Phone ap = pm.getBottomAlignmentElements().get(aIdx);

						if(ap != null && ap == p) {
							alignIdx = aIdx;
							break;
						}
					}

					if(alignIdx >= 0) {
						int widthOffset =
								alignIdx * refRect.width;
						Rectangle pRect = new Rectangle(refRect);
						pRect.translate(widthOffset, 0);

						if(syllRect == null)
							syllRect = new Rectangle(pRect);
						else
							syllRect.add(pRect);

						Area pArea = null;
						if(pIdx == 0) {
							if(actualSyll.size() > 1)
								pArea = createAreaForLeftEdge(pRect);
							else
								pArea = createRoundRectArea(pRect);
						} else if(pIdx == actualSyll.size()-1) {
							pArea = createAreaForRightEdge(pRect);
						} else {
							pArea = createRectArea(pRect);
						}
						actualPhoneAreas[alignIdx] = pArea;
						paintPhone(g2d, p, pArea, pRect);
					}
				}

				if(syllRect != null)
					actualSyllRects.add(syllRect);
			}

			g2d.setStroke(new BasicStroke(1.5f));
			g2d.setColor(Color.darkGray);

			for(Rectangle syllRect:targetSyllRects) {
				Color grad_top = new Color(150, 150, 150, 0);
				Color grad_btm = new Color(150, 150, 150, 100);
				GradientPaint gp = new GradientPaint(
						new Point(syllRect.x+(2*insetSize), (syllRect.y+syllRect.height)-3*insetSize), grad_top,
						new Point(syllRect.x+(2*insetSize), (syllRect.y+syllRect.height)-insetSize), grad_btm);
				Paint oldPaint = g2d.getPaint();
				g2d.setPaint(gp);
//				g2d.fillRoundRect(sR.x+insetSize, (sR.y+sR.height)-3*insetSize, sR.width-(2*insetSize), 2*insetSize,
//						(int)dArcLengthFill, (int)dArcLengthFill);
				g2d.setPaint(oldPaint);

				RoundRectangle2D.Double syllRect2d =
						new RoundRectangle2D.Double(syllRect.x, syllRect.y, syllRect.width, syllRect.height,
						dArcLengthFill, dArcLengthFill);
				g2d.draw(new Area(syllRect2d));
			}

			for(Rectangle syllRect:actualSyllRects) {
				Color grad_top = new Color(150, 150, 150, 0);
				Color grad_btm = new Color(150, 150, 150, 100);
				GradientPaint gp = new GradientPaint(
						new Point(syllRect.x+(2*insetSize), (syllRect.y+syllRect.height)-3*insetSize), grad_top,
						new Point(syllRect.x+(2*insetSize), (syllRect.y+syllRect.height)-insetSize), grad_btm);
				Paint oldPaint = g2d.getPaint();
				g2d.setPaint(gp);
//				g2d.fillRoundRect(sR.x+insetSize, (sR.y+sR.height)-3*insetSize, sR.width-(2*insetSize), 2*insetSize,
//						(int)dArcLengthFill, (int)dArcLengthFill);
				g2d.setPaint(oldPaint);
				
//				g2d.setColor(Color.RED);
				RoundRectangle2D.Double syllRect2d =
						new RoundRectangle2D.Double(syllRect.x, syllRect.y, syllRect.width, syllRect.height,
						dArcLengthFill, dArcLengthFill);
				g2d.draw(new Area(syllRect2d));

			}
			
			phoneRect.translate(
					pm.getAlignmentLength() * phoneRect.width + groupSpace, 0);

			// paint focus
			if(display.hasFocus()) {
				Tuple<Integer, Integer> groupLocation =
						display.positionToGroupPos(display.getFocusedPosition());
				if(groupLocation.getObj1() == gIdx) {
					Area tArea = targetPhoneAreas[groupLocation.getObj2()];
					Area aArea = actualPhoneAreas[groupLocation.getObj2()];

//					g2d.setColor(Color.YELLOW);
//					g2d.setStroke(new BasicStroke(1.5f));
					
					GlowPathEffect gpe = new GlowPathEffect();
					gpe.setRenderInsideShape(true);
					
					if(tArea != null) {
						if(drawPhoneLock && lockSide == Form.Target)
							gpe.setBrushColor(Color.cyan);
						else
							gpe.setBrushColor(Color.YELLOW);
//						g2d.draw(tArea);
						
						gpe.apply(g2d, tArea, 0, 0);
					}
					if(aArea != null) {
						if(drawPhoneLock && lockSide == Form.Actual)
							gpe.setBrushColor(Color.CYAN);
						else
							gpe.setBrushColor(Color.YELLOW);
//						g2d.draw(aArea);
						
						gpe.apply(g2d, aArea, 0, 0);
					}
				}
			}
		}

		
	}

	/* Helper methods for phone shapes */
	private Area createRectArea(Rectangle phoneRect) {
		Rectangle2D.Double rect2d =
				new Rectangle2D.Double(phoneRect.x, phoneRect.y, phoneRect.width, phoneRect.height);
		return new Area(rect2d);
	}

	private Area createRoundRectArea(Rectangle phoneRect) {
		double dArcLengthFill =
					Math.min(phoneRect.width/1.8, phoneRect.height/1.8);
		double dOffsetFill =
			dArcLengthFill / 2;

		RoundRectangle2D.Double roundPhoneRect2d =
								new RoundRectangle2D.Double(
								phoneRect.x, phoneRect.y,
								phoneRect.width, phoneRect.height,
								dArcLengthFill, dArcLengthFill);
		return new Area(roundPhoneRect2d);
	}

	private Area createAreaForLeftEdge(Rectangle phoneRect) {
		double dArcLengthFill =
					Math.min(phoneRect.width/1.8, phoneRect.height/1.8);
		double dOffsetFill =
			dArcLengthFill / 2;

		RoundRectangle2D.Double roundPhoneRect2d =
								new RoundRectangle2D.Double(
								phoneRect.x, phoneRect.y,
								phoneRect.width, phoneRect.height,
								dArcLengthFill, dArcLengthFill);
		Rectangle2D.Double fillRect =
				new Rectangle2D.Double(
				phoneRect.x+phoneRect.width-dOffsetFill,
				phoneRect.y,
				dOffsetFill, phoneRect.height);
		Area retVal = new Area(roundPhoneRect2d);
		retVal.add(new Area(fillRect));

		return retVal;
	}

	private Area createAreaForRightEdge(Rectangle phoneRect) {
		double dArcLengthFill =
					Math.min(phoneRect.width/1.8, phoneRect.height/1.8);
		double dOffsetFill =
			dArcLengthFill / 2;

		RoundRectangle2D.Double roundPhoneRect2d =
								new RoundRectangle2D.Double(
								phoneRect.x, phoneRect.y,
								phoneRect.width, phoneRect.height,
								dArcLengthFill, dArcLengthFill);
		Rectangle2D.Double fillRect =
				new Rectangle2D.Double(
				phoneRect.x,
				phoneRect.y,
				dOffsetFill, phoneRect.height);

		Area retVal = new Area(roundPhoneRect2d);
		retVal.add(new Area(fillRect));

		return retVal;
	}

	private Area appendAmbisyllabicLeftEdge(Area a, Rectangle phoneRect) {
		Area retVal = new Area(a);

		double dArcLengthFill =
					Math.min(phoneRect.width/1.8, phoneRect.height/1.8);
		double dOffsetFill =
			dArcLengthFill / 2;

		Rectangle2D.Double addRect =
								new Rectangle2D.Double(
								phoneRect.x+phoneRect.width, phoneRect.y,
								phoneRect.width, phoneRect.height);
		RoundRectangle2D.Double removeRect =
				new RoundRectangle2D.Double(
				phoneRect.x+phoneRect.width, phoneRect.y,
				phoneRect.width*2, phoneRect.height,
				dArcLengthFill, dArcLengthFill);
		Area areaToAdd = new Area(addRect);
		areaToAdd.subtract(new Area(removeRect));

		Rectangle2D.Double urFiller =
				new Rectangle2D.Double(
					phoneRect.x+phoneRect.width-dOffsetFill, phoneRect.y,
					dOffsetFill, dOffsetFill);
		Rectangle2D.Double brFiller =
				new Rectangle2D.Double(
					phoneRect.x+phoneRect.width-dOffsetFill, phoneRect.y+phoneRect.height-dOffsetFill,
					dOffsetFill, dOffsetFill);
		retVal.add(new Area(urFiller));
		retVal.add(new Area(brFiller));
		retVal.add(areaToAdd);

		return retVal;
	}

	private Area appendAmbisyllabicRightEdge(Area a, Rectangle phoneRect) {
		Area retVal = new Area(a);
		double dArcLengthFill =
					Math.min(phoneRect.width/1.8, phoneRect.height/1.8);
		double dOffsetFill =
			dArcLengthFill / 2;

		Rectangle2D.Double addRect =
				new Rectangle2D.Double(
				phoneRect.x-phoneRect.width, phoneRect.y,
				phoneRect.width, phoneRect.height);
		RoundRectangle2D.Double removeRect =
				new RoundRectangle2D.Double(
				phoneRect.x-(phoneRect.width*2), phoneRect.y,
				phoneRect.width*2, phoneRect.height,
				dArcLengthFill, dArcLengthFill);
		Area areaToAdd = new Area(addRect);
		areaToAdd.subtract(new Area(removeRect));

		Rectangle2D.Double ulFiller =
				new Rectangle2D.Double(
					phoneRect.x, phoneRect.y,
					dOffsetFill, dOffsetFill);
		Rectangle2D.Double blFiller =
				new Rectangle2D.Double(
					phoneRect.x, phoneRect.y+phoneRect.height-dOffsetFill,
					dOffsetFill, dOffsetFill);

		retVal.add(new Area(ulFiller));
		retVal.add(new Area(blFiller));
		retVal.add(areaToAdd);

//		phoneArea.add(areaToAdd);
		return retVal;
	}

	public Rectangle phoneRectForPosition(int pos) {
		Tuple<Integer, Integer> alignmentPos =
				display.positionToGroupPos(pos);

		int currentX = display.getInsets().left + insetSize;
		int widthPerPhone =
				phoneBoxInsets.right + phoneBoxInsets.left +
				phoneBoxSize.width;

		for(int gIdx = 0; gIdx < alignmentPos.getObj1(); gIdx++) {
			PhoneMap pm = display.getPhoneMapForGroup(gIdx);
			currentX += pm.getAlignmentLength() * widthPerPhone;
			currentX += groupSpace;
		}
		currentX += alignmentPos.getObj2() * widthPerPhone;

		return new Rectangle(currentX, 0, widthPerPhone, phoneBoxSize.height
				+ (2 * insetSize));
	}

	public int locationToAlignmentPosition(Point p) {
		int widthPerPhone =
				phoneBoxInsets.right + phoneBoxInsets.left +
				phoneBoxSize.width;
		int currentX = display.getInsets().left + insetSize;
		int pIdx = 0;
		int currentGrp = 0;
		while(currentX < p.x) {
			if(pIdx >= display.getNumberOfAlignmentPositions()) {
				pIdx = 0;
				break;
			}

			Tuple<Integer, Integer> alignmentPos =
					display.positionToGroupPos(pIdx);
			if(alignmentPos.getObj1() != currentGrp) {
				currentX += groupSpace;
				currentGrp = alignmentPos.getObj1();
			}
			currentX += widthPerPhone;
			pIdx++;
		}

		return pIdx-1;
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension retVal = new Dimension(0, 0);

		int widthPerPhone =
				phoneBoxInsets.right + phoneBoxInsets.left +
				phoneBoxSize.width;
		int height = 2 * (
				phoneBoxInsets.top + phoneBoxInsets.bottom +
				phoneBoxSize.height );

		retVal.width = widthPerPhone * (display.getNumberOfAlignmentPositions())
				+ c.getInsets().right + c.getInsets().left;
		if(display.getNumberOfGroups() > 0)
			retVal.width += (display.getNumberOfGroups() - 1) * groupSpace
					+ 2 * insetSize;
		retVal.height = height
				+ c.getInsets().top + c.getInsets().bottom
				+ /*padding*/ 2 * insetSize;

		return retVal;
	}

	/**
	 * Key listener for cmd/ctrl key press/release
	 */
	private class ChangeKeyListener implements KeyListener {

		Integer[][] origAlignment = null;

		@Override
		public void keyTyped(KeyEvent ke) {
		}

		@Override
		public void keyPressed(KeyEvent ke) {
			if(ke.getKeyCode() == KeyEvent.VK_ALT) {
				// get original alignment
				// for group
				Tuple<Integer, Integer> gPos =
						display.positionToGroupPos(display.getFocusedPosition());
				PhoneMap pm = display.getPhoneMapForGroup(gPos.getObj1());

				origAlignment = new Integer[2][];
				origAlignment[0] = Arrays.copyOf(pm.getTopAlignment(), pm.getAlignmentLength());
				origAlignment[1] = Arrays.copyOf(pm.getBottomAlignment(), pm.getAlignmentLength());

				drawPhoneLock = true;
				display.repaint();
			}
		}

		@Override
		public void keyReleased(KeyEvent ke) {
			if(ke.getKeyCode() == KeyEvent.VK_ALT) {
				// get original alignment
				// for group
				Tuple<Integer, Integer> gPos =
						display.positionToGroupPos(display.getFocusedPosition());
				PhoneMap pm = display.getPhoneMapForGroup(gPos.getObj1());

				drawPhoneLock = false;
				display.repaint();

				Integer[][] newAlignment = new Integer[2][];
				newAlignment[0] = Arrays.copyOf(pm.getTopAlignment(), pm.getAlignmentLength());
				newAlignment[1] = Arrays.copyOf(pm.getBottomAlignment(), pm.getAlignmentLength());

				display.fireAlignmentChange(
						new PhoneMapDisplay.AlignmentChangeData(gPos.getObj1(), origAlignment)
						, new PhoneMapDisplay.AlignmentChangeData(gPos.getObj1(), newAlignment));
				origAlignment = null;
			}
		}

	}

	/**
	 * Mouse handler for modifying alignment via drags.
	 */
	private class AlignmentMouseHandler extends MouseInputAdapter {

		Integer[][] origAlignment = null;

		@Override
		public void mouseClicked(MouseEvent me) {

		}

		@Override
		public void mousePressed(MouseEvent me) {
//					System.out.println(me);
			display.requestFocusInWindow();
			int pIdx = locationToAlignmentPosition(me.getPoint());
			if(pIdx >= 0) {
				// get original alignment
				// for group
				Tuple<Integer, Integer> gPos =
						display.positionToGroupPos(pIdx);
				PhoneMap pm = display.getPhoneMapForGroup(gPos.getObj1());

				origAlignment = new Integer[2][];
				origAlignment[0] = Arrays.copyOf(pm.getTopAlignment(), pm.getAlignmentLength());
				origAlignment[1] = Arrays.copyOf(pm.getBottomAlignment(), pm.getAlignmentLength());

				drawPhoneLock = true;
				if(me.getPoint().getY() > (phoneBoxSize.height
						+ (2 * insetSize)) ) {
					lockSide = Form.Actual;
				} else {
					lockSide = Form.Target;
				}
				isDragging = true;
				Rectangle phoneRect = phoneRectForPosition(pIdx);
				dragLeftEdge = phoneRect.x;
				dragRightEdge = phoneRect.x + phoneRect.width;
				display.setFocusedPosition(pIdx);
				display.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
//					int pIdx = locationToPhoneIndex(me.getPoint());
//					if(pIdx >= 0) {
//						display.setFocusedPhone(pIdx);
//						if(me.isPopupTrigger()) {
//							JPopupMenu menu = getContextMenu(pIdx);
//							menu.show(display, me.getPoint().x, me.getPoint().y);
//						}
//					}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if(drawPhoneLock) {
				drawPhoneLock = false;
				display.repaint();
				display.setCursor(Cursor.getDefaultCursor());

				// get original alignment
				// for group
				Tuple<Integer, Integer> gPos =
						display.positionToGroupPos(display.getFocusedPosition());
				PhoneMap pm = display.getPhoneMapForGroup(gPos.getObj1());

				Integer[][] newAlignment = new Integer[2][];
				newAlignment[0] = Arrays.copyOf(pm.getTopAlignment(), pm.getAlignmentLength());
				newAlignment[1] = Arrays.copyOf(pm.getBottomAlignment(), pm.getAlignmentLength());

				display.fireAlignmentChange(
						new PhoneMapDisplay.AlignmentChangeData(gPos.getObj1(), origAlignment)
						, new PhoneMapDisplay.AlignmentChangeData(gPos.getObj1(), newAlignment));
				origAlignment = null;
			}
		}

		@Override
		public void mouseDragged(MouseEvent me) {
			if(isDragging) {
				if(me.getPoint().getX() > dragRightEdge) {
//							System.out.println("Hit right edge");
					// calculate new right edge
//							int pIdx = locationToAlignmentPosition(me.getPoint());
//							if(pIdx > 0) {
//								int mutateIdx = pIdx-1;
						display.movePhoneRight(
								display.positionToGroupPos(display.getFocusedPosition()), lockSide);
						Rectangle newPRect = phoneRectForPosition(
								display.getFocusedPosition());
						dragLeftEdge = newPRect.x;
						dragRightEdge = newPRect.x + newPRect.width;
//							}
				}
				if(me.getPoint().getX() < dragLeftEdge) {
					display.movePhoneLeft(
							display.positionToGroupPos(display.getFocusedPosition()), lockSide);
					Rectangle newPRect = phoneRectForPosition(
							display.getFocusedPosition());
					dragLeftEdge = newPRect.x;
					dragRightEdge = newPRect.x + newPRect.width;
				}
			}
		}
	}
}
