package client.net;

import client.model.Client;
import client.net.codec.PacketDecoder;
import client.net.codec.PacketEncoder;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Add the encoders/decoders etc.
 */
public class ClientPipelineFactory implements ChannelPipelineFactory {

	private Client application = null;

	public ClientPipelineFactory(Client application) {
		this.application = application;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", new PacketDecoder());
		pipeline.addLast("encoder", new PacketEncoder());
		pipeline.addLast("handler", new ClientHandler(application));
		return pipeline;
	}
	
}
