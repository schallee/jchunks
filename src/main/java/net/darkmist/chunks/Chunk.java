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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Chunk extends AbstractNotSerializableList<Byte>
{
	private static final long serialVersionUID = 0l;
	private transient final ChunkSPI spi;
	static final Chunk EMPTY = EmptyChunkSPI.EMPTY.getChunk();

	private Chunk(ChunkSPI spi)
	{
		this.spi=requireNonNull(spi,"spi");
	}

	private Chunk selfIfNull(Chunk chunk)
	{
		if(chunk==null)
			return this;
		return chunk;
	}

        /***********/
        /* Methods */
        /***********/

	public byte getByte(long off)
	{
		return spi.getByte(off);
	}

	public byte getByte(int off)
	{
		return spi.getByte(off);
	}

	
	public Byte get(long off)
	{
		return getByte(off);
	}

	@Override // List<Byte>
	public Byte get(int off)
	{
		return getByte(off);
	}

	/**
	 * Get the size as a long.
	 * @return the size of the chunk as a long.
	 */
	public long getSizeLong()
	{
		return spi.getSizeLong();
	}

	/**
	 * @return <code>ture</code> if the size can be represented as an <code>long</code>.
	 */
	public boolean isSizeLong()
	{
		return spi.isSizeLong();
	}

	/**
	 * Get the size as a integer.
	 * @return the size of the chunk as an integer.
	 * @throws ArithmeticException if the size of the chunk cannot be represented as an <code>int</code>.
	 */
	public int getSizeInt()
	{
		return spi.getSizeInt();
	}

	/**
	 * @return <code>ture</code> if the size can be represented as an <code>int</code>.
	 */
	public boolean isSizeInt()
	{
		return spi.isSizeInt();
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
	@Override
	public int size()
	{
		if(isSizeInt())
			return getSizeInt();
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isEmpty()
	{
		return getSizeLong()==0;
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
	 * 	<code>off==0</code> and <code>len==getSizeLong()</code>.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	public Chunk subChunk(long off, long len)
	{
		Chunk ret;
		if(off==0 && len==getSizeLong())
			return this;
		if((ret=spi.subChunk(off,len))==null)
			return ret;
		return SubChunkSPI.instance(this, off, len);
	}

	/**
	 * @return The sub chunk which may be the same chunk if
	 * 	<code>off==0</code> and <code>len==getSizeInt()</code>.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	public Chunk subChunk(int off, int len)
	{
		Chunk ret;
		if(off==0 && len==getSizeLong())
			return this;
		if((ret=spi.subChunk(off,len))==null)
			return ret;
		return SubChunkSPI.instance(this, off, len);
	}

	public Chunk prepend(Chunk prefix)
	{
		return PairChunkSPI.instance(prefix, this);
	}

	public Chunk append(Chunk suffix)
	{
		return PairChunkSPI.instance(this, suffix);
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
        /* Factories */
        /*************/

	/**
	 * @return a Chunk containing no bytes.
	 */
	public static Chunk emptyInstance()
	{
		return EMPTY;
	}

	/**
	 * @return A chunk utilizing the provided service provider interface.
	 */
	public static Chunk instance(ChunkSPI spi)
	{
		return new Chunk(requireNonNull(spi));
	}

	/**
	 * @return A Chunk containing a single byte.
	 */
	public static Chunk byteInstance(byte b)
	{
		return ByteChunkSPI.instance(b);
	}

	/**
	 * @param i Integer to convert to the single byte for the returned
	 *	chunk. The conversion is done if <code>{@link Byte.MIN_VALUE}
	 *	&lt;= i &lt;=0xff</code> by anding it with <code>0xff</code>.
	 * @return A Chunk containing a single byte.
	 * @throws IllegalArgumentException If <code>i</code> is not
	 *	between <code>Byte.MIN_VALUE</code> and <code>0xff</code>
	 * 	inclusive.
	 */
	public static Chunk byteInstance(int i)
	{
		return ByteChunkSPI.instance(i);
	}

	/**
	 * Get chunk representing a string as UTF-8 bytes.
	 * @param str The string to get a chunk for. If this is
	 * 	<code>null</code>, it is treated as an empty string.
	 * @return A Chunk containing the UTF-8 encoded bytes from
	 *	<code>str</code> or an empty Chunk if <code>str</code>
	 * 	is <code>null</code>.
	 */
	public static Chunk instance(String str)
	{
		return instance(str, StandardCharsets.UTF_8);
	}

	/**
	 * Get chunk representing a string's bytes encoded in the provided
	 * 	character set.
	 * @param str The string to get a chunk for. If this is
	 * 	<code>null</code>, it is treated as an empty string.
	 * @param charset The character set to use to convert the string
	 * 	to bytes. If <code>str</code> is <code>null</code> this is
	 *	ignored. If <code>str</code> is not <code>null</code>
	 *	and this is <code>null</code> then a {@link
	 * 	NullPointerException} is thrown.
	 * @return a Chunk containing the bytes from <code>str</code>
	 * 	using the provided charset.
	 * @throws NullPointerException if <code>str</code> is non-null
	 * 	and <code>charset</code> is <code>null</code>.
	 */
	public static Chunk instance(String str, Charset charset)
	{
		if(str==null)
			return EMPTY;
		return giveInstance(str.getBytes(requireNonNull(charset, "charset")));
	}

	/**
	 * Copy the given buffer and return it as a chunk.
	 * @param buf The buffer to copy. This may be <code>null</code>.
	 * @return A copy of <code>buf</code> as a chunk. If
	 *	<code>buf</code> is <code>null</code>, an empty chunk
	 * 	is returned.
	 */
	public static Chunk copyInstance(ByteBuffer buf)
	{
		return BufferChunkSPI.copyInstance(buf);
	}

	/**
	 * Copy bytes from an array and return them as a chunk.
	 * @param array The array to copy from. This may be <code>null</code> if and only if <code>off==0</code> and <code>len==0</code>.
	 * @param off Offset of the bytes into <code>array</code>.
	 * @param len Number of bytes after offset to copy.
	 * @return Chunk containing a copy of the specified bytes
	 *	from the <code>array</code>. If <code>array==null</code>,
	 *	<code>off==0</code> and <code>len==0</code> then an
	 * 	empty chunk is returned.
	 * @throws NullPointerException if <code>array</code> is null and either <code>off</code> or <code>len</code> is not zero.
	 * @throws IndexOutOfBoundsException if <code>off</code> and <code>len<code> would reference bytes not in <code>array</code>.
	 */
	public static Chunk copyInstance(byte[] array, int off, int len)
	{
		return BufferChunkSPI.copyInstance(array, off, len);
	}

	public static Chunk copyInstance(byte[] array)
	{
		return BufferChunkSPI.copyInstance(array);
	}

	public static Chunk giveInstance(ByteBuffer buf)
	{
		return BufferChunkSPI.giveInstance(buf);
	}

	public static Chunk giveInstance(byte[] array, int off, int len)
	{
		return BufferChunkSPI.giveInstance(array,off,len);
	}

	public static Chunk giveInstance(byte[] array)
	{
		return BufferChunkSPI.giveInstance(array);
	}

	public static Chunk multiInstance(Chunk a, Chunk b)
	{
		return PairChunkSPI.instance(a,b);
	}

	public static Chunk multiInstance(Chunk...chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}

	public static Chunk multiInstance(List<Chunk> chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}
}
