/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.text;

import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import ca.phon.formatter.FormatterUtil;

public class TextCompleter implements DocumentListener, FocusListener, KeyListener, ListSelectionListener {

	private TextCompleterModel<?> model;

	private JTextComponent textComponent;

	private JWindow completionWindow;

	private JList<String> completionList;

	private JScrollPane listScroller;

	private List<String> completions = null;

	private DefaultListModel<String> completionListModel;

	private boolean useDataForCompletion = false;

	public TextCompleter() {
		this(new DefaultTextCompleterModel());
	}

	public TextCompleter(TextCompleterModel<?> model) {
		this.model = model;

		completionListModel = new DefaultListModel<>();
		completionList = new JList<>(completionListModel);
		completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		completionList.addListSelectionListener(this);
		completionList.setAutoscrolls(true);

		listScroller = new JScrollPane(completionList);

		completionWindow = new JWindow();
		completionWindow.add(listScroller);
	}

	public boolean isUseDataForCompletion() {
		return this.useDataForCompletion;
	}

	public void setUseDataForCompletion(boolean useDataForCompletion) {
		this.useDataForCompletion = useDataForCompletion;
	}

	public void install(JTextComponent textComponent) {
		this.textComponent = textComponent;
		textComponent.getDocument().addDocumentListener(this);
		textComponent.addFocusListener(this);
		textComponent.addKeyListener(this);
	}

	public void uninstall() {
		textComponent.getDocument().removeDocumentListener(this);
		textComponent.removeFocusListener(this);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		final int caretLoc = getTextComponent().getCaretPosition();
		final String text = getTextComponent().getText();
		if(caretLoc != text.length() - 1) return;
		buildAndShowPopup();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		final int caretLoc = getTextComponent().getCaretPosition();
		final String text = getTextComponent().getText();
		if(caretLoc != text.length() + 1) return;
		buildAndShowPopup();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {

	}

	public JWindow getPopup() {
		return completionWindow;
	}

	public JList<String> getCompletionLiist() {
		return this.completionList;
	}

	public JTextComponent getTextComponent() {
		return this.textComponent;
	}

	protected void buildPopup() {
		final String text = textComponent.getText();

		completionListModel.clear();
		completions = model.getCompletions(text);
		for(String completion:completions) {
			final String eleText = model.getDisplayText(completion);
			completionListModel.addElement(eleText);
		}
	}

	protected void showPopup() {
		if(completionListModel.size() == 0) {
			completionWindow.setVisible(false);
			return;
		}
		try {
			Point pos = textComponent.getLocationOnScreen();
			int popX = pos.x;
			int popY = pos.y + textComponent.getHeight();

			int numRowsVisible = Math.min(completionListModel.size()+1, 10);
			completionList.setVisibleRowCount(numRowsVisible);
			completionList.setPreferredSize(
					new Dimension(textComponent.getWidth(), (int)completionList.getPreferredSize().getHeight()));

			completionWindow.setLocation(popX, popY);
			completionWindow.pack();

			final Rectangle bounds = completionWindow.getBounds();
			if(bounds.width > textComponent.getWidth()) {
				bounds.width = textComponent.getWidth();
				completionWindow.setBounds(bounds);
			}

			completionWindow.setVisible(true);
		} catch (IllegalComponentStateException e) {
			// happens when component is not yet visible, ignore
		}
	}

	protected void hidePopup() {
		completionWindow.setVisible(false);
	}

	private void buildAndShowPopup() {
		if(textComponent.getText().length() < 1)
			return;
		buildPopup();
		showPopup();
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {
		hidePopup();
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE
				|| e.getKeyCode() == KeyEvent.VK_ENTER) {
			hidePopup();
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN
				|| e.getKeyCode() == KeyEvent.VK_UP) {
			int dir = (e.getKeyCode() == KeyEvent.VK_DOWN ? 1 : -1);
			if(completionWindow.isVisible()) {
				int selectedIdx = completionList.getSelectedIndex();
				selectedIdx += dir;
				if(selectedIdx >= 0 && selectedIdx < completions.size()) {
					completionList.setSelectedIndex(selectedIdx);
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) return;
		final int selectedIdx = completionList.getSelectedIndex();
		if(selectedIdx >= 0 && selectedIdx < completions.size()) {
			completionList.ensureIndexIsVisible(selectedIdx);
			final String completion =
					(isUseDataForCompletion() ? FormatterUtil.format(model.getData(completions.get(selectedIdx))) : completions.get(selectedIdx));
			String text = getTextComponent().getText();
			final String replacementText = model.completeText(text, completion);

			SwingUtilities.invokeLater( () -> { getTextComponent().setText(replacementText); } );
		}
	}

}
