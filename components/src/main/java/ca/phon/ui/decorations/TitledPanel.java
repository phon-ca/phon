package ca.phon.ui.decorations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
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
	
	private Component leftDecoration;
	
	private Component rightDecoration;
	
	private Container contentContainer;
	
	static {
		UIManager.put("titledpanel.foreground", Color.white);
		UIManager.put("titledpanel.background", Color.gray);
	}
	
	public TitledPanel(String title, Component content) {
		super();
		
		init();
	
		setTitle(title);
		getContentContainer().add(content, BorderLayout.CENTER);
	}
	
	private void init() {
		super.setLayout(new BorderLayout(0, 0));
		
		titlePanel = new JPanel(new BorderLayout());
		
		titleLabel = new JLabel(getTitle());
		titleLabel.setForeground(UIManager.getColor("titledpanel.foreground"));
		titleLabel.setBackground(UIManager.getColor("titledpanel.background"));
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

		titlePanel.add(titleLabel, BorderLayout.CENTER);
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
			titlePanel.add(leftComponent, BorderLayout.WEST);
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
			titlePanel.add(rightComponent, BorderLayout.EAST);
		}
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	public void setTitleLabel(JLabel titleLabel) {
		this.titleLabel = titleLabel;
	}
	
}
