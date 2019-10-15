package net.darkmist.chunks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LargeChunksHelperTest
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(LargeChunksHelperTest.class);

	@SuppressWarnings("UnnecessaryParentheses")
	@Test
	public void testTooManySubChunks()
	{
		assertThrows(UnsupportedOperationException.class, ()->
			LargeChunksHelper.instance(
				0,
				(long)(Integer.MAX_VALUE) * LargeChunksHelper.LARGE_CHUNK_SIZE + 1)
		);
	}
}
