package net.darkmist.chunks;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class IOEFunctionalTest
{
	//private static final Logger logger = LoggerFactory.getLogger(IOEFunctionalTest.class);
	private static final String TEST_STRING1 = "toast is yummy";
	private static final String TEST_STRING2 = "(like cabage)";

	private static String returnArg(String str)
	{
		return str;
	}

	@Test
	public void testAsFunctionSuccess()
	{
		assertEquals(
			TEST_STRING1,
			IOEFunctional.asUncheckedFunction(IOEFunctionalTest::returnArg).apply(TEST_STRING1)
		);
	}

	private static String throwIOE(String str) throws IOException
	{
		throw new IOException(str);
	}

	@Test
	public void testAsFunctionFailing()
	{
		assertThrows(UncheckedIOException.class, ()->IOEFunctional.asUncheckedFunction(IOEFunctionalTest::throwIOE).apply(TEST_STRING1));
	}

	@Test
	public void testAsIOEFunctionSuccess() throws IOException
	{
		assertEquals(
			TEST_STRING1,
			IOEFunctional.asIOEFunction(IOEFunctionalTest::returnArg).apply(TEST_STRING1)
		);
	}

	private static String throwUncheckedIOE(String str)
	{
		throw new UncheckedIOException(new IOException(str));
	}

	@Test
	public void testAsIOEFunctionFailing()
	{
		assertThrows(IOException.class, ()->IOEFunctional.asIOEFunction(IOEFunctionalTest::throwUncheckedIOE).apply(TEST_STRING1));
	}

	private static String joinLines(String str1, String str2)
	{
		return str1 + '\n' + str2;
	}

	private static String throwIOEBi(String str1, String str2) throws IOException
	{
		throw new IOException(str1 + '\n' + str2);
	}

	@Test
	public void testAsBiFunctionSuccess()
	{
		assertEquals(
			TEST_STRING1 + '\n' + TEST_STRING2,
			IOEFunctional.asBiFunction(IOEFunctionalTest::joinLines).apply(TEST_STRING1, TEST_STRING2)
		);
	}

	@Test
	public void testAsBiFunctionFailing()
	{
		assertThrows(UncheckedIOException.class, ()->IOEFunctional.asBiFunction(IOEFunctionalTest::throwIOEBi).apply(TEST_STRING1,TEST_STRING1));
	}

	@Test
	public void testAsIOEBiFunctionSuccess() throws IOException
	{
		assertEquals(
			TEST_STRING1 + '\n' + TEST_STRING2,
			IOEFunctional.asIOEBiFunction(IOEFunctionalTest::joinLines).apply(TEST_STRING1, TEST_STRING2)
		);
	}

	private static String throwUncheckedIOEBi(String str1, String str2)
	{
		throw new UncheckedIOException(new IOException(str1 + '\n' + str2));
	}

	@Test
	public void testAsIOEBiFunctionFailing()
	{
		assertThrows(IOException.class, ()->IOEFunctional.asIOEBiFunction(IOEFunctionalTest::throwUncheckedIOEBi).apply(TEST_STRING1,TEST_STRING1));
	}
}
