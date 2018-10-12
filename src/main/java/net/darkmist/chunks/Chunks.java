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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Static methods for creating instances of {@link Chunk}.
 */
// Utility class
@SuppressWarnings({"PMD.TooManyMethods","PMD.GodClass"})
	// It is BIG. It is also the front end to a bunch of encaspulated functionality.
public final class Chunks
{
	@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
	private static final Chunk EMPTY = EmptyChunkSPI.EMPTY.getChunk();

	private Chunks()
	{
	}

        /*************/
        /* Factories */
        /*************/

	/**
	 * Get a <code>Chunk</code> with no bytes in it.
	 * @return a Chunk containing no bytes.
	 */
	public static Chunk empty()
	{
		return EMPTY;
	}

	/**
	 * Get chunk representing a string's bytes encoded in the provided
	 * 	character set.
	 * @param str The string to get a chunk for. If this is
	 * 	<code>null</code>, it is treated as an empty string.
	 * @param charset The character set to use to convert the string
	 * 	to bytes. If <code>str</code> is <code>null</code> this is
	 *	ignored. If <code>str</code> is not <code>null</code>
	 *	and this is <code>null</code> then ISO-8859-1, which is 8bit clean, will be used.
	 * @return a Chunk containing the bytes from <code>str</code>
	 *	using the provided charset.
	 */
	@SuppressWarnings("PMD.AvoidReassigningParameters")
	public static Chunk from(String str, Charset charset)
	{
		if(str==null)
			return EMPTY;
		if(charset==null)
			charset = StandardCharsets.ISO_8859_1;
		return give(str.getBytes(charset));
	}

	/**
	 * Get chunk representing a string's bytes encoded in the
	 *	ISO-8859-1 character set. ISO-8859-1 is 8 bit clean.
	 * @param str The string to get a chunk for. If this is
	 * 	<code>null</code>, it is treated as an empty string.
	 * @return a Chunk containing the bytes from <code>str</code>
	 *	using encoded in ISO-8859-1.
	 */
	public static Chunk from(String str)
	{
		return from(str, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Get a <code>Chunk</code> containing a long value in a specified
	 * byte order.
	 * @param l The long value to return.
	 * @param bo The byte ordering to use.
	 * @return <code>Chunk</code> the eight bytes containing
	 * <code>l</code> in byte order <code>bo</code>.
	 */
	public static Chunk from(long l, ByteOrder bo)
	{
		return give(Util.bytesFrom(l, bo));
	}

	/**
	 * Get a <code>Chunk</code> containing a long value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param l The long value to return.
	 * @return <code>Chunk</code> the eight bytes containing
	 * <code>l</code> in big endian byte order.
	 */
	public static Chunk from(long l)
	{
		return give(Util.bytesFrom(l));
	}

	/**
	 * Get a <code>Chunk</code> containing a int value in a specified
	 * byte order.
	 * @param i The int value to return.
	 * @param bo The byte ordering to use.
	 * @return <code>Chunk</code> the four bytes containing
	 * <code>i</code> in byte order <code>bo</code>.
	 */
	public static Chunk from(int i, ByteOrder bo)
	{
		return give(Util.bytesFrom(i, bo));
	}

	/**
	 * Get a <code>Chunk</code> containing a int value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param i The int value to return.
	 * @return <code>Chunk</code> the four bytes containing
	 * <code>i</code> in big endian byte order.
	 */
	public static Chunk from(int i)
	{
		return give(Util.bytesFrom(i));
	}

	/**
	 * Get a <code>Chunk</code> containing a short value in a specified
	 * byte order.
	 * @param s The short value to return.
	 * @param bo The byte ordering to use.
	 * @return <code>Chunk</code> the two bytes containing
	 * <code>s</code> in byte order <code>bo</code>.
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk from(short s, ByteOrder bo)
	{
		return give(Util.bytesFrom(s, bo));
	}

	/**
	 * Get a <code>Chunk</code> containing a short value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param s The short value to return.
	 * @return <code>Chunk</code> the four bytes containing
	 * <code>s</code> in big endian byte order.
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk from(short s)
	{
		return give(Util.bytesFrom(s));
	}

	/**
	 * Get a chunk representing a single <code>byte</code>.
	 * @param b Single byte for <code>Chunk</code>.
	 * @return A Chunk containing a single byte.
	 */
	public static Chunk of(byte b)
	{
		return ByteChunkSPI.instance(b);
	}

	/**
	 * @param i Integer to convert to the single byte for the returned
	 *	chunk. The conversion is done if <code>{@link Byte#MIN_VALUE}
	 *	&lt;= i &lt;=0xff</code> by anding it with <code>0xff</code>.
	 * @return A Chunk containing a single byte.
	 * @throws IllegalArgumentException If <code>i</code> is not
	 *	between <code>Byte.MIN_VALUE</code> and <code>0xff</code>
	 * 	inclusive.
	 */
	public static Chunk of(int i)
	{
		return ByteChunkSPI.instance(i);
	}

	/**
	 * Get a chunk representing the byte value provided.
	 * @param byteValues <code>int</code>s containing byte values.
	 * @return <code>Chunk</code> containing the byte values.
	 * @throws IllegalArgumentException if any byte value is not
	 * between {@link Byte#MIN_VALUE} and <code>0xff</code> inclusive.
	 */
	public static Chunk of(int...byteValues)
	{
		byte[] bytes;
		
		if(byteValues==null || byteValues.length==0)
			return empty();
		if(byteValues.length==1)
			return of(byteValues[0]);
		bytes = new byte[byteValues.length];
		for(int i=0;i<byteValues.length;i++)
			bytes[i] = Util.requireExtendedByteValue(byteValues[i]);
		return give(bytes);
	}

	/**
	 * Get a chunk composed of two other chunks.
	 * @param a The first chunk
	 * @param b The second chunk
	 * @return a <code>Chunk</code>representing <code>a</code> followed by <code>b</code>
	 * @see Chunk#append(Chunk)
	 * @see Chunk#prepend(Chunk)
	 * @see #of(Chunk[])
	 * @see #of(List)
	 */
	public static Chunk of(Chunk a, Chunk b)
	{
		return PairChunkSPI.instance(a,b);
	}

	/**
	 * Get a chunk composed of multiple other chunks
	 * @param chunks the <code>Chunk</code>s to represent as one <code>Chunk</code>.
	 * @return a <code>Chunk</code>representing <code>chunks</code> in order.
	 * @see #of(List)
	 */
	public static Chunk of(Chunk...chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}

	/**
	 * Get a chunk composed of multiple other chunks
	 * @param chunks the <code>Chunk</code>s to represent as one <code>Chunk</code>.
	 * @return a <code>Chunk</code>representing <code>chunks</code> in order.
	 * @see #of(Chunk[])
	 */
	public static Chunk of(List<Chunk> chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}
	
	/**
	 * Get a chunk composed of the provided bytes.
	 * @param byteValues The values for the <code>Chunk</code>. A
	 * copy of the provided array is made internally.
	 * @return A <code>Chunk</code> representing the <code>byteValues</code>
	 * @see #give(byte[])
	 * @see #copy(byte[],int,int)
	 */
	public static Chunk copy(byte...byteValues)
	{
		return BufferChunkSPI.copyInstance(byteValues);
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
	 * @throws IndexOutOfBoundsException if <code>off</code> and <code>len</code> would reference bytes not in <code>array</code>.
	 * @see #copy(byte[])
	 * @see #give(byte[],int,int)
	 */
	public static Chunk copy(byte[] array, int off, int len)
	{
		return BufferChunkSPI.copyInstance(array, off, len);
	}

	/**
	 * Copy the given buffer and return it as a chunk.
	 * @param buf The buffer to copy. This may be <code>null</code>.
	 * @return A copy of <code>buf</code> as a chunk. If
	 *	<code>buf</code> is <code>null</code>, an empty chunk
	 * 	is returned.
	 * @see #copy(byte[])
	 * @see #copy(byte[],int,int)
	 */
	public static Chunk copy(ByteBuffer buf)
	{
		return BufferChunkSPI.copyInstance(buf);
	}

	/**
	 * Alias for {@link #copy(byte[])}.
	 * @param byteValues Array of bytes to create the chunk from.
	 * @return Chunk containing a copy of byteValues.
	 */
	public static Chunk of(byte...byteValues)
	{
		return copy(byteValues);
	}

	/**
	 * Get a <code>Chunk</code> backed by the provided <code>byte</code> array.
	 * @param bytes The <code>byte</code> array to use. <b><code>bytes</code> should not be changed after this call!</b>
	 * @return <code>Chunk</code> backed by <code>bytes</code>
	 */ 
	public static Chunk give(byte...bytes)
	{
		return BufferChunkSPI.giveInstance(bytes);
	}

	/**
	 * Get a <code>Chunk</code> backed by the provided <code>ByteBuffer</code>.
	 * @param buf The <code>ByteBuffer</code> to use. <b><code>buf</code> should not be changed after this call!</b>
	 * @return <code>Chunk</code> backed by <code>bytes</code>
	 */ 
	public static Chunk give(ByteBuffer buf)
	{
		return BufferChunkSPI.giveInstance(buf);
	}

	/**
	 * Get a <code>Chunk</code> backed by the provided <code>byte</code> array of a sub array.
	 * @param array The <code>byte</code> array to use. <b><code>bytes</code> should not be changed after this call!</b>
	 * @param off Offset into <code>bytes</code> inclusive.
	 * @param len Number of bytes
	 * @return <code>Chunk</code> backed by <code>bytes</code>
	 */ 
	public static Chunk give(byte[] array, int off, int len)
	{
		return BufferChunkSPI.giveInstance(array,off,len);
	}
}
