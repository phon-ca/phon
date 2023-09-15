package ca.phon.app.theme;

import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditorUIProps;
import ca.phon.ui.CommonModuleFrame;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class ColorPaletteDesigner extends JPanel {

	private UndoManager undoManager;

	private UndoableEditSupport undoSupport;

    private final static String[] DEFAULT_UI_MANAGER_KEYS = new String[]{
			"Button.background",
			"Button.darkShadow",
			"Button.disabledText",
			"Button.foreground",
			"Button.highlight",
			"Button.light",
			"Button.select",
			"Button.shadow",
			"CheckBox.background",
			"CheckBox.disabledText",
			"CheckBox.foreground",
			"CheckBox.select",
			"CheckBoxMenuItem.acceleratorForeground",
			"CheckBoxMenuItem.acceleratorSelectionForeground",
			"CheckBoxMenuItem.background",
			"CheckBoxMenuItem.disabledBackground",
			"CheckBoxMenuItem.disabledForeground",
			"CheckBoxMenuItem.foreground",
			"CheckBoxMenuItem.selectionBackground",
			"CheckBoxMenuItem.selectionForeground",
			"ColorChooser.background",
			"ColorChooser.foreground",
			"ColorChooser.swatchesDefaultRecentColor",
			"ComboBox.background",
			"ComboBox.buttonBackground",
			"ComboBox.buttonDarkShadow",
			"ComboBox.buttonHighlight",
			"ComboBox.buttonShadow",
			"ComboBox.disabledBackground",
			"ComboBox.disabledForeground",
			"ComboBox.foreground",
			"ComboBox.selectionBackground",
			"ComboBox.selectionForeground",
			"Desktop.background",
			"EditorPane.background",
			"EditorPane.caretForeground",
			"EditorPane.foreground",
			"EditorPane.inactiveBackground",
			"EditorPane.inactiveForeground",
			"EditorPane.selectionBackground",
			"EditorPane.selectionForeground",
			"Focus.color",
			"FormattedTextField.background",
			"FormattedTextField.caretForeground",
			"FormattedTextField.foreground",
			"FormattedTextField.inactiveBackground",
			"FormattedTextField.inactiveForeground",
			"FormattedTextField.selectionBackground",
			"FormattedTextField.selectionForeground",
			"InternalFrame.activeTitleBackground",
			"InternalFrame.activeTitleForeground",
			"InternalFrame.background",
			"InternalFrame.borderColor",
			"InternalFrame.borderDarkShadow",
			"InternalFrame.borderHighlight",
			"InternalFrame.borderLight",
			"InternalFrame.borderShadow",
			"InternalFrame.inactiveTitleBackground",
			"InternalFrame.inactiveTitleForeground",
			"InternalFrame.optionDialogBackground",
			"InternalFrame.paletteBackground",
			"Label.background",
			"Label.disabledForeground",
			"Label.disabledShadow",
			"Label.foreground",
			"List.background",
			"List.foreground",
			"List.selectionBackground",
			"List.selectionForeground",
			"Menu.acceleratorForeground",
			"Menu.acceleratorSelectionForeground",
			"Menu.background",
			"Menu.disabledBackground",
			"Menu.disabledForeground",
			"Menu.foreground",
			"Menu.selectionBackground",
			"Menu.selectionForeground",
			"MenuBar.background",
			"MenuBar.disabledBackground",
			"MenuBar.disabledForeground",
			"MenuBar.foreground",
			"MenuBar.highlight",
			"MenuBar.selectionBackground",
			"MenuBar.selectionForeground",
			"MenuBar.shadow",
			"MenuItem.acceleratorForeground",
			"MenuItem.acceleratorSelectionForeground",
			"MenuItem.background",
			"MenuItem.disabledBackground",
			"MenuItem.disabledForeground",
			"MenuItem.foreground",
			"MenuItem.selectionBackground",
			"MenuItem.selectionForeground",
			"OptionPane.background",
			"OptionPane.foreground",
			"OptionPane.messageForeground",
			"Panel.background",
			"Panel.foreground",
			"PasswordField.background",
			"PasswordField.caretForeground",
			"PasswordField.foreground",
			"PasswordField.inactiveBackground",
			"PasswordField.inactiveForeground",
			"PasswordField.selectionBackground",
			"PasswordField.selectionForeground",
			"PopupMenu.background",
			"PopupMenu.foreground",
			"PopupMenu.selectionBackground",
			"PopupMenu.selectionForeground",
			"ProgressBar.background",
			"ProgressBar.foreground",
			"ProgressBar.selectionBackground",
			"ProgressBar.selectionForeground",
			"RadioButton.background",
			"RadioButton.darkShadow",
			"RadioButton.disabledText",
			"RadioButton.foreground",
			"RadioButton.highlight",
			"RadioButton.light",
			"RadioButton.select",
			"RadioButton.shadow",
			"RadioButtonMenuItem.acceleratorForeground",
			"RadioButtonMenuItem.acceleratorSelectionForeground",
			"RadioButtonMenuItem.background",
			"RadioButtonMenuItem.disabledBackground",
			"RadioButtonMenuItem.disabledForeground",
			"RadioButtonMenuItem.foreground",
			"RadioButtonMenuItem.selectionBackground",
			"RadioButtonMenuItem.selectionForeground",
			"ScrollBar.background",
			"ScrollBar.foreground",
			"ScrollBar.thumb",
			"ScrollBar.thumbDarkShadow",
			"ScrollBar.thumbHighlight",
			"ScrollBar.thumbShadow",
			"ScrollBar.track",
			"ScrollBar.trackHighlight",
			"ScrollPane.background",
			"ScrollPane.foreground",
			"Separator.foreground",
			"Separator.highlight",
			"Separator.shadow",
			"Slider.background",
			"Slider.focus",
			"Slider.foreground",
			"Slider.highlight",
			"Slider.shadow",
			"Slider.tickColor",
			"Spinner.background",
			"Spinner.foreground",
			"SplitPane.background",
			"SplitPane.darkShadow",
			"SplitPane.highlight",
			"SplitPane.shadow",
			"SplitPaneDivider.draggingColor",
			"TabbedPane.background",
			"TabbedPane.darkShadow",
			"TabbedPane.focus",
			"TabbedPane.foreground",
			"TabbedPane.highlight",
			"TabbedPane.light",
			"TabbedPane.shadow",
			"Table.background",
			"Table.focusCellBackground",
			"Table.focusCellForeground",
			"Table.foreground",
			"Table.gridColor",
			"Table.selectionBackground",
			"Table.selectionForeground",
			"TableHeader.background",
			"TableHeader.foreground",
			"TextArea.background",
			"TextArea.caretForeground",
			"TextArea.foreground",
			"TextArea.inactiveBackground",
			"TextArea.inactiveForeground",
			"TextArea.selectionBackground",
			"TextArea.selectionForeground",
			"TextComponent.selectionBackgroundInactive",
			"TextField.background",
			"TextField.caretForeground",
			"TextField.darkShadow",
			"TextField.foreground",
			"TextField.highlight",
			"TextField.inactiveBackground",
			"TextField.inactiveForeground",
			"TextField.light",
			"TextField.selectionBackground",
			"TextField.selectionForeground",
			"TextField.shadow",
			"TextPane.background",
			"TextPane.caretForeground",
			"TextPane.foreground",
			"TextPane.inactiveBackground",
			"TextPane.inactiveForeground",
			"TextPane.selectionBackground",
			"TextPane.selectionForeground",
			"TitledBorder.titleColor",
			"ToggleButton.background",
			"ToggleButton.darkShadow",
			"ToggleButton.disabledText",
			"ToggleButton.foreground",
			"ToggleButton.highlight",
			"ToggleButton.light",
			"ToggleButton.shadow",
			"ToolBar.background",
			"ToolBar.darkShadow",
			"ToolBar.dockingBackground",
			"ToolBar.dockingForeground",
			"ToolBar.floatingBackground",
			"ToolBar.floatingForeground",
			"ToolBar.foreground",
			"ToolBar.highlight",
			"ToolBar.light",
			"ToolBar.shadow",
			"ToolTip.background",
			"ToolTip.foreground",
			"Tree.background",
			"Tree.foreground",
			"Tree.hash",
			"Tree.line",
			"Tree.selectionBackground",
			"Tree.selectionBorderColor",
			"Tree.selectionForeground",
			"Tree.textBackground",
			"Tree.textForeground",
			"Viewport.background",
			"Viewport.foreground",
			"activeCaption",
			"activeCaptionBorder",
			"activeCaptionText",
			"control",
			"controlDkShadow",
			"controlHighlight",
			"controlLtHighlight",
			"controlShadow",
			"controlText",
			"desktop",
			"inactiveCaption",
			"inactiveCaptionBorder",
			"inactiveCaptionText",
			"info",
			"infoText",
			"menu",
			"menuText",
			"scrollbar",
			"text",
			"textHighlight",
			"textHighlightText",
			"textInactiveText",
			"textText",
			"window",
			"windowBorder",
			"windowText"
	};

	private boolean modified;
	private final Map<String, List<String>> commonMap;

	public ColorPaletteDesigner() {
		super();

		this.undoManager = new UndoManager();
		this.undoSupport = new UndoableEditSupport();
		final var undoListener = new UndoableEditListener() {

			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				undoManager.addEdit(e.getEdit());
				setModified(true);
				repaintWindows();
			}

		};
		this.undoSupport.addUndoableEditListener(undoListener);

		Map<String, List<String>> commonMap = new HashMap<>();
		String[] commonKeys = new String[]{
				"background",
				"foreground",
				"selectedBackground",
				"selectedForeground",
				"disabledBackground",
				"disabledForeground"
		};
		Arrays.stream(commonKeys).forEach(key -> commonMap.put(key, new ArrayList<>()));
		this.commonMap = commonMap;

		init();
	}

	public void init() {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("All");

		Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

		for (String key : DEFAULT_UI_MANAGER_KEYS) {
			if (key.contains(".")) {
				String[] splitKey = key.split("\\.");
				if (!nodeMap.containsKey(splitKey[0])) {
					DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(splitKey[0]);
					root.add(branchNode);
					nodeMap.put(splitKey[0], branchNode);
				}

				DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(splitKey[1]);
				nodeMap.get(splitKey[0]).add(leafNode);

				if (commonMap.containsKey(splitKey[1])) {
					commonMap.get(splitKey[1]).add(key);
				}
			}
			else {
				DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode((key));
				root.add(leafNode);
			}
		}

		for (String key : UIDefaults.getInstance().getColorKeys()) {
			if (key.contains(".")) {
				String[] splitKey = key.split("\\.");
				if (!nodeMap.containsKey(splitKey[0])) {
					DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(splitKey[0]);
					root.add(branchNode);
					nodeMap.put(splitKey[0], branchNode);
				}
				DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(splitKey[1]);
				nodeMap.get(splitKey[0]).add(leafNode);
			}
			else {
				DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(key);
				root.add(leafNode);
			}
		}

		DefaultMutableTreeNode commonNode = new DefaultMutableTreeNode("Common");

		for (String key : commonMap.keySet()) {
			if (commonMap.get(key).size() == 0) continue;
			DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(key);
			commonNode.add(leafNode);
		}

		root.add(commonNode);

		JTree tree = new JTree(root);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					if (node != null && node.isLeaf()) {

						String colorKey = getColorKey(path);


						boolean isCommon = "Common".equals(path.getPathComponent(1).toString());
						System.out.println("Is common: " + isCommon);

						System.out.println("You clicked on: " + colorKey);

						Color color;
						if (isCommon) {
							color = UIManager.getColor(commonMap.get(path.getPathComponent(2).toString()).get(0));
						}
						else {
							color = UIManager.getColor(colorKey);
						}

						JColorChooser colorChooser = new JColorChooser(color);
						JDialog colorChooserDialog = JColorChooser.createDialog(
							ColorPaletteDesigner.this,
							isCommon ? "All " + path.getPathComponent(2).toString() : colorKey,
							false,
							colorChooser,
							(_evt) -> {
								if (isCommon) {
									List<String> commonKeys = commonMap.get(path.getPathComponent(2).toString());
									for (String commonKey : commonKeys) {
										changeColor(commonKey, colorChooser.getColor());
									}
								}
								else {
									changeColor(colorKey, colorChooser.getColor());
								}
							},
							(evt) -> System.out.println("Cancelled"));

						colorChooserDialog.setVisible(true);
					}
					else {
						if (tree.isCollapsed(path)) {
							tree.expandPath(path);
						}
						else{
							tree.collapsePath(path);
						}
					}
				}
			}
		});

		tree.setCellRenderer(new ColorPaletteTreeCellRenderer());

		setLayout(new BorderLayout());
		tree.setBorder(new EmptyBorder(8,8,8,8));
		JScrollPane treeScrollPane = new JScrollPane(tree);
		add(treeScrollPane, BorderLayout.CENTER);
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		var oldVal = this.modified;
		this.modified = modified;
		firePropertyChange("modified", oldVal, modified);
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	private void repaintWindows() {
		CommonModuleFrame.getOpenWindows().forEach(CommonModuleFrame::repaint);
	}

	/**
	 * Save all ui manager properties to file
	 * @param filename
	 * @throws IOException
	 */
	public void saveProperties(String filename) throws IOException {
		Properties props = new Properties();

		for (String key : DEFAULT_UI_MANAGER_KEYS) {
			props.put(key, UIManager.getColor(key));
		}

		for (String key : UIDefaults.getInstance().getColorKeys()) {
			props.put(key, UIManager.getColor(key));
		}

		props.save(Files.newOutputStream(Path.of(filename)), null);
	}

	/**
	 * Load all ui manager properties from file
	 *
	 * @param filename
	 * @throws IOException
	 */
	public void loadProperties(String filename) throws IOException {
		Properties props = new Properties();
		props.load(Files.newInputStream(Path.of(filename)));

		for (Object key : props.keySet()) {
			UIManager.put(key, props.get(key));
		}
	}

	/**
	 * Change color of given UIManager key
	 *
	 * @param key
	 * @param color
	 */
	public void changeColor(String key, Color color) {
		final ChangeColorEdit edit = new ChangeColorEdit(key, color);
		undoSupport.postEdit(edit);
	}

	private String getColorKey(TreePath path) {
		int pathLen = path.getPathCount();
		if (pathLen == 1) return null;

		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < pathLen; i++) {
			builder.append(path.getPathComponent(i).toString());
			if (i < pathLen - 1) {
				builder.append('.');
			}
		}

		return builder.toString();
	}

	private class ChangeColorEdit extends AbstractUndoableEdit {

		private String key;

		private Color color;

		private Color oldColor;

		public ChangeColorEdit(String key, Color color) {
			this.key = key;
			this.color = color;
			this.oldColor = UIManager.getColor(key);
			UIManager.put(key, color);
			System.out.println("Changed color of: " + key);
		}

		@Override
		public void undo() throws CannotUndoException {
			UIManager.put(key, oldColor);
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void redo() throws CannotRedoException {
			UIManager.put(key, color);
		}

		@Override
		public boolean canRedo() {
			return true;
		}

		@Override
		public boolean isSignificant() {
			return super.isSignificant();
		}

		@Override
		public String getPresentationName() {
			return String.format("Change color for %s to %s", key, color.toString());
		}
	}

	private class ColorPaletteTreeCellRenderer extends DefaultTreeCellRenderer {
		public ColorPaletteTreeCellRenderer() {

		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			DefaultMutableTreeNode node = ((DefaultMutableTreeNode) value);
			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			label.setText((String) node.getUserObject());
			if (!leaf) return label;

			TreeNode[] pathArray = node.getPath();

			JPanel leafPanel = new JPanel(new HorizontalLayout());
			leafPanel.setBackground(new Color(0,0,0,0));

			boolean isCommon = pathArray[1].toString().equals("Common");

			String colorKey = getColorKey(new TreePath(pathArray));
			Color color;
			if (isCommon) {
				color = UIManager.getColor(commonMap.get(pathArray[2].toString()).get(0));
			}
			else {
				color = UIManager.getColor(colorKey);
			}

			BufferedImage colorSwatch = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			var g = colorSwatch.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0,16, 16);
			g.setColor(color);
			g.fillRect(2, 2,12, 12);
			ImageIcon icon = new ImageIcon(colorSwatch);

			label.setIcon(icon);

			return label;
		}
	}
}
