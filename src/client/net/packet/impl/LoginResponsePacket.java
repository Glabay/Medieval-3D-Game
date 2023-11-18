package client.net.packet.impl;

import client.model.Client;
import client.net.packet.Packet;
import client.net.packet.PacketHandler;

public class LoginResponsePacket implements PacketHandler {

	private static final int LOGIN_OK = 1;
	private static final int LOGIN_WRONG_NAME_OR_PASS = 2;
	private static final int LOGIN_ALREADY_LOGGED_IN = 4;
	private static final int GAME_SERVER_IS_FULL = 3;

	@Override
	public void handle(final Client application, final Packet packet) {
		int response = packet.getByte();

		switch (response) {
		case LOGIN_OK:
			System.out.println("Everything went ok!");
			application.loggedIn = true;
			application.reconnectionAttempt = 0;
			break;
		case LOGIN_WRONG_NAME_OR_PASS:
			System.out.println("Wrong username or password!");
			break;
		case LOGIN_ALREADY_LOGGED_IN:
			System.out.println("You are already logged in!");
			break;
		case GAME_SERVER_IS_FULL:
			System.out.println("The server is full!");
			break;
		}
	}
}
