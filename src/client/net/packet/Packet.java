package client.net.packet;

import java.util.Arrays;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Used between the client and server communication.
 */
public class Packet {

	private ChannelBuffer buffer = null;
	private int opcode = 0;

	/**
	 * Creating a new empty packet.
	 *
	 * @param opcode the opcode of the packet
	 */
	public Packet(int opcode) {
		this.buffer = ChannelBuffers.dynamicBuffer();
		this.opcode = opcode;
	}

	/**
	 * Creating an already filled packet, used at the handling of the incoming
	 * packets
	 *
	 * @param opcode the opcode of the packet
	 * @param buffer the buffer
	 * @param length the length of the packet
	 */
	public Packet(int opcode, ChannelBuffer buffer, int length) {
		this.buffer = ChannelBuffers.copiedBuffer(buffer.readBytes(length));
		this.opcode = opcode;
	}

	/**
	 * Clear the buffer.
	 */
	public void clear() {
		buffer.clear();
	}

	//TODO: DOCS!
	public int getOpcode() {
		return opcode;
	}

	public Packet putByte(int b) {
		buffer.writeByte(b);
		return this;
	}

	public Packet putShort(int i) {
		buffer.writeShort(i);
		return this;
	}

	public Packet putInt(int i) {
		buffer.writeInt(i);
		return this;
	}

	public Packet putLong(long l) {
		buffer.writeLong(l);
		return this;
	}

	public Packet putString(String s) {
		buffer.writeByte(s.getBytes().length);
		buffer.writeBytes(s.getBytes());
		return this;
	}

	public Packet putFloat(float f) {
		buffer.writeFloat(f);
		return this;
	}

	public byte getByte() {
		return buffer.readByte();
	}

	public int getInt() {
		return buffer.readInt();
	}

	public short getShort() {
		return buffer.readShort();
	}

	public long getLong() {
		return buffer.readLong();
	}

	public String getString() {
		int length = getByte();
		byte[] b = new byte[length];
		for (int i = 0; i < b.length; i++) {
			b[i] = getByte();
		}
		return new String(b);
	}

	public float getFloat() {
		return buffer.readFloat();
	}

	public byte[] getDataByteArray() {
		if (buffer.hasArray()) {
			return Arrays.copyOfRange(buffer.array(), 0, buffer.writerIndex());
		}
		throw new IllegalStateException();
	}

	public int getReadableByteCount() {
		return buffer.readableBytes();
	}

}