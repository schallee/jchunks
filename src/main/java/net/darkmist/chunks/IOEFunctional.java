package net.darkmist.chunks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class IOEFunctional
{
	private IOEFunctional()
	{
	}

	@FunctionalInterface
	public static interface IOEThrowingFunction<T,R>
	{
		public R apply(T t) throws IOException;
	}

	public static <T,R> Function<T,R> asFunction(IOEThrowingFunction<T,R> func)
	{
		return (t)->
		{
			try
			{
				return func.apply(t);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}

	public static <T,R> IOEThrowingFunction<T,R> asIOEThrowingFunction(Function<T,R> func)
	{
		return (t)->
		{
			try
			{
				return func.apply(t);
			}
			catch(UncheckedIOException e)
			{
				throw e.getCause();
			}
		};
	}

	@FunctionalInterface
	public static interface IOEThrowingBiFunction<T,U,R>
	{
		public R apply(T t, U u) throws IOException;
	}

	public static <T,U,R> BiFunction<T,U,R> asBiFunction(IOEThrowingBiFunction<T,U,R> func)
	{
		return (t,u)->
		{
			try
			{
				return func.apply(t,u);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}

	public static <T,U,R> IOEThrowingBiFunction<T,U,R> asIOEThrowingBiFunction(BiFunction<T,U,R> func)
	{
		return (t,u)->
		{
			try
			{
				return func.apply(t,u);
			}
			catch(UncheckedIOException e)
			{
				throw e.getCause();
			}
		};
	}
}
