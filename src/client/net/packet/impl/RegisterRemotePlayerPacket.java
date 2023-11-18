package client.net.packet.impl;

import client.model.Client;
import client.net.packet.Packet;
import client.net.packet.PacketHandler;

public class RegisterRemotePlayerPacket implements PacketHandler {

	@Override
	public void handle(final Client application, final Packet packet) {
		int uid = packet.getInt();
		float x = packet.getFloat();
		float y = packet.getFloat() + 2.25f;
		float z = packet.getFloat();
		float rotationY = packet.getFloat();
		int privileges = packet.getInt();
		application.getPlayerManager().register(uid, x, y, z, rotationY, privileges);
	}
}
