/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipamap;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.InnerGlowPathEffect;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.ipa.features.Feature;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import ca.phon.ipamap.IpaMapSearchField.SearchType;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.CellProp;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.IpaGrids;
import ca.phon.ui.ipamap.io.ObjectFactory;
import ca.phon.ui.layout.GridCellConstraint;
import ca.phon.ui.layout.GridCellLayout;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.util.CollatorFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;

/**
 * Displays a sectioned list of 
 * ipa grids.  Each grid is inside a collapsible
 * panel.  The entire thing is already inside
 * a scroll pane.
 * 
 * The UI is scaled by font size. Minimum font
 * is 12pt and maximum is 24pt.  Scale is
 * set as a floating point value between 0 and 1.
 * 
 */
public class IpaMap extends JPanel implements ClipboardOwner, Scrollable {
	
	private static final long serialVersionUID = 1758355523938039972L;

	private static final Logger LOGGER = Logger.getLogger(IpaMap.class
			.getName());
	
	/**
	 * Pref prop for scale
	 */
	public final static String SCALE_PROP = 
		IpaMap.class.getName() + ".scale";
	
	/**
	 * Pref prop for font
	 */
	public final static String FONT_PROP = 
		IpaMap.class.getName() + ".font";
	
	/**
	 * Prop for storing stack of previously used
	 * phones.
	 */
	private final static String LAST_USED_PROP = 
		IpaMap.class.getName() + ".lastUsed";
	
	/**
	 * Pref for highlighting recently used
	 */
	public final static String HIGHLIGHT_RECENT_PROP = 
		IpaMap.class.getName() + ".highlightRecent";
	
	/**
	 * Pref for fading window
	 */
	public final static String FADE_WINDOW_PROP =
		IpaMap.class.getName() + ".fadeWindow";
	
	/**
	 * Pref for fade amount
	 */
	public final static String FADE_OPACITY_PROP = 
		IpaMap.class.getName() + ".fadeOpacity";
	
	/**
	 * Pref for favorites storage
	 */
	public final static String FAVORITES_PROP =
		IpaMap.class.getName() + ".favorites";
	
	/**
	 * Default scale
	 */
	private final static float DEFAULT_SCALE = 0.3f;
	
	/**
	 * Default fade opacity
	 */
	private final static float DEFAULT_FADE_OPACITY = 0.3f;
	
	/**
	 * Default font
	 */
	private final static String DEFAULT_FONT = "Charis SIL Compact-PLAIN-13";
	
	/**
	 * Load the scale property
	 * @return
	 */
	private static float getSavedScale() {
		return PrefHelper.getFloat(SCALE_PROP, DEFAULT_SCALE);
	}
	
	/**
	 * Set scale property
	 * @param scale
	 */
	private static void setSavedScale(float scale) {
		PrefHelper.getUserPreferences().putFloat(SCALE_PROP, scale);
	}
	
	/**
	 * Get recently used list.
	 * 
	 */
	private static UsageList getUsageList() {
		final UsageList retVal = new UsageList(MAX_LAST_USED_SIZE);
		
		final String recentProjects = PrefHelper.get(LAST_USED_PROP, "");
		if(recentProjects != null && recentProjects.length() > 0) {
			String lastUsedItems[] = recentProjects.split(":");
			for(String lastUsedItem:lastUsedItems) {
				retVal.add(lastUsedItem);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Save recently used list
	 * @param list
	 */
	private static void setUsageList(UsageList list) {
		String propVal = new String();
		int startIdx = Math.min(list.size()-1, list.maxsize()-1);
		for(int i = startIdx; i >=0; i--) {
			propVal += (propVal.length() > 0 ? ":" : "") + list.get(i);
		}
		PrefHelper.getUserPreferences().put(LAST_USED_PROP, propVal);
	}
	
	/**
	 * Get saved fade opacity
	 */
	private static float getSavedFadeOpacity() {
		return PrefHelper.getFloat(FADE_OPACITY_PROP, DEFAULT_FADE_OPACITY);
	}
	
	/**
	 * Pref for using window fading
	 */
	private static boolean getSavedFadeWindow() {
		return PrefHelper.getBoolean(FADE_WINDOW_PROP, Boolean.TRUE);
	}
	
	/**
	 * Pref for highlighting recently used
	 */
	private static boolean getSavedHighlightRecent() {
		return PrefHelper.getBoolean(HIGHLIGHT_RECENT_PROP, Boolean.TRUE);
	}
	
	private static void setSavedHighlightRecent(boolean highlight) {
		PrefHelper.getUserPreferences().putBoolean(HIGHLIGHT_RECENT_PROP, highlight);
	}
	
	private static boolean getSavedSectionToggle(String name) {
		final String propName = 
			IpaMap.class.getName() + ".grid." + name + ".collapsed";
		return PrefHelper.getBoolean(propName, Boolean.FALSE);
	}
	
	private static void setSavedSectionToggle(String name, boolean collapsed) {
		final String propName = 
			IpaMap.class.getName() + ".grid." + name + ".collapsed";
		PrefHelper.getUserPreferences().putBoolean(propName, collapsed);
	}
	 
	/**
	 * Max number of columns available for the 
	 * favories grid
	 */
	private final static int MAX_GRID_COLS = 24;
	
	/**
	 * Location of the grid file
	 */
	private final static String GRID_FILE = "ipagrids.xml";

	/**
	 * Static ref to ipa map data
	 */
	private static IpaGrids grids;
	
	/**
	 * Load static IPA map data
	 * 
	 */
	private static IpaGrids getGridData() {
		ObjectFactory factory = new ObjectFactory();
		if(grids == null) {
			try {
				JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
				Unmarshaller unmarshaller = ctx.createUnmarshaller();
//				unmarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				grids = (IpaGrids)unmarshaller.unmarshal(
						IpaMap.class.getResource(GRID_FILE));
				
				// add generated grids
				generateMissingGrids(grids);
			} catch (JAXBException e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
				grids = factory.createIpaGrids();
			}
		}
		return grids;
	}
	
	private static void generateMissingGrids(IpaGrids grids) {
		// create a set of characters defined in the xml file
		final Set<Character> supportedChars = new HashSet<Character>();
		for(Grid g:grids.getGrid()) {
			for(Cell c:g.getCell()) {
					String cellData = c.getText();
					cellData = cellData.replaceAll("\\u25cc", "");
					supportedChars.add(cellData.charAt(0));
			}
		}
	
		final IPATokens tokens = IPATokens.getSharedInstance();
		
		// generate 'Other consonants' section
		final Set<Character> cSet = tokens.getCharactersForType(IPATokenType.CONSONANT);
		cSet.addAll(tokens.getCharactersForType(IPATokenType.GLIDE));
		final int w = 2;
		final int h = 2;
		final int maxX = 40;
		
		cSet.removeAll(supportedChars);
		if(cSet.size() > 0) {
			final Grid cGrid = generateGrid(cSet, "Other Consonants", "", "", w, h, maxX);
			grids.getGrid().add(cGrid);
		}
		
		// generate Other Vowels
		final Set<Character> vSet = tokens.getCharactersForType(IPATokenType.VOWEL);
		vSet.removeAll(supportedChars);
		if(vSet.size() > 0) {
			final Grid vGrid = generateGrid(vSet, "Other Vowels", "", "", w, h, maxX);
			grids.getGrid().add(vGrid);
		}
		
		// prefix diacritics
		final Set<Character> pdSet = tokens.getCharactersForType(IPATokenType.PREFIX_DIACRITIC);
		pdSet.removeAll(supportedChars);
		if(pdSet.size() > 0) {
			final Grid pdGrid = generateGrid(pdSet, "Other Prefix Diacritics", "", "\u25cc", w, h, maxX);
			grids.getGrid().add(pdGrid);
		}
		
		// suffix diacritics
		final Set<Character> sdSet = tokens.getCharactersForType(IPATokenType.SUFFIX_DIACRITIC);
		sdSet.removeAll(supportedChars);
		if(sdSet.size() > 0) {
			final Grid sdGrid = generateGrid(sdSet, "Other Suffix Diacritics", "\u25cc", "", w, h, maxX);
			grids.getGrid().add(sdGrid);
		}
		
		// combining diacritics
		final Set<Character> cdSet = tokens.getCharactersForType(IPATokenType.COMBINING_DIACRITIC);
		cdSet.removeAll(supportedChars);
		if(cdSet.size() > 0) {
			final Grid cdGrid = generateGrid(cdSet, "Other Combining Diacritics", "\u25cc", "", w, h, maxX);
			grids.getGrid().add(cdGrid);
		}
		
		// tone diacritics
		final Set<Character> tSet = tokens.getCharactersForType(IPATokenType.TONE);
		tSet.removeAll(supportedChars);
		if(tSet.size() > 0) {
			final Grid tGrid = generateGrid(tSet, "Other Tone Diacritics", "\u25cc", "", w, h, maxX);
			grids.getGrid().add(tGrid);
		}
		
		// everything else...
		final Set<Character> everything = new HashSet<Character>(tokens.getCharacterSet());
		everything.removeAll(supportedChars);
		everything.removeAll(cSet);
		everything.removeAll(vSet);
		everything.removeAll(pdSet);
		everything.removeAll(tSet);
		everything.removeAll(sdSet);
		everything.removeAll(cdSet);
		
		if(everything.size() > 0) {
			final Grid eGrid = generateGrid(everything, "Other Symbols", "", "", w, h, maxX);
			grids.getGrid().add(eGrid);
		}
	}
	
	private static Grid generateGrid(Collection<Character> chars, String title, String prefix, String suffix, int w, int h, int maxX) {
		final ObjectFactory factory = new ObjectFactory();
		final Grid retVal = factory.createGrid();
		int x = 0;
		int y = 0;
		
		for(Character c:chars) {
			final Cell cell = factory.createCell();
			cell.setX(x);
			cell.setY(y);
			cell.setW(w);
			cell.setH(h);
			cell.setText(prefix + c + suffix);
			
			retVal.getCell().add(cell);
			
			x += w;
			if(x > maxX) {
				x = 0;
				y += h;
			}
		}
		
		retVal.setName(title);
		retVal.setCols(maxX);
		retVal.setRows((x == 0 ? y : y+h));
		
		return retVal;
	}
	
	/**
	 * Create a search grid
	 */
	private static Grid buildSearchGrid(Collection<Cell> searchResults) {
		ObjectFactory factory = new ObjectFactory();
		Grid retVal = factory.createGrid();
		retVal.setCols(22*2);
		retVal.setName("Search Results (" + searchResults.size() + ")");
		
		for(Cell origCell:searchResults) {
			Cell copyCell = factory.createCell();
			copyCell.setText(origCell.getText());
			for(CellProp origProp:origCell.getProperty()) {
				CellProp newProp = factory.createCellProp();
				newProp.setName(origProp.getName());
				newProp.setContent(origProp.getContent());
				copyCell.getProperty().add(newProp);
			}
			retVal.getCell().add(copyCell);
		}
//		retVal.getCell().addAll(searchResults);
		
		// setup cell locations and grid size
		int numRows = ((retVal.getCell().size() / 22) + (retVal.getCell().size() % 22 > 0 ? 1 : 0)) * 2;
		retVal.setRows(numRows);
		
		for(int i = 0; i < retVal.getCell().size(); i++) {
			int row = i / 22;
			int col = i % 22;
			Cell cell = retVal.getCell().get(i);
			cell.setX(col * 2);
			cell.setY(row * 2);
			cell.setH(2);
			cell.setW(2);
		}
		
		return retVal;
	}
	
	/**
	 * Perform a search
	 * 
	 * @param search - a set of search terms (separated by ',')
	 * @return a set of results
	 */
	private static Set<Cell> performSearch(SearchType st, String search) {
		Set<Cell> retVal = new HashSet<Cell>();
		
		FeatureMatrix fm = FeatureMatrix.getInstance();
		search = StringUtils.strip(search.toLowerCase());
		if(search.length() > 0) {
			for(Grid grid:getGridData().getGrid()) {
				for(Cell cell:grid.getCell()) {
	//				for(String searchTerm:searchTerms) {
	//					searchTerm = StringUtils.strip(searchTerm).toLowerCase();
	//					if(searchTerm.length() == 0) continue; 
						
						boolean addToResults = false;
						
						if(st == SearchType.ALL) {
							addToResults = cell.getText().toLowerCase().contains(search);
						}
						
						if(st == SearchType.ALL) {
							for(CellProp prop:cell.getProperty()) {
								if(prop.getName().equalsIgnoreCase("description")) continue;
								addToResults |= prop.getContent().toLowerCase().contains(search);
							}
						}
						
						
						if(st == SearchType.ALL || 
								st == SearchType.FEATURES) {
							String[] asFeature = search.split(",");
							FeatureSet fs = new FeatureSet();
							for(String feature:asFeature) {
								if(feature == null) break;
								Feature f = FeatureMatrix.getInstance().getFeature(StringUtils.strip(feature));
								if(f != null) {
									fs.union(f.getFeatureSet());
								}
							}
							FeatureSet charFs = new FeatureSet();
							// check feature set(s)
							for(Character c:cell.getText().toCharArray()) {
								FeatureSet cFs = fm.getFeatureSet(c);
								if(cFs != null) {
									charFs.union(cFs);
								}
							}
							FeatureSet intersectFs = FeatureSet.intersect(charFs, fs);
							if(intersectFs.getFeatures().size() > 0 && intersectFs.getFeatures().size() == fs.getFeatures().size()) {
								addToResults |= true;
							}
						}
	//					
						if(addToResults)
							retVal.add(cell);
	//				}
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Static ref to favorites data
	 */
	private static IpaGrids favGrid;
	
	/**
	 * Load favorites IPA map data
	 */
	private static IpaGrids getFavData() {
		final ObjectFactory factory = new ObjectFactory();
		
		final String favXML = PrefHelper.get(FAVORITES_PROP, null);
		
		// try to load file
		if(favGrid == null && favXML != null) {
			try {
				JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
				Unmarshaller unmarshaller = ctx.createUnmarshaller();
				favGrid = (IpaGrids)unmarshaller.unmarshal(new ByteArrayInputStream(favXML.getBytes()));
			} catch (JAXBException e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
			}
		}
		
		// create a default grid if still null
		if(favGrid == null) {
			favGrid = factory.createIpaGrids();
			Grid fg = factory.createGrid();
			fg.setCellheight(10);
			fg.setCellwidth(10);
			fg.setCols(MAX_GRID_COLS);
			fg.setRows(2);
			fg.setName("Favorites");
			favGrid.getGrid().add(fg);
		}
		
		return favGrid;
	}
	
	/**
	 * Saves the favorites data
	 * 
	 */
	private static void saveFavData() 
		throws IOException {
		final ObjectFactory factory = new ObjectFactory();
		final IpaGrids favMap = getFavData();
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			final JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
			final Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			
			marshaller.marshal(favMap, bos);
			
			PrefHelper.getUserPreferences().put(FAVORITES_PROP, bos.toString("UTF-8"));
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}
	
	/**
	 * Get font pref
	 */
	private static Font getFontPref() {
		return PrefHelper.getFont(FONT_PROP, Font.decode(DEFAULT_FONT));
	}
	
	/**
	 * set font pref
	 */
	private static void setFontPref(Font font) {
		PrefHelper.getUserPreferences().put(FONT_PROP, font.toString());
	}
	
	/**
	 * The scroller
	 */
	private JScrollPane scrollPane;
	
	/**
	 * Scale
	 */
	private float scale;
	
	/**
	 * Fading
	 */
	private boolean fadeWindow;
	private float fadeOpacity;
	
	/**
	 * List of grid panels
	 */
	private List<JXCollapsiblePane> gridPanels = new ArrayList<JXCollapsiblePane>();
	
	/**
	 * List of toggle buttons
	 */
	private List<JXButton> toggleButtons = new ArrayList<JXButton>();
	
	/**
	 * Favorites panel
	 */
	private JXButton favToggleButton = null;
	private JXCollapsiblePane favPanel = null;
	private Container favContainer = null;
	
	/**
	 * Search panel
	 */
	private JXButton searchToggleButton = null;
	private JXCollapsiblePane searchPanel = null;
	private Container searchContainer = null;
	
	private List<Cell> searchResults = 
		Collections.synchronizedList(new ArrayList<Cell>());
	
	/**
	 * Last used items
	 */
	private final static int MAX_LAST_USED_SIZE = 20;
	private UsageList lastUsed = new UsageList(MAX_LAST_USED_SIZE);
	
	private boolean highlightRecent;
	
	/**
	 * Info label for header view
	 */
	private JLabel infoLabel;
	
	/**
	 * Status panel
	 */
	private JXStatusBar statusBar;
	
	/**
	 * Base font
	 */
	private Font baseFont;
	
	/**
	 * map listeners
	 */
	private List<IpaMapListener> listeners = 
		Collections.synchronizedList(new ArrayList<IpaMapListener>());
	
	public void addListener(IpaMapListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeListener(IpaMapListener listener) {
		listeners.remove(listener);
	}
	
	public void fireMapEvent(String txt) {
		IpaMapListener ls[] = listeners.toArray(new IpaMapListener[0]);
		for(IpaMapListener l:ls) {
			l.ipaMapEvent(txt);
		}
	}
	
	public IpaMap() {
		super();
		
		setFont(getFontPref());
		
		scale = getSavedScale();
		highlightRecent = getSavedHighlightRecent();
		lastUsed = getUsageList();
		fadeWindow = getSavedFadeWindow();
		fadeOpacity = getSavedFadeOpacity();
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		// favorites
		IpaGrids favData = getFavData();
		final Grid fg = favData.getGrid().get(0);
		favPanel = getGridPanel(fg);
		favPanel.setCollapsed(getSavedSectionToggle(fg.getName()));
		favToggleButton = getToggleButton(fg, favPanel);
		favToggleButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setSavedSectionToggle(fg.getName(), !getSavedSectionToggle(fg.getName()));
			}
		});
		
		JPanel favSection = new JPanel(new VerticalLayout(0));
		favSection.add(favToggleButton);
		favSection.add(favPanel);
		favContainer = favSection;
		
		// search
		Grid emptyGrid = (new ObjectFactory()).createGrid();
		emptyGrid.setName("Search Results (0)");
		emptyGrid.setRows(0);
		emptyGrid.setCols(0);
		
		searchPanel = getGridPanel(emptyGrid);
		searchToggleButton = getToggleButton(emptyGrid, searchPanel);
		
		final JButton searchButton = new JButton("Search");
		searchButton.putClientProperty("JComponent.sizeVariant", "small");
		
		searchButton.addActionListener( this::showSearchFrame );
		
		JPanel searchSection = new JPanel(new VerticalLayout(0));
		searchSection.add(searchButton);
		searchSection.add(searchToggleButton);
		searchContainer = searchSection;
		
		// static content
		final JPanel centerPanel = new JPanel(new VerticalLayout(0));
		IpaGrids grids = getGridData();
		for(final Grid grid:grids.getGrid()) {
			final JXCollapsiblePane cp = getGridPanel(grid);
			
			cp.setCollapsed(getSavedSectionToggle(grid.getName()));
			JXButton toggleBtn = getToggleButton(grid, cp);
			toggleBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setSavedSectionToggle(grid.getName(), !getSavedSectionToggle(grid.getName()));
				}
			});
			
			toggleButtons.add(toggleBtn);
			
			centerPanel.add(toggleBtn);
			centerPanel.add(cp);
			
			gridPanels.add(cp);
		}
		
		scrollPane = new JScrollPane(centerPanel);
		scrollPane.setAutoscrolls(true);
		scrollPane.setWheelScrollingEnabled(true);
//		scrollPane.setViewportView(centerPanel);
		add(scrollPane, BorderLayout.CENTER);
		
//		JPanel btmPanel = new JPanel(new BorderLayout());
//		scalePanel.add(smallLbl, BorderLayout.WEST);
//		scalePanel.add(scaleSlider, BorderLayout.CENTER);
//		scalePanel.add(largeLbl, BorderLayout.EAST);
		
		final JButton scrollBtn = new JButton("-");
		scrollBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
//				popupMenu.show(scrollBtn, 0, scrollBtn.getHeight());
				JPopupMenu ctxMenu = new JPopupMenu();
				setupContextMenu(ctxMenu, scrollBtn);
				ctxMenu.show(scrollBtn, 0, scrollBtn.getHeight());
			}
		});
		
//		Font infoFont = new Font("Courier New", Font.PLAIN, 12);
		infoLabel = new JLabel();
		infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC));
		infoLabel.setText("[]");
		infoLabel.setOpaque(false);
		
		statusBar = new JXStatusBar();
		statusBar.setLayout(new BorderLayout());
		statusBar.add(infoLabel, BorderLayout.CENTER);
		
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, scrollBtn);
		
		add(statusBar, BorderLayout.SOUTH);
		
		JPanel topPanel = new JPanel(new VerticalLayout(0));
		topPanel.add(searchSection);
		topPanel.add(favSection);
		add(topPanel, BorderLayout.NORTH);
		
	}
	
	private IpaMapSearchField createSearchField() {
		final IpaMapSearchField searchField = new IpaMapSearchField();
		searchField.setPrompt("Search Glyphs");
		searchField.setFont(getFont().deriveFont(12.0f));
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if(searchField.getState() == FieldState.INPUT)
					updateSearchPanel(searchField.getSearchType(), searchField.getText());
				else
					updateSearchPanel(searchField.getSearchType(), "");
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if(searchField.getState() == FieldState.INPUT)
					updateSearchPanel(searchField.getSearchType(), searchField.getText());
				else
					updateSearchPanel(searchField.getSearchType(), "");
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				
			}
		});
		
		searchField.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				if(arg0.getPropertyName().equals(IpaMapSearchField.SEARCH_TYPE_PROP)) {
					if(searchField.getText().length() > 0) {
						updateSearchPanel(searchField.getSearchType(), searchField.getText());
					}
				}
			}
		});
		return searchField;
	}
	
	private JFrame searchFrame = null;
	private IpaMapSearchField searchField = null;
	public void showSearchFrame(ActionEvent ae) {
		if(searchFrame == null) {
			searchFrame = new JFrame("IPA Map : Search");
			searchFrame.setAlwaysOnTop(true);
			searchFrame.setUndecorated(true);
			searchFrame.getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
			
			searchField = createSearchField();
			searchFrame.add(searchField);
			
			searchField.getTextField().addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						searchFrame.setVisible(false);
					}
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
				
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						searchFrame.setVisible(false);
					}
				}
			});
			searchField.getEndButton().addActionListener( evt -> {
				searchFrame.setVisible(false);
			});
			
			searchFrame.addWindowFocusListener(new WindowFocusListener() {
				
				@Override
				public void windowLostFocus(WindowEvent e) {
					searchFrame.setVisible(false);
				}
				
				@Override
				public void windowGainedFocus(WindowEvent e) {
					
				}
			});
			
		}
		
		final JComponent source = (JComponent)ae.getSource();
		searchFrame.setSize(source.getWidth(), source.getHeight());
		
		Point sourcePt = ((JComponent)ae.getSource()).getLocationOnScreen();
		searchFrame.setLocation(sourcePt.x, sourcePt.y);
		searchFrame.setVisible(true);
		
		searchFrame.requestFocus();
		searchField.getTextField().requestFocus();
	}
	
	public float getScale() {
		return this.scale;
	}
	
	public void setScale(float s) {
		float oldScale = this.scale;
		this.scale = s;
		super.firePropertyChange(SCALE_PROP, oldScale, this.scale);
	}
	
	public void onGoto(PhonActionEvent pae) {
		JComponent comp = (JComponent)pae.getData();
		scrollPane.getViewport().setViewPosition(
				new Point(comp.getBounds().x, comp.getBounds().y));
	}
	
	private JXButton getToggleButton(Grid grid, JXCollapsiblePane cp) {
		Action toggleAction = cp.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
		                      UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
		                      UIManager.getIcon("Tree.collapsedIcon"));
		toggleAction.putValue(Action.NAME, grid.getName());

		JXButton btn = new JXButton(toggleAction) {
			@Override
			public Insets getInsets() {
				Insets retVal = super.getInsets();
				
				retVal.top = 0;
				retVal.bottom = 0;
				
				return retVal;
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, 20);
			}
			
			
		};
		
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		
		btn.setBackgroundPainter(new Painter<JXButton>() {
			
			@Override
			public void paint(Graphics2D g, JXButton object, int width, int height) {
				MattePainter mp = new MattePainter(UIManager.getColor("Button.background"));
				mp.paint(g, object, width, height);
			}
		});
		
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setBorderPainted(false);
		btn.setFocusable(false);
		
		btn.putClientProperty("JComponent.sizeVariant", "small");

		btn.revalidate();
		
		return btn;
	}
	
	@Override
	public void setFont(Font f) {
		super.setFont(f);
		_cFont = null;
		baseFont = f;
	}
	
	private Font _cFont = null;
	@Override
	public Font getFont() {
		if(_cFont == null) {
			int fmin = 12;
			int fmax = 36;
			
			float fontSize = 
				fmin + (scale) * (fmax - fmin);
			if(baseFont == null)
				baseFont = Font.decode(null);
			_cFont = baseFont.deriveFont(fontSize);
		}
		return _cFont;
	}

	public boolean isHighlightRecent() {
		return highlightRecent;
	}

	public void setHighlightRecent(boolean highlightRecent) {
		boolean oldHighlight = this.highlightRecent;
		this.highlightRecent = highlightRecent;
		super.firePropertyChange(HIGHLIGHT_RECENT_PROP, oldHighlight, highlightRecent);
	}

	private final JLabel cellTemplate = new JLabel();
	/**
	 * Calculate cell dimensions
	 */
	public Dimension getCellDimension() {
		Dimension retVal = new Dimension(0, 0);
		
		String tStr = (char)0x25cc + "" + (char)0x1d50;
		cellTemplate.setText(tStr);
		cellTemplate.setFont(getFont());
		
		retVal = cellTemplate.getPreferredSize();
		retVal.height /= 2;
		retVal.width /= 2;
		retVal.height += 2;
		retVal.width += 2;
//		Rectangle2D boundRect = fm.getStringBounds(tStr, getGraphics());
//		
////		int cellHeight = fm.getHeight() / 2 + 2;
////		int cellWidth = Math.round( (3.0f * cellHeight) / (4.0f) );
//		retVal.height = (int)Math.round(boundRect.getHeight()/2)+4;
//		retVal.width = (int)Math.round(boundRect.getWidth()/2)+2;
		
//		cachedDimension = retVal;
		
		return retVal;
	}
	
	/**
	 * Get preferred size.  Pref size is to see the vowels
	 * and consonants at the same time.
	 * @return preferred size
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension prefSize = super.getPreferredSize();
		
		int prefHeight = 0;
		if(favContainer != null) {
			prefHeight += favContainer.getPreferredSize().height;
		}
		
		// add size of first 3 grid panels
		if(gridPanels.size() >= 3) {
			for(int i = 0; i < 3; i++) {
				JXCollapsiblePane gridPanel = gridPanels.get(i);
				boolean wasCollapsed = gridPanel.isCollapsed();
				gridPanel.setCollapsed(false);
				prefHeight += gridPanel.getPreferredSize().height;
				gridPanel.setCollapsed(wasCollapsed);
				prefHeight += toggleButtons.get(i).getPreferredSize().height;
			}
		}
		
		if(scrollPane != null) {
			prefHeight += scrollPane.getHorizontalScrollBar().getPreferredSize().height;
		}
		
		if(statusBar != null) {
			prefHeight += statusBar.getPreferredSize().height;
		}
		
		if(prefHeight > 0) 
			prefSize.height = prefHeight + /* some extra space */ (6 * getCellDimension().height);
		
		return prefSize;
	}
	
	private JXCollapsiblePane getGridPanel(Grid grid) {
		Font tFont = getFont();
		
		Dimension cellDim = getCellDimension();
		int cellWidth = cellDim.width;
		int cellHeight = cellDim.height;
		
		final JXCollapsiblePane retVal = new JXCollapsiblePane();
		final JXPanel contentPanel = new JXPanel();
		retVal.setContentPane(contentPanel);
		
		GridCellLayout layout = new GridCellLayout(
				grid.getRows(), grid.getCols(),
				cellWidth, cellHeight);
		contentPanel.setLayout(layout);
		
		contentPanel.setOpaque(true);
		contentPanel.setBackground(Color.white);
		
		for(Cell cell:grid.getCell()) {
			JButton mapButton = getMapButton(cell);
			mapButton.setFont(tFont);
			
			GridCellConstraint cc = GridCellConstraint.xywh(cell.getX(), cell.getY(), cell.getW(), cell.getH());
			retVal.getContentPane().add(mapButton, cc);
			
		}
		
		contentPanel.setBackgroundPainter(new StripePainter());
		
		if(grid.getName().equalsIgnoreCase("vowels")) {
			CompoundPainter<JXPanel> cmpPainter = 
				new CompoundPainter<JXPanel>(contentPanel.getBackgroundPainter(), 
						new VowelsBGPainter());
			contentPanel.setBackgroundPainter(cmpPainter);
		}
		
		contentPanel.addMouseListener(new ContextMouseHandler());
		
		retVal.setAnimated(false);
		retVal.setFocusable(false);
		
		return retVal;
	}

	private JButton getMapButton(Cell cell) {
		PhonUIAction action = new PhonUIAction(this, "onCellClicked", cell);
		action.putValue(Action.NAME, cell.getText());
		action.putValue(Action.SHORT_DESCRIPTION, cell.getText());
		
		JButton retVal = new CellButton(cell);
		retVal.setAction(action);
		
		final Cell cellData = cell;
		retVal.addMouseListener(new MouseInputAdapter() {
			
			@Override
			public void mouseEntered(MouseEvent me) {
				String txt = cellData.getText();
				txt = txt.replaceAll("\u25cc", "");
				
				final IPATokens tokens = IPATokens.getSharedInstance();
				String uniVal = "";
				String name = "";
				for(Character c:txt.toCharArray()) {
					String cText = "0x" + StringUtils.leftPad(Integer.toHexString((int)c), 4, '0');
					uniVal += (uniVal.length() > 0 ? " + " : "") + cText;
					
					String cName = tokens.getCharacterName(c);
					name += (name.length() > 0 ? " + " : "") + cName;
				}
				String infoTxt = "[" + uniVal + "] " + name;
				infoLabel.setText(infoTxt);
			}
			
			@Override
			public void mouseExited(MouseEvent me) {
				infoLabel.setText("[]");
			}
		});
		
		retVal.addMouseListener(new ContextMouseHandler());
		
		// set tooltip delay to 10 minutes for the buttons
		retVal.addMouseListener(new MouseAdapter() {
		    final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
		    final int dismissDelayMinutes = (int) TimeUnit.MINUTES.toMillis(10); // 10 minutes
		    @Override
		    public void mouseEntered(MouseEvent me) {
		        ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
		    }
		 
		    @Override
		    public void mouseExited(MouseEvent me) {
		        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
		    }
		});
		
		return retVal;
	}
	
	/**
	 * Returns true if the given cell is in
	 * the favorite list.
	 * @param cell
	 * @return
	 */
	private boolean isInFavorites(Cell cell) {
		IpaGrids favData = getFavData();
		
		boolean retVal = false;
		Grid fg = favData.getGrid().get(0);
		for(Cell c:fg.getCell()) {
			if(c.getText().equals(cell.getText())) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}
	
	/**
	 * Create the context menu based on source component
	 */
	public void setupContextMenu(JPopupMenu menu, JComponent comp) {
		final CommonModuleFrame parentFrame = 
				(CommonModuleFrame)SwingUtilities.getAncestorOfClass(CommonModuleFrame.class, comp);
		if(parentFrame != null) {
			final PhonUIAction toggleAlwaysOnTopAct = 
					new PhonUIAction(parentFrame, "setAlwaysOnTop", !parentFrame.isAlwaysOnTop());
			toggleAlwaysOnTopAct.putValue(PhonUIAction.NAME, "Always on top");
			toggleAlwaysOnTopAct.putValue(PhonUIAction.SELECTED_KEY, parentFrame.isAlwaysOnTop());
			final JCheckBoxMenuItem toggleAlwaysOnTopItem = new JCheckBoxMenuItem(toggleAlwaysOnTopAct);
			menu.add(toggleAlwaysOnTopItem);
		}
		
		// button options first
		if(comp instanceof CellButton) {
			CellButton btn = (CellButton)comp;
			Cell cell = btn.cell;
			
			// copy to clipboard options
			String cellData = cell.getText().replaceAll(""+(char)0x25cc, "");
			PhonUIAction copyToClipboardAct = new PhonUIAction(this, "onCopyToClipboard", cellData);
			copyToClipboardAct.putValue(PhonUIAction.NAME, "Copy character (" + cell.getText() + ")");
			JMenuItem copyToClipboardItem = new JMenuItem(copyToClipboardAct);
			menu.add(copyToClipboardItem);
			
			String htmlVal = new String();
			for(Character c:cellData.toCharArray()) {
				htmlVal += 
					"&#" + (int)c + ";";
			}
			PhonUIAction copyHTMLToClipboardAct = new PhonUIAction(this, "onCopyToClipboard", htmlVal);
			copyHTMLToClipboardAct.putValue(PhonUIAction.NAME, "Copy as HTML (" + htmlVal + ")");
			JMenuItem copyHTMLToClipboardItem = new JMenuItem(copyHTMLToClipboardAct);
			menu.add(copyHTMLToClipboardItem);
		
			String hexVal = new String();
			for(Character c:cellData.toCharArray()) {
				hexVal += 
					(hexVal.length() > 0  ? " " : "") + Integer.toHexString((int)c);
			}
			hexVal = hexVal.toUpperCase();
			PhonUIAction copyHEXToClipboardAct = new PhonUIAction(this, "onCopyToClipboard", hexVal);
			copyHEXToClipboardAct.putValue(PhonUIAction.NAME, "Copy as Unicode HEX (" + hexVal + ")");
			JMenuItem copyHEXToClipboardItem = new JMenuItem(copyHEXToClipboardAct);
			menu.add(copyHEXToClipboardItem);
			
			menu.addSeparator();
			if(isInFavorites(cell)) {
				PhonUIAction removeFromFavAct = new PhonUIAction(this, "onRemoveCellFromFavorites", cell);
				removeFromFavAct.putValue(Action.NAME, "Remove from favorites");
				removeFromFavAct.putValue(Action.SHORT_DESCRIPTION, "Remove button from list of favorites");
				JMenuItem removeFromFavItem = new JMenuItem(removeFromFavAct);
				menu.add(removeFromFavItem);
			} else {
				PhonUIAction addToFavAct = new PhonUIAction(this, "onAddCellToFavorites", cell);
				addToFavAct.putValue(Action.NAME, "Add to favorites");
				addToFavAct.putValue(Action.SHORT_DESCRIPTION, "Add button to list of favorites");
				JMenuItem addToFavItem = new JMenuItem(addToFavAct);
				menu.add(addToFavItem);
			}
			menu.addSeparator();
		}
		
		// section scroll-tos
		JMenuItem gotoTitleItem = new JMenuItem("Scroll to:");
		gotoTitleItem.setEnabled(false);
		menu.add(gotoTitleItem);
		
		for(JXButton toggleBtn:toggleButtons) {
			PhonUIAction gotoAct = new PhonUIAction(this, "onGoto", toggleBtn);
			gotoAct.putValue(Action.NAME, toggleBtn.getText());
			gotoAct.putValue(Action.SHORT_DESCRIPTION, "Scroll to " + toggleBtn.getText());
			JMenuItem gotoItem = new JMenuItem(gotoAct);
			menu.add(gotoItem);
		}
		
		menu.addSeparator();
		
		// setup font scaler
		final JLabel smallLbl = new JLabel("A");
		smallLbl.setFont(getFont().deriveFont(12.0f));
		smallLbl.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel largeLbl = new JLabel("A");
		largeLbl.setFont(getFont().deriveFont(24.0f));
		largeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		final JSlider scaleSlider = new JSlider(1, 101);
		scaleSlider.setValue((int)(scale*100));
		scaleSlider.setMajorTickSpacing(20);
		scaleSlider.setMinorTickSpacing(10);
		scaleSlider.setSnapToTicks(true);
		scaleSlider.setPaintTicks(true);
		scaleSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int sliderVal = scaleSlider.getValue();
				
				float scale = (float)sliderVal/(float)100;

				_cFont = null;
				
				setSavedScale(scale);
				setScale(scale);				

				
			}
		});
		
		FormLayout scaleLayout = new FormLayout(
				"3dlu, center:pref, fill:pref:grow, center:pref, 3dlu",
				"pref");
		CellConstraints cc = new CellConstraints();
		JPanel scalePanel = new JPanel(scaleLayout) {
			@Override
			public Insets getInsets() {
				Insets retVal = super.getInsets();
				
				retVal.left += UIManager.getIcon("Tree.collapsedIcon").getIconWidth();
				
				return retVal;
			}
		};
		scalePanel.add(smallLbl, cc.xy(2, 1));
		scalePanel.add(scaleSlider, cc.xy(3, 1));
		scalePanel.add(largeLbl, cc.xy(4, 1));
		
		JMenuItem scaleItem = new JMenuItem("Font size");
		scaleItem.setEnabled(false);
		menu.add(scaleItem);
		menu.add(scalePanel);
		
		menu.addSeparator();
		
		// highlighting
		PhonUIAction onToggleHighlightAct = 
			new PhonUIAction(this, "onToggleHighlightRecent");
		onToggleHighlightAct.putValue(PhonUIAction.NAME, "Highlight recently used");
		onToggleHighlightAct.putValue(PhonUIAction.SELECTED_KEY, isHighlightRecent());
		JCheckBoxMenuItem onToggleHighlightItm = new JCheckBoxMenuItem(onToggleHighlightAct);
		menu.add(onToggleHighlightItm);
	}
	
	/**
	 * Update display
	 * Called when font or scale changes
	 */
	public void updateDisplay() {
		Font tFont = getFont();
		setFont(tFont);
		FontMetrics fm = getFontMetrics(tFont);
		
		Dimension cellDim = getCellDimension();
		int cellHeight = cellDim.height;
		int cellWidth = cellDim.width;

		GridCellLayout favLayout = (GridCellLayout)favPanel.getContentPane().getLayout();
		favLayout.setCellWidth(cellWidth);
		favLayout.setCellHeight(cellHeight);
		favPanel.invalidate();
		favPanel.getContentPane().invalidate();
		favContainer.validate();
		
		GridCellLayout searchLayout = (GridCellLayout)searchPanel.getContentPane().getLayout();
		searchLayout.setCellWidth(cellWidth);
		searchLayout.setCellHeight(cellHeight);
		searchPanel.invalidate();
		searchPanel.getContentPane().invalidate();
		searchContainer.validate();
		
		for(JXCollapsiblePane cp:gridPanels) {
			GridCellLayout layout = (GridCellLayout)cp.getContentPane().getLayout();
			
			layout.setCellWidth(cellWidth);
			layout.setCellHeight(cellHeight);
			cp.invalidate();
			cp.getContentPane().invalidate();
			cp.revalidate();
		}
		
		scrollPane.invalidate();
		scrollPane.revalidate();
	}
	
	/**
	 * Toggle highlighting recent
	 */
	public void onToggleHighlightRecent(PhonActionEvent pae) {
		boolean newVal = !isHighlightRecent();
		setHighlightRecent(newVal);
		setSavedHighlightRecent(newVal);
		
		repaint();
	}
	
	/**
	 * Cell button clicked
	 */
	public void onCellClicked(PhonActionEvent pae) {
		Cell cell = (Cell)pae.getData();
		String btnData = cell.getText();
		btnData = btnData.replaceAll("\\u25cc", "");
		
		lastUsed.add(cell.getText());
		setUsageList(lastUsed);
		IpaMap.this.repaint();
		
		fireMapEvent(btnData);
	}
	
	/**
	 * Copy text to system clipboard
	 */
	public void onCopyToClipboard(PhonActionEvent pae) {
		String txt = pae.getData().toString();
		Transferable toClipboard = 
			new StringSelection(txt);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(toClipboard, this);
	}
	
	/**
	 * Add cell to favorites grid
	 * 
	 */
	public void onAddCellToFavorites(PhonActionEvent pae) {
		Cell cell = (Cell)pae.getData();
		
		IpaGrids favData = getFavData();
		Grid fg = favData.getGrid().get(0);
		
		// make sure we don't have a duplicate
		for(Cell c:fg.getCell()) {
			if(cell.getText().equals(c.getText())) {
				// TODO make button 'flash'
				return;
			}
		}
		
		ObjectFactory factory = new ObjectFactory();
		Cell copyCell = factory.createCell();
		copyCell.setH(2);
		copyCell.setW(2);
		int row = fg.getCell().size() / 22 * 2;
		int col = fg.getCell().size() % 22 * 2;
		copyCell.setX(col);
		copyCell.setY(row);
		
		copyCell.setText(cell.getText());
		
		fg.setRows(row+2);
		
		for(CellProp cellProp:cell.getProperty()) {
			CellProp copyProp = factory.createCellProp();
			copyProp.setName(cellProp.getName());
			copyProp.setContent(cellProp.getContent());
			
			copyCell.getProperty().add(copyProp);
		}
		
		fg.getCell().add(copyCell);
		
		try {
			saveFavData();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}
		
		updateFavoritesPanel();
	}
	
	private void updateFavoritesPanel() {
		JXButton oldToggle = favToggleButton;
		JXCollapsiblePane oldPanel = favPanel;
		
		IpaGrids favData = getFavData();
		Grid fg = favData.getGrid().get(0);
		
		favPanel = getGridPanel(fg);
		favToggleButton = getToggleButton(fg, favPanel);
		
		favContainer.remove(oldToggle);
		favContainer.remove(oldPanel);
		
		favContainer.add(favToggleButton);
		favContainer.add(favPanel);
		
		favContainer.validate();
	}
	
	private void updateSearchPanel(SearchType st, String query) {
		JXButton oldToggle = searchToggleButton;
		JXCollapsiblePane oldPanel = searchPanel;

		Collection<Cell> searchResults = performSearch(st, query);
		this.searchResults.clear();
		this.searchResults.addAll(searchResults);
		Collections.sort(this.searchResults, new CellComprator());
		Grid grid = buildSearchGrid(this.searchResults);
		searchPanel = getGridPanel(grid);
		searchToggleButton = getToggleButton(grid, searchPanel);
		
		searchContainer.remove(oldToggle);
		searchContainer.remove(oldPanel);
		
		searchContainer.add(searchToggleButton);
		searchContainer.add(searchPanel);
		
		searchContainer.validate();
		
		repaint();
	}
	
	/**
	 * Remove cell from the favorites list
	 */
	public void onRemoveCellFromFavorites(PhonActionEvent pae) {
		Cell cell = (Cell)pae.getData();
		
		IpaGrids favData = getFavData();
		Grid fg = favData.getGrid().get(0);
		
		Cell toRemove = null;
		for(Cell c:fg.getCell()) {
			if(cell.getText().equals(c.getText())) {
				toRemove = c;
			}
		}
		
		if(toRemove != null) {
			fg.getCell().remove(toRemove);
			
			// fix cell locations
			int numRows = 0;
			for(int i = 0; i < fg.getCell().size(); i++) {
				int row = i / 22 * 2;
				numRows = Math.max(numRows, row+2);
				int col = i % 22 * 2;
				fg.getCell().get(i).setX(col);
				fg.getCell().get(i).setY(row);
			}
			fg.setRows(numRows);
			
			try {
				saveFavData();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.warning(e.toString());
			}
		
			updateFavoritesPanel();
		}
	}
	
	/**
	 * stripe background painter
	 */
	private class StripePainter implements Painter<JXPanel> {
		@Override
		public void paint(Graphics2D g, JXPanel contentPanel,
				int width, int height) {
			GridCellLayout layout = (GridCellLayout)contentPanel.getLayout();
			
			// only paint stripes if we have more than 2 rows of glyphs
			if(layout.getNumRows() > 4) {
				int cellWidth = layout.getCellWidth();
				int cellHeight = layout.getCellHeight();
				
				// alternate row colouring
				boolean alt = false;
				for(int i = 0; i < layout.getNumRows(); i+=2) {
					g.setColor((alt ? PhonGuiConstants.PHON_UI_STRIP_COLOR : Color.white));
					alt = !alt;
					g.fillRect(0, i*cellHeight, width, 2*cellHeight);
				}
			}
		}
	}
	
	/**
	 * background painter for vowels grid
	 */
	private class VowelsBGPainter implements Painter<JXPanel> {
		
		private Point2D[] dotPts = {
				new Point2D.Float(2.5f, 1f),
				new Point2D.Float(12.5f, 1f),
				new Point2D.Float(21.5f, 1f),
				new Point2D.Float(4.5f, 5.0f),
				new Point2D.Float(13.5f, 5.0f),
				new Point2D.Float(21.5f, 5.0f),
				new Point2D.Float(6.5f, 9.0f),
				new Point2D.Float(14.5f, 9.0f),
				new Point2D.Float(21.5f, 9.0f),
				new Point2D.Float(8.5f, 13.0f),
				new Point2D.Float(21.5f, 13.0f)
		};
		
		private LineSegment lines[] = {
				// far-left diag line
				new LineSegment(dotPts[0], dotPts[9]),
				
				// middle diag line
				new LineSegment(dotPts[1], new Point2D.Float(13.75f, 6.0f)),
				new LineSegment(new Point2D.Float(14.25f, 8.0f), new Point2D.Float(14.75f, 10.0f)),
				new LineSegment(new Point2D.Float(15.25f, 12.0f), new Point2D.Float(15.5f, 13.0f)),
				
				// right vert line
				new LineSegment(dotPts[2], dotPts[10]),
				
				// top horizontal line
				new LineSegment(new Point2D.Float(5.0f, 1.0f), new Point2D.Float(10.0f, 1.0f)),
				new LineSegment(new Point2D.Float(15.0f, 1.0f), new Point2D.Float(19.0f, 1.0f)),
				
				// 2nd horizontal line
				new LineSegment(new Point2D.Float(7.0f, 5.0f), new Point2D.Float(11.0f, 5.0f)),
				new LineSegment(new Point2D.Float(16.0f, 5.0f), new Point2D.Float(19.0f, 5.0f)),
				
				// 3rd horizontal line
				new LineSegment(new Point2D.Float(9.0f, 9.0f), new Point2D.Float(12.0f, 9.0f)),
				new LineSegment(new Point2D.Float(17.0f, 9.0f), new Point2D.Float(19.0f, 9.0f)),
				
				// bottom horizontal line
				new LineSegment(new Point2D.Float(11.0f, 13.0f), new Point2D.Float(19.0f, 13.0f)),
				
				// extra-vowels line
//				new LineSegment(new Point2D.Float(26.0f, 6.0f), new Point2D.Float(26.0f, 8.0f))
		};

		@Override
		public void paint(Graphics2D arg0, JXPanel arg1, int arg2, int arg3) {
			GridCellLayout grid = (GridCellLayout)arg1.getLayout();
			
			int gridw = grid.getCellWidth();
			int gridh = grid.getCellHeight();
			
			// draw points
			Graphics2D g2d = (Graphics2D)arg0;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			
			g2d.setColor(Color.lightGray);
			
			for(Point2D dotPt:this.dotPts) {
				Ellipse2D ellipse = convertPointToCircle(dotPt, gridw, gridh);
				g2d.fill(ellipse);
			}
			
			Stroke s = new BasicStroke(3.0f*getScale());
			g2d.setStroke(s);
			for(LineSegment line:lines) {
				Line2D l = new Line2D.Float(
						convertGridPointToView(line.getObj1(), gridw, gridh), 
						convertGridPointToView(line.getObj2(), gridw, gridh) );
				g2d.draw(l);
			}
		}
		
		private Point2D convertGridPointToView(Point2D p, int cw, int ch) {
			Point2D.Float retVal = new Point2D.Float();
			
			retVal.x = (float)p.getX() * cw;
			retVal.y = (float)p.getY() * ch;
			
			return retVal;
		}
		
		private Ellipse2D convertPointToCircle(Point2D p, int cw, int ch) {
			Ellipse2D.Float retVal = new Ellipse2D.Float();
			
			Point2D.Float centre = new Point2D.Float();
			
			float dotSize = 12.0f * getScale();
			
			centre.x = (float)((float)cw * p.getX());
			centre.y = (float)((float)ch * p.getY());
			
			retVal.x = (float)centre.x - dotSize/2;
			retVal.width = dotSize;
			retVal.y = (float)centre.y - dotSize/2;
			retVal.height = dotSize;
			
			return retVal;
		}
		
		
	}
	
	private class ConsonantsBgPainter implements Painter<JXPanel> {
		
		/**
		 * Lines
		 */
		private LineSegment lines[] = {
				new LineSegment(new Point2D.Float(0.0f, 2.0f), new Point2D.Float(2.0f, 2.0f)),
				new LineSegment(new Point2D.Float(4.0f, 2.0f), new Point2D.Float(12.0f, 2.0f))
		};

		@Override
		public void paint(Graphics2D g2d, JXPanel arg1, int width, int height) {
			GridCellLayout grid = (GridCellLayout)arg1.getLayout();
			int gridw = grid.getCellWidth();
			int gridh = grid.getCellHeight();
			g2d.setColor(Color.LIGHT_GRAY);
			for(int x = 0; x < width; x+=4*gridw) {
				g2d.drawLine(x, 0, x, height);
			}
			for(int y = 0; y < height; y+=2*gridh) {
				g2d.drawLine(0, y, width, y);
			}
		}
		
		private Point2D convertGridPointToView(Point2D p, int cw, int ch) {
			Point2D.Float retVal = new Point2D.Float();
			
			retVal.x = (float)p.getX() * cw;
			retVal.y = (float)p.getY() * ch;
			
			return retVal;
		}
	}
	
	private class LineSegment extends Tuple<Point2D, Point2D> {
		
		public LineSegment(Point2D a, Point2D b) {
			super(a, b);
		}
	}
	
	/**
	 * Context mouse listener
	 */
	private class ContextMouseHandler extends MouseInputAdapter {
		@Override
		public void mousePressed(MouseEvent arg0) {
			if(arg0.isPopupTrigger()) {
				JPopupMenu popupMenu = new JPopupMenu();
				setupContextMenu(popupMenu, (JComponent)arg0.getComponent());
				popupMenu.show((Component)arg0.getSource(), arg0.getPoint().x, arg0.getPoint().y);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(arg0.isPopupTrigger()) {
				JPopupMenu popupMenu = new JPopupMenu();
				setupContextMenu(popupMenu, (JComponent)arg0.getComponent());
				popupMenu.show((Component)arg0.getSource(), arg0.getPoint().x, arg0.getPoint().y);
			}
		}
	}
	
	/**
	 * Comparator for cells
	 */
	private class CellComprator implements Comparator<Cell> {

		@Override
		public int compare(Cell arg0, Cell arg1) {
			Collator c = CollatorFactory.defaultCollator();
			return c.compare(arg0.getText(), arg1.getText());
		}
		
	}

	private class CellButtonBgPainter implements Painter<JButton> {
		
		@Override
		public void paint(Graphics2D g, JButton arg1, int width, int height) {
			final CellButton cellButton = (CellButton)arg1;
			final Cell cell = cellButton.cell;
			final boolean highlight = cellButton.highlight;
			
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			RoundRectangle2D boundRect = new RoundRectangle2D.Float(
					1.0f, 1.0f, width-1.0f, height-1.0f, 10.0f, 10.0f);
			
			if(isHighlightRecent() && lastUsed.contains(cell.getText())) {
				int alpha = 
					255 - lastUsed.indexOf(cell.getText()) * (255/MAX_LAST_USED_SIZE);
				Color c = PhonGuiConstants.PHON_ORANGE.brighter();
				c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
				g.setColor(c);
//				g.fillRoundRect(1, 1, width-1, height-1, 10, 10);
				g.fill(boundRect);
			}
			if(highlight) {
				g.setColor(PhonGuiConstants.PHON_ORANGE.brighter());
//				g.fillRect(0, 0, width, height);
//				g.fillRoundRect(1, 1, width-1, height-1, 10, 10);
				g.fill(boundRect);
			} else {
				Color bgColor = new Color(255, 255, 255, 0);
				g.setColor(bgColor);
				g.fillRect(0, 0, width-1, height-1);
			}
			
			// TEMP check for private use glyphs
			for(Character c:cell.getText().toCharArray()) {
				final Integer i = (int)c;
				if(StringUtils.leftPad(Integer.toHexString(i), 4, '0').startsWith("f")) {
					g.setColor(Color.red);
					g.fillRect(0, 0, width-1, height-1);
				}
			}
			
			if(searchResults.contains(cell)) {
				InnerGlowPathEffect gpe = new InnerGlowPathEffect();
				gpe.setBrushColor(Color.blue);
				
				float effectWidth = getScale() * gpe.getEffectWidth();
				int ew = Math.round(effectWidth);
				gpe.setEffectWidth(Math.max(ew, 1));
				
				gpe.apply(g, boundRect, width, height);
			}
		}
		
	}
	
	private class CellButtonFgPainter implements Painter<JButton> {

		@Override
		public void paint(Graphics2D g, JButton arg1, int width, int height) {
			final FontMetrics fm = g.getFontMetrics();
			final String txt = arg1.getText();
			final Rectangle2D b = fm.getStringBounds(txt, g);
			
			float x = Math.round((width / 2.0f) - (b.getCenterX()));
			float y = Math.round((height / 2.0f) + (b.getCenterY()) + fm.getAscent() - fm.getDescent());
			
			g.setColor(arg1.getForeground());
			g.setFont(arg1.getFont());
			g.drawString(txt, x, y);
		}
		
	}

	/**
	 * Button class for cells
	 */
	private class CellButton extends JButton implements MouseListener {
		
		private Cell cell;
		
		private boolean highlight = false;
		
		public CellButton(Cell cell) {
			this.cell = cell;
			setText(cell.getText());
			setFocusable(false);
			setToolTipText(" das");
			setOpaque(false);
			super.setBorderPainted(false);
//			super.setPaintBorderInsets(false);
//			setBackgroundPainter(new CellButtonBgPainter());
//			setForegroundPainter(new CellButtonFgPainter());
			addMouseListener(this);
		}
		
		@Override
		public Insets getInsets() {
			Insets retVal = super.getInsets();
			
			retVal.top = 0;
			retVal.bottom = 0;
			
			retVal.left = 0;
			retVal.right = 0;
			
			return retVal;
		}
		
		@Override
		public Font getFont() {
			return IpaMap.this.getFont();
		}

		@Override
		public JToolTip createToolTip() {
			return new CellToolTip(cell);
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			highlight = true;
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			highlight = false;
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
		
		@Override
		public void paintComponent(Graphics g) {
			final Graphics2D g2d = (Graphics2D)g;
			
			(new CellButtonBgPainter()).paint(g2d, this, getWidth(), getHeight());
			(new CellButtonFgPainter()).paint(g2d, this, getWidth(), getHeight());
		}
		
	}
	
	/**
	 * Cell tooltip
	 */
	private class CellToolTip extends JToolTip {
		/**
		 * Cell data
		 */
		private Cell cell;
		
		private JPanel innerPanel = new JPanel();
		
		public CellToolTip(Cell cell) {
			super();
			
			this.cell = cell;
			init();
		}
		
		@Override
		public Dimension getPreferredSize() {
			Dimension innerPref = innerPanel.getPreferredSize();
			
			int h = innerPref.height + 50 + innerPanel.getInsets().top + 
				innerPanel.getInsets().bottom + super.getInsets().top + 
				super.getInsets().bottom + 
				super.getBorder().getBorderInsets(innerPanel).top +
				super.getBorder().getBorderInsets(innerPanel).bottom;
			int w = 400;
			
			return new Dimension(w, h);
		}
		
		private void init() {
			innerPanel = new JPanel();
			FormLayout layout = new FormLayout(
					"60px, 340px",
					"fill:default:grow, pref");
			CellConstraints cc = new CellConstraints();
			innerPanel.setLayout(layout);
			
			final IPATokens tokens = IPATokens.getSharedInstance();
			
			JLabel previewLabel = new JLabel(cell.getText());
			previewLabel.setFont(IpaMap.this.getFont().deriveFont(36.0f));
			previewLabel.setOpaque(true);
			previewLabel.setBackground(Color.white);
			previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			String uniVal = "";
			String name = "";
			
			String cellText = cell.getText().replaceAll("\u25cc", "");
			for(Character c:cellText.toCharArray()) {
				String cText = "0x" + StringUtils.leftPad(Integer.toHexString((int)c), 4, '0');
				uniVal += (uniVal.length() > 0 ? " + " : "") + cText;
				
				String cName = tokens.getCharacterName(c);
				name += (name.length() > 0 ? " + " : "") + cName;
			}
			
			// create feature set
			FeatureMatrix fm = FeatureMatrix.getInstance();
			FeatureSet customFs = new FeatureSet();
			String cellData = cell.getText().replaceAll(""+(char)0x25cc, "");
			for(Character c:cellData.toCharArray()) {
				FeatureSet fs = fm.getFeatureSet(c);
				if(fs != null) {
					customFs = customFs.union(fs);
				}
			}
			
			String fsString = customFs.toString();
			fsString = fsString.substring(1, fsString.length()-1);
			
			String infoTxt = "<html><table border='0'>" +
					"<tr><td>Name:</td><td>" + name + "</td></tr>" +
					"<tr><td>Unicode:</td><td>" + uniVal + "</td></tr>" +
					"<tr><td>Features:</td><td>" + fsString + "</td></tr>" +
					"</table></html>";
			JEditorPane infoLbl = new JEditorPane("text/html", infoTxt);
			infoLbl.setEditable(false);
			infoLbl.setOpaque(true);
			infoLbl.setBackground(PhonGuiConstants.PHON_ORANGE.brighter());
			
			innerPanel.add(previewLabel, cc.xywh(1,1,1,2));
			innerPanel.add(new JScrollPane(infoLbl), cc.xy(2,1));
			
			setLayout(new BorderLayout());
			add(innerPanel, BorderLayout.CENTER);
		}
	}
	
	/**
	 * Class to store previously used cells
	 */
	private static class UsageList extends ArrayList<String> {
		private int maxSize = 0;
		
		public UsageList(int maxSize) {
			super(maxSize);
			this.maxSize = maxSize;
		}

		@Override
		public boolean add(String e) {
			// if already in list, remove from it's position
			if(contains(e)) remove(e);
			// always add to the beginning
			super.add(0, e);
			return true;
		}

		public int maxsize() {
			return maxSize;
		}
		
		@Override
		public boolean contains(Object e) {
			boolean retVal = false;
			for(int i = 0; i < maxSize && i < super.size(); i++) {
				if(get(i).equals(e))
					retVal = true;
			}
			return retVal;
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		int prefWidth = 0;
		int prefHeight = 0;
		// add size of first 3 grid panels
		if(gridPanels.size() >= 3) {
			for(int i = 0; i < 3; i++) {
				JXCollapsiblePane gridPanel = gridPanels.get(i);
				boolean wasCollapsed = gridPanel.isCollapsed();
				gridPanel.setCollapsed(false);
				prefHeight += gridPanel.getPreferredSize().height;
				gridPanel.setCollapsed(wasCollapsed);
				prefHeight += toggleButtons.get(i).getPreferredSize().height;
				prefWidth = (int)Math.max(prefWidth, gridPanel.getPreferredSize().getWidth());
			}
			
		}
		
		return new Dimension(prefWidth, prefHeight);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return (int)getCellDimension().getHeight();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return getScrollableUnitIncrement(visibleRect, orientation, direction) * 3;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
}
