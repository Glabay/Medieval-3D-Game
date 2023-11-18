package client.util;

import java.io.File;

public class Settings {

	/**
	 * FULL_SCREEN does exactly what you think it does.
	 */
	public static final boolean FULL_SCREEN = false;
	
	/**
	 * Enabling PERFORMANCE_MODE will decrease overhead by lowering game aesthetics.
	 */
	public static final boolean PERFORMANCE_MODE = false;
	
	/**
	 * Setting this value too low will cause a lot of overhead.
	 * Setting this value too high will make entities to not be displayed fast enough.
	 * By default, this value is 6000, but can be adjusted to suit game needs.
	 */
	public static final int ENTITY_VISIBILITY_UPDATE_DELAY = 6000;
	
	public static final String HOST_IP = "127.0.0.1";
	
	public static final int HOST_PORT = 43594;
	
	public static boolean DEBUG = true;

	
	
	public static String getFileStorage(boolean published, String title) {
		if (published) {
			String path = ".dse_console" + File.separator + "games" + File.separator + title + File.separator;
			return System.getProperty("user.home", ".") + path;
		} else {
			return "./file_storage/";
		}
	}

}
