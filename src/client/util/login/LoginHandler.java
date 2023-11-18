package client.util.login;

import client.util.Loader;
import client.util.Loader.State;

public class LoginHandler {
	
	private String username;
	private String password;
	
	public void initLogin(String name, String pass) {
		setUsername(name);
		setPassword(pass);
		
		Loader.setName(getUsername());
		Loader.setPassword(getPassword());
		new Loader(State.GAME);
	}


	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
