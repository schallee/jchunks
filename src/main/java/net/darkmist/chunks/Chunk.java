/*
 *  Copyright (C) 2018 Ed Schaller <schallee@darkmist.net>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.darkmist.chunks;

import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Set;
import java.util.AbstractList;
import java.util.List;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Immutable byte buffer interface class.
 */
// Immutability not liked by errorprone because it extneds AbstractList. Bletch.
@com.google.errorprone.annotations.Immutable
@Immutable
@SuppressWarnings({"PMD.TooManyMethods","PMD.GodClass","Immutable"})
	// It is BIG. It is also the front end to a bunch of encaspulated functionality.
public final class Chunk extends AbstractList<Byte> implements Serializable, Comparable<Chunk>
{	// Only serializable via proxy
	private static final long serialVersionUID = 0L;
	//private static final Logger logger = LoggerFactory.getLogger(Chunk.class);

	@SuppressFBWarnings(value={"SE_TRANSIENT_FIELD_NOT_RESTORED","NFF_NON_FUNCTIONAL_FIELD"}, justification="proxy used for serialization.")
	private transient final ChunkSPI spi;

	/**
	 * Size of the underlying SPI. Profiling of packet capture
	 * software using Chunk revealed a huge number of calls to the
	 * various size methods that all resulted in methods in the
	 * underlying SPIs to be called. As the size, like the chunk
	 * itself, is constant for the life of the Chunk we're going to
	 * cache it here and see if it helps at all.
	 */
	@SuppressFBWarnings(value={"SE_TRANSIENT_FIELD_NOT_RESTORED","NFF_NON_FUNCTIONAL_FIELD"}, justification="proxy used for serialization.")
	private transient final long spiSize;

	/**
	 * Private constructor.
	 * @param spi SPI to use for this chunk.
	 */
	private Chunk(ChunkSPI spi)
	{
		this.spi=requireNonNull(spi,"spi");
		this.spiSize=spi.getSize();
	}

	/**
	 * @param chunk {@code Chunk} to return if not
	 * {@code null}.
	 *
	 * @return {@code chunk} if it is not {@code null}
	 * or {@code this} otherwise.
	 */
	private Chunk selfIfNull(Chunk chunk)
	{
		if(chunk==null)
			return this;
		return chunk;
	}

	/**
	 * Get a {@code Chunk} instance backed by the provided SPI.
	 * @param spi The {@link ChunkSPI} implmenting the new Chunk.
	 * @return A chunk utilizing the provided service provider interface.
	 */
	static Chunk instance(ChunkSPI spi)
	{
		return new Chunk(requireNonNull(spi));
	}

	/**
	 * Get a {@code Chunk} instance backed by the provided SPI.
	 * @param spi The {@link ChunkIntSPI} implmenting the new Chunk.
	 * @return A chunk utilizing the provided service provider interface.
	 */
	static Chunk instance(ChunkIntSPI spi)
	{
		return new Chunk(ChunkIntSPI.adapt(requireNonNull(spi)));
	}

       /*---------+
        | Methods |
        +---------*/

	/**
	 * Get a specified byte from a chunk.
	 * @param off Offset into chunk to get.
	 * @return byte from offset inside chunk.
	 * @see #get(int)
	 * @see #get(long)
	 * @see #getByte(long)
	 */
	public final int getByte(int off)
	{	// pass directly down so we don't needlessly convert off from int to long to int
		return spi.getByte(off);
	}

	/**
	 * Get a specified byte from a chunk.
	 * @param off Offset into chunk to get.
	 * @return byte from offset inside chunk.
	 * @see #get(int)
	 * @see #get(long)
	 * @see #getByte(int)
	 */
	public int getByte(long off)
	{
		return spi.getByte(off);
	}

	/**
	 * Get a specified unsigned byte from a chunk.
	 * @param off Offset into chunk to get.
	 * @return The effective {@code usnigned byte} value at {@code off} as a {@code int}.
	 */
	// FIXME: it would appear that most (all?) spi's return a unsigned value for a byte from getByte(Long). This needs to be cleared up.
	public int getByteUnsigned(long off)
	{
		return spi.getByte(off)&0xff;
	}

	/**
	 * Get a signed {@code short} value.
	 * @param off Offset of the desired {@code sort}
	 * @param order The byte order of the {@code short} to return.
	 * @return {@code short} value at {@code off}
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order)
	{
		return spi.getShort(off, order);
	}

	/**
	 * Get an unsigned {@code short} value.
	 * @param off Offset of the desired {@code sort}
	 * @param order The byte order of the {@code short} to return.
	 * @return  The effective {@code unsigned short} value at {@code off} as a {@code int}.
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public int getShortUnsigned(long off, ByteOrder order)
	{
		return ((int)spi.getShort(off, order))&0xffff;
	}

	/**
	 * Get a signed {@code int} value.
	 * @param off Offset of the desired {@code int}
	 * @param order The byte order of the {@code int} to return.
	 * @return {@code int} value at {@code off}
	 */
	public int getInt(long off, ByteOrder order)
	{
		return spi.getInt(off, order);
	}

	/**
	 * Get an unsigned {@code int} value.
	 * @param off Offset of the desired {@code int}
	 * @param order The byte order of the {@code int} to return.
	 * @return  The effective {@code unsigned int} value at {@code off} as a {@code long}.
	 */
	@SuppressWarnings("UnnecessaryParentheses")
	public long getIntUnsigned(long off, ByteOrder order)
	{
		/*
		int orig;
		long l;

		orig = spi.getInt(off, order);
		l = orig;
		l &= 0xffffffffl;
		return l;
		*/
		return ((long)(spi.getInt(off, order)))&0xffffffffL;
	}

	/**
	 * Get a signed {@code long} value.
	 * @param off Offset of the desired {@code long}
	 * @param order The byte order of the {@code long} to return.
	 * @return {@code long} value at {@code off}
	 */
	public long getLong(long off, ByteOrder order)
	{
		return spi.getLong(off, order);
	}

	/**
	 * Get byte at an offset.
	 *
	 * Most JVMs construct and cache all {@link Byte} instances at
	 * startup so this is not as inefficient as it may seem. If a
	 * {@code byte} is needed this method allows it without
	 * casting to {@code byte} as {@link #getByte(long)} does.
	 *
	 * @param off Offset of the {@code byte}.
	 * @return value of {@code byte} at offset. This will not return {@code null}.
	 */
	public Byte get(long off)
	{
		return (byte)spi.getByte(off);
	}

	/**
	 * Get byte at an {@code int} offset.
	 * 
	 * <b>Note</b> that this will <em>not</em> be able to address
	 * bytes at offsets greater than {@link Integer#MAX_VALUE}. Use
	 * either {@link #get(long)} or {@link #getByte(long)} if this
	 * is an issue. This method is present to fulfill the contract
	 * of {@link List}.
	 *
	 * Also note that the return type is {@code Byte}. As the normal implementation of Byte precreates all {@code Byte} values on start up this is not as big of a performance hit as it might initially seem. {@link #getByte(int)} is an alternative that returns {@code int}.
	 *
	 * @param off Offset of the {@code byte}.
	 * @return value of {@code byte} at offset. This will not return {@code null}.
	 *
	 * @see #get(long)
	 * @see #getByte(long)
	 */
	@Override
	public final Byte get(int off)
	{	// pass directly down so we don't needlessly convert off from int to long to int
		return Byte.valueOf((byte)(spi.getByte(off)));
	}

	/**
	 * Get the size of this {@code Chunk} as a long.
	 * @return The size of the {@code Chunk} as a long.
	 */
	public long getSize()
	{
		return spiSize;
		//return spi.getSize();
	}

	/**
	 * Get the size of this {@code Chunk} as an int.
	 * @return The size of the {@code Chunk} as a int.
	 * @throws IndexOutOfBoundsException if the size of this chunk is larger than can be stored in an {@link Integer#MAX_VALUE integer}.
	 */
	public int getIntSize()
	{
		if(spiSize > Integer.MAX_VALUE)
			throw new IndexOutOfBoundsException("Chunk has size of " + spiSize + " which is larger than the maximum value of an integer " + Integer.MAX_VALUE + '.');
		return (int)spiSize;
		//return (int)(spi.getSize());
	}

	/**
	 * Return the size as an integer.
	 *
	 * <b>Note:</b> This method will return {@link Integer#MAX_VALUE}
	 * if the size of the {@code Chunk} is larger than
	 * {@code Integer.MAX_VALUE}. This method exists to fulfill
	 * the contract of {@link List}. Use  {@link #getSize()}
	 * instead.
	 *
	 * @return Integer size of the chunk. If the size is greater than
	 * can be represented by an integer {@code Integer.MAX_VALUE}
	 * is returned instead.
	 */
	@Override
	public int size()
	{
		if(Util.isInt(spiSize))
			return (int)spiSize;
		return Integer.MAX_VALUE;
	}

	/**
	 * Does this {@code Chunk} contain any bytes?
	 * @return {@code true} if this chunk contains one or more
	 * bytes. {@code false} otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return getSize()==0L;
	}

	/** 
	 * Is the chunk in it's "smallest/most efficient/best".
	 * "smallest/most efficient/best" is subjective and is at
	 * thediscretion of the backing {@link ChunkSPI}. The backing
	 * may return it's self or another @return {@code true}
	 * if the chunk is coalesced or {@code false} otherwise.
	 * @return {@code true} if this {@code Chunk} is in
	 * the best representation. {@code false} otherwise.
	 */
	public boolean isCoalesced()
	{
		return spi.isCoalesced();
	}

	/**
	 * Make an effort to coalesce this chunk into the smallest/most
	 * efficient/best  representation. "smallest/most efficient/best"
	 * is subjective and is at thediscretion of the backing {@link
	 * ChunkSPI}. The backing may return it's self or another
	 * {@code Chunk}. For large {@code Chunk}s this may
	 * require substantial memory allocation and copying.
	 *
	 * @return The coalesced chunk or {@code this}.
	 */
	public Chunk coalesce()
	{
		return selfIfNull(spi.coalesce());
	}

	/**
	 * Get a subchunk.
	 * @param off Offset of sub chunk in parent chunk.
	 * @param len Number of bytes after offset for sub chunk.
	 * @return The sub chunk which may be the same chunk if
	 * 	{@code off==0} and {@code len==getSize()}.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	public Chunk subChunk(long off, long len)
	{
		return selfIfNull(spi.subChunk(off,len));
		/*
		Chunk subChunk;

		if((subChunk=spi.subChunk(off,len))==null)
			return this;
		return subChunk;
		*/
	}

	/**
	 * Get a subchunk starting at an offset.
	 * @param off Offset into parent chunk for sub chunk.
	 * @return The sub chunk which may be the same chunk if
	 * 	{@code off==0}.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	public Chunk subChunk(long off)
	{
		return subChunk(off, Math.subtractExact(getSize(),off));
	}

	/**
	 * Get a {@code Chunk} representing this chunk prefixed by another {@code Chunk}.
	 * @param prefix {@code Chunk} to prefix this {@code Chunk} with.
	 * @return {@code Chunk} representing {@code prefix}
	 * followed by this {@code Chunk}
	 * @see #append(Chunk)
	 * @see Chunks#ofChunks(Chunk,Chunk)
	 */
	public Chunk prepend(Chunk prefix)
	{
		return Chunks.ofChunks(prefix, this);
	}

	/**
	 * Get a {@code Chunk} representing this chunk suffixed by another {@code Chunk}.
	 * @param suffix {@code Chunk} to suffix this {@code Chunk} with.
	 * @return {@code Chunk} representing this {@code Chunk}
	 * followed by {@code suffix}.
	 * @see #prepend(Chunk)
	 * @see Chunks#ofChunks(Chunk,Chunk)
	 */
	public Chunk append(Chunk suffix)
	{
		return Chunks.ofChunks(this, suffix);
	}

	/**
	 * Copy a subset of the contents of this {@code Chunk} to a {@code byte[]}.
	 * @param bytes Byte array to copy contents into.
	 * @param chunkOff Offset into the {@code Chunk} for the start of bytes to copy (inclusive).
	 * @param arrayOff The off set into {@code bytes} to start writing to.
	 * @param len The number of bytes to copy.
	 * @return bytes as a convenience.
	 */
	public final byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		return spi.copyTo(bytes, chunkOff, arrayOff, len);
	}

	/**
	 * Copy the contents of the chunk into a newly allocated byte array of the needed size.
	 * @return byte array of the chunks size containing the contents of the chunk.
	 * @throws IndexOutOfBoundsException if the size of the Chunk
	 *	will not fit in an array (eg: size is more than {@link
	 *	Integer#MAX_VALUE}).
	 */
	public final byte[] copy()
	{
		int size = Util.requirePosInt(spiSize, IndexOutOfBoundsException::new);
		return copyTo(new byte[size], 0, 0, size);
	}

	/**
	 * Copy a subset of the contents of this {@code Chunk} to a {@code byte[]}.
	 * @param off Offset into the {@code Chunk} for the start of bytes to copy (inclusive).
	 * @param len The number of bytes to copy.
	 * @return Byte array containing the desired contents.
	 */
	public final byte[] copy(long off, int len)
	{
		return copyTo(new byte[len], off, 0, len);
	}

       /*---------------+
        | Serialization |
        +---------------*/

	/**
	 * Serialize via proxy.
	 * @return Serialization proxy for this chunk.
	 * @throws ObjectStreamException Doesn't. This is per the serialization specification.
	 */
	@SuppressFBWarnings(value="BED_BOGUS_EXCEPTION_DECLARATION",justification="Serialization API")
	private Object writeReplace() throws ObjectStreamException
	{
		return new ChunkSerializationProxy(this);
	}

       /*---------+
        | trusted |
        +---------*/

	/**
	 * Write this {@code Chunk} to a {@link DataOutput}.
	 * @param dataOut Output to write to.
	 * @param flags Presently not utilized.
	 */
	void writeTo(DataOutput dataOut, Set<WriteFlag> flags) throws IOException
	{
		spi.writeTo(dataOut, flags);
	}

	/**
	 * Write this {@code Chunk} to a {@link DataOutput}.
	 * @param dataOut Output to write to.
	 * @throws IOException if writing to {@code dataOut} does.
	 */
	public void writeTo(DataOutput dataOut) throws IOException
	{
		writeTo(dataOut, Collections.emptySet());
	}

       /*-----------+
        | debugging |
        +-----------*/

	/**
	 * Get the SPI for this {@code Chunk}
	 * @return SPI for this {@code Chunk}.
	 */
	ChunkSPI getSPI()
	{
		return spi;
	}

	/**
	 * Compare this {@code Chunk} to another {@code Chunk}. This is done by comparing each byte of both chunks from the first byte to the last of the smallest {@code Chunk}.
	 * @return If at any point when comparing bytes the bytes
	 * are not the same, the difference is returned. If both
	 * {@code Chunk}s are the same up till the size of
	 * the smallest {@code Chunk} a negative value is
	 * returned. Otherwise zero is returned.
	 */
	@Override
	public int compareTo(Chunk that)
	{
		long thisSize;
		long thatSize;

		if(this==that)
			return 0;
		thisSize = getSize();
		thatSize = that.getSize();
		for(long off=0L;off<thisSize&&off<thatSize;off++)
		{
			int diff = this.getByte(off)-that.getByte(off);
			if(diff!=0L)
				return diff;
		}
		if(thisSize==thatSize)
			return 0;
		if(thisSize<thatSize)
			return -1;
		return 1;
	}
}
