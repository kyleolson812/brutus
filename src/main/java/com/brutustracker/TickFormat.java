package com.brutustracker;

final class TickFormat
{
	private static final int TENTHS_PER_TICK = 6;

	private TickFormat()
	{
	}

	/** Formats game ticks (0.6s each) as m:ss.s */
	static String format(int ticks)
	{
		int tenths = ticks * TENTHS_PER_TICK;
		return String.format("%d:%02d.%d", tenths / 600, (tenths % 600) / 10, tenths % 10);
	}
}
