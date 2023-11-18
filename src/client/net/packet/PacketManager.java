package client.net.packet;

import client.model.Client;
import client.net.packet.impl.*;

public class PacketManager {

	public void handle(Packet packet, Client application) {
		switch (packet.getOpcode()) {
		case 1: //Login response
			new LoginResponsePacket().handle(application, packet);
			break;
		case 2: //Update local player
			new LocalSessionDataPacket().handle(application, packet);
			break;
		case 3: //Add remote player
			new RegisterRemotePlayerPacket().handle(application, packet);
			break;
		case 4: //Remove remote player
			new UnregisterRemotePlayerPacket().handle(application, packet);
			break;
		case 5: //Update remote player position
			new PlayerPositionUpdatePacket().handle(application, packet);
			break;
		case 7: //Add chatbox message
			new ConsoleMessagePacket().handle(application, packet);
			break;
		case 10: //Update player animation
			new AnimationUpdatePacket().handle(application, packet);
			break;
		default:
			System.out.println("Unregistered packet OpCode: " + packet.getOpcode());
			break;
		}
	}
	
}
