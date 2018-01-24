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
package ca.phon.ui;


import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import com.jgoodies.forms.layout.*;

import ca.phon.extensions.*;
import ca.phon.ui.menu.MenuManager;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.*;


/**
 * The common class for a gui window
 */
public class CommonModuleFrame extends JFrame implements IExtendable {

	private static final long serialVersionUID = 2112769368100535156L;

	private static final Logger LOGGER = Logger.getLogger(CommonModuleFrame.class.getName());

	/**
	 * Property to enable fullscreen on macos
	 */
	public static final String MACOS_ENABLE_FULLSCREEN = "macos.enableFullscreen";

	public static final boolean DEFAULT_MACOS_ENABLE_FULLSCREEN = true;

	/** The list of open module frames */
	private static ArrayList<CommonModuleFrame> openFrames =
		new ArrayList<CommonModuleFrame>();

	private static void newWindowCreated(CommonModuleFrame f) {
		openFrames.add(f);
	}

	public static ArrayList<CommonModuleFrame> getOpenWindows() {
		return openFrames;
	}

	private static void removeWindow(CommonModuleFrame f) {
		if(openFrames.contains(f))
			openFrames.remove(f);

		/**if(openFrames.size() == 0 && !PhonUtilities.isMacOs()) {
			ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction(
				"ca.phon.modules.core.OpenProjectController");
			LoadModule lm = new LoadModule(mi, new HashMap<String, Object>());
			lm.start();
		}*/
	}

	public static CommonModuleFrame getCurrentFrame() {
		CommonModuleFrame[] openWindows =
			CommonModuleFrame.getOpenWindows().toArray(new CommonModuleFrame[0]);
		for(CommonModuleFrame f:openWindows)
			if(f.isActive())
				return f;
		return null;
	}

	protected String title;

	private CommonModuleFrame parentFrame;

	private boolean showInWindowMenu = true;

	/**
	 * Window name
	 */
	private String windowName = null;

	private volatile transient boolean modified = false;

	/**
	 * Messages for unsaved data
	 */
	private String unsavedChangesTitle = "Save data?";

	private String unsavedChangesMessage = "Save changes before close?";

	/**
	 * Creates a new CommonModuleFrame
	 *
	 */
	public CommonModuleFrame() {
		this(" -- untitled -- ");
	}

	/**
	 * Creates a new CommonModuleFrame
	 *
	 * @param title the title string for the frame
	 *
	 */
	public CommonModuleFrame(String title) {
		super(title);

		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// assign the title
		this.title = title;

		// make the frame resizable
		this.setResizable(true);

		// defer menu creation until after window construction
		// some menu handlers may require information not created
		// until sub-class construction is complete
		SwingUtilities.invokeLater(() -> {
			final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
			setJMenuBar(menuBar);
		});

		this.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				removeWindowFromActiveList();
				CommonModuleFrame.this.removeWindowListener(this);
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				((CommonModuleFrame)arg0.getWindow()).close();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				//removeWindowFromActiveList();
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}

		});

		if(!OSInfo.isMacOs()) {
			// set window icon
			ImageIcon icon =
				IconManager.getInstance().getIcon("apps/database-phon", IconSize.SMALL);
			ImageIcon largeIcon =
				IconManager.getInstance().getIcon("apps/database-phon", IconSize.LARGE);
			ImageIcon xLargIcon =
				IconManager.getInstance().getIcon("apps/database-phon", IconSize.XLARGE);
			final Image icons[] = new Image[]{ icon.getImage(), largeIcon.getImage(), xLargIcon.getImage() };
			if(icon != null) {
				super.setIconImages(Arrays.asList(icons));
			}
		} else {
			// fullscreen support
			getRootPane().putClientProperty("apple.awt.fullscreenable",
					PrefHelper.getBoolean(MACOS_ENABLE_FULLSCREEN, DEFAULT_MACOS_ENABLE_FULLSCREEN));
		}

		newWindowCreated(this);
	}

	public String getUnsavedChangesTitle() {
		return this.unsavedChangesTitle;
	}

	public void setUnsavedChangesTitle(String title) {
		this.unsavedChangesTitle = title;
	}

	public String getUnsavedChangesMessage() {
		return this.unsavedChangesMessage;
	}

	public void setUnsavedChangesMessage(String message) {
		this.unsavedChangesMessage = message;
	}

	public void close() {
		if(hasUnsavedChanges()) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(this);
			props.setOptions(MessageDialogProperties.yesNoCancelOptions);
			props.setTitle(getTitle());
			props.setHeader(getUnsavedChangesTitle());
			props.setMessage(getUnsavedChangesMessage());
			props.setRunAsync(true);
			props.setListener( (e) -> {
				int retVal = e.getDialogResult();
				if(retVal == 0) {
					try {
						if(saveData()) {
							dispose();
						}
					} catch (IOException ex) {
						Toolkit.getDefaultToolkit().beep();
						LOGGER.severe(ex.getMessage());

						showMessageDialog("Save Failed", ex.getLocalizedMessage(), MessageDialogProperties.okOptions);
					}
				} else if(retVal == 1) {
					dispose();
				}
			});
			NativeDialogs.showMessageDialog(props);
		} else {
			dispose();
		}
	}

	/**
	 * Overridden to provide consistent
	 * naming in all windows.  Use setWindowName()
	 * to setup the custom name for the window.
	 */
	@Override
	public String getTitle() {
		String retVal = "";
		if(getWindowName() != null) {
			retVal += (retVal.length() > 0 ? " : " : "") + getWindowName();
		}
		return retVal;
	}

	/**
	 * Set window name
	 */
	public void setWindowName(String name) {
		this.windowName = name;
		setTitle(getTitle());
	}

	/**
	 * Get window name
	 */
	public String getWindowName() {
		return this.windowName;
	}

	private void removeWindowFromActiveList() {
		CommonModuleFrame.removeWindow(this);
	}

	public void display() {
		this.pack();
		this.setVisible(true);
	}

	public CommonModuleFrame getParentFrame() {
		return parentFrame;
	}

	private WindowListener parentFrameListener = null;
	public void setParentFrame(CommonModuleFrame frame) {
		if(this.parentFrame != null) {
			this.parentFrame.removeWindowListener(parentFrameListener);
		}
		this.parentFrame = frame;

		if(parentFrameListener == null) {
			parentFrameListener = new WindowListener() {

				@Override
				public void windowActivated(WindowEvent e) {

				}

				@Override
				public void windowClosed(WindowEvent e) {
					if(e.getWindow() == parentFrame) {
						close();
						parentFrame.removeWindowListener(parentFrameListener);
					}
				}

				@Override
				public void windowClosing(WindowEvent e) {

				}

				@Override
				public void windowDeactivated(WindowEvent e) {

				}

				@Override
				public void windowDeiconified(WindowEvent e) {

				}

				@Override
				public void windowIconified(WindowEvent e) {

				}

				@Override
				public void windowOpened(WindowEvent e) {

				}

			};
		}
		this.parentFrame.addWindowListener(parentFrameListener);

//		updateWindowMenus();
	}

	public boolean isShowInWindowMenu() {
		return showInWindowMenu;
	}

	public void setShowInWindowMenu(boolean showInWindowMenu) {
		this.showInWindowMenu = showInWindowMenu;

//		updateWindowMenus();
	}

	/**
	 * Center the window in the middle of the
	 * display.  If size is not defined preferred size
	 * is used.  If the window has already been
	 * realized, it will not be resized.
	 */
	public void centerWindow() {
		Dimension ss =
			Toolkit.getDefaultToolkit().getScreenSize();

		Dimension size = getSize();
		if(size.width == 0 && size.height == 0)
			size = getPreferredSize();

		int xPos = ss.width / 2 - (size.width/2);
		int yPos = ss.height / 2 - (size.height/2);

		setBounds(xPos, yPos, size.width, size.height);
	}

	public void cascadeWindow(JFrame frame) {
		int x = 50;
		int y = 50;

		if(frame != null) {
			final int titleHeight = frame.getInsets().top;
			final int borderWidth = frame.getInsets().left;
			final int x2 = frame.getX();
			final int y2 = frame.getY();
			y = y2 + titleHeight;
			x = x2 + borderWidth;
		}

		Dimension size = getSize();
		if(size.width == 0 && size.height == 0)
			size = getPreferredSize();

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(y + size.height > screenSize.height) {
			y = screenSize.height - size.height;
		}
		if(x + size.width > screenSize.width) {
			x = screenSize.width - size.width;
		}

		setBounds(x, y, size.width, size.height);
	}

	/**
	 * Places window in top-right corner of screen.
	 *
	 */
	public void placeTopRight() {
		Dimension ss =
			Toolkit.getDefaultToolkit().getScreenSize();

		Dimension size = getSize();
		if(size.width == 0 && size.height == 0)
			size = getPreferredSize();

		int xPos = ss.width - size.width;
		int yPos = 0;

		setBounds(xPos, yPos, size.width, size.height);
	}

	// a status display to use on the glass pane
	private class StatusDisplay extends JComponent {
		private JLabel messageLabel;
		private Color backColor;
		private Color foreColor;

		public StatusDisplay(String message) {
			this(message, Color.lightGray, Color.black);
		}

		public StatusDisplay(String message, Color backColor, Color foreColor) {
			super();

			this.backColor = backColor;
			this.foreColor = foreColor;

			init();
			setMessage(message);
		}

		private void init() {
			FormLayout layout = new FormLayout(
					"fill:pref:grow, pref, 3dlu",
					"3dlu, pref, fill:pref:grow");
			CellConstraints cc = new CellConstraints();
			this.setLayout(layout);

			messageLabel = new JLabel();
			messageLabel.setBackground(backColor);
			messageLabel.setForeground(foreColor);
			messageLabel.setOpaque(true);
			messageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			messageLabel.setToolTipText("Click to hide");
			messageLabel.addMouseListener(new MouseInputAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					hideStatusComponent();
				}

			});

			messageLabel.setHorizontalAlignment(SwingConstants.RIGHT);

			add(messageLabel, cc.xy(2, 2));
		}

		public void setMessage(String msg) {
			messageLabel.setText(" " + msg + " ");
		}

		public void setForeColor(Color foreColor) {
			this.foreColor = foreColor;
			messageLabel.setForeground(foreColor);
		}

		public void setBackColor(Color backColor) {
			this.backColor = backColor;
			messageLabel.setBackground(backColor);
		}
	}

	public void showOkDialog(String title, String message) {
		showMessageDialog(title, message, MessageDialogProperties.okOptions);
	}

	public int showOkCancelDialog(String title, String message) {
		return showMessageDialog(title, message, MessageDialogProperties.okCancelOptions);
	}

	public int showYesNoDialog(String title, String message) {
		return showMessageDialog(title, message, MessageDialogProperties.yesNoOptions);
	}

	public int showYesNoCancelDialog(String title, String message) {
		return showMessageDialog(title, message, MessageDialogProperties.yesNoCancelOptions);
	}

	/**
	 * Display a message dialog to the user positioned for
	 * this window.
	 *
	 * @param title
	 * @param message
	 * @param options list of options displayed to the user
	 * @return the selected option
	 */
	public int showMessageDialog(String title, String message, String[] options) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(this);
		props.setRunAsync(false);
		props.setTitle(title);
		props.setHeader(title);
		props.setMessage(message);
		props.setOptions(options);

		return NativeDialogs.showMessageDialog(props);
	}

	// TODO Fix these methods and make more useful
	// Glass pane could be used to draw red around components of interest
	private StatusDisplay statusDisplay;
	public void showStatusMessage(String message) {
		if(statusDisplay == null) {
			statusDisplay = new StatusDisplay("");
			((Container)getGlassPane()).setLayout(new BorderLayout());
			((Container)getGlassPane()).add(statusDisplay, BorderLayout.CENTER);
		}
		statusDisplay.setMessage(message);
		statusDisplay.setBackColor(Color.decode("0x3366cc"));
		statusDisplay.setForeColor(Color.white);
		getGlassPane().setVisible(true);
	}

	public void showErrorMessage(String message) {
		if(statusDisplay == null) {
			statusDisplay = new StatusDisplay("");
			((Container)getGlassPane()).setLayout(new BorderLayout());
			((Container)getGlassPane()).add(statusDisplay, BorderLayout.CENTER);
		}
		statusDisplay.setMessage(message);
		statusDisplay.setForeColor(Color.white);
		statusDisplay.setBackColor(Color.red);
		getGlassPane().setVisible(true);
	}

	public void hideStatusComponent() {
		if(getGlassPane() != null
				) {
			getGlassPane().setVisible(false);
		}
	}

	public void resetStatusComponent() {
		if(getGlassPane() != null) {
			((Container)getGlassPane()).remove(statusDisplay);
			statusDisplay = null;
		}
		repaint();
	}

	/**
	 * Does this window have un-saved changes?
	 *
	 * @return true if this window has un-saved changes
	 */
	public boolean hasUnsavedChanges() {
		return this.modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;

		getRootPane().putClientProperty("Window.documentModified", hasUnsavedChanges());
	}

	/**
	 * Save window changes
	 *
	 * @boolean true on success
	 * @throws IOException
	 */
	public boolean saveData()
		throws IOException {
		return true;
	}

	/* window positioning */
	/**
	 * Position the window relative to another window
	 * using the boxSize and position parameters.
	 *
	 * @param boxSide one of SwingConstants.TOP,BOTTOM,RIGHT,LEFT.
	 * The window will be positioned on this side of the given window.
	 *
	 * @param position one of SwingConstants.LEADING,CENTER,TRAILING.
	 * The window will be positioned using standard left-to-right, top-to-bottom
	 * positioning based on this parameter.
	 *
	 * @param parentFrame.  The frame to use as the anchor.
	 *
	 * @throws IllegalArgumentException if boxSize or position are not
	 * one of the accept values.
	 *
	 */
	public void positionRelativeTo(int boxSide, int position,
			CommonModuleFrame component) {

		// check params
		if(boxSide != SwingConstants.TOP
				&& boxSide != SwingConstants.BOTTOM
				&& boxSide != SwingConstants.LEFT
				&& boxSide != SwingConstants.RIGHT)
			throw new IllegalArgumentException("Invalid boxSide given.");

		if(position != SwingConstants.LEADING
				&& position != SwingConstants.CENTER
				&& position != SwingConstants.TRAILING)
			throw new IllegalArgumentException("Invalid position given.");

		Rectangle compBounds = component.getBounds();

		Dimension size = getPreferredSize();
		if(isVisible()) {
			size = getSize();
		}

		// get the screen width
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int newX, newY, newW, newH;
		newX = newY  = 0;
		newW = size.width;
		newH = size.height;

		if(boxSide == SwingConstants.LEFT) {
			newX = compBounds.x - newW - 5;
		} else if(boxSide == SwingConstants.RIGHT) {
			newX = compBounds.x + compBounds.width + 5;
		} else if(boxSide == SwingConstants.TOP) {
			newY = compBounds.y - newH - 5;
		} else if(boxSide == SwingConstants.BOTTOM) {
			newY = compBounds.y + compBounds.height + 5;
		}

		if(boxSide == SwingConstants.LEFT
				|| boxSide == SwingConstants.RIGHT) {
			if(position == SwingConstants.LEADING) {
				newY = compBounds.y;
			} else if(position == SwingConstants.CENTER) {
				int halfComp = compBounds.height / 2;
				int halfSelf = newH / 2;
				newY = compBounds.y + halfComp - halfSelf;
			} else if(position == SwingConstants.TRAILING) {
				newY = compBounds.y + compBounds.height - newH;
			}
		} else {
			if(position == SwingConstants.LEADING) {
				newX = compBounds.x;
			} else if(position == SwingConstants.CENTER) {
				int halfComp = compBounds.width / 2;
				int halfSelf = newW / 2;
				newX = compBounds.x + halfComp - halfSelf;
			} else if(position == SwingConstants.TRAILING) {
				newX = compBounds.x + compBounds.width - newW;
			}
		}

		// make sure we are still on the screen
		if(newX < 0)
			newX = 0;
		if(newY < 0)
			newY = 0;

		if(newX + newW > screenSize.width) {
			newX = screenSize.width - newW;
		}

		if(newY + newH > screenSize.height) {
			newY = screenSize.height - newH;
		}

		setBounds(newX, newY, newW, newH);
	}

	/*
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(CommonModuleFrame.class, this);

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
}
