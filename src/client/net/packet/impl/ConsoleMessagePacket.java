package client.net.packet.impl;

import client.model.Client;
import client.net.packet.Packet;
import client.net.packet.PacketHandler;

public class ConsoleMessagePacket implements PacketHandler {

	@Override
	public void handle(final Client application, final Packet packet) {
		application.addMessage(packet.getString());
	}
	
}
