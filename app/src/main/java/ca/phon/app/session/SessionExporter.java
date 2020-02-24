package ca.phon.app.session;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.app.log.LogUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

public abstract class SessionExporter {
	
	private SessionExportSettings settings;
	
	private SyllabificationDisplay syllabificationDisplay;
	private JPanel syllabificationRenderPane;

	private PhoneMapDisplay alignmentDisplay;
	private JPanel alignmentRenderPane;
	
	public SessionExporter() {
		this(new SessionExportSettings());
	}
	
	public SessionExporter(SessionExportSettings settings) {
		super();
		
		this.settings = settings;
	}

	public SessionExportSettings getSettings() {
		return this.settings;
	}
	
	public void setSettings(SessionExportSettings settings) {
		this.settings = settings;
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
	
	protected String createSyllabificationImageBase64(IPATranscript ipa) {
		final BufferedImage img = createSyllabificationImage(ipa);
		return imgToBase64(img);
	}
	
	protected BufferedImage createSyllabificationImage(IPATranscript ipa) {
		final SyllabificationDisplay display = getSyllabificationDisplay();
		final AtomicReference<BufferedImage> imgRef = new AtomicReference<>();
		
		try {
			SwingUtilities.invokeAndWait( () -> {
				display.setTranscript(ipa);
				display.revalidate();
				
				Dimension prefSize = display.getPreferredSize();
				
				BufferedImage img = new BufferedImage((int)prefSize.width+5, (int)prefSize.height, BufferedImage.TYPE_4BYTE_ABGR);
				
				Graphics2D g = img.createGraphics();
				SwingUtilities.paintComponent(g, display, syllabificationRenderPane, 0, 0, prefSize.width+5, prefSize.height);
				
				imgRef.set(img);;
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
		
		return imgRef.get();
	}
	
	protected BufferedImage createAlignmentImage(PhoneMap alignment) {
		final PhoneMapDisplay display = getAlignmentDisplay();
		final AtomicReference<BufferedImage> imgRef = new AtomicReference<>();
		
		try {
			SwingUtilities.invokeAndWait( () -> {
				display.setPhoneMapForGroup(0, alignment);
				display.revalidate();
				
				Dimension prefSize = display.getPreferredSize();
				
				BufferedImage img = new BufferedImage((int)prefSize.width, (int)prefSize.height, BufferedImage.TYPE_4BYTE_ABGR);
				
				Graphics2D g = img.createGraphics();
				SwingUtilities.paintComponent(g, display, alignmentRenderPane, 0, 0, prefSize.width, prefSize.height);
				
				imgRef.set(img);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
		
		return imgRef.get();
	}
	
	protected String createAlignmentImageBase64(PhoneMap alignment) {
		final BufferedImage img = createAlignmentImage(alignment);
		return imgToBase64(img);
	}
	
	private String imgToBase64(BufferedImage img) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", bos);
		} catch (IOException e) {
			LogUtil.warning(e.getLocalizedMessage(), e);
		}
		
		var base64data = Base64.getEncoder().encodeToString(bos.toByteArray());
		return base64data;
	}
	
}
