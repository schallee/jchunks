package net.darkmist.chunks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

//import net.jcip.annotations.Immutable;
//import net.jcip.annotations.NotThreadSafe;

import org.immutables.value.Value;

@CheckReturnValue
@Immutable
@ParametersAreNonnullByDefault
@Value.Immutable(builder=false)
abstract class IdFlagsChunkOffLen<I,F>
{
	@CheckReturnValue
	@Nonnull
	@Value.Parameter
	abstract I getId();

	@CheckReturnValue
	@Value.Parameter
	abstract F getFlags();

	@CheckReturnValue
	@Nonnull
	@Value.Parameter
	abstract Chunk getChunk();
	// Note: Do not set @Nonnegative as this is for testing and negatives are needed.

	@CheckReturnValue
	@Value.Parameter
	abstract long getChunkOffset();

	// Note: Do not set @Nonnegative as this is for testing and negatives are needed.
	@CheckReturnValue
	@Value.Parameter
	abstract long getChunkLength();

	@CheckReturnValue
	static <I,F> IdFlagsChunkOffLen<I,F> instance(I id, F flags, Chunk chunk, long chunkOff, long chunkLen)
	{
		return ImmutableIdFlagsChunkOffLen.of(id,flags,chunk,chunkOff,chunkLen);
	}

	@CheckReturnValue
	final Object[] toObjectArray()
	{
		return new Object[]{getId(),getFlags(),getChunk(),getChunkOffset(),getChunkLength()};
	}

	@CheckReturnValue
	final Object[] toObjectArrayWithoutFlags()
	{
		return new Object[]{getId(),getChunk(),getChunkOffset(),getChunkLength()};
	}
}
