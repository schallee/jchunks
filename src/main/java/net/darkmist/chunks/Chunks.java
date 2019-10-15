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
 *
 * <b>Discussion</b>
 * <ul>
 * 	<li>Should {@link #fromInt(int)} and related (eg: {@link #fromShort(short)}) require a byte order? Presently the are big endian as most of java (eg: {@link java.io.DataOutput}) is. Alternatively should they be replaced with {@code fromIntBig(int i)} and {@code fromIntLittle(int i)} or the like?</li>
 * 	<li>Should {@link #fromShort(short)} and {@link #fromShort(short, ByteOrder)} accept an {@code int} instead of a {@code short} and then validate that the {@code int} is actually a {@code short}? Should both be provided?</li>
 * </ul>
 */
// Utility class
@SuppressWarnings({"PMD.TooManyMethods","PMD.GodClass","PMD.AvoidDuplicateLiterals"})
	// It is BIG. It is also the front end to a bunch of encaspulated functionality.
	// PMD.AvoidDuplicateLiterals is literally complaining about the string "PMD.AvoidUsingShortType".
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
	 * Get a {@code Chunk} with no bytes in it.
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
	 * 	{@code null}, it is treated as an empty string.
	 * @param charset The character set to use to convert the string
	 * 	to bytes. If {@code str} is {@code null} this is
	 *	ignored. If {@code str} is not {@code null}
	 *	and this is {@code null} then a
	 * 	{@link NullPointerException} will be throw.
	 * @return a Chunk containing the bytes from {@code str}
	 *	using the provided charset.
	 * @throws NullPointerException if {@code str} is not {@code null}
	 *  	but {@code charset} is {@code null}.
	 * @see #fromISOLatin1(String)
	 * @see #fromUTF8(String)
	 */
	@SuppressWarnings("PMD.AvoidReassigningParameters")
	public static Chunk fromString(/* @Nullable */ String str, Charset charset)
	{
		if(str==null || str.isEmpty())
			return EMPTY;
		if(charset==null)
			throw new NullPointerException("Charset cannot be null when str is not null.");
		return giveBytes(str.getBytes(charset));
	}

	/**
	 * Get chunk representing a string's bytes encoded in the provided
	 * 	character set.
	 * @param str The string to get a chunk for. If this is
	 * 	{@code null}, it is treated as an empty string.
	 * @param charset The character set to use to convert the string
	 * 	to bytes. If {@code str} is {@code null} this is
	 *	ignored. If {@code str} is not {@code null}
	 *	and this is {@code null} then a
	 *	{@link NullPointerException} will be throw.
	 * @return a Chunk containing the bytes from {@code str}
	 *	using the provided charset.
	 * @throws NullPointerException if {@code str} is not {@code null}
	 * 	but {@code charset} is {@code null}.
	 * @see #fromISOLatin1(String)
	 * @see #fromString(String,Charset)
	 * @see #fromUTF8(String)
	 * @deprecated in favor of {@link #fromString(String, Charset)}
	 * 	for type clarity.
	 */
	@Deprecated
	@SuppressWarnings("PMD.AvoidReassigningParameters")
	public static Chunk from(String str, Charset charset)
	{
		return fromString(str, charset);
	}

	/**
	 * Get chunk representing a string's bytes encoded in the
	 *	ISO-8859-1 character set. ISO-8859-1 is 8 bit clean.
	 * @param str The string to get a chunk for. If this is
	 * 	{@code null}, it is treated as an empty string.
	 * @return a Chunk containing the bytes from {@code str}
	 *	using encoded in ISO-8859-1.
	 * @see #fromISOLatin1(String)
	 * @see #fromString(String, Charset)
	 * @see #fromUTF8(String)
	 * @deprecated To clarify both the type of argument
	 * and the encoding. The direct replacement is
	 * {@link #fromISOLatin1(String)}.
	 */
	@Deprecated
	public static Chunk from(String str)
	{
		return fromISOLatin1(str);
	}

	/**
	 * Get a {@code Chunk} containing a long value in a specified
	 * byte order.
	 * @param l The long value to return.
	 * @param bo The byte ordering to use.
	 * @return {@code Chunk} the eight bytes containing
	 * {@code l} in byte order {@code bo}.
	 * @see #fromLong(long)
	 * @see #fromLong(long,ByteOrder)
	 * @deprecated in favor of {@link #fromLong(long,ByteOrder)}
	 * for type clarity.
	 */
	@Deprecated
	public static Chunk from(long l, ByteOrder bo)
	{
		return fromLong(l, bo);
	}

	/**
	 * Get a {@code Chunk} containing a long value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param l The long value to return.
	 * @return {@code Chunk} the eight bytes containing
	 * {@code l} in big endian byte order.
	 * @see #fromLong(long)
	 * @see #fromLong(long,ByteOrder)
	 * @deprecated In favor of {@link #fromLong(long)}.
	 */
	@Deprecated
	public static Chunk from(long l)
	{
		return fromLong(l);
	}

	/**
	 * Get a {@code Chunk} containing a int value in a specified
	 * byte order.
	 * @param i The int value to return.
	 * @param bo The byte ordering to use.
	 * @return {@code Chunk} the four bytes containing
	 * {@code i} in byte order {@code bo}.
	 * @see #fromInt(int)
	 * @see #fromInt(int, ByteOrder)
	 * @deprecated in favor of {@link #fromInt(int, ByteOrder)}.
	 */
	@Deprecated
	public static Chunk from(int i, ByteOrder bo)
	{
		return fromInt(i, bo);
	}

	/**
	 * Get a {@code Chunk} containing a int value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param i The int value to return.
	 * @return {@code Chunk} the four bytes containing
	 * {@code i} in big endian byte order.
	 *
	 * @deprecated in favor of {@link #fromInt(int)} for type clarity.
	 */
	@Deprecated
	public static Chunk from(int i)
	{
		return fromInt(i);
	}

	/**
	 * Get a {@code Chunk} containing a short value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param s The short value to return.
	 * @return {@code Chunk} the four bytes containing
	 * {@code s} in big endian byte order.
	 *
	 * @deprecated in favor of {@link #fromShort(short)} for type clarity.
	 */
	@Deprecated
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk from(short s)
	{
		return fromShort(s);
	}

	/**
	 * Get a {@code Chunk} containing a short value in a specified
	 * byte order.
	 * @param s The short value to return.
	 * @param bo The byte ordering to use.
	 * @return {@code Chunk} the two bytes containing
	 * {@code s} in byte order {@code bo}.
	 *
	 * @deprecated in favor of {@link #fromShort(short, ByteOrder)}. 
	 */
	@Deprecated
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk from(short s, ByteOrder bo)
	{
		return giveBytes(Util.bytesFrom(s, bo));
	}

	/**
	 * Get chunk representing a string's bytes encoded in the
	 *	UTF-8
	 * @param str The string to get a chunk for. If this is
	 * 	{@code null}, it is treated as an empty string.
	 * @return a Chunk containing the bytes from {@code str}
	 *	using encoded in UTF-8.
	 * @see #fromISOLatin1(String)
	 * @see #fromString(String, Charset)
	 */
	public static Chunk fromUTF8(String str)
	{
		return fromString(str, StandardCharsets.UTF_8);
	}

	/**
	 * Get chunk representing a string's bytes encoded in the
	 *	ISO-8859-1 (ISO-LATIN-1).
	 * @param str The string to get a chunk for. If this is
	 * 	{@code null}, it is treated as an empty string.
	 * @return a Chunk containing the bytes from {@code str}
	 *	using encoded in ISO-8859-1.
	 * @see #fromString(String, Charset)
	 * @see #fromUTF8(String)
	 */
	public static Chunk fromISOLatin1(String str)
	{
		return fromString(str, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Get a {@code Chunk} containing a long value in a specified
	 * byte order.
	 * @param l The long value to return.
	 * @param bo The byte ordering to use.
	 * @return {@code Chunk} the eight bytes containing
	 * {@code l} in byte order {@code bo}.
	 * @see #fromLong(long)
	 */
	public static Chunk fromLong(long l, ByteOrder bo)
	{
		return giveBytes(Util.bytesFrom(l, bo));
	}

	/**
	 * Get a {@code Chunk} containing a long value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param l The long value to return.
	 * @return {@code Chunk} the eight bytes containing
	 * {@code l} in big endian byte order.
	 * @see #fromLong(long,ByteOrder)
	 */
	public static Chunk fromLong(long l)
	{
		return giveBytes(Util.bytesFrom(l));
	}

	/**
	 * Get a {@code Chunk} containing a int value in a specified
	 * byte order.
	 * @param i The int value to return.
	 * @param bo The byte ordering to use.
	 * @return {@code Chunk} the four bytes containing
	 * {@code i} in byte order {@code bo}.
	 * @see #fromInt(int)
	 */
	public static Chunk fromInt(int i, ByteOrder bo)
	{
		return giveBytes(Util.bytesFrom(i, bo));
	}

	/**
	 * Get a {@code Chunk} containing a int value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param i The int value to return.
	 * @return {@code Chunk} the four bytes containing
	 * {@code i} in big endian byte order.
	 */
	public static Chunk fromInt(int i)
	{
		return giveBytes(Util.bytesFrom(i));
	}

	/**
	 * Get a {@code Chunk} containing a short value in a specified
	 * byte order.
	 * @param s The short value to return.
	 * @param bo The byte ordering to use.
	 * @return {@code Chunk} the two bytes containing
	 * {@code s} in byte order {@code bo}.
	 *
	 * FIXME: should this use int and then check it?
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk fromShort(short s, ByteOrder bo)
	{
		return giveBytes(Util.bytesFrom(s, bo));
	}

	/**
	 * Get a {@code Chunk} containing a short value in big
	 * endian (aka: network, java, or {@link ByteOrder#BIG_ENDIAN})
	 * byte order.
	 * @param s The short value to return.
	 * @return {@code Chunk} the four bytes containing
	 * {@code s} in big endian byte order.
	 *
	 * FIXME: should this use int and then check it?
	 */
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static Chunk fromShort(short s)
	{
		return giveBytes(Util.bytesFrom(s));
	}

	/**
	 * Get a chunk representing a single {@code byte}.
	 * @param b Single byte for {@code Chunk}.
	 * @return A Chunk containing a single byte.
	 */
	public static Chunk ofByte(byte b)
	{
		return ByteChunkSPI.instance(b);
	}

	/**
	 * Get a chunk representing a single {@code byte}.
	 * @param i Integer to convert to the single byte for the returned
	 *	chunk. The conversion is done if {@code {@link Byte#MIN_VALUE}
	 *	<= i <=0xff} by anding it with {@code 0xff}.
	 * @return A Chunk containing a single byte.
	 * @throws IllegalArgumentException If {@code i} is not
	 *	between {@code Byte.MIN_VALUE} and {@code 0xff}
	 * 	inclusive.
	 */
	public static Chunk ofByte(int i)
	{
		return ByteChunkSPI.instance(i);
	}

	/**
	 * Get a chunk representing a single {@code byte}.
	 * @param b Single byte for {@code Chunk}.
	 * @return A Chunk containing a single byte.
	 * @deprecated Because parameter types are not clear from usage requiring documentation check. replaced by {@link #ofByte(byte)}
	 */
	@Deprecated
	public static Chunk of(byte b)
	{
		return ofByte(b);
	}

	/**
	 * Get a chunk composed of two other chunks.
	 * @param a The first chunk
	 * @param b The second chunk
	 * @return a {@code Chunk}representing {@code a} followed by {@code b}
	 * @see Chunk#append(Chunk)
	 * @see Chunk#prepend(Chunk)
	 * @see #ofChunks(Chunk[])
	 * @see #ofChunks(List)
	 *
	 * @deprecated in favor of {@link #ofChunks(Chunk,Chunk)} for type clarity.
	 */
	@Deprecated
	@SuppressWarnings("InconsistentOverloads")
		// errorprone wants b,a because of of(byte b) above
	public static Chunk of(Chunk a, Chunk b)
	{
		return ofChunks(a,b);
	}

	/**
	 * Get a chunk composed of multiple other chunks
	 * @param chunks the {@code Chunk}s to represent as one {@code Chunk}.
	 * @return a {@code Chunk}representing {@code chunks} in order.
	 * @see #ofChunks(List)
	 *
	 * @deprecated in favor of {@link #ofChunks(Chunk[])} for type clarity.
	 */
	@Deprecated
	public static Chunk of(Chunk...chunks)
	{
		return ofChunks(chunks);
	}

	/**
	 * Get a chunk composed of multiple other chunks
	 * @param chunks the {@code Chunk}s to represent as one {@code Chunk}.
	 * @return a {@code Chunk}representing {@code chunks} in order.
	 * @see #of(Chunk[])
	 *
	 * @deprecated in favor of {@link #ofChunks(List)}.
	 */
	@Deprecated
	public static Chunk of(List<Chunk> chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}

	/**
	 * Get a chunk representing the byte value provided.
	 * @param byteValues {@code int}s containing byte values.
	 * @return {@code Chunk} containing the byte values.
	 * @throws IllegalArgumentException if any byte value is not
	 * between {@link Byte#MIN_VALUE} and {@code 0xff} inclusive.
	 * @deprecated Because parameter types are not clear from usage requiring documentation check. replaced by {@link #ofByte(int)}
	 */
	@Deprecated
	public static Chunk of(int...byteValues)
	{
		return ofBytes(byteValues);
	}

	/**
	 * Get a chunk representing a single {@code byte}.
	 * @param i Integer to convert to the single byte for the returned
	 *	chunk. The conversion is done if {@code {@link Byte#MIN_VALUE}
	 *	&lt;= i &lt;=0xff} by anding it with {@code 0xff}.
	 * @return A Chunk containing a single byte.
	 * @throws IllegalArgumentException If {@code i} is not
	 *	between {@code Byte.MIN_VALUE} and {@code 0xff}
	 * 	inclusive.
	 * @deprecated Because parameter types are not clear from usage requiring documentation check. replaced by {@link #ofByte(int)}
	 */
	@SuppressWarnings("EscapedEntity")
	@Deprecated
	public static Chunk of(int i)
	{
		return ofByte(i);
	}

	/**
	 * Alias for {@link #copy(byte[])}.
	 * @param byteValues Array of bytes to create the chunk from.
	 * @return Chunk containing a copy of byteValues.
	 * @deprecated Because parameter types are not clear from usage requiring documentation check. replaced by {@link #ofBytes(byte[])}
	 */
	@Deprecated
	public static Chunk of(byte...byteValues)
	{
		return ofBytes(byteValues);
	}
	
	/**
	 * Get a chunk representing the byte value provided.
	 * @param byteValues {@code int}s containing byte values.
	 * @return {@code Chunk} containing the byte values.
	 * @throws IllegalArgumentException if any byte value is not
	 * between {@link Byte#MIN_VALUE} and {@code 0xff} inclusive.
	 */
	public static Chunk ofBytes(int...byteValues)
	{
		byte[] bytes;
		
		if(byteValues==null || byteValues.length==0)
			return empty();
		if(byteValues.length==1)
			return ofByte(byteValues[0]);
		bytes = new byte[byteValues.length];
		for(int i=0;i<byteValues.length;i++)
			bytes[i] = Util.requireExtendedByteValue(byteValues[i]);
		return giveBytes(bytes);
	}

	/**
	 * Alias for {@link #copy(byte[])}.
	 * @param byteValues Array of bytes to create the chunk from.
	 * @return Chunk containing a copy of byteValues.
	 */
	public static Chunk ofBytes(byte...byteValues)
	{
		return copyBytes(byteValues);
	}

	/**
	 * Get a chunk composed of two other chunks.
	 * @param a The first chunk
	 * @param b The second chunk
	 * @return a {@code Chunk}representing {@code a} followed by {@code b}
	 * @see Chunk#append(Chunk)
	 * @see Chunk#prepend(Chunk)
	 * @see #ofChunks(Chunk[])
	 * @see #ofChunks(List)
	 */
	public static Chunk ofChunks(Chunk a, Chunk b)
	{
		return PairChunkSPI.instance(a,b);
	}

	/**
	 * Get a chunk composed of multiple other chunks
	 * @param chunks the {@code Chunk}s to represent as one {@code Chunk}.
	 * @return a {@code Chunk}representing {@code chunks} in order.
	 * @see #ofChunks(List)
	 */
	public static Chunk ofChunks(Chunk...chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}

	/**
	 * Get a chunk composed of multiple other chunks
	 * @param chunks the {@code Chunk}s to represent as one {@code Chunk}.
	 * @return a {@code Chunk}representing {@code chunks} in order.
	 * @see #ofChunks(Chunk[])
	 */
	public static Chunk ofChunks(List<Chunk> chunks)
	{
		return MultiChunkSPI.instance(chunks);
	}
	
	/**
	 * Get a chunk composed of the provided bytes.
	 * @param byteValues The values for the {@code Chunk}. A
	 * copy of the provided array is made internally.
	 * @return A {@code Chunk} representing the {@code byteValues}
	 * @see #giveBytes(byte[])
	 * @see #copyBytes(byte[],int,int)
	 */
	public static Chunk copyBytes(byte...byteValues)
	{
		return BufferChunkSPI.copyInstance(byteValues);
	}

	/**
	 * Copy bytes from an array and return them as a chunk.
	 * @param array The array to copy from. This may be {@code null} if and only if {@code off==0} and {@code len==0}.
	 * @param off Offset of the bytes into {@code array}.
	 * @param len Number of bytes after offset to copy.
	 * @return Chunk containing a copy of the specified bytes
	 *	from the {@code array}. If {@code array==null},
	 *	{@code off==0} and {@code len==0} then an
	 * 	empty chunk is returned.
	 * @throws NullPointerException if {@code array} is null and either {@code off} or {@code len} is not zero.
	 * @throws IndexOutOfBoundsException if {@code off} and {@code len} would reference bytes not in {@code array}.
	 * @see #copyBytes(byte[])
	 * @see #giveBytes(byte[],int,int)
	 */
	public static Chunk copyBytes(byte[] array, int off, int len)
	{
		return BufferChunkSPI.copyInstance(array, off, len);
	}

	/**
	 * Get a chunk composed of the provided bytes.
	 * @param byteValues The values for the {@code Chunk}. A
	 * copy of the provided array is made internally.
	 * @return A {@code Chunk} representing the {@code byteValues}
	 * @see #giveBytes(byte[])
	 * @see #copyBytes(byte[],int,int)
	 * @deprecated Because parameter types are not clear from usage requiring documentation check. replaced by {@link #copyBytes(byte[])}
	 */
	@Deprecated
	public static Chunk copy(byte...byteValues)
	{
		return copyBytes(byteValues);
	}

	/**
	 * Copy the given buffer and return it as a chunk.
	 * @param buf The buffer to copy. This may be {@code null}.
	 * @return A copy of {@code buf} as a chunk. If
	 *	{@code buf} is {@code null}, an empty chunk
	 * 	is returned.
	 * @see #copy(byte[])
	 * @see #copy(byte[],int,int)
	 * 
	 * @deprecated in favor of {@link #copyBuffer(ByteBuffer)} for type clarity.
	 */
	@Deprecated
	public static Chunk copy(ByteBuffer buf)
	{
		return copyBuffer(buf);
	}

	/**
	 * Copy bytes from an array and return them as a chunk.
	 * @param array The array to copy from. This may be {@code null} if and only if {@code off==0} and {@code len==0}.
	 * @param off Offset of the bytes into {@code array}.
	 * @param len Number of bytes after offset to copy.
	 * @return Chunk containing a copy of the specified bytes
	 *	from the {@code array}. If {@code array==null},
	 *	{@code off==0} and {@code len==0} then an
	 * 	empty chunk is returned.
	 * @throws NullPointerException if {@code array} is null and either {@code off} or {@code len} is not zero.
	 * @throws IndexOutOfBoundsException if {@code off} and {@code len} would reference bytes not in {@code array}.
	 * @see #copyBytes(byte[])
	 * @see #giveBytes(byte[],int,int)
	 *
	 * @deprecated in favor of {@link #copyBytes(byte[], int, int)} for type clarity.
	 */
	@Deprecated
	public static Chunk copy(byte[] array, int off, int len)
	{
		return copyBytes(array, off, len);
	}

	/**
	 * Copy the given buffer and return it as a chunk.
	 * @param buf The buffer to copy. This may be {@code null}.
	 * @return A copy of {@code buf} as a chunk. If
	 *	{@code buf} is {@code null}, an empty chunk
	 * 	is returned.
	 * @see #copy(byte[])
	 * @see #copy(byte[],int,int)
	 */
	public static Chunk copyBuffer(ByteBuffer buf)
	{
		return BufferChunkSPI.copyInstance(buf);
	}

	/**
	 * Get a {@code Chunk} backed by the provided {@code byte} array.
	 * @param bytes The {@code byte} array to use. <b>{@code bytes} should not be changed after this call!</b>
	 * @return {@code Chunk} backed by {@code bytes}
	 */ 
	public static Chunk giveBytes(byte...bytes)
	{
		return BufferChunkSPI.giveInstance(bytes);
	}

	/**
	 * Get a {@code Chunk} backed by the provided {@code byte} array of a sub array.
	 * @param array The {@code byte} array to use. <b>{@code bytes} should not be changed after this call!</b>
	 * @param off Offset into {@code bytes} inclusive.
	 * @param len Number of bytes
	 * @return {@code Chunk} backed by {@code bytes}
	 */ 
	public static Chunk giveBytes(byte[] array, int off, int len)
	{
		return BufferChunkSPI.giveInstance(array,off,len);
	}

	/**
	 * Get a {@code Chunk} backed by the provided {@code byte} array.
	 * @param bytes The {@code byte} array to use. <b>{@code bytes} should not be changed after this call!</b>
	 * @return {@code Chunk} backed by {@code bytes}
	 * @deprecated Because parameter types are not clear from usage requiring documentation check. replaced by {@link #giveBytes(byte[])}
	 */ 
	@Deprecated
	public static Chunk give(byte...bytes)
	{
		return giveBytes(bytes);
	}

	/**
	 * Get a {@code Chunk} backed by the provided {@code ByteBuffer}.
	 * @param buf The {@code ByteBuffer} to use. <b>{@code buf} should not be changed after this call!</b>
	 * @return {@code Chunk} backed by {@code bytes}
	 *
	 * @deprecated in favor of {@link #giveBuffer(ByteBuffer)} for type clarity.
	 */ 
	@Deprecated
	public static Chunk give(ByteBuffer buf)
	{
		return giveBuffer(buf);
	}

	/**
	 * Get a {@code Chunk} backed by the provided {@code byte} array of a sub array.
	 * @param array The {@code byte} array to use. <b>{@code bytes} should not be changed after this call!</b>
	 * @param off Offset into {@code bytes} inclusive.
	 * @param len Number of bytes
	 * @return {@code Chunk} backed by {@code bytes}
	 *
	 * @deprecated in favor of {@link #giveBytes(byte[], int, int)}.
	 */ 
	@Deprecated
	public static Chunk give(byte[] array, int off, int len)
	{
		return giveBytes(array, off, len);
	}

	/**
	 * Get a {@code Chunk} backed by the provided {@code ByteBuffer}.
	 * @param buf The {@code ByteBuffer} to use. <b>{@code buf} should not be changed after this call!</b>
	 * @return {@code Chunk} backed by {@code bytes}
	 */ 
	public static Chunk giveBuffer(ByteBuffer buf)
	{
		return BufferChunkSPI.giveInstance(buf);
	}
}
