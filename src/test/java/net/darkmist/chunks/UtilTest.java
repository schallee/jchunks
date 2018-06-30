package net.darkmist.chunks;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTest
{
	@Test
	public void requireValidOffLen0_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(0,0,0));
	}

	@Test
	public void requireValidOffLen10_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(10,0,0));
	}

	@Test
	public void requireValidOffLen10_0_1()
	{
		assertEquals(1, Util.requireValidOffLenRetEnd(10,0,1));
	}

	@Test
	public void requireValidOffLen10_1_1()
	{
		assertEquals(2, Util.requireValidOffLenRetEnd(10,1,1));
	}

	@Test
	public void requireValidOffLen10_0_10()
	{
		assertEquals(10, Util.requireValidOffLenRetEnd(10,0,10));
	}

	@Test
	public void requireValidOffLen10_1_10()
	{
		try
		{
			Util.requireValidOffLen(10,1,10);
			fail();
		}
		catch(Exception expected)
		{
		}
	}

	@Test
	public void requireValidOffLenNeg1_Neg1_Neg1()
	{
		try
		{
			Util.requireValidOffLen(-1,-1,-1);
			fail();
		}
		catch(Exception e)
		{
			// expected
		}
	}

	@Test
	public void requireValidOffLen10_5_MAX()
	{
		try
		{
			Util.requireValidOffLen(10,5,Integer.MAX_VALUE);
			fail();
		}
		catch(Exception e)
		{
			// expected
		}
	}

	@Test
	public void requireValidOffLen10_MIN_5()
	{
		try
		{
			Util.requireValidOffLen(10,Integer.MIN_VALUE,5);
			fail();
		}
		catch(Exception e)
		{
			// expected
		}
	}
}
