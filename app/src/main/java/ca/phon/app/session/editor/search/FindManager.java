/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

package ca.phon.app.session.editor.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.phon.formatter.FormatterUtil;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.session.position.GroupRange;
import ca.phon.session.position.RecordLocation;
import ca.phon.session.position.RecordRange;
import ca.phon.session.position.SessionLocation;
import ca.phon.session.position.SessionRange;
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
	
	/** Search expr */
	private FindExpr anyExpr;
	
	private final Map<String, FindExpr> tierExprs = new HashMap<String, FindExpr>();

	/** Current location */
	private SessionLocation forwardsLocation = null;
	private SessionLocation backwardsLocation = null;
	
	/** Direction */
	private FindDirection direction = FindDirection.FORWARDS;
	
	/** Current find status */
	private FindStatus findStatus = FindStatus.INIT;

	/** Tiers to search */
	private List<String> searchTiers;
	
	/**
	 * Last expression that matched
	 */
	private FindExpr lastExpr = null;
	
	private SessionRange lastRange = null;
	
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
	}
	
	public Session getSession() {
		return this.session;
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
		return (getDirection() == FindDirection.FORWARDS ? 
				forwardsLocation : backwardsLocation);
	}
	
	private SessionLocation getNextLocation() {
		return (getDirection() == FindDirection.FORWARDS ? 
				forwardsLocation : backwardsLocation);
	}
	
	private SessionLocation getPrevLocation() {
		return (getDirection() == FindDirection.BACKWARDS ?
				forwardsLocation : backwardsLocation);
	}

	public void setCurrentLocation(SessionLocation location) {
		this.forwardsLocation = location;
		this.backwardsLocation = location;
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
	
	public FindExpr getMatchedExpr() {
		return this.lastExpr;
	}
	
	public SessionRange getMatchedRange() {
		return this.lastRange;
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

		final SessionLocation currentLocation = getNextLocation();
		// start from current location and in specified direction
		if(direction == FindDirection.FORWARDS) {
			retVal = findForwards(currentLocation);
		} else if(direction == FindDirection.BACKWARDS) {
			retVal = findBackwards(currentLocation);
		}

		// update current position if we have a result
		if(retVal != null) {
			forwardsLocation = retVal.end();
			backwardsLocation = retVal.start();
			findStatus = FindStatus.HIT_RESULT;
		} else {
			if(direction == FindDirection.FORWARDS)
				findStatus = FindStatus.HIT_END;
			else if(direction == FindDirection.BACKWARDS)
				findStatus = FindStatus.HIT_BEGINNING;
		}
		
		lastRange = retVal;

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

		final SessionLocation currentLocation = getPrevLocation();
		// start from current location and in specified direction
		if(direction == FindDirection.FORWARDS) {
			retVal = findBackwards(currentLocation);
		} else if(direction == FindDirection.BACKWARDS) {
			retVal = findForwards(currentLocation);
		}

		// update current position if we have a result
		if(retVal != null) {
			forwardsLocation = retVal.end();
			backwardsLocation = retVal.start();
			findStatus = FindStatus.HIT_RESULT;
		} else {
			if(direction == FindDirection.BACKWARDS)
				findStatus = FindStatus.HIT_END;
			else if(direction == FindDirection.FORWARDS)
				findStatus = FindStatus.HIT_BEGINNING;
		}
		
		lastRange = retVal;

		return retVal;

	}

	private SessionRange findForwards(SessionLocation pos) {
		SessionRange retVal = null;
		
		int uttIdx = pos.getRecordIndex();
		int tierIdx = searchTiers.indexOf(pos.getRecordLocation().getTier());
		if(tierIdx < 0)
			tierIdx = 0;
		int grpIdx = pos.getRecordLocation().getGroupLocation().getGroupIndex();
		int charIdx = pos.getRecordLocation().getGroupLocation().getCharIndex();

		final FindExpr anyExpr = getAnyExpr();
		while(uttIdx < session.getRecordCount()) {
			Record currentUtt = session.getRecord(uttIdx);

			while(tierIdx < searchTiers.size() && retVal == null) {
				String searchTier = searchTiers.get(tierIdx);

				final Tier<?> tier = currentUtt.getTier(searchTier);
				if(tier == null) {
					tierIdx++;
					grpIdx = 0;
					continue;
				}
				
				for(int i = grpIdx; i < tier.numberOfGroups(); i++) {
					Range charRange = null;
					Range tierExprRange = null;
					Range anyExprRange = null;
					
					final FindExpr tierExpr = getExprForTier(tier.getName());
					if(tierExpr != null) {
						tierExprRange = tierExpr.findNext(tier.getGroup(i), charIdx);
					} 
					if(anyExpr != null) {
						anyExprRange = anyExpr.findNext(tier.getGroup(i), charIdx);
					}
					
					lastExpr = (tierExprRange != null ? tierExpr : 
						(anyExprRange != null ? anyExpr : null));
					
					if(tierExprRange != null && anyExprRange != null) {
						charRange = 
								(tierExprRange.getFirst() <= anyExprRange.getFirst() ? tierExprRange : anyExprRange);
					} else {
						charRange =
								(tierExprRange != null ? tierExprRange : anyExprRange);
					}
					
					if(charRange != null) {
						charRange.setExcludesEnd(true);
						final RecordRange recRange =
								new RecordRange(tier.getName(), new GroupRange(i, charRange));
						retVal = new SessionRange(uttIdx, recRange);
						break;
					}
					// reset charIdx
					charIdx = 0;
				}
				if(retVal != null) break;
				tierIdx++;
				grpIdx = 0;
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
		int grpIdx = pos.getRecordLocation().getGroupLocation().getGroupIndex();
		int charIdx = pos.getRecordLocation().getGroupLocation().getCharIndex();

		final FindExpr anyExpr = getAnyExpr();
		while(uttIdx >= 0) {
			Record currentUtt = session.getRecord(uttIdx);

			while(tierIdx >= 0 && retVal == null) {
				String searchTier = searchTiers.get(tierIdx);

				final Tier<?> tier = currentUtt.getTier(searchTier);
				if(tier == null) continue;
				
				if(grpIdx == Integer.MAX_VALUE) {
					grpIdx = currentUtt.numberOfGroups() - 1;
				}
				
				for(int i = grpIdx; i >= 0 && tier.numberOfGroups() > i; i--) {
					Range charRange = null;
					Range tierExprRange = null;
					Range anyExprRange = null;
										
					Object grpVal = tier.getGroup(i);
					if(charIdx == Integer.MAX_VALUE) {
						final String grpTxt = FormatterUtil.format(grpVal);
						charIdx = grpTxt.length();
					}
					
					final FindExpr tierExpr = getExprForTier(tier.getName());
					if(tierExpr != null) {
						tierExprRange = tierExpr.findPrev(tier.getGroup(i), charIdx);
					} 
					if(anyExpr != null) {
						anyExprRange = anyExpr.findPrev(tier.getGroup(i), charIdx);
					}
					
					lastExpr = (tierExprRange != null ? tierExpr : 
						(anyExprRange != null ? anyExpr : null));
					
					if(tierExprRange != null && anyExprRange != null) {
						charRange = 
								(tierExprRange.getFirst() >= anyExprRange.getFirst() ? tierExprRange : anyExprRange);
					} else {
						charRange =
								(tierExprRange != null ? tierExprRange : anyExprRange);
					}
					
					if(charRange != null) {
						charRange.setExcludesEnd(true);
						final RecordRange recRange =
								new RecordRange(tier.getName(), new GroupRange(i, charRange));
						retVal = new SessionRange(uttIdx, recRange);
						break;
					}
					// reset char idx
					charIdx = Integer.MAX_VALUE;
				}
				if(retVal != null) break;
				tierIdx--;
				grpIdx = Integer.MAX_VALUE;
			}
			if(retVal != null) break;
			uttIdx--;
			tierIdx = searchTiers.size() - 1;
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
			
			int AgIdx = Ar.getGroupLocation().getGroupIndex();
			int BgIdx = Br.getGroupLocation().getGroupIndex();
			
			int AcIdx = Ar.getGroupLocation().getCharIndex();
			int BcIdx = Br.getGroupLocation().getCharIndex();

			if(AtIdx < BtIdx) {
				retVal = -1;
			} else if(AtIdx > BtIdx) {
				retVal = 1;
			} else {
				if(AgIdx < BgIdx) {
					retVal = -1;
				} else if (AgIdx > BgIdx) {
					retVal = 1;
				} else {
					if(AcIdx < BcIdx) {
						retVal = -1;
					} else if(AcIdx > BcIdx) {
						retVal = 1;
					} else {
						retVal = 0;
					}
				}
			}
		}

		return retVal;
	}
}
