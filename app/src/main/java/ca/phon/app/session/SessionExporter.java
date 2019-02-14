package ca.phon.app.session;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.app.log.LogUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.query.db.ResultSet;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

public abstract class SessionExporter<T> {
	
	// primary record filter
	private RecordFilter recordFilter;
	

	// participant information
	private boolean includeParticipantInfo;
	private boolean includeAge = true;
	private boolean includeBirthday = false;
	private boolean includeRole = true;
	private boolean includeSex = false;
	private boolean includeLanguage = false;
	private boolean includeGroup = false;
	private boolean includeEducation = false;
	private boolean includeSES = false;
	
	// tier data
	private boolean includeTierData = true;
		
	// options to include/exclude tiers by name
	private boolean excludeTiers = false;
	private List<String> tierList = new ArrayList<>();
	
	// add syllabification display data to IPA Target/Actual tiers
	private boolean includeSyllabification;
	private SyllabificationDisplay syllabificationDisplay;
	private JPanel syllabificationRenderPane;
	
	// add alignment display to end of tier information
	private boolean includeAlignment;
	private PhoneMapDisplay alignmentDisplay;
	private JPanel alignmentRenderPane;
	
	// tier view - order, visibility and fonts
	private List<TierViewItem> tierView;
	
	// query result options
	private boolean includeQueryResults = false;
	private boolean filterRecordsUsingQueryResults = false;
	
	// use in some outputs (HTML/Excel) to determine if query results are printed
	// as the first content in record tables
	private boolean showQueryResultsFirst = false;
	
	// options to include/exclude result values/metadata by name
	private boolean excludeResultValues;
	private List<String> resultValueList = new ArrayList<>();
	
	public SessionExporter() {
		this(true, true, true, null);
	}
	
	/**
	 * 
	 * @param includeParticipantInfo
	 * @param includeSyllabification
	 * @param includeAlignment
	 * @param tierView if <code>null</code>, tierView from session is used
	 */
	public SessionExporter(boolean includeParticipantInfo, boolean includeSyllabification,
			boolean includeAlignment, List<TierViewItem> tierView) {
		super();
		
		this.includeParticipantInfo = includeParticipantInfo;
		this.includeSyllabification = includeSyllabification;
		this.includeAlignment = includeAlignment;
		this.tierView = tierView;
	}

	public boolean isIncludeParticipantInfo() {
		return includeParticipantInfo;
	}

	public void setIncludeParticipantInfo(boolean includeParticipantInfo) {
		this.includeParticipantInfo = includeParticipantInfo;
	}
	
	public boolean isIncludeAge() {
		return includeAge;
	}

	public void setIncludeAge(boolean includeAge) {
		this.includeAge = includeAge;
	}

	public boolean isIncludeBirthday() {
		return includeBirthday;
	}

	public void setIncludeBirthday(boolean includeBirthday) {
		this.includeBirthday = includeBirthday;
	}

	public boolean isIncludeRole() {
		return includeRole;
	}

	public void setIncludeRole(boolean includeRole) {
		this.includeRole = includeRole;
	}

	public boolean isIncludeSex() {
		return includeSex;
	}

	public void setIncludeSex(boolean includeSex) {
		this.includeSex = includeSex;
	}

	public boolean isIncludeLanguage() {
		return includeLanguage;
	}

	public void setIncludeLanguage(boolean includeLanguage) {
		this.includeLanguage = includeLanguage;
	}

	public boolean isIncludeGroup() {
		return includeGroup;
	}

	public void setIncludeGroup(boolean includeGroup) {
		this.includeGroup = includeGroup;
	}

	public boolean isIncludeEducation() {
		return includeEducation;
	}

	public void setIncludeEducation(boolean includeEducation) {
		this.includeEducation = includeEducation;
	}

	public boolean isIncludeSES() {
		return includeSES;
	}

	public void setIncludeSES(boolean includeSES) {
		this.includeSES = includeSES;
	}

	public boolean isIncludeSyllabification() {
		return includeSyllabification;
	}

	public void setIncludeSyllabification(boolean includeSyllabification) {
		this.includeSyllabification = includeSyllabification;
	}

	public boolean isIncludeAlignment() {
		return includeAlignment;
	}

	public void setIncludeAlignment(boolean includeAlignment) {
		this.includeAlignment = includeAlignment;
	}

	public boolean isIncludeTierData() {
		return this.includeTierData;
	}
	
	public void setIncludeTierData(boolean includeTierData) {
		this.includeTierData = includeTierData;
	}
	
	public boolean isExcludeTiers() {
		return this.excludeTiers;
	}
	
	public void setExcludeTiers(boolean excludeTiers) {
		this.excludeTiers = excludeTiers;
	}
	
	public List<String> getTierList() {
		return this.tierList;
	}
	
	public void setTierList(List<String> tierList) {
		this.tierList = tierList;
	}
	
	public List<TierViewItem> getTierView() {
		return tierView;
	}

	public void setTierView(List<TierViewItem> tierView) {
		this.tierView = tierView;
	}
	
	public boolean isIncludeQueryResults() {
		return this.includeQueryResults;
	}
	
	public void setIncludeQueryResults(boolean includeQueryResults) {
		this.includeQueryResults = includeQueryResults;
	}
	
	public boolean isFilterRecordsUsingQueryResults() {
		return this.filterRecordsUsingQueryResults;
	}
	
	public void setFilterRecordsUsingQueryResults(boolean filterRecords) {
		this.filterRecordsUsingQueryResults = filterRecords;
	}
	
	public boolean isShowQueryResultsFirst() {
		return showQueryResultsFirst;
	}

	public void setShowQueryResultsFirst(boolean queryResultsFirst) {
		this.showQueryResultsFirst = queryResultsFirst;
	}

	public boolean isExcludeResultValues() {
		return this.excludeResultValues;
	}
	
	public void setExcludeResultValues(boolean excludeResultValues) {
		this.excludeResultValues = excludeResultValues;
	}
	
	public List<String> getResultValues() {
		return this.resultValueList;
	}
	
	public void setResultValues(List<String> resultValues) {
		this.resultValueList = resultValues;
	}
	
	public RecordFilter getRecordFilter() {
		return this.recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public SyllabificationDisplay getSyllabificationDisplay() {
		if(syllabificationDisplay == null) {
			try {
				SwingUtilities.invokeAndWait( () -> {
					syllabificationDisplay = new SyllabificationDisplay();
					syllabificationRenderPane = new JPanel();
					syllabificationDisplay.setFont(FontPreferences.getUIIpaFont());
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
		}
		return this.syllabificationDisplay;
	}
	
	public PhoneMapDisplay getAlignmentDisplay() {
		if(alignmentDisplay == null) {
			try {
				SwingUtilities.invokeAndWait( () -> {
					alignmentDisplay = new PhoneMapDisplay();
					alignmentDisplay.setBackground(Color.white);
					alignmentDisplay.setFont(FontPreferences.getUIIpaFont());
					alignmentRenderPane = new JPanel();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
		}
		return alignmentDisplay;
	}
	
	protected String createSyllabificationImageData(IPATranscript ipa) {
		final StringBuffer buffer = new StringBuffer();
		final SyllabificationDisplay display = getSyllabificationDisplay();
		
		try {
			SwingUtilities.invokeAndWait( () -> {
				display.setTranscript(ipa);
				display.revalidate();
				
				Dimension prefSize = display.getPreferredSize();
				
				BufferedImage img = new BufferedImage((int)prefSize.width+5, (int)prefSize.height, BufferedImage.TYPE_4BYTE_ABGR);
				
				Graphics2D g = img.createGraphics();
				SwingUtilities.paintComponent(g, display, syllabificationRenderPane, 0, 0, prefSize.width+5, prefSize.height);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ImageIO.write(img, "png", bos);
				} catch (IOException e) {
					LogUtil.warning(e.getLocalizedMessage(), e);
				}
				
				var base64data = Base64.getEncoder().encodeToString(bos.toByteArray());
				buffer.append(base64data);				
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
		
		return buffer.toString();
	}
	
	protected String createAlignmentImageData(PhoneMap alignment) {
		final StringBuffer buffer = new StringBuffer();
		final PhoneMapDisplay display = getAlignmentDisplay();
		
		try {
			SwingUtilities.invokeAndWait( () -> {
				display.setPhoneMapForGroup(0, alignment);
				display.revalidate();
				
				Dimension prefSize = display.getPreferredSize();
				
				BufferedImage img = new BufferedImage((int)prefSize.width, (int)prefSize.height, BufferedImage.TYPE_4BYTE_ABGR);
				
				Graphics2D g = img.createGraphics();
				SwingUtilities.paintComponent(g, display, alignmentRenderPane, 0, 0, prefSize.width, prefSize.height);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ImageIO.write(img, "png", bos);
				} catch (IOException e) {
					LogUtil.warning(e.getLocalizedMessage(), e);
				}
				
				var base64data = Base64.getEncoder().encodeToString(bos.toByteArray());
				buffer.append(base64data);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
		
		return buffer.toString();
	}
	
	/**
	 * Map session to required type.
	 * 
	 * @param session
	 * @param resultSet (optional)
	 * @return 
	 */
	public abstract T export(Session session, Optional<ResultSet> results);
	
}
