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
package ca.phon.ipamap2;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import jakarta.xml.bind.*;

import org.apache.commons.lang3.*;

import ca.phon.ipa.features.*;
import ca.phon.ipa.parser.*;
import ca.phon.ipamap.IpaMapSearchField.*;
import ca.phon.ui.ipamap.io.*;

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
		Optional<Grid> toneGrid = Optional.empty();
		// create a set of characters defined in the xml file
		final Set<Character> supportedChars = new HashSet<Character>();
		for(Grid g:grids.getGrid()) {
			if(g.getName().equals("Tones")) {
				toneGrid = Optional.of(g);
			}
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
		
		// tone diacritics
		Set<Character> tSet = new LinkedHashSet<Character>();
		if(toneGrid.isPresent()) {
			final ObjectFactory factory = new ObjectFactory();
			FeatureMatrix fm = FeatureMatrix.getInstance();
			tSet = tokens.getCharacterSet()
					.stream()
					.filter( (c) -> {
						if(supportedChars.contains(c)) return false;
						
						for(String feature:fm.getFeatureSet(c).getFeatures()) {
							if(feature.startsWith("tone")) return true;
						}
						return false;
					})
					.collect(Collectors.toSet());
			
			int x = 14;
			int y = 0;
			List<Character> tchars = new ArrayList<Character>(tSet);
			tchars.sort(Character::compareTo);
			for(Character ch:tchars) {
				final Cell cell = factory.createCell();
				cell.setX(x);
				cell.setY(y);
				cell.setW(w);
				cell.setH(h);
				cell.setText("\u25cc" + ch);
				
				toneGrid.get().getCell().add(cell);
				
				x += w;
				if(x >= 38) {
					x = 14;
					y += h;
				}
			}
			
			toneGrid.get().setRows(y+h);
		}
		
		// prefix diacritics
		final Set<Character> pdSet = tokens.getCharactersForType(IPATokenType.PREFIX_DIACRITIC);
		pdSet.removeAll(tSet);
		pdSet.removeAll(supportedChars);
		if(pdSet.size() > 0) {
			final Grid pdGrid = generateGrid(pdSet, "Other Prefix Diacritics", "", "\u25cc", w, h, maxX);
			grids.getGrid().add(pdGrid);
		}
		
		// suffix diacritics
		final Set<Character> sdSet = tokens.getCharactersForType(IPATokenType.SUFFIX_DIACRITIC);
		sdSet.removeAll(tSet);
		sdSet.removeAll(supportedChars);
		if(sdSet.size() > 0) {
			final Grid sdGrid = generateGrid(sdSet, "Other Suffix Diacritics", "\u25cc", "", w, h, maxX);
			grids.getGrid().add(sdGrid);
		}
		
		// combining diacritics
		final Set<Character> cdSet = tokens.getCharactersForType(IPATokenType.COMBINING_DIACRITIC);
		cdSet.removeAll(tSet);
		cdSet.removeAll(supportedChars);
		if(cdSet.size() > 0) {
			final Grid cdGrid = generateGrid(cdSet, "Other Combining Diacritics", "\u25cc", "", w, h, maxX);
			grids.getGrid().add(cdGrid);
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
