package net.darkmist.chunks;


/**
 * Abstract class to simplifty implemenation of {@link ChunkSPI}.
 */
public abstract class AbstractChunkSPI implements ChunkSPI
{
	protected final transient long sizeLong;
	protected final transient int sizeInt;
	@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
	protected final transient boolean isSizeInt;

	protected AbstractChunkSPI(long sizeLong)
	{
		if(sizeLong<0)
			throw new NegativeArraySizeException();
		this.sizeLong = sizeLong;
		if(sizeLong <= Integer.MAX_VALUE)
		{
			this.sizeInt = (int)sizeLong;
			isSizeInt = true;
		}
		else
		{
			this.sizeInt = -1;
			isSizeInt = false;
		}
	}

	protected final long requireValidOffset(long off)
	{
		if(off<0||sizeLong<=off)
			throw new IndexOutOfBoundsException();
		return off;
	}

	protected final int requireValidOffset(int off)
	{
		if(off<0)
			throw new IndexOutOfBoundsException();
		if(isSizeInt && sizeInt <= off)
			throw new IndexOutOfBoundsException();
		return off;
	}
	
	protected final int requirePositive(int i)
	{
		if(i<0)
			throw new IndexOutOfBoundsException();
		return i;
	}

	protected final long requirePositive(long l)
	{
		if(l<0)
			throw new IndexOutOfBoundsException();
		return l;
	}

	protected final int validEndForOffAndLen(int off, int len)
	{
		int end = Math.addExact(requireValidOffset(off),requirePositive(len));
		if(sizeInt < end)	// == is valid here
			throw new IndexOutOfBoundsException();
		return end;
	}

	protected final long validEndForOffAndLen(long off, long len)
	{
		long end = Math.addExact(requireValidOffset(off),requirePositive(len));
		if(sizeLong < end)	// == is valid here
			throw new IndexOutOfBoundsException();
		return end;
	}

	/**
	 * @note One of <code>getByte(long)</code> or {@link #getByte(int)} must be overridden.
	 * @return Result of getByte(int) if <code>off</code> is less then or eqaul to {@link Integer.MAX_VALUE}.
	 * @throws IndexOutOfBoundsException if <code>off</code> is greater than <code>Integer.MAX_VALUE</code>.
	 */
	@Override
	public byte getByte(long off)
	{
		// we defer to getByte(int) so off beter be a int
		if(Integer.MAX_VALUE < requireValidOffset(off))
			throw new IndexOutOfBoundsException();
		return getByte((int)off);
	}

	/**
	 * @note One of {@link #getByte(long)} or <code>getByte(int)</code> must be overridden.
	 * @return Result of getByte(long).
	 */
	@Override
	public byte getByte(int off)
	{
		return getByte((long)off);
	}

	/**
	 * One of {@link #size()} or <code>getSizeLong()</code> must be overridden.
	 * @return Result of <code>size()</code>.
	 */
	@Override
	public final long getSizeLong()
	{
		return sizeLong;
	}

	@Override
	public final boolean isSizeLong()
	{
		return true;
	}

	/**
	 * @note One of <code>getSizeInt()</code> or {@link #sizeLong()} must be overridden.
	 * @return Result of {@link #sizeLong()} if it is less than or equal to {@link Integer#MAX_VALUE} or <code>Integer.MAX_VALUE</code> otherwise.
	 */
	@Override
	public final int getSizeInt()
	{
		if(isSizeInt)
			return sizeInt;
		throw new ArithmeticException("Size of chunk is " + sizeLong + " which will not fit in an int.");
	}

	@Override
	public final boolean isSizeInt()
	{
		return isSizeInt;
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isCoalesced()
	{
		return true;
	}

	/**
	 * @return this
	 */
	@Override
	@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
	public Chunk coalesce()
	{
		return null;
	}

	/**
	 * Default implementation defers to {@link #subChunk(int,int)} if arguments fit in ints. Otherwise it returns <code>null</code>.
	 */
	@Override
	public abstract Chunk subChunk(long off, long len);

	protected Chunk subChunkToInt(long off, long len)
	{
		if(off < Integer.MAX_VALUE && len < Integer.MAX_VALUE)
			return subChunk((int)off,(int)len);
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Default implementation calls {@link #subChunk(long,long)}.*
	 */
	@Override
	public Chunk subChunk(int off, int len)
	{
		return subChunk((long)off, (long)len);
	}
}

