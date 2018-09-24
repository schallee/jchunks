package net.darkmist.chunks;

import java.nio.ByteOrder;
import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize","PMD.AvoidDuplicateLiterals"})
// PMD thinks this is a bean and doesn't like not having accessors.
// FIXME: we should NOT extend an abstract here as we want to be as thin
// a layer as possible before the subchunks
final class PairChunkSPI extends AbstractChunkSPI
{
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
		requireValidOffset(off);
		if(requireValidOffset(off+Short.BYTES-1) < secondOffset)
		{
			if(first.getSize() - off < Short.BYTES)
				return super.getShort(off, order);
			else
				return first.getShort(off, order);
		}
		return second.getShort(off - secondOffset, order);
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
		requireValidOffset(off);
		requireValidOffset(off+Long.BYTES-1);
		if(off < secondOffset)
		{
			if(first.getSize() - off < Long.BYTES)
				return super.getLong(off, order);
			else
				return first.getLong(off, order);
		}
		return second.getLong(off - secondOffset, order);
	}

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
			return Chunks.of(getByte(off));

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
	{	// FIXME: wecan do this better....
		long chunkEndOff = Math.addExact(chunkOff, len);
		requireNonNull(bytes);
		requireValidOffset(chunkOff);
		requireValidOffset(chunkEndOff);

		Util.requireValidOffLen(bytes, arrayOff, len);
		if(chunkEndOff < secondOffset)
			return first.copyTo(bytes, chunkOff, arrayOff, len);
		if(secondOffset <= chunkOff)
			return second.copyTo(bytes, chunkOff-secondOffset, arrayOff, len);
		return super.copyTo(bytes,chunkOff,arrayOff,len);
	}

	@Override
	public boolean isCoalesced()
	{
		return size<=Integer.MAX_VALUE;
	}

	@Override
	public Chunk coalesce()
	{
		byte[] bytes;

		if(size>Integer.MAX_VALUE)
			return null;	// this
		bytes = new byte[(int)size];
		first.copyTo(bytes, 0l, 0, (int)first.getSize());
		second.copyTo(bytes, secondOffset, (int)secondOffset, (int)second.getSize());
		return Chunks.give(bytes);
	}
}
