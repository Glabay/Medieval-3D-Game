package client.net.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import client.net.packet.Packet;

public class PacketEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel chl, Object obj) throws Exception {
		Packet packet = (Packet) obj;
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeShort(packet.getReadableByteCount());
		buffer.writeByte(packet.getOpcode());
		buffer.writeBytes(packet.getDataByteArray());

		return buffer;
	}
}