package net.darkmist.chunks;

import java.nio.ByteOrder;

// FUTURE: support sizes, lengths and offsets as java.lang.Number and handle sizes larger than Long.MAX_VALUE.
public interface ChunkSPI
{
	/**
	 * Get the byte at the specified offset.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	public int getByte(long off);

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order);
	public int getInt(long off, ByteOrder order);
	public long getLong(long off, ByteOrder order);

	/**
	 * Get the size as a long.
	 * @return the size of the chunk as a long.
	 */
	public long getSize();

	/**
	 * Get a chunk from a subset of the current chunk. 
	 * @return The sub chunk, <code>null</code> if the same chunk should be returned or <code>null</code> if the default subchunking should be used.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 *
	 * FIXME: This should not be part of the base SPI. It should be handled by a capability interface.
	 *
	 */
	public Chunk subChunk(long off, long len);

	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len);

	/** 
	 * @return <code>true</code> if the chunk coalesced or <code>false</code> otherwise.
	 */
	public boolean isCoalesced();

	/**
	 * @return The coalesced chunk or <code>null</code> if he chunk itself should be returned.
	 */
	public Chunk coalesce();

}
