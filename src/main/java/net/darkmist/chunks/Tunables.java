package net.darkmist.chunks;

final class Tunables
{
	private static final int PAGE_SIZE = 4096;	// FIXME!

	private Tunables()
	{
	}

	static int getTmpBufSize()
	{
		return PAGE_SIZE;
	}

	static byte[] getTmpBuf()
	{
		return new byte[PAGE_SIZE];
	}
}
