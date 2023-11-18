package client.util;

import client.engine.Engine;
import client.util.login.LoginWindow;

public class Loader {

	private static String name = "";
	private static String password = "";
	
	public Loader(State state) {
		switch(state) {
			case LOGIN_MENU:
				new LoginWindow();
				break;
			case GAME:
				new Thread(new Engine(getName(), getPassword())).run();
				break;
			case CHARACTER_CREATION:
				break;
			default:
				break;
		}
	}
	
	public static void setName(String name) {
		Loader.name = name;
	}
	
	private String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}
	
	public static void setPassword(String password) {
		Loader.password = password;
	}
	
	public static enum State {
		LOGIN_MENU, GAME, CHARACTER_CREATION
	}
}
