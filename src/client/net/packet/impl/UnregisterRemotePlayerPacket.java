package client.net.packet.impl;

import client.model.Client;
import client.net.packet.Packet;
import client.net.packet.PacketHandler;

public class UnregisterRemotePlayerPacket implements PacketHandler {

	@Override
	public void handle(final Client application, final Packet packet) {
		application.getPlayerManager().unregister(packet.getInt());
	}
}
