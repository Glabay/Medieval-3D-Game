package client.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import client.model.Client;
import client.net.packet.Packet;

public class Connection {

	private Channel channel = null;
	private Client application = null;

	public Connection(Client application) {
		this.application = application;
	}

	/**
	 * Connect the client to the server.
	 */
	public void connect(String hostname, int port) {
		try {
			ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
			bootstrap.setPipelineFactory(new ClientPipelineFactory(application));
			channel = bootstrap.connect(new InetSocketAddress(hostname, port)).awaitUninterruptibly().getChannel();
		} catch (Exception e) {
			Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, "No connection to the gameserver!", e);
		}
	}

	public void disconnect() {
		if (channel != null) {
			channel.close();
		}
	}

	public boolean isConnected() {
		if (channel != null) {
			return channel.isConnected();
		}
		return false;
	}

	public void sendPacket(Packet packet) {
		if (channel.isConnected()) {
			channel.write(packet);
		}
	}
}
