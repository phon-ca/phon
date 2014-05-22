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

package ca.phon.app.session.editor.view.find_and_replace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.util.Range;

/**
 * Class to manage find and replace for a
 * session.
 */
public class FindManager {

	public enum FindDirection {
		FORWARDS,
		BACKWARDS;
	}

	public enum FindStatus {
		HIT_END,			// searching forward, hit end of session
		HIT_BEGINNING,		// search backward, hit beginning of session
		HIT_RESULT,			// result found
		INIT;				// initial state
	};

	/** Session */
	private Session session;
	
	private final Integer exprMutex = new Integer(1);

	/** Search expr */
	private FindExpr anyExpr;
	
	private final Map<String, FindExpr> tierExprs = new HashMap<String, FindExpr>();

	/** Current location */
	private SessionLocation currentLocation;
	
	/** Direction */
	private FindDirection direction = FindDirection.FORWARDS;
	
	/** Current find status */
	private FindStatus findStatus = FindStatus.INIT;

	/** Tiers to search */
	private List<String> searchTiers;
	
	/**
	 * Constructor
	 */
	public FindManager(Session session) {
		this.session = session;

		this.searchTiers = new ArrayList<String>();

		for(TierViewItem toi:session.getTierView()) {
			if(toi.isVisible()) {
				this.searchTiers.add(toi.getTierName());
			}
		}

		// setup start location
		currentLocation = null;
	}

	public FindDirection getDirection() {
		return this.direction;
	}

	public void setDirection(FindDirection dir) {
		this.direction = dir;
	}

	public FindStatus getStatus() {
		return this.findStatus;
	}

	public void setStatus(FindStatus status) {
		this.findStatus = status;
	}
	
	public String[] getSearchTiers() {
		return this.searchTiers.toArray(new String[0]);
	}

	public void setSearchTier(String[] tiers) {
		this.searchTiers.clear();
		for(String tier:tiers)
			this.searchTiers.add(tier);
	}

	public void setSearchTier(List<String> tiers) {
		this.searchTiers.clear();
		this.searchTiers.addAll(tiers);
	}

	public SessionLocation getCurrentLocation() {
		return this.currentLocation;
	}

	public void setCurrentLocation(SessionLocation location) {
		this.currentLocation = location;
	}
	
	public FindExpr getAnyExpr() {
		return this.anyExpr;
	}
	
	public void setAnyExpr(FindExpr expr) {
		this.anyExpr = expr;
	}
	
	public FindExpr getExprForTier(String tierName) {
		return tierExprs.get(tierName);
	}
	
	public void setExprForTier(String tierName, FindExpr expr) {
		tierExprs.put(tierName, expr);
	}

	/**
	 * Search for the next instance of the given expression
	 * and return it's location.
	 *
	 * @return the location of the next instance of the given pattern
	 *
	 * @throws FindException
	 *
	 */
	public SessionRange findNext() {
		SessionRange retVal = null;

		// be wary of expression changes in the middle of a search
		synchronized(exprMutex) {
			// start from current location and in specified direction
			if(direction == FindDirection.FORWARDS) {
				retVal = findForwards(currentLocation);
			} else if(direction == FindDirection.BACKWARDS) {
				retVal = findBackwards(currentLocation);
			}
		}

		// update current position if we have a result
		if(retVal != null) {
			int charPos =
					(direction == FindDirection.FORWARDS
						? retVal.getRange().getRange().getLast()
						: retVal.getRange().getRange().getFirst());

			currentLocation = new SessionLocation(
					retVal.getRecordIndex(),
					new RecordLocation(retVal.getRange().getTier(),
									charPos));
			findStatus = FindStatus.HIT_RESULT;
		} else {
			if(direction == FindDirection.FORWARDS)
				findStatus = FindStatus.HIT_END;
			else if(direction == FindDirection.BACKWARDS)
				findStatus = FindStatus.HIT_BEGINNING;
		}

		return retVal;
	}

	/**
	 * Search for the next instance of the given expression
	 * and return it's location.
	 *
	 * @return the location of the next instance of the given pattern
	 *
	 * @throws FindException
	 *
	 */
	public SessionRange findPrev() {
		SessionRange retVal = null;

		// be wary of expression changes in the middle of a search
		synchronized(exprMutex) {
			// start from current location and in specified direction
			if(direction == FindDirection.FORWARDS) {
				retVal = findBackwards(currentLocation);
			} else if(direction == FindDirection.BACKWARDS) {
				retVal = findForwards(currentLocation);
			}
		}

		// update current position if we have a result
		if(retVal != null) {
			int charPos =
					(direction == FindDirection.BACKWARDS
						? retVal.getRange().getRange().getLast()
						: retVal.getRange().getRange().getFirst());

			currentLocation = new SessionLocation(
					retVal.getRecordIndex(),
					new RecordLocation(retVal.getRange().getTier(),
									charPos));
			findStatus = FindStatus.HIT_RESULT;
		} else {
			if(direction == FindDirection.BACKWARDS)
				findStatus = FindStatus.HIT_END;
			else if(direction == FindDirection.FORWARDS)
				findStatus = FindStatus.HIT_BEGINNING;
		}

		return retVal;

	}

	private SessionRange findForwards(SessionLocation pos) {
		SessionRange retVal = null;
		
		int uttIdx = pos.getRecordIndex();
		int tierIdx = searchTiers.indexOf(pos.getRecordLocation().getTier());
		if(tierIdx < 0)
			tierIdx = 0;
		int charIdx = pos.getRecordLocation().getLocation();

		while(uttIdx < session.getRecordCount()) {
			Record currentUtt = session.getRecord(uttIdx);

			while(tierIdx < searchTiers.size()) {
				String searchTier = searchTiers.get(tierIdx);

				final Tier<?> tier = currentUtt.getTier(searchTier);
				
				// look for expression in tierData
				if(searchType == SearchType.PLAIN) {
					
					if(!isCaseSensitive()) {
						tierData = tierData.toLowerCase();
					}
					String expression =
							(!isCaseSensitive() ? expr.toLowerCase() : expr);
					int idx = tierData.indexOf(expression, charIdx);
					if(idx >= 0) {
						// we have a match, create a new range to return
						RecordRange recRange =
								new RecordRange(searchTier,
								new Range(idx, idx+expr.length(), true));
						retVal = new SessionRange(uttIdx, recRange);
						break;
					} else {
						// goto next tier
						tierIdx++;
						charIdx = 0;
					}
				} else if(searchType == SearchType.REGEX) {
					int flags =
							(!isCaseSensitive() ? Pattern.CASE_INSENSITIVE : 0);
					Pattern p = Pattern.compile(expr, flags);
					Matcher m = p.matcher(tierData);

					if(charIdx < tierData.length() && m.find(charIdx)) {
						int startIdx = m.start();
						int endIdx = m.end();

						RecordRange recRange =
								new RecordRange(searchTier,
								new Range(startIdx, endIdx, true));
						retVal = new SessionRange(uttIdx, recRange);
						break;
					} else {
						tierIdx++;
						charIdx = 0;
					}
				}
			}
			if(retVal != null) break;
			uttIdx++;
			tierIdx = 0;
		}

		return retVal;
	}

	private SessionRange findBackwards(SessionLocation pos) {
		SessionRange retVal = null;

		int uttIdx = pos.getRecordIndex();
		int tierIdx = searchTiers.indexOf(pos.getRecordLocation().getTier());
		if(tierIdx < 0)
			tierIdx = 0;
		int charIdx = pos.getRecordLocation().getLocation();

		while(uttIdx >= 0) {
			Record currentUtt = session.getRecord(uttIdx);

			while(tierIdx >= 0) {
				String searchTier = searchTiers.get(tierIdx);
				
				final Tier<?> tierData = currentUtt.getTier(searchTier);
				
				if(searchType == SearchType.PLAIN) {
					if(charIdx == -1)
						charIdx = tierData.length();
					String searchStr = tierData.substring(0, charIdx);
					
					if(!isCaseSensitive()) {
						tierData = tierData.toLowerCase();
					}
					String expression =
							(!isCaseSensitive() ? expr.toLowerCase() : expr);
					int idx = searchStr.lastIndexOf(expression);

					if(idx >= 0) {
						RecordRange recRange =
								new RecordRange(searchTier,
								new Range(idx, idx+expr.length(), false));
						retVal = new SessionRange(uttIdx, recRange);
						break;
					} else {
						tierIdx--;
						charIdx = -1;
					}
				} else if(searchType == SearchType.REGEX) {
					int flags =
							(!isCaseSensitive() ? Pattern.CASE_INSENSITIVE : 0);
					Pattern p = Pattern.compile(expr, flags);
					Matcher m = p.matcher(searchStr);

					int startIdx = -1;
					int endIdx = -1;
					while(m.find()) {
						startIdx = m.start();
						endIdx = m.end();
					}

					if(startIdx >= 0 && endIdx >= 0) {
						RecordRange recRange =
								new RecordRange(searchTier,
								new Range(startIdx, endIdx, false));
						retVal = new SessionRange(uttIdx, recRange);
						break;
					} else {
						tierIdx--;
						charIdx = -1;
					}
				}
			}

			if(retVal != null) break;
			uttIdx--;
			tierIdx = searchTiers.size()-1;
		}

		return retVal;
	}

	/**
	 * Method to compare session locations
	 *
	 * @return < 0 if A < B, > 0 if A > B and 0 if A == B
	 */
	private int compareLocations(SessionLocation A, SessionLocation B) {
		int retVal = 0;

		if(A.getRecordIndex() < B.getRecordIndex()) {
			retVal = -1;
		} else if(A.getRecordIndex() > B.getRecordIndex()) {
			retVal = 1;
		} else {
			RecordLocation Ar = A.getRecordLocation();
			RecordLocation Br = B.getRecordLocation();

			int AtIdx = searchTiers.indexOf(Ar.getTier());
			int BtIdx = searchTiers.indexOf(Br.getTier());

			if(AtIdx < BtIdx) {
				retVal = -1;
			} else if(AtIdx > BtIdx) {
				retVal = 1;
			} else {

				if(Ar.getLocation() < Br.getLocation()) {
					retVal = -1;
				} else if(Ar.getLocation() > Br.getLocation()) {
					retVal = 1;
				} else {
					retVal = 0;
				}

			}
		}

		return retVal;
	}
}
