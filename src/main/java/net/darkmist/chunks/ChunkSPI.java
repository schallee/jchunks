package net.darkmist.chunks;

import java.io.IOException;
import java.io.DataOutput;
import java.nio.ByteOrder;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FUTURE: support sizes, lengths and offsets as java.lang.Number and handle sizes larger than Long.MAX_VALUE.
public interface ChunkSPI
{
	public static final Logger logger = LoggerFactory.getLogger(ChunkSPI.class);

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
	 * @return The sub chunk, <code>null</code> if the same chunk should be returned.
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

		logger.debug("writeTo: size={} bufSize={}", size, bufSize);
		if(size <= bufSize)
		{
			buf = new byte[(int)size];
			copyTo(buf, 0l, 0, (int)size);
			dataOut.write(buf);
			return;
		}

		extra = (int)(size % bufSize);
		logger.debug("\textra={}", extra);

		fullBuffersEnd = size;
		if(extra != 0)
		{
			logger.debug("have extra");
			fullBuffersEnd = size-extra;
		}

		buf=Tunables.getTmpBuf();
		for(off=0l;off<fullBuffersEnd;off+=buf.length)
		{
			copyTo(buf, off, 0, buf.length);
			dataOut.write(buf);
		}

		if(extra != 0)
		{
			logger.debug("have extra");
			copyTo(buf, fullBuffersEnd, 0, extra);
			dataOut.write(buf, 0, extra);
		}
	}

	/*
	public static class Wrapper implements ChunkSPI
	{
		private final ChunkSPI target;

		public Wrapper(ChunkSPI target)
		{
			this.target = Objects.requireNonNull(target);
		}

		@Override
		public int getByte(long off)
		{
			return target.getByte(off);
		}

		@Override
		@SuppressWarnings("PMD.AvoidUsingShortType")
		public short getShort(long off, ByteOrder order)
		{
			return target.getShort(off, order);
		}

		@Override
		public int getInt(long off, ByteOrder order)
		{
			return target.getInt(off, order);
		}

		@Override
		public long getLong(long off, ByteOrder order)
		{
			return target.getLong(off, order);
		}

		@Override
		public long getSize()
		{
			return target.getSize();
		}

		@Override
		public Chunk subChunk(long off, long len)
		{
			return target.subChunk(off,len);
		}

		@Override
		public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
		{
			return target.copyTo(bytes, chunkOff, arrayOff, len);
		}

		@Override
		public boolean isCoalesced()
		{
			return target.isCoalesced();
		}

		@Override
		public Chunk coalesce()
		{
			return target.coalesce();
		}

		@Override
		public void writeTo(DataOutput dataOut, Set<WriteFlag> flags) throws IOException
		{
			target.writeTo(dataOut, flags);
		}
	}
	*/
}
