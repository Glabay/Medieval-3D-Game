package client.net.packet;

import client.model.Client;

public interface PacketHandler {

	public void handle(Client application, Packet packet);
}
