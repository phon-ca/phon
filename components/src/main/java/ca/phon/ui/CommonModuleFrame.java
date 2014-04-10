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
package ca.phon.ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * The common class for a gui window
 */
public class CommonModuleFrame extends SnapshotFrame implements IExtendable {
	
	private static final long serialVersionUID = 2112769368100535156L;
	
	private static final Logger LOGGER = Logger.getLogger(CommonModuleFrame.class.getName());
	
	/** The list of open module frames */
	private static ArrayList<CommonModuleFrame> openFrames = 
		new ArrayList<CommonModuleFrame>();
	private static CommonModuleFrame currentFrame;
	
	private static void newWindowCreated(CommonModuleFrame f) {
		openFrames.add(f);
//		updateWindowMenus();
	}
	
	public static ArrayList<CommonModuleFrame> getOpenWindows() {
		return openFrames;
	}
	
	private static void removeWindow(CommonModuleFrame f) {
		if(openFrames.contains(f))
			openFrames.remove(f);
//		updateWindowMenus();
		
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
//		this.setFocusableWindowState(false);
		
		// assign the title
		this.title = title;
		
		// make the frame resizable
		this.setResizable(true);
		
		JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);
		
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
				//CommonModuleFrame.windowActivated(CommonModuleFrame.this);
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				removeWindowFromActiveList();
				CommonModuleFrame.this.removeWindowListener(this);
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				// remove from the active list
				//removeWindowFromActiveList();
				((CommonModuleFrame)arg0.getWindow()).close();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				//removeWindowFromActiveList();
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				//CommonModuleFrame.newWindowCreated(CommonModuleFrame.this);
			}
			
		});
		
		// set window icon
		if(!OSInfo.isMacOs()) {
			ImageIcon icon = 
				IconManager.getInstance().getIcon("apps/database-phon", IconSize.SMALL);
			if(icon != null) {
				super.setIconImage(icon.getImage());
			}
		}
		
		newWindowCreated(this);
	}
	
	public void close() {
		if(hasUnsavedChanges()) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(this);
			props.setOptions(MessageDialogProperties.yesNoCancelOptions);
			props.setTitle("Save changes?");
			props.setHeader("Save changes?");
			props.setMessage("Save changes before closing?");
			props.setRunAsync(false);
			
			int retVal = NativeDialogs.showMessageDialog(props);
			if(retVal == 0) {
				try {
					if(!saveData()) {
						throw new IOException("");
					}
					dispose();
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.warning(e.getMessage());
				}
			} else if(retVal == 1) {
				dispose();
			}
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
		// TODO fix window title w/ project name
//		if(getProject() != null) {
//			retVal += getProject().getProjectName();
//		}
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
					if(e.getWindow() == parentFrame)
						close();
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
//			this.setLayout(new BorderLayout());
			
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

		public Color getForeColor() {
			return foreColor;
		}

		public void setForeColor(Color foreColor) {
			this.foreColor = foreColor;
			messageLabel.setForeground(foreColor);
		}

		public Color getBackColor() {
			return backColor;
		}

		public void setBackColor(Color backColor) {
			this.backColor = backColor;
			messageLabel.setBackground(backColor);
		}
	}
	
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
		return false;
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
