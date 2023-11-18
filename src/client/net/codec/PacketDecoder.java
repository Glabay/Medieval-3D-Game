package client.net.codec;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import client.net.packet.Packet;

public class PacketDecoder extends FrameDecoder {

	private static final int OPCODE_LENGTH = 1;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel chl, ChannelBuffer buf) throws Exception {
		// Make sure if the length field was received.
		if (buf.readableBytes() < 2) {
			// The length field was not received yet - return null.
			// This method will be invoked again when more packets are
			// received and appended to the buffer.
			return null;
		}

		// Mark the current buffer position before reading the length field
		// because the whole frame might not be in the buffer yet.
		// We will reset the buffer position to the marked position if
		// there's not enough bytes in the buffer.
		buf.markReaderIndex();

		// Read the length field. 
		int length = buf.readShort() + OPCODE_LENGTH;

		// Make sure if there's enough bytes in the buffer.
		if (buf.readableBytes() < length) {
			// The whole bytes were not received yet - return null.
			// This method will be invoked again when more packets are
			// received and appended to the buffer.

			// Reset to the marked position to read the length field again
			// next time.
			buf.resetReaderIndex();

			return null;
		}

		// There's enough bytes in the buffer. Read it.
		Packet packet = new Packet((int) buf.readByte(), buf, length - OPCODE_LENGTH);

		// Successfully decoded a frame.  Return the decoded frame.
		return packet;
	}

}