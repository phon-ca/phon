package ca.phon.ui.decorations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

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

	static {
		UIManager.put("titledpanel.foreground", Color.white);
		UIManager.put("titledpanel.background", Color.gray);
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

		titlePanel = new JPanel(new GridBagLayout()) {
			@Override
			public void paintComponent(Graphics g) {
				final Graphics2D g2 = (Graphics2D)g;

				final GradientPaint gp = new GradientPaint(0.0f, 0.0f, Color.decode("#bbbbbb"),
						0.0f, (float)getHeight(), Color.decode("#7c7c7c"));
				g2.setPaint(gp);
				g2.fillRect(0, 0, getWidth(), getHeight());
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
