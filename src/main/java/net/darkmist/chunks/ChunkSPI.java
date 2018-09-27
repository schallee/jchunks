package net.darkmist.chunks;

import java.io.IOException;
import java.io.DataOutput;
import java.nio.ByteOrder;
import java.util.Set;

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
	 * @param off Offset into chunk.
	 * @param len Length of sub chunk.
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

	public default void writeTo(DataOutput dataOut, Set<WriteFlag> flags) throws IOException
	{
		final long size = getSize();
		byte[] buf;
		long fullBuffersEnd;
		long off;
		int extra;
		int bufSize = Tunables.getTmpBufSize();

		if(size <= bufSize)
		{
			buf = new byte[(int)size];
			copyTo(buf, 0l, 0, (int)size);
			dataOut.write(buf);
			return;
		}

		extra = (int)(size % bufSize);

		fullBuffersEnd = size;
		if(extra != 0)
			fullBuffersEnd = size-extra;

		buf=Tunables.getTmpBuf();
		for(off=0l;off<fullBuffersEnd;off+=buf.length)
		{
			copyTo(buf, off, 0, buf.length);
			dataOut.write(buf);
		}

		if(extra != 0)
		{
			copyTo(buf, fullBuffersEnd, 0, extra);
			dataOut.write(buf, 0, extra);
		}
	}
}
