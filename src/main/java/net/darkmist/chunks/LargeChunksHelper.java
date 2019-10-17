package net.darkmist.chunks;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.errorprone.annotations.Var;

import org.immutables.value.Value;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@CheckReturnValue
@Immutable
@ParametersAreNonnullByDefault
@Value.Immutable
abstract class LargeChunksHelper
{
	//private static final Logger logger = LoggerFactory.getLogger(LargeChunksHelper.class);

	// For multi gig chunks, align to this. This MUST be a power of 2.
	static final long LARGE_CHUNK_ALIGNMENT = 64 * 1024;
	// Right shift needed to divide by LARGE_CHUNK_ALIGNMENT
	static final long LARGE_CHUNK_ALIGNMENT_SHIFT = Long.numberOfTrailingZeros(LARGE_CHUNK_ALIGNMENT);
	// Mask to do modulus by LARGE_CHUNK_ALIGNMENT
	static final long LARGE_CHUNK_ALIGNMENT_MASK = ~(Long.MAX_VALUE << LARGE_CHUNK_ALIGNMENT_SHIFT);

	// Size of multi gig chunks. This MUST be a power of 2 >= LARGE_CHUNK_ALIGNMENT and <= Integer.MAX_VALUE.
	// Integer.MAX_VALUE is NOT a power of two.
	static final long LARGE_CHUNK_SIZE = 1024L * 1024L * 1024L;	// 1 GByte
	// Right shift needed to divide by LARGE_CHUNK_SIZE:
	static final long LARGE_CHUNK_SIZE_SHIFT = Long.numberOfTrailingZeros(LARGE_CHUNK_SIZE);
	// Mask to do modulus by LARGE_CHUNK_SIZE:
	static final long LARGE_CHUNK_SIZE_MASK = ~(Long.MAX_VALUE << LARGE_CHUNK_SIZE_SHIFT);

	@Nonnegative
	static LargeChunksHelper instance(long off, long len)
	{
		return ImmutableLargeChunksHelper.of(off,len);
	}

	@Nonnegative
	@Value.Parameter
	abstract long getChunkOff();

	@Nonnegative
	@Value.Parameter
	abstract long getChunkLen();

	// Leading Chunk:

	@Nonnegative
	@Value.Derived
	long getLeadingChunkOff()
	{
		return getChunkOff();
	}

	@Nonnegative
	@Value.Derived
	long getLeadingChunkLen()
	{
		return ((0 - getChunkOff()) & (LARGE_CHUNK_ALIGNMENT - 1));
	}

	@Nonnegative
	@Value.Derived
	boolean hasLeading()
	{
		return getLeadingChunkLen()>0;
	}

	// Large Chunks:

	@Value.Derived
	@Nonnegative
	long getLargeChunksOff()
	{
		return getChunkOff() + getLeadingChunkLen();
	}

	@Value.Derived
	@Nonnegative
	long getLargeChunksCount()
	{
		return (getChunkLen() - getLeadingChunkLen()) >>> LARGE_CHUNK_SIZE_SHIFT;
	}
	
	@Value.Derived
	@Nonnegative
	long getLargeChunksLen()
	{
		return getLargeChunksCount() << LARGE_CHUNK_SIZE_SHIFT;
	}

	// Trailing Chunk:

	@Value.Derived
	@Nonnegative
	long getTrailingChunkOff()
	{
		return getLargeChunksOff() + getLargeChunksLen();
	}

	@Value.Derived
	@Nonnegative
	long getTrailingChunkLen()
	{
		return getChunkLen() - getLargeChunksLen() - getLeadingChunkLen();
	}

	@Value.Derived
	@Nonnegative
	boolean hasTrailing()
	{
		return getTrailingChunkLen()>0;
	}

	// Final number of chunks:

	@Nonnegative
	@Value.Derived
	long getSubChunksCount()
	{
		// This is checked in the checkSubChunksCount method.
		return getLargeChunksCount() + (hasLeading()?1:0) + (hasTrailing()?1:0);
	}

	@Nonnegative
	@Value.Check
	protected void checkSubChunksCount()
	{
		long subChunksCount = getSubChunksCount();

		if(subChunksCount> Integer.MAX_VALUE)
			throw new UnsupportedOperationException("Number of needed subchunks=" + subChunksCount + " exceeds Integer.MAX_VALUE=" + Integer.MAX_VALUE + '!');
	}
	
	// Utils:

	final Chunk readChunks(IOEFunctional.IOEBiFunction<Long,Long,Chunk> readFunc) throws IOException
	{
		@Var
		long off;
		long count;
		List<Chunk> chunks = new ArrayList<>((int)getSubChunksCount());

		if(hasLeading())
			chunks.add(readFunc.apply(getLeadingChunkOff(), getLeadingChunkLen()));

		off	= getLargeChunksOff();
		count	= getLargeChunksCount();
		// Read  in large chunks
		for(long l=0L;l<count;l++)
		{
			chunks.add(readFunc.apply(off, LARGE_CHUNK_SIZE));
			off += LARGE_CHUNK_SIZE;
		}

		if(hasTrailing())
			chunks.add(readFunc.apply(getTrailingChunkOff(), getTrailingChunkLen()));

		return Chunks.ofChunks(chunks); 
	}
}

