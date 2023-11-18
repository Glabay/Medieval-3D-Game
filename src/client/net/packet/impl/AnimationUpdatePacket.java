package client.net.packet.impl;

import client.model.Client;
import client.net.packet.Packet;
import client.net.packet.PacketHandler;

public class AnimationUpdatePacket implements PacketHandler {

	@Override
	public void handle(final Client application, final Packet packet) {
		int uid = packet.getInt();
		int animationId = packet.getInt();
		application.updateAnimation(uid, animationId);
	}
	
}
