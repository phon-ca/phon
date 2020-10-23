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
package ca.phon.ui.decorations;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;

import ca.phon.ui.painter.Painter;

/**
 * Panel with a title, content section, and optional left/right decorations.
 *
 */
public class TitledPanel extends JPanel {

	private static final long serialVersionUID = 2098225083577027792L;

	private JPanel titlePanel;

	private JLabel titleLabel;
	private GridBagConstraints gbcTitle;

	private Component leftDecoration;
	private GridBagConstraints gbcLeft;

	private Component rightDecoration;
	private GridBagConstraints gbcRight;

	private Container contentContainer;
	
	private final Painter<JPanel> defaultTitlePanelPainter = new Painter<JPanel>() {
		
		@Override
		public void paint(JPanel obj, Graphics2D g2, Rectangle2D bounds) {
			final GradientPaint gp = new GradientPaint(0.0f, 0.0f, getTopColor(),
					0.0f, (float)bounds.getHeight(), getBottomColor());
			g2.setPaint(gp);
			g2.fill(bounds);
		}
		
	};
	private Painter<JPanel> titlePanelPainter = defaultTitlePanelPainter;
	
	private Color topColor;
	private Color bottomColor;

	static {
		UIManager.put("titledpanel.foreground", Color.white);
		UIManager.put("titledpanel.background", Color.gray);
		UIManager.put("titledpanel.backgroundTop",  Color.decode("#bbbbbb"));
		UIManager.put("titledpanel.backgroundBottom", Color.decode("#8c8c8c"));
	}

	public TitledPanel() {
		this("", null);
	}

	public TitledPanel(String title) {
		this(title, null);
	}

	public TitledPanel(String title, Component content) {
		super();

		init();

		setTitle(title);
		if(content != null)
			getContentContainer().add(content, BorderLayout.CENTER);
	}

	@SuppressWarnings("serial")
	private void init() {
		super.setLayout(new BorderLayout(0, 0));
		
		topColor = UIManager.getColor("titledpanel.backgroundTop");
		bottomColor = UIManager.getColor("titledpanel.backgroundBottom");

		titlePanel = new JPanel(new GridBagLayout()) {
			@Override
			public void paintComponent(Graphics g) {
				final Graphics2D g2 = (Graphics2D)g;
				final Rectangle2D rect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
				getTitlePanelPainter().paint(titlePanel, g2, rect);
			}
			
		};

		gbcLeft = new GridBagConstraints();
		gbcLeft.gridx = 0;
		gbcLeft.gridy = 0;
		gbcLeft.insets = new Insets(0, 5, 0, 5);
		gbcLeft.anchor = GridBagConstraints.WEST;
		gbcLeft.fill = GridBagConstraints.NONE;
		gbcLeft.gridwidth = 1;
		gbcLeft.gridheight = 1;
		gbcLeft.weightx = 0.0;
		gbcLeft.weighty = 0.0;

		gbcTitle = (GridBagConstraints)gbcLeft.clone();
		gbcTitle.gridx = 1;
		gbcTitle.weightx = 1.0;
		gbcTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcTitle.insets = new Insets(5, 2, 5, 2);

		gbcRight = (GridBagConstraints)gbcLeft.clone();
		gbcRight.gridx = 2;
		gbcRight.insets = new Insets(0, 5, 0, 5);
		gbcRight.anchor = GridBagConstraints.EAST;

		titleLabel = new JLabel(getTitle());
		titleLabel.setForeground(UIManager.getColor("titledpanel.foreground"));
		titleLabel.setBackground(UIManager.getColor("titledpanel.background"));
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

		titlePanel.add(titleLabel, gbcTitle);
		titlePanel.setOpaque(true);
		titlePanel.setBackground(UIManager.getColor("titledpanel.background"));

		add(titlePanel, BorderLayout.NORTH);

		contentContainer = new JPanel(new BorderLayout());
		add(contentContainer, BorderLayout.CENTER);

		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	}

	@Override
	public void setLayout(LayoutManager layout) {
		// don't allow setting other layouts
	}

	public Container getContentContainer() {
		return this.contentContainer;
	}
	
	public void setContentContainer(Container container) {
		super.remove(this.contentContainer);
		this.contentContainer = container;
		add(contentContainer, BorderLayout.CENTER);
		revalidate();
	}

	public Painter<JPanel> getTitlePanelPainter() {
		return titlePanelPainter;
	}

	public void setTitlePanelPainter(Painter<JPanel> titlePanelPainter) {
		this.titlePanelPainter = titlePanelPainter;
	}

	public Color getTopColor() {
		return topColor;
	}

	public void setTopColor(Color topColor) {
		this.topColor = topColor;
	}

	public Color getBottomColor() {
		return bottomColor;
	}

	public void setBottomColor(Color bottomColor) {
		this.bottomColor = bottomColor;
	}

	public String getTitle() {
		return (this.titleLabel != null ? this.titleLabel.getText() : "");
	}

	public void setTitle(String title) {
		if(titleLabel != null)
			titleLabel.setText(title);
	}

	public void setIcon(Icon icon) {
		titleLabel.setIcon(icon);
	}

	public Icon getIcon() {
		return titleLabel.getIcon();
	}

	public Component getLeftDecoration() {
		return leftDecoration;
	}

	public void setLeftDecoration(Component leftComponent) {
		if(this.leftDecoration != null) {
			titlePanel.remove(this.leftDecoration);
		}

		this.leftDecoration = leftComponent;

		if(titlePanel != null && leftComponent != null) {
			titlePanel.add(leftComponent, gbcLeft);
		}
	}

	public Component getRightDecoration() {
		return rightDecoration;
	}

	public void setRightDecoration(Component rightComponent) {
		if(this.rightDecoration != null) {
			titlePanel.remove(this.rightDecoration);
		}

		this.rightDecoration = rightComponent;

		if(titlePanel != null && rightComponent != null) {
			titlePanel.add(rightComponent, gbcRight);
		}
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	public void setTitleLabel(JLabel titleLabel) {
		this.titleLabel = titleLabel;
	}
	
	

}
