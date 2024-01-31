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

package ca.phon.app.session.editor.search;

import ca.phon.formatter.FormatterUtil;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.position.*;
import ca.phon.util.Range;

import java.util.*;

/**
 * Class to manage find and replace for a transcript.
 */
public class FindManager {

	/**
	 * Direction of search
	 */
	public enum FindDirection {
		FORWARDS,
		BACKWARDS;
	}

	/**
	 * Current find status
	 */
	public enum FindStatus {
		HIT_END,			// searching forward, hit end of session
		HIT_BEGINNING,		// search backward, hit beginning of session
		HIT_RESULT,			// result found
		INIT;				// initial state
	};

	/** Session */
	private Session session;
	
	/** Search expr for all tiers (default) */
	private FindExpr anyExpr;

	/** Search expr for specific tiers */
	private final Map<String, FindExpr> tierExprs = new HashMap<String, FindExpr>();

	/** Current location */
	private TranscriptElementLocation forwardsLocation = null;
	private TranscriptElementLocation backwardsLocation = null;
	
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

	/**
	 * Last transcript range found
	 */
	private TranscriptElementRange lastRange = null;

	/**
	 * Include comments
	 */
	private boolean includeComments = true;

	/**
	 * Include gems
	 */
	private boolean includeGems = true;
	
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

	public TranscriptElementLocation getCurrentLocation() {
		return (getDirection() == FindDirection.FORWARDS ? 
				forwardsLocation : backwardsLocation);
	}
	
	private TranscriptElementLocation getNextLocation() {
		return (getDirection() == FindDirection.FORWARDS ? 
				forwardsLocation : backwardsLocation);
	}
	
	private TranscriptElementLocation getPrevLocation() {
		return (getDirection() == FindDirection.BACKWARDS ?
				forwardsLocation : backwardsLocation);
	}

	public void setCurrentLocation(TranscriptElementLocation location) {
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
	
	public TranscriptElementRange getMatchedRange() {
		return this.lastRange;
	}

	public boolean isIncludeComments() {
		return this.includeComments;
	}

	public void setIncludeComments(boolean includeComments) {
		this.includeComments = includeComments;
	}

	public boolean isIncludeGems() {
		return this.includeGems;
	}

	public void setIncludeGems(boolean includeGems) {
		this.includeGems = includeGems;
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
	public TranscriptElementRange findNext() {
		TranscriptElementRange retVal = null;

		final TranscriptElementLocation currentLocation = getNextLocation();
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
	public TranscriptElementRange findPrev() {
		TranscriptElementRange retVal = null;

		final TranscriptElementLocation currentLocation = getPrevLocation();
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

	private TranscriptElementRange findForwards(TranscriptElementLocation pos) {
		TranscriptElementRange retVal = null;
		
		int eleIdx = pos.transcriptElementIndex();
		int tierIdx = searchTiers.indexOf(pos.tier());
		if(tierIdx < 0)
			tierIdx = 0;
		int charIdx = pos.charPosition();

		final FindExpr anyExpr = getAnyExpr();
		while(eleIdx < session.getTranscript().getNumberOfElements()) {
			final Transcript.Element ele = session.getTranscript().getElementAt(eleIdx);

			if(ele.isComment()) {
				final Comment comment = ele.asComment();
				if(isIncludeComments()) {
					if(anyExpr != null) {
						Range range = anyExpr.findNext(comment.getValue(), charIdx);
						lastExpr = anyExpr;
						if(range != null) {
							range.setExcludesEnd(true);
							retVal = new TranscriptElementRange(eleIdx, comment.getType().toString(), range);
							break;
						}
					}
				}
			} else if(ele.isGem()) {
				final Gem gem = ele.asGem();
				if(isIncludeGems()) {
					if(anyExpr != null) {
						Range range = anyExpr.findNext(gem.getLabel(), charIdx);
						lastExpr = anyExpr;
						if(range != null) {
							range.setExcludesEnd(true);
							retVal = new TranscriptElementRange(eleIdx, gem.getType().toString(), range);
							break;
						}
					}
				}
			} else if(ele.isRecord()) {
				Record currentUtt = ele.asRecord();
				while (tierIdx < searchTiers.size() && retVal == null) {
					String searchTier = searchTiers.get(tierIdx);

					final Tier<?> tier = currentUtt.getTier(searchTier);
					if (tier == null) {
						tierIdx++;
						continue;
					}

					Range charRange = null;
					Range tierExprRange = null;
					Range anyExprRange = null;

					final FindExpr tierExpr = getExprForTier(tier.getName());
					if (tierExpr != null) {
						tierExprRange = tierExpr.findNext(tier.getValue(), charIdx);
					}
					if (anyExpr != null) {
						anyExprRange = anyExpr.findNext(tier.getValue(), charIdx);
					}

					lastExpr = (tierExprRange != null ? tierExpr :
							(anyExprRange != null ? anyExpr : null));

					if (tierExprRange != null && anyExprRange != null) {
						charRange =
								(tierExprRange.getFirst() <= anyExprRange.getFirst() ? tierExprRange : anyExprRange);
					} else {
						charRange =
								(tierExprRange != null ? tierExprRange : anyExprRange);
					}

					if (charRange != null) {
						charRange.setExcludesEnd(true);
						retVal = new TranscriptElementRange(eleIdx, tier.getName(), charRange);
						break;
					}
					// reset charIdx
					charIdx = 0;

					if (retVal != null) break;
					tierIdx++;
				}
			}

			if(retVal != null) break;
			eleIdx++;
			tierIdx = 0;
		}

		return retVal;
	}

	private TranscriptElementRange findBackwards(TranscriptElementLocation pos) {
		TranscriptElementRange retVal = null;
		
		int eleIdx = pos.transcriptElementIndex();
		int tierIdx = searchTiers.indexOf(pos.tier());
		if(tierIdx < 0)
			tierIdx = 0;
		int charIdx = pos.charPosition();

		final FindExpr anyExpr = getAnyExpr();
		while(eleIdx >= 0) {
			final Transcript.Element ele = session.getTranscript().getElementAt(eleIdx);
			if(ele.isComment()) {
				final Comment comment = ele.asComment();
				if(isIncludeComments()) {
					if(anyExpr != null) {
						Range range = anyExpr.findPrev(comment.getValue(), charIdx);
						lastExpr = anyExpr;
						if(range != null) {
							range.setExcludesEnd(true);
							retVal = new TranscriptElementRange(eleIdx, comment.getType().toString(), range);
							break;
						}
					}
				}
			} else if(ele.isGem()) {
				final Gem gem = ele.asGem();
				if(isIncludeGems()) {
					if(anyExpr != null) {
						Range range = anyExpr.findPrev(gem.getLabel(), charIdx);
						lastExpr = anyExpr;
						if(range != null) {
							range.setExcludesEnd(true);
							retVal = new TranscriptElementRange(eleIdx, gem.getType().toString(), range);
							break;
						}
					}
				}
			} else if(ele.isRecord()) {
				Record currentUtt = ele.asRecord();

				while (tierIdx >= 0 && retVal == null) {
					String searchTier = searchTiers.get(tierIdx);

					final Tier<?> tier = currentUtt.getTier(searchTier);
					if (tier == null) continue;

					Range charRange = null;
					Range tierExprRange = null;
					Range anyExprRange = null;

					Object grpVal = tier.getValue();
					if (charIdx == Integer.MAX_VALUE) {
						final String grpTxt = FormatterUtil.format(grpVal);
						charIdx = grpTxt.length();
					}

					final FindExpr tierExpr = getExprForTier(tier.getName());
					if (tierExpr != null) {
						tierExprRange = tierExpr.findPrev(tier.getValue(), charIdx);
					}
					if (anyExpr != null) {
						anyExprRange = anyExpr.findPrev(tier.getValue(), charIdx);
					}

					lastExpr = (tierExprRange != null ? tierExpr :
							(anyExprRange != null ? anyExpr : null));

					if (tierExprRange != null && anyExprRange != null) {
						charRange =
								(tierExprRange.getFirst() >= anyExprRange.getFirst() ? tierExprRange : anyExprRange);
					} else {
						charRange =
								(tierExprRange != null ? tierExprRange : anyExprRange);
					}

					if (charRange != null) {
						charRange.setExcludesEnd(true);
						retVal = new TranscriptElementRange(eleIdx, tier.getName(), charRange);
						break;
					}
					// reset char idx
					charIdx = Integer.MAX_VALUE;
					if (retVal != null) break;
					tierIdx--;
				}
			}

			if(retVal != null) break;
			eleIdx--;
			tierIdx = searchTiers.size() - 1;
		}

		return retVal;
	}

	/**
	 * Method to compare session locations
	 *
	 * @return < 0 if A < B, > 0 if A > B and 0 if A == B
	 */
	private int compareLocations(TranscriptElementLocation A, TranscriptElementLocation B) {
		int retVal = 0;

		if(A.transcriptElementIndex() < B.transcriptElementIndex()) {
			retVal = -1;
		} else if(A.transcriptElementIndex() > B.transcriptElementIndex()) {
			retVal = 1;
		} else {

			int AtIdx = searchTiers.indexOf(A.tier());
			int BtIdx = searchTiers.indexOf(B.tier());
			
			int AcIdx = A.charPosition();
			int BcIdx = B.charPosition();

			if(AtIdx < BtIdx) {
				retVal = -1;
			} else if(AtIdx > BtIdx) {
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

		return retVal;
	}
}
