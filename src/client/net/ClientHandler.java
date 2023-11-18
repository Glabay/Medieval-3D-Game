package client.net;

import java.util.logging.Level;
import java.util.logging.Logger;

import client.model.Client;
import client.net.packet.Packet;

import org.jboss.netty.channel.*;

import client.net.packet.PacketManager;

/**
 * Handle the client events like connecting, disconnecting etc.
 */
public class ClientHandler extends SimpleChannelHandler {

	private PacketManager packetHandler = new PacketManager();
	private Client application = null;

	public ClientHandler(Client application){
		this.application=application;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Packet packet = (Packet) e.getMessage();
		if (packet != null) {
			packetHandler.handle(packet, application);
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Logger.getLogger(ClientHandler.class.getName()).log(Level.WARNING, "Exception caught in the networking: {0}", e.getCause());
	}
}