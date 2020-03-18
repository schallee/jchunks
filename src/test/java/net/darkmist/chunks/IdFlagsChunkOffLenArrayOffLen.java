package net.darkmist.chunks;

import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.immutables.value.Value;

//import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

@CheckReturnValue
@Immutable
@ParametersAreNonnullByDefault
@Value.Immutable
// FIXME: remove this annotation when immutables issue is fixed...
@SuppressUnnecessaryCheckNotNullBuilderWarning
abstract class IdFlagsChunkOffLenArrayOffLen<I,F>
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
	@Nonnull
	@Value.Parameter
	abstract List<Byte> getByteList();

	@Nonnull
	final byte[] getArray()
	{
		List<Byte> list = getByteList();
		byte[] bytes = new byte[list.size()];

		for(int i=0;i<bytes.length;i++)
			bytes[i]=list.get(i);
		return bytes;
	}

	@CheckReturnValue
	@Value.Parameter
	abstract int getArrayOffset();

	// Note: Do not set @Nonnegative as this is for testing and negatives are needed.
	@CheckReturnValue
	@Value.Parameter
	abstract int getArrayLength();

	@NotThreadSafe
	static abstract class Builder<I,F>
	{
		abstract Builder<I,F> from(IdFlagsChunkOffLenArrayOffLen<I,F> base);

		final Builder<I,F> from(IdFlagsChunkOffLen<I,F> base)
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

		@Nonnull
		abstract Builder<I,F> addAllByteList(Iterable<Byte> list);

		@Nonnull
		final Builder<I,F> array(byte...bytes)
		{
			Byte[] byteObjs = new Byte[bytes.length];
			for(int i=0; i<bytes.length; i++)
				byteObjs[i] = bytes[i];
			return this.addAllByteList(Arrays.asList(byteObjs));
		}

		abstract Builder<I,F> arrayOffset(int arrayOffset);
		@Value.Default
		final int arrayOffset()
		{
			return 0;
		}

		abstract Builder<I,F> arrayLength(int arrayLength);
		@Value.Default
		final int arrayLength()
		{
			return 0;
		}

		abstract IdFlagsChunkOffLenArrayOffLen<I,F> build();
	}

	@CheckReturnValue
	static <I,F> Builder<I,F> builder()
	{
		return ImmutableIdFlagsChunkOffLenArrayOffLen.builder();
	}

	@CheckReturnValue
	static <I,F> IdFlagsChunkOffLenArrayOffLen<I,F> instance(I id, F flags, Chunk chunk, long chunkOff, long chunkLen)
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
	static <I,F> IdFlagsChunkOffLenArrayOffLen<I,F> instance(I id, F flags, Chunk chunk, long chunkOff, long chunkLen, int arrayOff, int arrayLen)
	{	// we need to type the builder or the compiler can't figure it out
		Builder<I,F> builder = builder();

		return builder
			.id(id)
			.flags(flags)
			.chunk(chunk)
			.chunkOffset(chunkOff)
			.chunkLength(chunkLen)
			.arrayOffset(arrayOff)
			.arrayLength(arrayLen)
			.build();
	}
}
