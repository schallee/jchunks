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

	public static Chunk from(long l, ByteOrder bo)
	{
		return give(Util.bytesFrom(l, bo));
	}

	public static Chunk from(long l)
	{
		return give(Util.bytesFrom(l));
	}

	public static Chunk from(int i, ByteOrder bo)
	{
		return give(Util.bytesFrom(i, bo));
	}

	public static Chunk from(int i)
	{
		return give(Util.bytesFrom(i));
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk from(short s, ByteOrder bo)
	{
		return give(Util.bytesFrom(s, bo));
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk from(short s)
	{
		return give(Util.bytesFrom(s));
	}

	/**
	 * @return A Chunk containing a single byte.
	 */
	public static Chunk of(byte b)
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
	public static Chunk of(int i)
	{
		return ByteChunkSPI.instance(i);
	}

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

	public static Chunk of(Chunk a, Chunk b)
	{
		return PairChunkSPI.instance(a,b);
	}

	public static Chunk of(Chunk...chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}

	public static Chunk of(List<Chunk> chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}
	
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
	 * @throws IndexOutOfBoundsException if <code>off</code> and <code>len<code> would reference bytes not in <code>array</code>.
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
	 */
	public static Chunk copy(ByteBuffer buf)
	{
		return BufferChunkSPI.copyInstance(buf);
	}

	/**
	 * Alias for {@link copy(byte...byteValues)}.
	 */
	public static Chunk of(byte...byteValues)
	{
		return copy(byteValues);
	}

	public static Chunk give(byte...bytes)
	{
		return BufferChunkSPI.giveInstance(bytes);
	}

	public static Chunk give(ByteBuffer buf)
	{
		return BufferChunkSPI.giveInstance(buf);
	}

	public static Chunk give(byte[] array, int off, int len)
	{
		return BufferChunkSPI.giveInstance(array,off,len);
	}
}