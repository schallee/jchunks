package net.darkmist.chunks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiFunction;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.immutables.value.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CheckReturnValue
@Immutable
@ParametersAreNonnullByDefault
@Value.Immutable
abstract class LargeChunksHelper
{
	private static final Logger logger = LoggerFactory.getLogger(LargeChunksHelper.class);

	// For multi gig chunks, align to this. This MUST be a power of 2.
	static final long LARGE_CHUNK_ALIGNMENT = 64 * 1024;
	// Right shift needed to divide by LARGE_CHUNK_ALIGNMENT
	static final long LARGE_CHUNK_ALIGNMENT_SHIFT = Long.numberOfTrailingZeros(LARGE_CHUNK_ALIGNMENT);
	// Mask to do modulus by LARGE_CHUNK_ALIGNMENT
	static final long LARGE_CHUNK_ALIGNMENT_MASK = ~(Long.MAX_VALUE << LARGE_CHUNK_ALIGNMENT_SHIFT);

	// Size of multi gig chunks. This MUST be a power of 2 >= LARGE_CHUNK_ALIGNMENT and <= Integer.MAX_VALUE.
	// Integer.MAX_VALUE is NOT a power of two.
	static final long LARGE_CHUNK_SIZE = 1024l * 1024l * 1024l;	// 1 GByte
	//static final long LARGE_CHUNK_SIZE = 1024l * 1024l * 128l;	// 256 MByte
	// Right shift needed to divide by LARGE_CHUNK_SIZE:
	static final long LARGE_CHUNK_SIZE_SHIFT = Long.numberOfTrailingZeros(LARGE_CHUNK_SIZE);
	// MAxk to do modulus by LARGE_CHUNK_SIZE:
	static final long LARGE_CHUNK_SIZE_MASK = ~(Long.MAX_VALUE << LARGE_CHUNK_SIZE_SHIFT);

	/*
	final long chunkOff;
	final long chunkLen;

	final int subChunksCount;

	final boolean leading;
	static final long leadingChunkOff=0;	// for completeness
	final long leadingChunkLen;

	final long largeChunksOff;
	final long largeChunksCount;
	final long largeChunksLen;

	final boolean trailing;
	final long trailingChunkOff;
	final long trailingChunkLen;
	*/

	// Verify constants.
	static
	{
		verifyConstants();
	}

	@SuppressWarnings({ "PMD.AvoidLiteralsInIfCondition", "unused" })
	private static void verifyConstants()
	{
		// Make absolutely sure that LARGE_CHUNK_ALIGNMENT is a power of 2:
		if(Long.bitCount(LARGE_CHUNK_ALIGNMENT)!=1)
			throw new IllegalStateException("Constant LARGE_CHUNK_ALIGNMENT=" + LARGE_CHUNK_ALIGNMENT + " is not a power of two!");
		// Make absolutely sure that LARGE_CHUNK_SIZE is a power of 2:
		if(Long.bitCount(LARGE_CHUNK_SIZE)!=1)
			throw new IllegalStateException("Constant LARGE_CHUNK_SIZE=" + LARGE_CHUNK_SIZE + " is not a power of two!");
		// Make absolutely sure that LARGE_CHUNK_SIZE is <= Integer.MAX_VALUE:
		if(LARGE_CHUNK_SIZE > Integer.MAX_VALUE)
			throw new IllegalStateException("Constant LARGE_CHUNK_SIZE=" + LARGE_CHUNK_SIZE + " is greater than Integer.MAX_VALUE=" + Integer.MAX_VALUE + '!');
		// Make absolutely sure that LARGE_CHUNK_SIZE is >= LARGE_CHUNK_ALIGNMENT:
		if(LARGE_CHUNK_SIZE < LARGE_CHUNK_ALIGNMENT)
			throw new IllegalStateException("Constant LARGE_CHUNK_ALIGNMENT=" + LARGE_CHUNK_ALIGNMENT + " is greater than LARGE_CHUNK_SIZE=" + LARGE_CHUNK_SIZE + '!');
		if(logger.isDebugEnabled())
		{
			logger.debug("LARGE_CHUNK_ALIGNMENT: dec={} hex={} bin={}", LARGE_CHUNK_ALIGNMENT, Long.toHexString(LARGE_CHUNK_ALIGNMENT), Long.toBinaryString(LARGE_CHUNK_ALIGNMENT));
			logger.debug("LARGE_CHUNK_ALIGNMENT_SHIFT: dec={} hex={} bin={}", LARGE_CHUNK_ALIGNMENT_SHIFT, Long.toHexString(LARGE_CHUNK_ALIGNMENT_SHIFT), Long.toBinaryString(LARGE_CHUNK_ALIGNMENT_SHIFT));
			logger.debug("LARGE_CHUNK_ALIGNMENT_MASK: dec={} hex={} bin={}", LARGE_CHUNK_ALIGNMENT_MASK, Long.toHexString(LARGE_CHUNK_ALIGNMENT_MASK), Long.toBinaryString(LARGE_CHUNK_ALIGNMENT_MASK));
			logger.debug("LARGE_CHUNK_SIZE: dec={} hex={} bin={}", LARGE_CHUNK_SIZE, Long.toHexString(LARGE_CHUNK_SIZE), Long.toBinaryString(LARGE_CHUNK_SIZE));
			logger.debug("LARGE_CHUNK_SIZE_SHIFT: dec={} hex={} bin={}", LARGE_CHUNK_SIZE_SHIFT, Long.toHexString(LARGE_CHUNK_SIZE_SHIFT), Long.toBinaryString(LARGE_CHUNK_SIZE_SHIFT));
			logger.debug("LARGE_CHUNK_SIZE_MASK: dec={} hex={} bin={}", LARGE_CHUNK_SIZE_MASK, Long.toHexString(LARGE_CHUNK_SIZE_MASK), Long.toBinaryString(LARGE_CHUNK_SIZE_MASK));
		}
	}

	@Nonnegative
	public static LargeChunksHelper instance(long off, long len)
	{
		return ImmutableLargeChunksHelper.of(off,len);
	}

	@Nonnegative
	@Value.Parameter
	public abstract long getChunkOff();

	@Nonnegative
	@Value.Parameter
	public abstract long getChunkLen();

	// Leading Chunk:

	@Nonnegative
	@Value.Derived
	public long getLeadingChunkOff()
	{
		long ret;

		ret = getChunkOff();
		if(logger.isDebugEnabled())
			logger.debug("leadingChunkOff: chunkOff={} leadinmgChunkOff={}", getChunkOff(), ret);
		return ret;
	}

	@Nonnegative
	@Value.Derived
	public long getLeadingChunkLen()
	{
		long ret;

		ret = padding(getChunkOff());
		if(logger.isDebugEnabled())
			logger.debug("leadingChunkLen: chunkOff={}, leadingChunkLen={}", getChunkOff(), ret);
		return ret;
	}

	@Nonnegative
	@Value.Derived
	public boolean hasLeading()
	{
		return getLeadingChunkLen()>0;
	}

	// Large Chunks:

	@Value.Derived
	@Nonnegative
	public long getLargeChunksOff()
	{
		long ret;

		ret =  getChunkOff() + getLeadingChunkLen();
		if(logger.isDebugEnabled())
			logger.debug("largeChunksOff: chunkOff={} leadingChunkLen={} largeChunksOff={}", getChunkOff(), getLeadingChunkLen(), ret);
		return ret;
	}

	@Value.Derived
	@Nonnegative
	public long getLargeChunksCount()
	{
		long ret;

		ret = pow2DenominatorDivide(getChunkLen() - getLeadingChunkLen(), LARGE_CHUNK_SIZE,  LARGE_CHUNK_SIZE_SHIFT);
		if(logger.isDebugEnabled())
			logger.debug("largeChunksCount: chunkLen={} leadingChunkLen={} LARGE_CHUNK_SIZE={} largeChunksCount={}", getChunkLen(), getLeadingChunkLen(), LARGE_CHUNK_SIZE, ret);
		return ret;
	}
	
	@Value.Derived
	@Nonnegative
	public long getLargeChunksLen()
	{
		long ret;
		ret = pow2Multiply(getLargeChunksCount(), LARGE_CHUNK_SIZE, LARGE_CHUNK_SIZE_SHIFT);
		//ret = getChunkLen() - getLeadingChunkLen() - getTrailingChunkLen();
		if(logger.isDebugEnabled())
			logger.debug("largeChunksLen: largeChunksCount={} LARGE_CHUNK_SIZE={} largeChunksLen={}", getLargeChunksCount(), LARGE_CHUNK_SIZE, ret);
		return ret;
	}

	// Trailing Chunk:

	@Value.Derived
	@Nonnegative
	public long getTrailingChunkOff()
	{
		long ret;

		ret = getLargeChunksOff() + getLargeChunksLen();
		if(logger.isDebugEnabled())
			logger.debug("trailingChunkOff: largeChunksOff={} largeChunksLen={} trailingChunkOff={}", getLargeChunksOff(), getLargeChunksLen(), ret);
		return ret;
	}

	@Value.Derived
	@Nonnegative
	public long getTrailingChunkLen()
	{
		long ret;
		//ret = pow2DenominatorModulus(getLargeChunksLen(), LARGE_CHUNK_SIZE, LARGE_CHUNK_SIZE_MASK);
		ret = getChunkLen() - getLargeChunksLen() - getLeadingChunkLen();
		if(logger.isDebugEnabled())
			logger.debug("tailingChunkLen: chunkLen={} largeChunksLen={} leadingChunkLen={} trailingChunkLen={}", getChunkLen(), getLargeChunksLen(), getLeadingChunkLen(), ret);
		return ret;
	}

	@Value.Derived
	@Nonnegative
	public boolean hasTrailing()
	{
		return getTrailingChunkLen()>0;
	}

	@Nonnegative
	@Value.Derived
	public long getSubChunksCount()
	{
		// This is checked in the checkSubChunksCount method.
		return getLargeChunksCount() + (hasLeading()?1:0) + (hasTrailing()?1:0);
	}

	// Final number of chunks:

	@Nonnegative
	@Value.Check
	protected void checkSubChunksCount()
	{
		if(getSubChunksCount() > Integer.MAX_VALUE)
			throw new UnsupportedOperationException("Number of needed subchunks=" + getSubChunksCount() + " exceeds Integer.MAX_VALUE=" + Integer.MAX_VALUE + '!');
	}



	/*
	private LargeChunksHelper(long off, long len)
	{
		long l;

		this.chunkOff = off;
		this.chunkLen = len;

		// Calculate any leading chunk:
		leadingChunkLen		= padding(chunkOff);
		leading		= leadingChunkLen>0;

		// Calculate large chunks:
		largeChunksOff		= chunkOff + leadingChunkLen;
		largeChunksLen		= chunkLen - leadingChunkLen;
		largeChunksCount	= pow2DenominatorDivide(largeChunksLen, LARGE_CHUNK_SIZE,  LARGE_CHUNK_SIZE_SHIFT);

		// Calculate any trailing chunks
		trailingChunkLen	= pow2DenominatorModulus(largeChunksLen, LARGE_CHUNK_SIZE, LARGE_CHUNK_SIZE_MASK);
		trailingChunkOff	= largeChunksOff + largeChunksLen;
		trailing		= trailingChunkLen>0;

		// Calculate total number of chunks
		l			= largeChunksCount + (leading?1:0) + (trailing?1:0);
		if(l > Integer.MAX_VALUE)
			throw new UnsupportedOperationException("Number of needed subchunks=" + l + " exceeds Integer.MAX_VALUE=" + Integer.MAX_VALUE + '!');
		subChunksCount=(int)l;
	}
	*/

	/*
	static LargeChunksHelper instance(long off, long len)
	{
		return new LargeChunksHelper(off,len);
	}
	*/
	
	private static final long pow2DenominatorDivide(long numerator, long denominator, long shift)
	{
		long ret;
	
		ret = numerator >>> shift;
		if(logger.isDebugEnabled() && ret!=(numerator / denominator))
			throw new IllegalStateException("Shift based divide result=" + ret + " differes from divide " + (numerator/denominator) + '!');
		return ret;
	}
	
	/*
	private static final long pow2DenominatorModulus(long numerator, long denominator, long mask)
	{
		long ret;
	
		ret = numerator & mask;
		if(logger.isDebugEnabled() && ret!=(numerator % denominator))
			throw new IllegalStateException("Mask based modulus result=" + ret + " differes from divide " + (numerator/denominator) + '!');
		return ret;
	}
	*/

	private static final long pow2Multiply(long notPow2, long pow2, long shift)
	{
		long ret;

		ret = notPow2<<shift;
		if(logger.isDebugEnabled() && ret!=notPow2*pow2)
			throw new IllegalStateException("Shift based multiply result=" + ret + " differs from multiply " + (notPow2*pow2) + '!');
		return ret;
	}

	@SuppressWarnings("PMD.UselessParentheses")
	private static final long padding(long offset)
	{
		long ret;
	
		// https://en.wikipedia.org/wiki/Data_structure_alignment:
		// padding	= (align - (offset & (align - 1))) & (align - 1)
		//		= (-offset & (align - 1))
		ret = (-offset & (LARGE_CHUNK_ALIGNMENT - 1));
		if(logger.isDebugEnabled() && (offset + ret)%LARGE_CHUNK_ALIGNMENT != 0)
			throw new IllegalStateException("Padding for alignment=" + ret + " of offset=" + offset + " results in a unaligned next offset=" + (offset + ret) + '.');
		return ret;
	}
	
	/*
	private static final long alignment(long offset)
	{
		// https://en.wikipedia.org/wiki/Data_structure_alignment:
		// aligned	= (offset + (align - 1)) & ~(align - 1)
		//		= (offset + (align - 1)) & -align
		return (offset + (LARGE_CHUNK_ALIGNMENT - 1)) & -LARGE_CHUNK_ALIGNMENT;
	}
	*/

	// Utils:
	public final Chunk readChunks(BiFunction<Long,Long,Chunk> readFunc) throws IOException
	{
		long off;
		long count;
		List<Chunk> chunks = new ArrayList<>((int)getSubChunksCount());
		Chunk chunk;

		try
		{
			if(hasLeading())
			{
				if(logger.isDebugEnabled())
					logger.debug("hasLeadingChunk: leadingChunkOff={} leadingChunkLen={}", getLeadingChunkOff(), getLeadingChunkLen());
				chunks.add(readFunc.apply(getLeadingChunkOff(), getLeadingChunkLen()));
			}

			off	= getLargeChunksOff();
			count	= getLargeChunksCount();
			// Read  in large chunks
			if(logger.isDebugEnabled())
				logger.debug("largeChunksOff={} largeChunksCount={} largeChunksLen={}", getLargeChunksOff(), getLargeChunksCount(), getLargeChunksLen());
			for(long l=0l;l<count;l++)
			{
				chunks.add(readFunc.apply(off, LARGE_CHUNK_SIZE));
				off += LARGE_CHUNK_SIZE;
			}

			if(hasTrailing())
			{
				if(logger.isDebugEnabled())
					logger.debug("hasTrailingChunk: trailingChunkOff={} trailingChunkLen={}", getTrailingChunkOff(), getTrailingChunkLen());
				chunks.add(readFunc.apply(getTrailingChunkOff(), getTrailingChunkLen()));
			}

			chunk = Chunks.of(chunks); 

			return chunk;
		}
		catch(UncheckedIOException e)
		{
			throw e.getCause();
		}
	}
}

