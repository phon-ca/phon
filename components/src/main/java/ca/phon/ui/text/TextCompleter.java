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
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import ca.phon.formatter.*;

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
	
	public TextCompleterModel<?> getModel() {
		return this.model;
	}
	
	public void setModel(TextCompleterModel<?> model) {
		this.model = model;
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

	public List<String> getCompletions() {
		return this.completions;
	}
	
	public JList<String> getCompletionLiist() {
		return this.completionList;
	}

	public JTextComponent getTextComponent() {
		return this.textComponent;
	}

	protected void buildPopup() {
		String text = textComponent.getText();

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
