package net.darkmist.chunks;

import java.nio.ByteOrder;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static net.darkmist.chunks.Util.requirePosInt;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Variant of {@link ChunkSPI} using integers for offsets and lengths. Use
 * {@link #adapt(ChunkIntSPI)} to adapt a <code>ChunkIntSPI</code>
 * to a <code>ChunkSPI</code>.
 */
@com.google.errorprone.annotations.Immutable
@Immutable
interface ChunkIntSPI
{
	/**
	 * Get the byte at the specified offset.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	public int getByte(int off);

	/**
	 * Get a signed <code>short</code> value.
	 * @param off Offset of the desired <code>sort</code>
	 * @param order The byte order of the <code>short</code> to return.
	 * @return <code>short</code> value at <code>off</code>
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(int off, ByteOrder order);

	/**
	 * Get a signed <code>int</code> value.
	 * @param off Offset of the desired <code>int</code>
	 * @param order The byte order of the <code>int</code> to return.
	 * @return <code>int</code> value at <code>off</code>
	 */
	public int getInt(int off, ByteOrder order);

	/**
	 * Get a signed <code>int</code> value.
	 * @param off Offset of the desired <code>int</code>
	 * @param order The byte order of the <code>int</code> to return.
	 * @return <code>int</code> value at <code>off</code>
	 */
	public long getLong(int off, ByteOrder order);

	/**
	 * Get the size as a int.
	 * @return the size of the chunk as a {@code long}. This is a {@code long} so a class can implement both {@code ChunkIntSPI} and {@code ChunnkSPI}.
	 */
	public long getSize();

	/** 
	 * Test if this chunk is coalesced
	 * @return <code>true</code> if the chunk coalesced or <code>false</code> otherwise.
	 */
	public boolean isCoalesced();

	/**
	 * Attempt to coalese this chunk
	 * @return The coalesced chunk or <code>null</code> if he chunk itself should be returned.
	 */
	public Chunk coalesce();

	/**
	 * Get a chunk from a subset of the current chunk. 
	 * @param off Offset into parent chunk.
	 * @param len Length of subchunk after offset.
	 * @return The sub chunk, <code>null</code> if the same chunk should be returned or <code>null</code> if the default subchunking should be used.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 *
	 * FIXME: This should not be part of the base SPI. It should be handled by a capability interface.
	 *
	 */
	public Chunk subChunk(int off, int len);

	/**
	 * Copy a subset of the contents of this <code>Chunk</code> to a <code>byte[]</code>.
	 * @param bytes Byte array to copy contents into.
	 * @param chunkOff Offset into the <code>Chunk</code> for the start of bytes to copy (inclusive).
	 * @param arrayOff The off set into <code>bytes</code> to start writing to.
	 * @param len The number of bytes to copy.
	 * @return bytes as a convenience.
	 */
	public byte[] copyTo(byte[] bytes, int chunkOff, int arrayOff, int len);

	@com.google.errorprone.annotations.Immutable
	@Immutable
	static abstract class Abstract implements ChunkIntSPI, ChunkSPI
	{
		@Override
		public abstract int getByte(int off);
		
		@Override
		public final int getByte(long off)
		{
			return getByte(requirePosInt(off,IndexOutOfBoundsException::new));
		}

		@Override
		public abstract short getShort(int off, ByteOrder order);

		@Override
		@SuppressWarnings("PMD.AvoidUsingShortType")
		public final short getShort(long off, ByteOrder order)
		{
			return getShort(requirePosInt(off,IndexOutOfBoundsException::new),order);
		}
		
		@Override
		public abstract int getInt(int off, ByteOrder order);

		@Override
		public final int getInt(long off, ByteOrder order)
		{
			return getInt(requirePosInt(off,IndexOutOfBoundsException::new), order);
		}

		@Override
		public abstract long getLong(int off, ByteOrder order);
		
		@Override
		public final long getLong(long off, ByteOrder order)
		{
			return getLong(requirePosInt(off,IndexOutOfBoundsException::new), order);
		}

		@Override
		public abstract long getSize();
		
		@Override
		public abstract boolean isCoalesced();
		
		@Override
		@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
		public abstract Chunk coalesce();
		
		@Override
		public abstract Chunk subChunk(int off, int len);

		@Override
		@SuppressFBWarnings(value="CRLF_INJECTION_LOGS", justification="This could only happen if the caught index out of bounds exception msg contains a CRLF.")
		public final Chunk subChunk(long off, long len)
		{
			return subChunk(requirePosInt(off,IndexOutOfBoundsException::new), requirePosInt(len,IndexOutOfBoundsException::new));
		}
		
		@Override
		public abstract byte[] copyTo(byte[] bytes, int chunkOff, int arrayOff, int len);

		@Override
		public final byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
		{
			return copyTo(bytes, requirePosInt(chunkOff,IndexOutOfBoundsException::new), arrayOff, len);
		}

                /*--------+
                 | Object |
                 +--------*/

		@Override
		public final boolean equals(Object o)
		{
			return ChunkSPI.defaultEquals(this, o);
		}

		@Override
		public final int hashCode()
		{
			return ChunkSPI.defaultHashCode(this);
		}
	}

	/**
	 * Adapt a <code>ChunkIntSPI</code> to be a <code>ChunkSPI</code>.
	 * @param target The <code>ChunkIntSPI</code> instance to adapt.
	 * @return <code>ChunkSPI</code> that delegates to the wrapped <code>ChunkIntSPI</code>
	 */
	@SuppressWarnings("PMD.ExcessiveMethodLength")	// It's all an anoymous class
	public static ChunkSPI adapt(ChunkIntSPI target)
	{
		//final Logger logger = LoggerFactory.getLogger(ChunkIntSPI.class);
		requireNonNull(target);
		return new ChunkSPI()
		{
			@Override
			public final int getByte(long off)
			{
				return target.getByte(requirePosInt(off,IndexOutOfBoundsException::new));
			}
			
			@Override
			@SuppressWarnings("PMD.AvoidUsingShortType")
			public final short getShort(long off, ByteOrder order)
			{
				return target.getShort(requirePosInt(off,IndexOutOfBoundsException::new),order);
			}
			
			@Override
			public final int getInt(long off, ByteOrder order)
			{
				return target.getInt(requirePosInt(off,IndexOutOfBoundsException::new), order);
			}
			
			@Override
			public final long getLong(long off, ByteOrder order)
			{
				return target.getLong(requirePosInt(off,IndexOutOfBoundsException::new), order);
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
				return target.subChunk(requirePosInt(off,IndexOutOfBoundsException::new), requirePosInt(len,IndexOutOfBoundsException::new));
			}
			
			@Override
			public final byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
			{
				return target.copyTo(bytes, requirePosInt(chunkOff,IndexOutOfBoundsException::new), arrayOff, len);
			}
		};
	}
}
