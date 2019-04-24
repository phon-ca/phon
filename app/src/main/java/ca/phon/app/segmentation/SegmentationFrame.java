package ca.phon.app.segmentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.swing.JScrollPane;

import ca.phon.media.sampled.PCMSampled;
import ca.phon.ui.CommonModuleFrame;

/**
 * Segmentation UI
 */
public class SegmentationFrame extends CommonModuleFrame {
	
	public static void main(String[] args) throws Exception {
		
		PCMSampled sampled = new PCMSampled(new File("/Users/ghedlund/Movies/The.Knick.S02E01.720p.HDTV.x264-KILLERS[eztv].wav"));
		System.out.println(sampled.getLength());
		WaveformDisplay display = new WaveformDisplay(sampled);
		display.setOpaque(true);
		display.setBackground(Color.white);
		display.setChannelHeight(50);
		display.setChannelGap(50);
		display.setStartTime(0f);
		display.setEndTime(sampled.getLength());
		display.setSecondsPerPixel(0.05f);
		
		SegmentationFrame f = new SegmentationFrame();
		
		f.setLayout(new BorderLayout());
		f.add(new JScrollPane(display), BorderLayout.CENTER);
		f.pack();
		f.setSize(500, 300);
		f.centerWindow();
		f.setVisible(true);
	}
	
}
