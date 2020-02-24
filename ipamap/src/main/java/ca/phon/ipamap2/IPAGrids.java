package ca.phon.ipamap2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;

import ca.phon.ipa.features.Feature;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import ca.phon.ipamap.IpaMapSearchField.SearchType;
import ca.phon.ui.ipamap.io.Cell;
import ca.phon.ui.ipamap.io.CellProp;
import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.IpaGrids;
import ca.phon.ui.ipamap.io.ObjectFactory;

public class IPAGrids {
	
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IPAGrids.class
			.getName());

	/**
	 * Static ref to ipa map data
	 */
	private IpaGrids grids;
	
	/**
	 * Location of the grid file
	 */
	private final static String GRID_FILE = "ipagrids.xml";
	
	/**
	 * Load static IPA map data
	 * 
	 */
	public IpaGrids loadDefaultGridData() {
		ObjectFactory factory = new ObjectFactory();
		try {
			JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			grids = (IpaGrids)unmarshaller.unmarshal(
					IPAGrids.class.getResource(GRID_FILE));
		} catch (JAXBException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			grids = factory.createIpaGrids();
		}
		return grids;
	}
	
	public IpaGrids loadGridData(InputStream in) throws IOException {
		ObjectFactory factory = new ObjectFactory();
		try {
			JAXBContext ctx = JAXBContext.newInstance(factory.getClass());
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			grids = (IpaGrids)unmarshaller.unmarshal(in);
		} catch (JAXBException e) {throw new IOException(e);
		}
		return grids;
	}
	
	public void generateMissingGrids() {
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
	
	private Grid generateGrid(Collection<Character> chars, String title, String prefix, String suffix, int w, int h, int maxX) {
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
	public Grid buildGrid(Collection<Cell> searchResults) {
		ObjectFactory factory = new ObjectFactory();
		Grid retVal = factory.createGrid();
		buildGrid(retVal, searchResults);
		return retVal;
	}
	
	/**
	 * Setup grid of cells.
	 * 
	 */
	public void buildGrid(Grid grid, Collection<Cell> searchResults) {
		ObjectFactory factory = new ObjectFactory();
		
		grid.setCols(22*2);
		grid.setName("Search Results (" + searchResults.size() + ")");
		
		for(Cell origCell:searchResults) {
			Cell copyCell = factory.createCell();
			copyCell.setText(origCell.getText());
			for(CellProp origProp:origCell.getProperty()) {
				CellProp newProp = factory.createCellProp();
				newProp.setName(origProp.getName());
				newProp.setContent(origProp.getContent());
				copyCell.getProperty().add(newProp);
			}
			grid.getCell().add(copyCell);
		}
		
		// setup cell locations and grid size
		int numRows = ((grid.getCell().size() / 22) + (grid.getCell().size() % 22 > 0 ? 1 : 0)) * 2;
		grid.setRows(numRows);
		
		for(int i = 0; i < grid.getCell().size(); i++) {
			int row = i / 22;
			int col = i % 22;
			Cell cell = grid.getCell().get(i);
			cell.setX(col * 2);
			cell.setY(row * 2);
			cell.setH(2);
			cell.setW(2);
		}
	}
		
	/**
	 * Perform a search
	 * 
	 * @param search - a set of search terms (separated by ',')
	 * @return a set of results
	 */
	public Set<Cell> performSearch(SearchType st, String search) {
		Set<Cell> retVal = new HashSet<Cell>();
		
		FeatureMatrix fm = FeatureMatrix.getInstance();
		search = StringUtils.strip(search.toLowerCase());
		if(search.length() > 0) {
			for(Grid grid:getInternal().getGrid()) {
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
									fs = FeatureSet.union(fs, f.getFeatureSet());
								}
							}
							FeatureSet charFs = new FeatureSet();
							// check feature set(s)
							for(Character c:cell.getText().toCharArray()) {
								FeatureSet cFs = fm.getFeatureSet(c);
								if(cFs != null) {
									charFs = FeatureSet.union(charFs, cFs);
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
	
	public IpaGrids getInternal() {
		return this.grids;
	}
	
}
