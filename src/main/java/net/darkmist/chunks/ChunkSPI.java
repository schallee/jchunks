package net.darkmist.chunks;

import java.io.IOException;
import java.io.DataOutput;
import java.nio.ByteOrder;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.errorprone.annotations.Var;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// FUTURE: support sizes, lengths and offsets as java.lang.Number and handle sizes larger than Long.MAX_VALUE.
@com.google.errorprone.annotations.Immutable
@Immutable
public interface ChunkSPI
{
	/**
	 * Get the byte at the specified offset.
	 * <strong>Note:</strong> The default implementation delegates to {@link getByte(long)}. Override if there is no need to convert from an {@code int} to a {@code long} and then back to an {@code int}.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	public default int getByte(int off)
	{	
		return getByte((long)off);
	}

	/**
	 * Get the byte at the specified offset.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	public int getByte(long off);

	/**
	 * Get a signed <code>short</code> value.
	 * @param off Offset of the desired <code>sort</code>
	 * @param order The byte order of the <code>short</code> to return.
	 * @return <code>short</code> value at <code>off</code>
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order);

	/**
	 * Get a signed <code>int</code> value.
	 * @param off Offset of the desired <code>int</code>
	 * @param order The byte order of the <code>int</code> to return.
	 * @return <code>int</code> value at <code>off</code>
	 */
	public int getInt(long off, ByteOrder order);

	/**
	 * Get a signed <code>int</code> value.
	 * @param off Offset of the desired <code>int</code>
	 * @param order The byte order of the <code>int</code> to return.
	 * @return <code>int</code> value at <code>off</code>
	 */
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

	/**
	 * Copy a subset of the contents of this <code>Chunk</code> to a <code>byte[]</code>.
	 * @param bytes Byte array to copy contents into.
	 * @param chunkOff Offset into the <code>Chunk</code> for the start of bytes to copy (inclusive).
	 * @param arrayOff The off set into <code>bytes</code> to start writing to.
	 * @param len The number of bytes to copy.
	 * @return bytes as a convenience.
	 */
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len);

	/** 
	 * Query whether this chunk is coalesced or not.
	 * @return <code>true</code> if the chunk coalesced or <code>false</code> otherwise.
	 */
	public boolean isCoalesced();

	/**
	 * Attempt to coalesce the chunk
	 * @return The coalesced chunk or <code>null</code> if he chunk itself should be returned.
	 */
	public Chunk coalesce();

	/**
	 * Write this <code>Chunk</code> to a {@link DataOutput}.
	 * @param dataOut Output to write to.
	 * @param flags Presently not utilized.
	 * @see Chunk#writeTo(DataOutput)
	 * @throws IOException if writing to <code>dataOut</code> does.
	 */
	public default void writeTo(DataOutput dataOut, Set<WriteFlag> flags) throws IOException
	{
		//final Logger logger = LoggerFactory.getLogger(ChunkSPI.class);
		long size = getSize();
		byte[] buf;
		@Var
		long fullBuffersEnd;
		int extra;
		int bufSize = Tunables.getTmpBufSize();

		//logger.debug("writeTo: size={} bufSize={}", size, bufSize);
		if(size <= bufSize)
		{
			buf = new byte[(int)size];
			copyTo(buf, 0L, 0, (int)size);
			dataOut.write(buf);
			return;
		}

		extra = (int)(size % bufSize);
		//logger.debug("\textra={}", extra);

		fullBuffersEnd = size;
		if(extra != 0)
		{
			//logger.debug("have extra");
			fullBuffersEnd = size-extra;
		}

		buf=Tunables.getTmpBuf();
		for(long off=0L;off<fullBuffersEnd;off+=buf.length)
		{
			copyTo(buf, off, 0, buf.length);
			dataOut.write(buf);
		}

		if(extra != 0)
		{
			//logger.debug("have extra");
			copyTo(buf, fullBuffersEnd, 0, extra);
			dataOut.write(buf, 0, extra);
		}
	}

	/**
	 * {@link Object#equals(Object)} that can be used to implement equals for {@code ChunkSPI} implementations.
	 * @param a chunk spi to compare to {@code o}
	 * @param o object to compare {@code a} to
	 * @return {@code true} if {@code a} and {@code o} are equal. {@code false} otherwise.
	 */
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
		// PMD.CompareObjectsWithEquals: Default implementation of equals for this interface.
	/* IFACEPROTECTED */public static boolean defaultEquals(ChunkSPI a, Object o)
	{
		long size;
		ChunkSPI b;

		if(a==o)
			return true;
		if(a==null||o==null)
			return false;

		if(!(o instanceof ChunkSPI))
			return false;
		b = (ChunkSPI)o;

		size = a.getSize();
		if(size != b.getSize())
			return false;
		for(long l=0L;l<size;l++)
			if(a.getByte(l)!=b.getByte(l))
				return false;
		return true;
	}

	/**
	 * {@link Object#hashCode()} that can be used to implement equals for {@code ChunkSPI} implementations.
	 * @param spi what to get the hashcode of
	 * @return hashcode for {@code spi}
	 */
	/* IFACEPROTECTED */ public static int defaultHashCode(ChunkSPI spi)
	{
		long size;
		@Var
		int hash = 0;

		if(spi==null)
			return 0;
		size = spi.getSize();
		for(long l=0L;l<size;l++)
			hash = hash*31 + spi.getByte(l)&0xff;
		return hash;
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
