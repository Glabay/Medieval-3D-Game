package client;

import java.io.File;

import client.util.Loader;
import client.util.Settings;
import client.util.Loader.State;

public class Main {

	public static void main(String[] args) {
		try {
			System.setProperty("sun.java2d.noddraw", "true");
			boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
			System.setProperty("org.lwjgl.librarypath", new File((windows ? "./file_storage/" : Settings.getFileStorage(false, "glabtech")) + "java" + File.separator + (windows ? "windows" : "linux") + File.separator).getAbsolutePath());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		new Loader(State.LOGIN_MENU);
	}

}
