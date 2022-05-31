package ca.phon.media;

import ca.phon.opgraph.nodes.phonex.PhonexGroupNode;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.util.PhonConstants;
import ca.phon.util.icons.*;
import org.w3c.dom.css.Rect;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.PropertyChangeListener;

public class DefaultVolumeSliderUI extends VolumeSliderUI {

	private final static int DEFAULT_PREF_HEIGHT = IconSize.SMALL.getHeight() + 10;

	// preferred length of slider
	private final static int DEFAULT_PREF_LENGTH = 80;

	private final static int ICON_MARGIN_RIGHT = 2;

	private final static float FONT_SIZE = 10.0f;

	private ImageIcon volumeIcon = IconManager.getInstance().getIcon("status/audio-volume-high", IconSize.SMALL);

	private ImageIcon mutedIcon = IconManager.getInstance().getIcon("status/audio-volume-muted", IconSize.SMALL);

	private VolumeSlider slider;

	private final PropertyChangeListener propListener = (evt) -> {
		if(slider != null)
			slider.repaint();
	};

	@Override
	public void installUI(JComponent c) {
		if(!(c instanceof VolumeSlider)) throw new IllegalArgumentException("not a volume slider");
		super.installUI(c);
		this.slider = (VolumeSlider) c;
		this.slider.addPropertyChangeListener(propListener);
		this.slider.addMouseListener(mouseHandler);
		this.slider.addMouseMotionListener(mouseHandler);
		this.slider.addMouseWheelListener(mouseHandler);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		if(this.slider != null) {
			this.slider.removePropertyChangeListener(propListener);
			this.slider.removeMouseListener(mouseHandler);
			this.slider.removeMouseMotionListener(mouseHandler);
			this.slider.removeMouseWheelListener(mouseHandler);
		}
	}

	private Point interpolate(Point p1, Point p2, double t){
		return new Point((int)Math.round(p1.x * (1-t) + p2.x*t),
				(int)Math.round(p1.y * (1-t) + p2.y*t));
	}

	private Rectangle getVolumeIconRect(JComponent c) {
		final int height = c.getHeight();
		final int btm = height - c.getInsets().bottom;
		final int left = c.getInsets().left;

		return new Rectangle(left, btm - IconSize.SMALL.getHeight(),
				IconSize.SMALL.getWidth(), IconSize.SMALL.getHeight());
	}

	private Rectangle getVolumeSliderRect(JComponent c) {
		final int height = c.getHeight();
		final int width = c.getWidth();

		final int btm = height - c.getInsets().bottom;
		final int top = c.getInsets().top;
		final int triX = IconSize.SMALL.getWidth() + ICON_MARGIN_RIGHT;
		final int triWidth = width - ICON_MARGIN_RIGHT - IconSize.SMALL.getWidth();

		return new Rectangle(triX, top, triWidth, btm - top);
	}

	private Path2D getVolumeTri(Rectangle volumeRect) {
		float cornerDist = 5.0f;
		final double lineWidth = 2.0;
		final double points[][] = {
				{volumeRect.getMinX(), volumeRect.getMaxY()-lineWidth},
				{volumeRect.getMaxX() - lineWidth, volumeRect.getMaxY()-lineWidth},
				{volumeRect.getMaxX() - lineWidth, volumeRect.getMinY() + lineWidth},
				{volumeRect.getMinX(), volumeRect.getMaxY() - cornerDist - lineWidth}
		};

		Point p1 = new Point((int)points[0][0], (int)points[0][1]);
		Point p2 = new Point((int)points[1][0], (int)points[1][1]);
		Point p3 = new Point((int)points[2][0], (int)points[2][1]);
		Point p4 = new Point((int)points[3][0], (int)points[3][1]);

		// 5px on each side of line
		float offset1 = cornerDist / (float)volumeRect.getWidth();
		Point p1p2a = interpolate(p1, p2, offset1);
		Point p1p2b = interpolate(p1, p2, 1-offset1);

		float offset2 = cornerDist / (float)volumeRect.getHeight();
		Point p2p3a = interpolate(p2, p3, offset2);
		Point p2p3b = interpolate(p2, p3, 1-offset2);

		float diag = (float)Math.sqrt(volumeRect.getWidth() * volumeRect.getWidth()
				+ volumeRect.getHeight() * volumeRect.getHeight());
		float offset3 = cornerDist / diag;
		Point p3p4a = interpolate(p3, p4, offset3);
		Point p3p4b = interpolate(p3, p4, 1-offset3);

		float offset4 = (cornerDist/2) / (float)p4.distance(p1);
		Point p4p1a = interpolate(p4, p1, offset4);
		Point p4p1b = interpolate(p4, p1, 1.0f-offset4);

		QuadCurve2D c1 = new QuadCurve2D.Double(p1p2b.x, p1p2b.y, p2.x, p2.y, p2p3a.x, p2p3a.y);
		QuadCurve2D c2 = new QuadCurve2D.Double(p2p3b.x, p2p3b.y, p3.x, p3.y, p3p4a.x, p3p4a.y);
		QuadCurve2D c3 = new QuadCurve2D.Double(p3p4b.x, p3p4b.y, p4.x, p4.y, p4p1a.x, p4p1a.y);
		QuadCurve2D c4 = new QuadCurve2D.Double(p4p1b.x, p4p1b.y, p1.x, p1.y, p1p2a.x, p1p2a.y);

		Path2D path = new Path2D.Double();
		AffineTransform at = new AffineTransform();
		path.moveTo(p1p2a.x, p1p2a.y);
		path.lineTo(p1p2b.x, p1p2b.y);
		path.append(c1.getPathIterator(at), true);
		path.lineTo(p2p3b.x, p2p3b.y);
		path.append(c2.getPathIterator(at), true);
		path.lineTo(p3p4b.x, p3p4b.y);
		path.append(c3.getPathIterator(at), true);
		path.lineTo(p4p1b.x, p4p1b.y);
		path.append(c4.getPathIterator(at), true);
		path.closePath();

		return path;
	}

	private Paint createVolumePaint(Rectangle volumeRect) {
		int width = volumeRect.width;
		int height = volumeRect.height;

		final Color colors[] = new Color[] {
				//	Color.green.darker(), Color.green, Color.orange
				(slider.isMuted() ? Color.gray : Color.decode("#9fafd1")),
				(slider.isMuted() ? Color.lightGray : Color.decode("#c7d0e4")),
				(slider.isMuted() ? Color.gray : Color.orange)
		};
		final float vols[] = { 0.0f, 1.0f, 1.25f };

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();

		for(int i = 0; i < colors.length-1; i++) {
			float x1 = Math.round(width * (vols[i] / slider.getModel().getMaximumVolumeLevel()));
			float x2 = Math.round(width * (vols[i+1] / slider.getModel().getMaximumVolumeLevel()));
			Color c1 = colors[i];
			Color c2 = colors[i+1];

			GradientPaint gp = new GradientPaint((int)x1, 0, c1, (int)x2, 0, c2);
			g2.setPaint(gp);
			g2.fillRect((int)x1, 0, (int)(x2-x1), height);
		}
		g2.dispose();

		TexturePaint tp = new TexturePaint(img, new Rectangle2D.Double(volumeRect.x, volumeRect.y, width, height));
		return tp;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		final int height = c.getHeight();
		final int width = c.getWidth();

		final int btm = height - c.getInsets().bottom;
		final int top = c.getInsets().top;
		final int left = c.getInsets().left;
		final int right = width - c.getInsets().right;

		ImageIcon icn = (slider.isMuted() ? mutedIcon : volumeIcon);
		if(!c.isEnabled())
			icn = IconManager.getInstance().getDisabledIcon(icn);
		icn.paintIcon(c, g, left, btm - IconSize.SMALL.getHeight());

		Stroke s = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		Color strokeColor = (!c.isEnabled() ? Color.lightGray : (c.isFocusOwner() ? Color.blue : Color.gray));

		Rectangle volumeTriRect = getVolumeSliderRect(c);
		Path2D volumeTri = getVolumeTri(volumeTriRect);

		Rectangle2D volumeRect = new Rectangle2D.Double(volumeTriRect.x, volumeTriRect.y,
			volumeTriRect.getWidth() * (slider.getVolumeLevel()/slider.getModel().getMaximumVolumeLevel()), volumeTriRect.height);

		Area a1 = new Area(volumeRect);
		Area a2 = new Area(volumeTri);
		a1.intersect(a2);

		Paint oldPaint = g2.getPaint();
		g2.setPaint(createVolumePaint(volumeTriRect));
		g2.fill(a1);
		g2.setPaint(oldPaint);

		g2.setColor(strokeColor);
		g2.setStroke(s);
		g2.draw(volumeTri);

		Font f = c.getFont().deriveFont(FONT_SIZE);
		g2.setFont(f);
		Color fontColor = (!c.isEnabled() ? Color.lightGray : c.getForeground());
		g2.setColor(fontColor);
		g2.drawString(String.format("%d%%", Math.round(100 * slider.getVolumeLevel())),
				IconSize.SMALL.getWidth() + ICON_MARGIN_RIGHT, top + 10);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		// TODO handle vertical orientation
		int prefHeight = DEFAULT_PREF_HEIGHT;
		int prefWidth = IconSize.SMALL.getWidth() + ICON_MARGIN_RIGHT + DEFAULT_PREF_LENGTH;

		prefHeight += c.getInsets().top + c.getInsets().bottom;
		prefWidth += c.getInsets().left + c.getInsets().right;

		return new Dimension(prefWidth, prefHeight);
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {
		return getPreferredSize(c);
	}

	private final MouseInputAdapter mouseHandler = new MouseInputAdapter() {

		private void handleVolumeHit(MouseEvent e) {
			final Rectangle volRect = getVolumeSliderRect(slider);
			if(volRect.contains(e.getPoint())) {
				float newVolumeLevel = (float) ((e.getPoint().x - volRect.getX()) / volRect.getWidth());
				newVolumeLevel *= slider.getModel().getMaximumVolumeLevel() * 100.0f;
				int volumeAsInteger = (int) Math.round(newVolumeLevel);
				slider.getModel().setVolumeLevel(volumeAsInteger / 100.0f);
			} else {
				if(volRect.getMaxX() < e.getX())
					slider.getModel().setVolumeLevel(VolumeModel.MAX_LEVEL);
				else if(volRect.getMinX() > e.getX())
					slider.getModel().setVolumeLevel(VolumeModel.MIN_LEVEL);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			final Rectangle iconRect = getVolumeIconRect(slider);
			if(iconRect.contains(e.getPoint())) {
				// toggle mute
				slider.getModel().setMuted(!slider.getModel().isMuted());
			} else {
				handleVolumeHit(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// move to nearest 5
			float currentVolume = slider.getVolumeLevel() * 100.0f;
			int mod5 = (int)(currentVolume) % 5;

			if(e.getWheelRotation() < 0) {
				// move up
				currentVolume = Math.min(VolumeModel.MAX_LEVEL * 100.0f, currentVolume + (5 - mod5));
			} else {
				// move down
				currentVolume = Math.max(VolumeModel.MIN_LEVEL * 100.0f, currentVolume - (mod5 == 0 ? 5.0f : mod5));
			}

			slider.setVolumeLevel(Math.round(currentVolume) / 100.0f);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			handleVolumeHit(e);
		}

	};

}
