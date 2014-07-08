/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.phon.ui.ipa;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;

/**
 *
 */
public class DefaultSyllabificationDisplayUI extends SyllabificationDisplayUI {

	/* Action IDs */
	private static final String FOCUS_PREVIOUS = "_FOCUS_PREV_PHONE_";
	private static final String FOCUS_NEXT = "_FOCUS_NEXT_PHONE_";
	private static final String TOGGLE_HIATUS = "_TOGGLE_HIATUS_";
	private static final String SET_SCTYPE_PREFIX = "_SET_SCTYPE_";
	private static final String BACKSPACE = "_backspace_";
	private static final String RESET_SYLLABIFICATION = "_RESET_SYLLABIFIACTION_";

	private static final int insetSize = 2;
	private Insets phoneBoxInsets = new Insets(insetSize, insetSize, insetSize,
			insetSize);
	private Dimension phoneBoxSize = new Dimension(18, 20);

	/** Display we are installed on */
	private SyllabificationDisplay display;

	public DefaultSyllabificationDisplayUI(SyllabificationDisplay display) {
		super();

		this.display = display;
	}

	@Override
	public void installUI(JComponent c) {
		display = (SyllabificationDisplay) c;

		setupActions();
		installMouseListener();

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
		display.setRequestFocusEnabled(true);
	}

	/** Setup actions for component */
	private void setupActions() {
		ActionMap actionMap = display.getActionMap();
		InputMap inputMap = display.getInputMap(JComponent.WHEN_FOCUSED);

		PhonUIAction focusNextAct = new PhonUIAction(this, "focusNextPhone");
		actionMap.put(FOCUS_NEXT, focusNextAct);
		KeyStroke focusNextKs = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		inputMap.put(focusNextKs, FOCUS_NEXT);

		PhonUIAction focusPrevAct = new PhonUIAction(this, "focusPrevPhone");
		actionMap.put(FOCUS_PREVIOUS, focusPrevAct);
		KeyStroke focusPrevKs = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		inputMap.put(focusPrevKs, FOCUS_PREVIOUS);

		PhonUIAction backSpaceAct = new PhonUIAction(this, "onBackspace",
				SyllableConstituentType.UNKNOWN);
		actionMap.put(BACKSPACE, backSpaceAct);
		KeyStroke delKs = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		inputMap.put(delKs, BACKSPACE);

		PhonUIAction toggleHiatusAct = new PhonUIAction(this, "toggleHiatus");
		actionMap.put(TOGGLE_HIATUS, toggleHiatusAct);
		KeyStroke toggleHiatusKs1 = KeyStroke.getKeyStroke('h');
		KeyStroke toggleHiatusKs2 = KeyStroke.getKeyStroke('H');
		inputMap.put(toggleHiatusKs1, TOGGLE_HIATUS);
		inputMap.put(toggleHiatusKs2, TOGGLE_HIATUS);

		// PhonUIAction resyllabifyAct =
		// new PhonUIAction(this, "resyllabify",
		// Syllabifier.getDefaultLanguage());
		// actionMap.put(RESET_SYLLABIFICATION, resyllabifyAct);
		// KeyStroke resyllabifyKs =
		// KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK);
		// inputMap.put(resyllabifyKs, RESET_SYLLABIFICATION);

		// sc type identifiers
		for (int scTypeIdx = 0; scTypeIdx < SyllableConstituentType.values().length; scTypeIdx++) {

			SyllableConstituentType scType = SyllableConstituentType.values()[scTypeIdx];
			String scChar1 = (scType.getIdChar() + "").toUpperCase();
			String scChar2 = (scType.getIdChar() + "").toLowerCase();

			String scTypeActID = SET_SCTYPE_PREFIX + "_"
					+ scType.getIdentifier().toUpperCase() + "_";
			PhonUIAction scTypeAct = new PhonUIAction(this, "setScType", scType);
			actionMap.put(scTypeActID, scTypeAct);

			KeyStroke scKs1 = KeyStroke.getKeyStroke(scChar1.charAt(0));
			inputMap.put(scKs1, scTypeActID);

			KeyStroke scKs2 = KeyStroke.getKeyStroke(scChar2.charAt(0));
			inputMap.put(scKs2, scTypeActID);

		}

		String removeSyllabificationID = SET_SCTYPE_PREFIX + "_"
				+ SyllableConstituentType.UNKNOWN.getIdentifier().toUpperCase()
				+ "_";
		PhonUIAction removeSyllabificationAct = new PhonUIAction(this,
				"setScType", SyllableConstituentType.UNKNOWN);
		actionMap.put(removeSyllabificationID, removeSyllabificationAct);
		KeyStroke removeSyllabificationKs = KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0);
		inputMap.put(removeSyllabificationKs, removeSyllabificationID);
	}

	/** UI Actions */
	public void onBackspace(PhonActionEvent pae) {
		int pIdx = display.getFocusedPhone();
		display.setSyllabificationAtIndex(pIdx,
				(SyllableConstituentType) pae.getData());
		int prevFocus = pIdx - 1;
		if (prevFocus >= 0) {
			display.setFocusedPhone(prevFocus);
		}
	}

	public void focusNextPhone(PhonActionEvent pae) {
		int currentFocus = display.getFocusedPhone();
		int nextFocus = currentFocus + 1;

		if (nextFocus < display.getNumberOfDisplayedPhones()) {
			display.setFocusedPhone(nextFocus);
		}
	}

	public void focusPrevPhone(PhonActionEvent pae) {
		int currentFocus = display.getFocusedPhone();
		int prevFocus = currentFocus - 1;
		if (prevFocus >= 0) {
			display.setFocusedPhone(prevFocus);
		}
	}

	public void toggleHiatus(PhonActionEvent pae) {
		int pIdx = display.getFocusedPhone();
		display.toggleHiatus(pIdx);
	}

	public void setScType(PhonActionEvent pae) {
		int pIdx = display.getFocusedPhone();
		display.setSyllabificationAtIndex(pIdx,
				(SyllableConstituentType) pae.getData());
		display.setFocusedPhone(pIdx + 1);
	}

	public void menuSetScType(PhonActionEvent pae) {
		int pIdx = display.getFocusedPhone();
		display.setSyllabificationAtIndex(pIdx,
				(SyllableConstituentType) pae.getData());
	}

	public void resyllabify(PhonActionEvent pae) {
		String syllabifierName = pae.getData().toString();
		display.resyllabifiy(syllabifierName);
	}

	/**
	 * Get the context menu for the specified phone
	 */
	private JPopupMenu getContextMenu(int pIdx) {
		final JPopupMenu retVal = new JPopupMenu();
//		final IPATranscript ipa = display.getContextMenuTranscript();

		IPAElement phone = display.getPhoneAtIndex(pIdx);
		if (phone != null) {
			for (int i = 0; i < SyllableConstituentType.values().length; i++) {
				SyllableConstituentType scType = SyllableConstituentType
						.values()[i];

				String itemText = "<html>";
				if (scType == SyllableConstituentType.ONSET) {
					itemText += "<u><b>O</b></u>nset";
				} else if (scType == SyllableConstituentType.NUCLEUS) {
					itemText += "<u><b>N</b></u>ucleus";
				} else if (scType == SyllableConstituentType.CODA) {
					itemText += "<u><b>C</b></u>oda";
				} else if (scType == SyllableConstituentType.LEFTAPPENDIX) {
					itemText += "<u><b>L</b></u>eft Appendix";
				} else if (scType == SyllableConstituentType.RIGHTAPPENDIX) {
					itemText += "<u><b>R</b></u>ight Appendix";
				} else if (scType == SyllableConstituentType.OEHS) {
					itemText += "O<u><b>E</b></u>HS";
				} else if (scType == SyllableConstituentType.AMBISYLLABIC) {
					itemText += "<u><b>A</b></u>mbisyllabic";
				} else if (scType == SyllableConstituentType.UNKNOWN) {
					itemText += "<u><b>U</b></u>nknown";
				} else {
					continue;
				}
				itemText += "</html>";

				final JMenuItem constituentItem = new JMenuItem(itemText);
				PhonUIAction constituentAction = new PhonUIAction(this,
						"menuSetScType", scType);
				constituentAction.putValue(Action.NAME, itemText);
				constituentItem.setAction(constituentAction);

				retVal.add(constituentItem);
			}

			if (pIdx > 0
					&& phone.getScType() == SyllableConstituentType.NUCLEUS) {
				IPAElement prevPhone = display.getPhoneAtIndex(pIdx - 1);

				if (prevPhone.getScType() == SyllableConstituentType.NUCLEUS) {
					String itemText = "<html>Toggle <u><b>H</b></u>iatus";
					JMenuItem item = new JMenuItem();
					PhonUIAction toggleHiatusAct = new PhonUIAction(this,
							"toggleHiatus");
					toggleHiatusAct.putValue(Action.NAME, itemText);
					item.setAction(toggleHiatusAct);

					retVal.addSeparator();
					retVal.add(item);
				}
			}
		}

		return retVal;
	}

	/** Install mouse listener for component */
	private void installMouseListener() {
		MouseListener mouseListener = new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent me) {

			}

			@Override
			public void mousePressed(MouseEvent me) {
				// System.out.println(me);
				display.requestFocusInWindow();
				int pIdx = locationToPhoneIndex(me.getPoint());
				if (pIdx >= 0) {
					display.setFocusedPhone(pIdx);
					if (me.isPopupTrigger()) {
						JPopupMenu menu = getContextMenu(pIdx);
						menu.show(display, me.getPoint().x, me.getPoint().y);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					JPopupMenu menu = getContextMenu(display.getFocusedPhone());
					menu.show(display, me.getPoint().x, me.getPoint().y);
				}
			}

		};
		display.addMouseListener(mouseListener);
	}

	/**
	 * Paint phones
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g;

		// Enable antialiasing for shapes
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// Enable antialiasing for text
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Dimension size = display.getSize();
		if (display.isOpaque()) {
			g2d.setColor(display.getBackground());
			g2d.fillRect(0, 0, size.width, size.height);
		}

		// setup phone rect
		int pX = c.getInsets().left + insetSize;
		int pY = c.getInsets().top + insetSize;
		int pW = phoneBoxInsets.left + phoneBoxInsets.right
				+ phoneBoxSize.width;
		int pH = phoneBoxInsets.top + phoneBoxInsets.bottom
				+ phoneBoxSize.height;

		Font displayFont = display.getFont();
		displayFont = new Font("Charis SIL Compact", Font.PLAIN, 12);

		Rectangle phoneRect = new Rectangle(pX, pY, pW, pH);

		double dArcLengthFill = Math.min(phoneRect.width / 1.8,
				phoneRect.height / 1.8);
		double dOffsetFill = dArcLengthFill / 2;

		// draw syllable background first
		List<Area> syllAreas = new ArrayList<Area>();
		List<Area> phoneAreas = new ArrayList<Area>();
		int syllCurrentX = phoneRect.x;
		IPATranscript grpPhones = 
				(display.getTranscript() == null ? new IPATranscript() : display.getTranscript());
		List<IPATranscript> syllables = grpPhones.syllables();

		for (IPATranscript s : syllables) {
			IPATranscript syllablePhones = s.removePunctuation();

			int sX = syllCurrentX;
			int sY = phoneRect.y;
			int sW = syllablePhones.length() * phoneRect.width;
			int sH = phoneRect.height;

			syllCurrentX += sW;

			Rectangle sR = new Rectangle(sX, sY, sW, sH);

			// outer area
			RoundRectangle2D.Double rrect2dFill = new RoundRectangle2D.Double(
					sR.x, sR.y, sR.width, sR.height, dArcLengthFill,
					dArcLengthFill);
			Area fillArea = new Area(rrect2dFill);

			syllAreas.add(fillArea);

			Rectangle savedPhoneRect = new Rectangle(phoneRect);
			for (int pIdx = 0; pIdx < syllablePhones.length(); pIdx++) {
				IPAElement p = syllablePhones.elementAt(pIdx);

				// calculate fill area
				Rectangle2D.Double phoneRect2d = new Rectangle2D.Double(
						phoneRect.x, phoneRect.y, phoneRect.width,
						phoneRect.height);
				Area phoneArea = new Area(phoneRect2d);

				if (pIdx == 0) {
					RoundRectangle2D.Double roundPhoneRect2d = new RoundRectangle2D.Double(
							phoneRect.x, phoneRect.y, phoneRect.width,
							phoneRect.height, dArcLengthFill, dArcLengthFill);
					Rectangle2D.Double fillRect = new Rectangle2D.Double(
							phoneRect.x + phoneRect.width - dOffsetFill,
							phoneRect.y, dOffsetFill, phoneRect.height);
					phoneArea = new Area(roundPhoneRect2d);

					if (syllablePhones.length() > 1)
						phoneArea.add(new Area(fillRect));

				} else if (pIdx == syllablePhones.length() - 1) {
					RoundRectangle2D.Double roundPhoneRect2d = new RoundRectangle2D.Double(
							phoneRect.x, phoneRect.y, phoneRect.width,
							phoneRect.height, dArcLengthFill, dArcLengthFill);
					Rectangle2D.Double fillRect = new Rectangle2D.Double(
							phoneRect.x, phoneRect.y, dOffsetFill,
							phoneRect.height);

					phoneArea = new Area(roundPhoneRect2d);
					phoneArea.add(new Area(fillRect));
				}

				Color grad_top = p.getScType().getColor().brighter();
				Color grad_btm = p.getScType().getColor().darker();
				GradientPaint gp = new GradientPaint(new Point(phoneRect.x,
						phoneRect.y), grad_top, new Point(phoneRect.x,
						phoneRect.y + phoneRect.height), grad_btm);
				Paint oldPaiont = g2d.getPaint();
				g2d.setPaint(gp);
				g2d.fill(phoneArea);
				g2d.setPaint(oldPaiont);

				phoneAreas.add(phoneArea);

				g2d.setColor(Color.gray);
				g2d.setStroke(new BasicStroke(0.5f));
				g2d.draw(phoneArea);

				phoneRect.translate(phoneRect.width, 0);
			}

			// draw bottom syllable highlight
			Color grad_top = new Color(150, 150, 150, 0);
			Color grad_btm = new Color(150, 150, 150, 100);
			GradientPaint gp = new GradientPaint(new Point(sR.x
					+ (2 * insetSize), (sR.y + sR.height) - 3 * insetSize),
					grad_top, new Point(sR.x + (2 * insetSize),
							(sR.y + sR.height) - insetSize), grad_btm);
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(gp);

			g2d.setPaint(oldPaint);

			phoneRect = new Rectangle(savedPhoneRect);
			for (int pIdx = 0; pIdx < syllablePhones.length(); pIdx++) {
				IPAElement p = syllablePhones.elementAt(pIdx);

				// draw phone string
				Rectangle pBox = new Rectangle(phoneRect.x
						+ phoneBoxInsets.left,
						phoneRect.y + phoneBoxInsets.top, phoneBoxSize.width,
						phoneBoxSize.height);

				Font f = displayFont;
				FontMetrics fm = g.getFontMetrics(f);
				Rectangle2D stringBounds = fm.getStringBounds(p.getText(), g);
				while ((stringBounds.getWidth() > pBox.width)
						|| (stringBounds.getHeight() > pBox.height)) {
					f = f.deriveFont(f.getSize2D() - 0.2f);
					fm = g.getFontMetrics(f);
					stringBounds = fm.getStringBounds(p.getText(), g);
				}

				float phoneX = pBox.x + (pBox.width / 2.0f)
						- ((float) stringBounds.getWidth() / 2.0f);
				float phoneY = (float) pBox.y + (float) pBox.height
						- fm.getDescent();

				g2d.setFont(f);
				g2d.setColor(c.getForeground());
				g2d.drawString(p.getText(), phoneX, phoneY);

				phoneRect.translate(phoneRect.width, 0);
			}

			// draw top highlight
			grad_top = new Color(255, 255, 255, 80);
			grad_btm = new Color(255, 255, 255, 0);
			gp = new GradientPaint(new Point(sR.x, sR.y), grad_top, new Point(
					sR.x, sR.y + 3 * insetSize), grad_btm);
			oldPaint = g2d.getPaint();
			g2d.setPaint(gp);
			g2d.fillRoundRect(sR.x, sR.y, sR.width, 3 * insetSize,
					(int) dArcLengthFill, (int) dArcLengthFill);
			g2d.setPaint(oldPaint);
		}

		// draw syllable outlines
		for (Area syllArea : syllAreas) {
			g2d.setStroke(new BasicStroke(1.5f));
			g2d.setColor(Color.darkGray);
			g2d.draw(syllArea);
		}

		if (display.hasFocus() && display.getNumberOfDisplayedPhones() > 0) {

			Area phoneArea = phoneAreas.get(display.getFocusedPhone());

			GlowPathEffect gpe = new GlowPathEffect();
			gpe.setRenderInsideShape(true);
			gpe.setBrushColor(Color.yellow);
			gpe.apply(g2d, phoneArea, 0, 0);

		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension retVal = new Dimension(0, 0);

		int widthPerPhone = phoneBoxInsets.right + phoneBoxInsets.left
				+ phoneBoxSize.width;
		int height = phoneBoxInsets.top + phoneBoxInsets.bottom
				+ phoneBoxSize.height;

		retVal.width = widthPerPhone * (display.getNumberOfDisplayedPhones())
				+ c.getInsets().right + c.getInsets().left;

		retVal.height = height + c.getInsets().top + c.getInsets().bottom
				+ /* padding */2 * insetSize;

		return retVal;
	}

	private Dimension getPhoneSize() {
		int widthPerPhone = phoneBoxInsets.right + phoneBoxInsets.left
				+ phoneBoxSize.width;
		int height = phoneBoxInsets.top + phoneBoxInsets.bottom
				+ phoneBoxSize.height;
		return new Dimension(widthPerPhone, height);
	}

	private Shape getSyllableStartShape() {
		final Dimension phoneSize = getPhoneSize();
		final Rectangle rect = new Rectangle(0, 0, phoneSize.width,
				phoneSize.height);

		return rect;
	}

	private Shape getSyllableMedianShape() {
		final Dimension phoneSize = getPhoneSize();
		final Rectangle rect = new Rectangle(0, 0, phoneSize.width,
				phoneSize.height);

		return rect;
	}

	private Shape getSyllableFinalShape() {
		final Dimension phoneSize = getPhoneSize();
		final Rectangle rect = new Rectangle(0, 0, phoneSize.width,
				phoneSize.height);

		return rect;
	}

	public int locationToPhoneIndex(Point p) {
		int widthPerPhone = phoneBoxInsets.right + phoneBoxInsets.left
				+ phoneBoxSize.width;
		int currentX = display.getInsets().left + insetSize;
		int pIdx = 0;

		IPATranscript grpPhones = display.getTranscript().removePunctuation();
		int grpSize = widthPerPhone * grpPhones.length();

		if ((currentX + grpSize) >= p.x) {
			for (int grpPIdx = 0; grpPIdx < grpPhones.length(); grpPIdx++) {
				currentX += widthPerPhone;
				if (currentX >= p.x) {
					break;
				}
				pIdx++;
			}

		}

		return pIdx;
	}

}
