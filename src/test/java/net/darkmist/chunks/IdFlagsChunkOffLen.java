package net.darkmist.chunks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

//import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import org.immutables.value.Value;

@CheckReturnValue
@Immutable
@ParametersAreNonnullByDefault
@Value.Immutable
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

	@NotThreadSafe
	static abstract class Builder<I,F>
	{
		abstract Builder<I,F> from(IdFlagsChunkOffLen<I,F> base);

		final Builder<I,F> from(IdFlagsChunkOffLenArrayOffLen<I,F> base)
		{
			return this
				.id(base.getId())
				.flags(base.getFlags())
				.chunk(base.getChunk())
				.chunkOffset(base.getChunkOffset())
				.chunkLength(base.getChunkLength());
		}

		@Nonnull
		abstract Builder<I,F> id(I id);
		abstract Builder<I,F> flags(F id);

		@Nonnull
		abstract Builder<I,F> chunk(Chunk chunk);

		abstract Builder<I,F> chunkOffset(long chunkOffset);
		@Value.Default
		final long chunkOffset()
		{
			return 0L;
		}

		abstract Builder<I,F> chunkLength(long chunkLength);
		@Value.Default
		final long chunkLength()
		{
			return 0L;
		}

		abstract IdFlagsChunkOffLen<I,F> build();
	}

	@CheckReturnValue
	static <I,F> Builder<I,F> builder()
	{
		return ImmutableIdFlagsChunkOffLen.builder();
	}

	@CheckReturnValue
	static <I,F> IdFlagsChunkOffLen<I,F> instance(I id, F flags, Chunk chunk, long chunkOff, long chunkLen)
	{	// we need to type the builder or the compiler can't figure it out
		Builder<I,F> builder = builder();

		return builder
			.id(id)
			.flags(flags)
			.chunk(chunk)
			.chunkOffset(chunkOff)
			.chunkLength(chunkLen)
			.build();
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
