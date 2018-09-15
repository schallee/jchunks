package net.darkmist.chunks;

import java.nio.ByteOrder;

@SuppressWarnings("PMD.AvoidUsingShortType")
public interface ShortAccessibleSPI extends ChunkSPI
{
	/**
	 * The default byte order which is {@link ByteOrder.BIG_ENDIAN} for java.
	 */
	public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;

	/**
	 * Get the <code>short</code> at the specified offset.
	 * @param byteOffset Offset, in bytes, of the short to get.
	 * @param endian Byte order to read the short as.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	@Override
	public short getShort(long byteOffset, ByteOrder endian);

	/**
	 * Get the <code>short</code> at the specified offset.
	 * @param byteOffset Offset, in bytes, of the short to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	default public short getShort(long byteOffset)
	{
		return getShort(byteOffset, DEFAULT_BYTE_ORDER);
	}

	/**
	 * Get the <code>short</code> at the specified offset.
	 * @param byteOffset Offset, in bytes, of the short to get.
	 * @param endian Byte order to read the short as.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	default public short getShort(int byteOffset, ByteOrder endian)
	{
		return getShort((long)byteOffset, endian);
	}
	
	/**
	 * Get the <code>short</code> at the specified offset.
	 * @param byteOffset Offset, in bytes, of the short to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	default public short getShort(int byteOffset)
	{
		return getShort((long)byteOffset, DEFAULT_BYTE_ORDER);
	}
}
