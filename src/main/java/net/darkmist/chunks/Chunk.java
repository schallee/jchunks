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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.ByteOrder;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

@Immutable
@SuppressWarnings({"PMD.TooManyMethods","PMD.GodClass"})
	// It is BIG. It is also the front end to a bunch of encaspulated functionality.
public final class Chunk extends AbstractNotSerializableList<Byte> implements Serializable
{	// Only serializable via proxy
	private static final long serialVersionUID = 0l;
	// private static final Logger logger = LoggerFactory.getLogger(Chunk.class);

	@SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="proxy used for serialization.")
	private transient final ChunkSPI spi;

	/**
	 * Size of the underlying SPI. Profiling of packet capture
	 * software using Chunk revealed a huge number of calls to the
	 * various size methods that all resulted in methods in the
	 * underlying SPIs to be called. As the size, like the chunk
	 * itself, is constant for the life of the Chunk we're going to
	 * cache it here and see if it helps at all.
	 */
	@SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="proxy used for serialization.")
	private transient final long spiSize;

	private Chunk(ChunkSPI spi)
	{
		this.spi=requireNonNull(spi,"spi");
		this.spiSize=spi.getSize();
	}

	private Chunk selfIfNull(Chunk chunk)
	{
		if(chunk==null)
			return this;
		return chunk;
	}

	/**
	 * @return A chunk utilizing the provided service provider interface.
	 */
	public static Chunk instance(ChunkSPI spi)
	{
		return new Chunk(requireNonNull(spi));
	}

	/**
	 * @return A chunk utilizing the provided service provider interface.
	 */
	public static Chunk instance(ChunkIntSPI spi)
	{
		return new Chunk(ChunkIntSPI.adapt(requireNonNull(spi)));
	}

        /***********/
        /* Methods */
        /***********/

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public byte getByte(long off)
	{
		return spi.getByte(off);
	}

	public int getByteUnsigned(long off)
	{
		return ((int)(spi.getByte(off)))&0xff;
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order)
	{
		return spi.getShort(off, order);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public final short getShort(long off)
	{
		return spi.getShort(off, ByteOrder.BIG_ENDIAN);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public int getShortUnsigned(long off, ByteOrder order)
	{
		return ((int)spi.getShort(off, order))&0xffff;
	}

	public int getInt(long off, ByteOrder order)
	{
		return spi.getInt(off, order);
	}

	public long getIntUnsigned(long off, ByteOrder order)
	{
		return ((long)spi.getInt(off, order))&0xffffffff;
	}

	public long getLong(long off, ByteOrder order)
	{
		return spi.getInt(off, order);
	}

	// List<Byte>ish
	public Byte get(long off)
	{
		return spi.getByte(off);
	}

	@Override // List<Byte>
	public final Byte get(int off)
	{
		return get((long)off);
	}

	/**
	 * Get the size as a long.
	 * @return the size of the chunk as a long.
	 */
	public long getSize()
	{
		return spiSize;
		//return spi.getSize();
	}

	/**
	 * Return the size as an integer. This differs from {@link
	 * #getSizeInt} in that if the size is larger than can be
	 * represented in an integer {@link Integer.MAX_VALUE} is returned
	 * instead of throwing an exception.
	 * @return Integer size of the chunk. If the size is greater than
	 * can be represented by an integer <code>Integer.MAX_VALUE</code>
	 * is returned instead.
	 */
	@Override	// List<Byte>
	public int size()
	{
		// if(Util.isInt(getSize()))
			// return (int)getSize();
		// return Integer.MAX_VALUE;
		if(Util.isInt(spiSize))
			return (int)spiSize;
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isEmpty()
	{
		return getSize()==0l;
	}

	/** 
	 * @return <code>true</code> if the chunk is coalesced or <code>false</code> otherwise.
	 */
	public boolean isCoalesced()
	{
		return spi.isCoalesced();
	}

	/**
	 * @return The coalesced chunk which may be the same chunk.
	 */
	public Chunk coalesce()
	{
		return selfIfNull(spi.coalesce());
	}

	/**
	 * @return The sub chunk which may be the same chunk if
	 * 	<code>off==0</code> and <code>len==getSize()</code>.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	public Chunk subChunk(long off, long len)
	{
		Chunk ret;

		if(off==0 && len==getSize())
			return this;
		if((ret=spi.subChunk(off,len))!=null)
			return ret;
		return SubChunkSPI.instance(this, off, len);
	}

	/**
	 * @return The sub chunk which may be the same chunk if
	 * 	<code>off==0</code>.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	public Chunk subChunk(long off)
	{
		return subChunk(off, Math.subtractExact(getSize(),off));
	}

	public Chunk prepend(Chunk prefix)
	{
		return PairChunkSPI.instance(prefix, this);
	}

	public Chunk append(Chunk suffix)
	{
		return PairChunkSPI.instance(this, suffix);
	}

	public final byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		return spi.copyTo(bytes, chunkOff, arrayOff, len);
	}

	/**
	 * Copy the contents of the chunk into a newly allocated byte array of the needed size.
	 * @return byte array of the chunks size containing the contents of the chunk.
	 * @throws IndexOutOfBoundsException if the size of the Chunk
	 *	will not fit in an array (eg: size is more than {@link
	 *	Integer.MAX_VALUE}).
	 */
	public final byte[] copy()
	{
		int size = Util.requirePosIntOff(spiSize);
		//int size = Util.requirePosIntOff(getSize());
		return copyTo(new byte[size], 0, 0, size);
	}

	public final byte[] copy(long off, int len)
	{
		return copyTo(new byte[len], off, 0, len);
	}

        /*****************/
        /* Serialization */
        /*****************/

	/**
	 * Serialize via proxy.
	 */
	private Object writeReplace() throws ObjectStreamException
	{
		return new ChunkSerializationProxy(this);
	}

        /*************/
        /* debugging */
        /*************/

	ChunkSPI getSPI()
	{
		return spi;
	}
}
