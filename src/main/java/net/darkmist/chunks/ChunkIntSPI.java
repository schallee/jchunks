package net.darkmist.chunks;

import java.nio.ByteOrder;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static net.darkmist.chunks.Util.requirePosIntOff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ChunkIntSPI
{
	/**
	 * Get the byte at the specified offset.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	public int getByte(int off);

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(int off, ByteOrder order);
	public int getInt(int off, ByteOrder order);
	public long getLong(int off, ByteOrder order);

	/**
	 * Get the size as a int.
	 * @return the size of the chunk as a int.
	 */
	public int getSize();

	/** 
	 * @return <code>true</code> if the chunk coalesced or <code>false</code> otherwise.
	 */
	public boolean isCoalesced();

	/**
	 * @return The coalesced chunk or <code>null</code> if he chunk itself should be returned.
	 */
	public Chunk coalesce();

	/**
	 * Get a chunk from a subset of the current chunk. 
	 * @return The sub chunk, <code>null</code> if the same chunk should be returned or <code>null</code> if the default subchunking should be used.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 *
	 * FIXME: This should not be part of the base SPI. It should be handled by a capability interface.
	 *
	 */
	public Chunk subChunk(int off, int len);

	public byte[] copyTo(byte[] bytes, int chunkOff, int arrayOff, int len);

	@SuppressWarnings("PMD.ExcessiveMethodLength")	// It's all an anoymous class
	public static ChunkSPI adapt(final ChunkIntSPI target)
	{
		final Logger logger = LoggerFactory.getLogger(ChunkIntSPI.class);
		requireNonNull(target);
		return new ChunkSPI()
		{
			@Override
			public final int getByte(long off)
			{
				return target.getByte(requirePosIntOff(off));
			}
			
			@Override
			@SuppressWarnings("PMD.AvoidUsingShortType")
			public final short getShort(long off, ByteOrder order)
			{
				return target.getShort(requirePosIntOff(off),order);
			}
			
			@Override
			public final int getInt(long off, ByteOrder order)
			{
				return target.getInt(requirePosIntOff(off), order);
			}
			
			@Override
			public final long getLong(long off, ByteOrder order)
			{
				return target.getLong(requirePosIntOff(off), order);
			}
			
			@Override
			public final long getSize()
			{
				return target.getSize();
			}
			
			@Override
			public final boolean isCoalesced()
			{
				return target.isCoalesced();
			}
			
			@Override
			@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
			public final Chunk coalesce()
			{
				return target.coalesce();
			}
			
			@Override
			@SuppressFBWarnings(value="CRLF_INJECTION_LOGS", justification="This could only happen if the caught index out of bounds exception msg contains a CRLF.")
			public final Chunk subChunk(long off, long len)
			{
				try
				{
					return target.subChunk(requirePosIntOff(off), requirePosIntOff(len));
				}
				catch(IndexOutOfBoundsException e)
				{
					if(logger.isDebugEnabled())
						logger.debug("getSize()={} off={} len={}", getSize(), off, len, e);
					throw e;
				}
			}
			
			@Override
			public final byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
			{
				return target.copyTo(bytes, requirePosIntOff(chunkOff), arrayOff, len);
			}

			@Override
			public boolean equals(Object o)
			{
				int size;
				ChunkSPI that;

				if(this==o)
					return true;
				if(o==null)
					return false;
				if(!(o instanceof ChunkSPI))
					return false;

				that = (ChunkSPI)o;

				if(this.getSize() != that.getSize())
					return false;
				size = target.getSize();
				// FIXME: performance?
				for(int i=0;i<size;i++)
					if(this.getByte(i) != that.getByte(i))
						return false;
				return true;
			}

			@Override
			public String toString()
			{
				return target.toString();
			}

			@Override
			public int hashCode()
			{
				return target.hashCode();
			}
		};
	}
}
