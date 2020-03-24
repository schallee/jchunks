package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.Objects;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressFBWarnings(value="FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY",justification=/*FIXME:*/"Is there a decent way to break these cycles?")
@SuppressWarnings({"PMD.BeanMembersShouldSerialize","PMD.AvoidDuplicateLiterals"})
// PMD thinks this is a bean and doesn't like not having accessors.
// FIXME: we should NOT extend an abstract here as we want to be as thin
// a layer as possible before the subchunks
final class PairChunkSPI extends AbstractChunkSPI
{
	private static final Class<PairChunkSPI> CLASS = PairChunkSPI.class;
	@SuppressWarnings({"UnusedVariable","PMD.UnusedPrivateField", "unused"})
	private static final Logger logger = LoggerFactory.getLogger(CLASS);
	private final Chunk first;
	private final Chunk second;
	private final long secondOffset;

	private PairChunkSPI(Chunk first, Chunk second, long secondOffset, long size)
	{	// checks performed in factory
		super(size);
		this.first=first;
		this.second=second;
		this.secondOffset = secondOffset;
	}

	static Chunk instance(Chunk first, Chunk second)
	{
		long secondOffset;
		long size;

		if(first == null || first.isEmpty())
			if(second == null || second.isEmpty())
				return Chunks.empty();
			else
				return second;
		else if(second == null || second.isEmpty())
			return first;
		// OK, now we know both are non-null
		secondOffset = first.getSize();
		size = Math.addExact(secondOffset, second.getSize());
		return Chunk.instance(new PairChunkSPI(first,second, secondOffset, size));
	}

	@Override
	public int getByte(long off)
	{
		if(requireValidOffset(off) < secondOffset)
			return first.getByte(off);
		return second.getByte(off - secondOffset);
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification="validity checks")
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order)
	{
		/*
		if(logger.isDebugEnabled())
		{
			logger.debug("getShort(off={}, order={}):", off, order);
			if(first.getSize()<=32)
				logger.debug("\tfirst={}", first);
			else
				logger.debug("f\tirst=[{},{},{},{}]", first.getByte(0), first.getByte(1), first.getByte(2), first.getByte(3));
			if(second.getSize()<=32)
				logger.debug("\tsecond={}", second);
			else
				logger.debug("\tsecond=[{},{},{},{}]", second.getByte(0), second.getByte(1), second.getByte(2), second.getByte(3));
			logger.debug("\tsize={} secondOffset={}", size, secondOffset);
		}
		try
		{*/
			long aOff = requireValidOffset(off);
			long bOff = requireValidOffset(off+Short.BYTES-1);

			if(bOff < secondOffset)
			{	// all in first chunk
				return first.getShort(off, order);
			}
			if(secondOffset <= aOff)
			{	// all in second chunk
				return second.getShort(off-secondOffset, order);
			}
			// split
			return Util.shortFromBytes(first.getByte(aOff), second.getByte(0), order);
		/*}
		catch(IndexOutOfBoundsException e)
		{
			logger.debug("index out of bounds:", e);
			throw e;
		}*/
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification="validity checks")
	public int getInt(long off, ByteOrder order)
	{
		requireValidOffset(off+Integer.BYTES-1);
		if(requireValidOffset(off) < secondOffset)
		{
			if(first.getSize() - off < Integer.BYTES)
				return super.getInt(off, order);
			else
				return first.getInt(off, order);
		}
		return second.getInt(off - secondOffset, order);
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification="validity checks")
	public long getLong(long off, ByteOrder order)
	{
		long firstOff = requireValidOffset(off);
		long lastOff = requireValidOffset(off+Long.BYTES-1);

		if(lastOff < secondOffset)
			return first.getLong(off, order);
		if(secondOffset <= firstOff)
			return second.getLong(off - secondOffset, order);
		return super.getLong(off, order);
	}

	@Nullable
	@Override
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	public Chunk subChunk(long off, long len)
	{
		long end;
		long newFirstOff;
		long newFirstLen;
		long newSecondOff;
		long newSecondLen;
		Chunk newFirst;
		Chunk newSecond;

		if(off==0 && len==size)
			return null;	// self
		end = Util.requireValidOffLenRetEnd(size, off, len);
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.ofByte(getByte(off));

		if(off < secondOffset)
		{
			if(end < secondOffset)
				return first.subChunk(off, len);
			else
			{
				newFirstOff = off;
				newFirstLen = first.getSize() - off;
				newFirst = first.subChunk(newFirstOff, newFirstLen);

				newSecondOff = 0;
				newSecondLen = len - newFirstLen;
				newSecond = second.subChunk(newSecondOff, newSecondLen);

				return instance(newFirst, newSecond);
			}
		}
		else
			return second.subChunk(off - secondOffset, len);
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification="validity checks")
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{	// FIXME: we can do this better....
		long chunkEndOff = Math.addExact(chunkOff, len);
		Objects.requireNonNull(bytes);
		/*
		if(logger.isDebugEnabled())
		{
			logger.error("copyTo(bytes.length={}, chunkOff={}, arrayOff={}, len={}): size={}", bytes.length, chunkOff, arrayOff, len, size);
			logger.error("copyTo(...): first.getSize()={} secondOffset={} second.getSize()={}", first.getSize(), secondOffset, second.getSize());
		}
		*/

		Util.requireValidOffLen(bytes, arrayOff, len);
		Util.requireValidOffLen(size,chunkOff,len);
		if(chunkEndOff < secondOffset)
			return first.copyTo(bytes, chunkOff, arrayOff, len);
		if(secondOffset <= chunkOff)
			return second.copyTo(bytes, chunkOff-secondOffset, arrayOff, len);
		return super.copyTo(bytes,chunkOff,arrayOff,len);
	}

	@Override
	public boolean isCoalesced()
	{
		// If we're longer than our LARGE_CHUNK_SIZE we'll claim to be coalesced. In theory wec ould make it all the way to Integer.MAX_VALUE.
		return size>LargeChunksHelper.LARGE_CHUNK_SIZE;
	}

	@Nullable
	@Override
	public Chunk coalesce()
	{
		byte[] bytes;

		if(size>LargeChunksHelper.LARGE_CHUNK_SIZE)
			return null;	// this
		bytes = new byte[(int)size];
		first.copyTo(bytes, 0L, 0, (int)first.getSize());
		second.copyTo(bytes, 0L, (int)secondOffset, (int)second.getSize());
		return Chunks.giveBytes(bytes);
	}

        /*--------+
         | Object |
         +--------*/

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " of " + first + " and " + second;
	}
}
