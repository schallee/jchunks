package net.darkmist.chunks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiFunction;
import java.util.function.Function;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class IOEFunctional
{
	private IOEFunctional()
	{
	}

	@FunctionalInterface
	static interface IOEFunction<T,R>
	{
		public R apply(T t) throws IOException;
	}

	@SuppressFBWarnings(value="EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS", justification="Purpos of the function.")
	static <T,R> R wrapIOEFunction(IOEFunction<T,R> func, T t)
	{
		try
		{
			return func.apply(t);
		}
		catch(IOException ioe)
		{
			throw new UncheckedIOException(ioe);
		}
	}

	@SuppressWarnings("NoFunctionalReturnType")
		// purpose of method
	static <T,R> Function<T,R> asUncheckedFunction(IOEFunction<T,R> func)
	{
		return (t)->wrapIOEFunction(func,t);
	}

	static <T,R> R unwrapIOEFunction(Function<T,R> func, T t) throws IOException
	{
		try
		{
			return func.apply(t);
		}
		catch(UncheckedIOException e)
		{
			throw e.getCause();
		}
	}

	static <T,R> IOEFunction<T,R> asIOEFunction(Function<T,R> func)
	{
		return (t)->unwrapIOEFunction(func, t);
	}

	@FunctionalInterface
	static interface IOEBiFunction<T,U,R>
	{
		public R apply(T t, U u) throws IOException;
	}

	@SuppressWarnings("NoFunctionalReturnType")
		// purpose of method
	static <T,U,R> BiFunction<T,U,R> asBiFunction(IOEBiFunction<T,U,R> func)
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

	static <T,U,R> IOEBiFunction<T,U,R> asIOEBiFunction(BiFunction<T,U,R> func)
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
