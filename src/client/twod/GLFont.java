package client.twod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import client.twod.TexturePack;

import com.threed.jpct.FrameBuffer;

/**
 * <p>
 * creates GL renderable (blittable) font out of given AWT font. a jPCT texture is created and added to TextureManager on the fly.
 * </p>
 * 
 * <p>
 * in contrast with its name, this class can be used for software renderer too. but to tell the truth, i would stick to Java2D for software renderer ;)
 * </p>
 * 
 * this class uses {@link TexturePack} behind the scenes.
 * 
 * @see TexturePack
 * 
 * @author hakan eryargi (r a f t)
 */
public class GLFont {

	/** standard characters */
	public static final String ENGLISH = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`1234567890-=~!@#$%^&*()_+[]{}\\|:;\"'<>,.?/";

	/** the awt font */
	public final Font font;
	/** characters this GLFont is created for */
	public final String alphabet;
	/**
	 * regular font height. note some special characters may not fit into this height. see {@link FontMetrics} for a discussion
	 */
	public final int fontHeight;
	private final int baseline;

	private final int[] charWidths;
	private final Dimension stringBounds = new Dimension();

	private final TexturePack pack = new TexturePack();

	/**
	 * creates a GLFont for given awt Font consists of characters
	 * 
	 * @param font
	 *            the awt font
	 */
	public GLFont(Font font) {
		this.font = font;
		this.alphabet = eliminateDuplicates(ENGLISH);
		this.charWidths = new int[alphabet.length()];

		Graphics2D g2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		FontMetrics fontMetrics = g2d.getFontMetrics(font);

		this.fontHeight = fontMetrics.getHeight();
		this.baseline = fontMetrics.getMaxAscent();
		int height = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();

		for (int i = 0; i < alphabet.length(); i++) {
			String c = alphabet.substring(i, i + 1);
			Rectangle2D bounds = fontMetrics.getStringBounds(c, g2d);
			int width = (int) bounds.getWidth();
			charWidths[i] = width;

			BufferedImage charImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D charGraphics = charImage.createGraphics();
			charGraphics.setRenderingHints(g2d.getRenderingHints());

			charGraphics.setFont(font);
			charGraphics.setColor(Color.WHITE);
			charGraphics.drawString(c, 0, baseline);

			charGraphics.dispose();
			pack.addImage(charImage);
		}
		pack.pack(TexturePack.ALPHA_USE);
	}

	private String eliminateDuplicates(String s) {
		StringBuilder sb = new StringBuilder(s);

		for (int i = 0; i < sb.length(); i++) {
			String c = sb.substring(i, i + 1);
			int next = -1;
			while ((next = sb.indexOf(c, i + 1)) != -1) {
				sb.deleteCharAt(next);
			}
		}
		return sb.toString();
	}

	/**
	 * returns how much area given string occupies. note this method always returns same Dimension instance
	 */
	public Dimension getStringBounds(String s) {
		int width = 0;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int index = alphabet.indexOf(c);
			if (index == -1)
				index = alphabet.indexOf('?');
			if (index != -1) {
				width += charWidths[index];
			}
		}
		stringBounds.setSize(width, fontHeight);
		return stringBounds;
	}

	/**
	 * blits given string to frame buffer. works very similar to awt.Graphics#drawString(..) that is: x coordinate is left most point in string, y is baseline
	 * 
	 * @param buffer
	 *            buffer to blit into
	 * @param s
	 *            string to blit
	 * @param x
	 *            leftmost point
	 * @param transparency
	 *            transparency value, make sure >= 0
	 * @param color
	 *            text color
	 * @param y
	 *            baseline private void blitString(FrameBuffer buffer, String s, int x, int y, int transparency, Color color) { y -= baseline;
	 * 
	 *            for (int i = 0; i < s.length(); i++) { char c = s.charAt(i); int index = alphabet.indexOf(c); if (index == -1) index = alphabet.indexOf('?'); if (index != -1) { Dimension size = pack.blit(buffer, index, x, y, transparency, false, color); x += size.width; } } }
	 */

	public void drawString(FrameBuffer buffer, String s, int x, int y, Color color) {
		y -= baseline;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int index = alphabet.indexOf(c);
			if (index == -1)
				index = alphabet.indexOf('?');
			if (index != -1) {
				Dimension size = pack.blit(buffer, index, x, y, 0, false, color);
				x += size.width;
			}
		}
	}

	public void drawShadowedString(FrameBuffer buffer, String s, int x, int y, Color font) {
		y -= baseline;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int index = alphabet.indexOf(c);
			if (index == -1) {
				index = alphabet.indexOf('?');
			}
			if (index != -1) {
				Dimension size = pack.blit(buffer, index, x - 1, y - 1, 0, false, Color.BLACK);
				size = pack.blit(buffer, index, x, y, 0, false, font);
				x += size.width;
			}
		}
	}

	public void drawShadowedString(FrameBuffer buffer, String s, int x, int y, Color font, Color shadow) {
		y -= baseline;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int index = alphabet.indexOf(c);
			if (index == -1) {
				index = alphabet.indexOf('?');
			}
			if (index != -1) {
				Dimension size = pack.blit(buffer, index, x - 1, y - 1, 0, false, shadow);
				size = pack.blit(buffer, index, x, y, 0, false, font);
				x += size.width;
			}
		}
	}

}