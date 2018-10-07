package net.darkmist.chunks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LargeChunksHelperTest
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(LargeChunksHelperTest.class);

	@Test
	public void testTooManySubChunks()
	{
		LargeChunksHelper helper;

		try
		{
			helper = LargeChunksHelper.instance(0,
				(long)(Integer.MAX_VALUE) * LargeChunksHelper.LARGE_CHUNK_SIZE + 1);
			fail("Expected exception due to number of chunks exceeding Integer.MAX_VALUE but got " + helper + '.');
		}
		catch(UnsupportedOperationException expected)
		{
			logger.debug("Got expected exceptoion.", expected);
		}
	}
}
