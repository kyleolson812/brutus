package com.brutustracker;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TickFormatTest
{
	@Test
	public void formatsMinutesSecondsAndTenths()
	{
		assertEquals("0:00.0", TickFormat.format(0));
		assertEquals("0:00.6", TickFormat.format(1));
		assertEquals("0:30.0", TickFormat.format(50));
		assertEquals("1:00.0", TickFormat.format(100));
		assertEquals("1:05.4", TickFormat.format(109));
		assertEquals("10:00.0", TickFormat.format(1000));
	}
}
