package net.darkmist.chunks;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class Tunables
{
	private static final int PAGE_SIZE = 4096;	// FIXME!

	private Tunables()
	{
	}

	@SuppressFBWarnings(value="MRC_METHOD_RETURNS_CONSTANT",justification="Future extension.")
	static int getTmpBufSize()
	{
		return PAGE_SIZE;
	}

	static byte[] getTmpBuf()
	{
		return new byte[PAGE_SIZE];
	}
}
