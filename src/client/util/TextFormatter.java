package client.util;

import java.awt.event.KeyEvent;

/**
 * Contains a collection of useful text related utilities.
 *
 */
public class TextFormatter { 

	private static long consoleInputThrottler = 0L;
	
	/**
	 * Default valid characters.
	 */
	private static final char VALID_CHARS[] = { '!', '@', '#', '$', '%', '^',
			'&', '*', '(', ')', '-', '_', '=', '+', '[', '{', ']', '}', ';',
			':', '\'', '"', ',', '<', '.', '>', '/', '?', ' ' };

	/**
	 * aA-zZ, 0-9, and VALID_CHARS[] are THE ONLY usable chat
	 * characters.
	 */
	public static boolean validChar(char keyChar) {
		if (Character.isLetterOrDigit(keyChar)) {
			return true;
		}
		for (int i = 0; i < VALID_CHARS.length; i++) {
			if (keyChar == VALID_CHARS[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Updates a string of text. Cuts the text at <maxLength>.
	 * This is ideal for optimizing in-game chat / input related content.
	 */
	public static String updateText(String text, int keyCode, char keyChar, int maxLength) {
		if (keyCode == KeyEvent.VK_BACK_SPACE && text.length() > 0 && consoleInputThrottler <= System.currentTimeMillis() - 100) {
			consoleInputThrottler = System.currentTimeMillis();
			text = text.substring(0, text.length() - 1);
		} else if (validChar(keyChar) && text.length() + 1 <= maxLength) {
			text += keyChar;
		}
		return text;
	}
	
	/**
	 * Formats a string, specifically strings like a village name, entity name, item name, etc....
	 * Example: "mIke gLAbaY" would come out as "Mike Glabay"
	 */
	public static String formatForDisplay(String string) {
		char[] c = string.toLowerCase().trim().toCharArray();
		if (c.length > 0) {
			c[0] = Character.toUpperCase(c[0]);
		}
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i + 1] = Character.toUpperCase(c[i + 1]);
			}
		}
		return new String(c);
	}

	/**
	 * Converts a long into a string.
	 */
	public static String longToString(long l) {
		int i = 0;
		char ac[] = new char[12];
		while (l != 0L) {
			long l1 = l;
			l /= 37L;
			ac[11 - i++] = VALID_CHARS[(int) (l1 - l * 37L)];
		}
		return new String(ac, 12 - i, i);
	}
	
	/**
	 * Converts a string into a long.
	 */
	public static long stringToLong(String s) {
		long l = 0L;
		for (int i = 0; i < s.length() && i < 12; i++) {
			char c = s.charAt(i);
			l *= 37L;
			if (c >= 'A' && c <= 'Z') {
				l += 1 + c - 65;
			} else if (c >= 'a' && c <= 'z') {
				l += 1 + c - 97;
			} else if (c >= '0' && c <= '9') {
				l += 27 + c - 48;
			}
		}
		while (l % 37L == 0L && l != 0L) {
			l /= 37L;
		}
		return l;
	}

}