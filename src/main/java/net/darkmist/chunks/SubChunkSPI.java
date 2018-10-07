package net.darkmist.chunks;

import java.nio.ByteOrder;
import static java.util.Objects.requireNonNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.meta.When;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
// PMD thinks this is a bean and doesn't like not having accessors.
final class SubChunkSPI extends AbstractChunkSPI
{
	//private static final Logger logger = LoggerFactory.getLogger(SubChunkSPI.class);
	private final Chunk chunk;
	private final long subChunkOff;

	private SubChunkSPI(Chunk subChunk, long subChunkOff, long subChunkLen)
	{	// checks performed in factory
		super(subChunkLen);
		this.chunk = subChunk;
		this.subChunkOff = subChunkOff;
		//logger.info("Who's creating these?", new Exception().fillInStackTrace());
	}

	static Chunk instance(Chunk subChunk, long subChunkOff, long subChunkLen)
	{
		long subChunkSize = requireNonNull(subChunk).getSize();

		Util.requireValidOffLen(subChunkSize, subChunkOff, subChunkLen);

		if(subChunkLen==0)
			return Chunks.empty();
		if(subChunkLen==1)
			return Chunks.of(subChunk.getByte(subChunkOff));
		if(subChunkOff==0 && subChunkSize==subChunkLen)
			return subChunk;
		return Chunk.instance(new SubChunkSPI(subChunk, subChunkOff, subChunkLen));
	}

	@CheckReturnValue(when=When.NEVER)
	@Override
	public int getByte(long off)
	{
		return chunk.getByte(
			Math.addExact(
				requireValidOffset(off),
				subChunkOff)
			);
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",justification="verification of values")
	public short getShort(long off, ByteOrder bo)
	{
		long offFirst = requireValidOffset(off);
		requireValidOffset(off+Short.BYTES-1);

		return chunk.getShort(
			Math.addExact(
				offFirst,
				subChunkOff
			),
			bo
		);
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",justification="verification of values")
	public int getInt(long off, ByteOrder bo)
	{
		long offFirst = requireValidOffset(off);
		requireValidOffset(off+Integer.BYTES-1);

		return chunk.getInt(
			Math.addExact(
				offFirst,
				subChunkOff
			),
			bo
		);
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",justification="verification of values")
	public long getLong(long off, ByteOrder bo)
	{
		long offFirst = requireValidOffset(off);
		requireValidOffset(off+Long.BYTES-1);

		return chunk.getLong(
			Math.addExact(
				offFirst,
				subChunkOff
			),
			bo
		);
	}

	@Override
	public boolean isCoalesced()
	{
		return false;
	}

	@Override
	public Chunk subChunk(long off, long len)
	{	// Build a new subChunk based on the original wrapped chunk instead of cascading another subChunk.
		Util.requireValidOffLen(size, off, len);
		return instance(
			chunk,
			Math.addExact(subChunkOff,off),
			len
		);
	}

	@Override
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		Util.requireValidOffLen(size, chunkOff, len);
		return chunk.copyTo(
			bytes,
			Math.addExact(subChunkOff, chunkOff),
			arrayOff, 
			len);
	}
}
