package net.darkmist.chunks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
//import java.lang.invoke.MethodHandles;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.errorprone.annotations.Var;

import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TestUtil
{
	private static final Logger logger = LoggerFactory.getLogger(TestSources.class);

	private TestUtil()
	{
	}

	static <T extends Serializable> byte[] serialize(T obj) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(obj);
		oos.close();
		return baos.toByteArray();
	}

	static <T extends Serializable> T deserialize(Class<T> cls, byte[] bytes) throws ClassNotFoundException, IOException
	{
		Object o;

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		o = ois.readObject();
		return cls.cast(o);
	}

	static <T extends Serializable, O extends T> T serializeDeserialize(Class<T> cls, O obj) throws ClassNotFoundException, IOException
	{
		assertTrue(cls.isInstance(obj));
		return deserialize(cls, serialize(obj));
	}

	private static List<Throwable> getSuppressedAndCauses(List<Throwable> topLevelTs)
	{
		List<Throwable> ret;

		ret = new ArrayList<Throwable>();
		for(Throwable topLevelT : topLevelTs)
		{
			Throwable topLevelCause = topLevelT.getCause();
			if(topLevelCause!=null)
				ret.add(topLevelCause);
			for(Throwable t : topLevelT.getSuppressed())
				ret.add(t);
		}
		return ret;
	}

	private static <T extends Throwable> List<T> addAllocIfNull(List<T> possiblyNullList, T t)
	{
		List<T> list = possiblyNullList==null ? new ArrayList<T>() : possiblyNullList;

		list.add(t);
		return list;
	}

	public static <T extends Throwable> T setCauseOrSuppressed(T exception, List<Throwable> exceptions)
	{
		if(exceptions==null || exceptions.isEmpty())
			return exception;
		if(exceptions.size()==1)
			exception.initCause(exceptions.get(0));
		else
			for(Throwable t : exceptions)
				exception.addSuppressed(t);
		return exception;
	}

	@Deprecated
	private static Method getDeclaredMethodAndMakeAccessible(Class<?> cls, String name, Class<?>...argTypes) throws NoSuchMethodException
	{
		if(logger.isDebugEnabled())
			logger.debug("Trying: cls={} name={} argTypes={}", cls, name, Arrays.toString(argTypes));
		Method meth = cls.getDeclaredMethod(name, argTypes);
		if(!meth.isAccessible())
		// if(!meth.canAccess())
			meth.setAccessible(true);
		return meth;
	}

	private static Method getMethodRecursive(Class<?> baseCls, String name, Class<?>...argTypes) throws NoSuchMethodException
	{
		@Var
		List<Throwable> exceptions = null;

		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(baseCls, "baseCls");
		for(Class<?> cls=baseCls; cls!=null&&!cls.equals(Object.class); cls=cls.getSuperclass())
			try
			{
				return getDeclaredMethodAndMakeAccessible(cls, name, argTypes);
			}
			catch(NoSuchMethodException e)
			{
				exceptions = addAllocIfNull(exceptions, e);
				logger.debug("Failed to get: cls={} name={} argTypes={}", cls, name, Arrays.toString(argTypes), e);
			}
		throw setCauseOrSuppressed(
			new NoSuchMethodException("Unable to get method " + name + " from class " + baseCls + " with arguemnts " + Arrays.toString(argTypes) + '.'),
			exceptions
		);
	}

	static Method getMethod(Class<?> baseCls, String name, Class<?>...argTypes)
	{
		@Var
		List<Throwable> exceptions = null;

		try
		{
			return getMethodRecursive(baseCls, name, argTypes);
		}
		catch(NoSuchMethodException e)
		{
			exceptions = addAllocIfNull(exceptions, e);
		}
		for(Class<?> iface : baseCls.getInterfaces())
			try
			{
				return getMethodRecursive(iface, name, argTypes);
			}
			catch(NoSuchMethodException e)
			{
				exceptions.add(e);
				logger.debug("Failed to get: iface={} name={} argTypes={}", iface, name, Arrays.toString(argTypes), e);
			}

		exceptions = getSuppressedAndCauses(exceptions);
		IllegalStateException ise = new IllegalStateException("Unable to get method " + name + " from class " + baseCls + " with arguemnts " + Arrays.toString(argTypes) + '.');
		throw setCauseOrSuppressed(ise, exceptions);
	}

	static Arguments debugArgumentsOf(Object...objs)
	{
		if(logger.isDebugEnabled())
			logger.debug("debugArgumentsOf(objs={})", Arrays.toString(objs));
		return Arguments.of(objs);
	}

	static byte[] mkByteArray(int...values)
	{
		byte[] bytes = new byte[values.length];
		@Var
		int i=0;

		for(int b : values)
			if((b&0xff) != b)
				throw new IllegalArgumentException("Int value " + b + " is not a valid byte value.");
			else
				bytes[i] = (byte)b;
		return bytes;
	}

	static Byte[] mkByteObjectArray(int...values)
	{
		Byte[] bytes = new Byte[values.length];
		@Var
		int i=0;

		for(int b : values)
			if((b&0xff) != b)
				throw new IllegalArgumentException("Int value " + b + " is not a valid byte value.");
			else
				bytes[i] = (byte)b;
		return bytes;
	}
}
