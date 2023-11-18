package client.net.packet.impl;

import client.model.Client;
import client.net.packet.Packet;
import client.net.packet.PacketHandler;

/**
 * Read elements here, such as things the local player would need displayed in 2D.
 * Example: coins, health, stats, exp, days of membership left, etc.
 *
 */
public class LocalSessionDataPacket implements PacketHandler {

	@Override
	public void handle(final Client application, final Packet packet) {
		application.coins = packet.getInt();
		int skillCount = packet.getInt();
		for (int i = 0; i < skillCount; i++) {
			int skill = packet.getInt();
			int exp = packet.getInt();
			System.out.println("local player's skill #" + i + " exp is equal to " + exp);
		}
	}
	
}
