package com.brutustracker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.OptionalInt;
import org.junit.Test;

public class StatTest
{
	private static final BrutusVariant VARIANT = BrutusVariant.NORMAL;

	private static BrutusTrackerConfig allOn()
	{
		return new BrutusTrackerConfig()
		{
		};
	}

	@Test
	public void displayOrderMatchesOverlay()
	{
		String[] labels = new String[Stat.values().length];
		for (Stat stat : Stat.values())
		{
			labels[stat.ordinal()] = stat.label();
		}
		assertArrayEquals(
			new String[]{"Kills", "KC (all time)", "Last", "Best (session)", "PB", "Kills/hr"},
			labels);
	}

	@Test
	public void textForKnownValues()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(VARIANT, 0);
		session.onBrutusDied(VARIANT, 100);
		session.setAllTimeKillCount(VARIANT, OptionalInt.of(42));
		session.setPersonalBest(VARIANT, OptionalInt.of(80));

		assertEquals("1", Stat.KILLS.text(session, VARIANT, 100));
		assertEquals("42", Stat.ALL_TIME_KC.text(session, VARIANT, 100));
		assertEquals("1:00.0", Stat.LAST_KILL.text(session, VARIANT, 100));
		assertEquals("1:00.0", Stat.SESSION_BEST.text(session, VARIANT, 100));
		assertEquals("0:48.0", Stat.PERSONAL_BEST.text(session, VARIANT, 100));
		assertEquals("60.0", Stat.KILLS_PER_HOUR.text(session, VARIANT, 100));
	}

	@Test
	public void missingValuesShowDash()
	{
		BrutusSession session = new BrutusSession();
		assertEquals("-", Stat.ALL_TIME_KC.text(session, VARIANT, 0));
		assertEquals("-", Stat.LAST_KILL.text(session, VARIANT, 0));
		assertEquals("-", Stat.SESSION_BEST.text(session, VARIANT, 0));
		assertEquals("-", Stat.PERSONAL_BEST.text(session, VARIANT, 0));
	}

	@Test
	public void allStatsVisibleByDefault()
	{
		BrutusTrackerConfig config = allOn();
		for (Stat stat : Stat.values())
		{
			assertTrue(stat.name(), stat.visible(config));
		}
		assertTrue(Stat.anyVisible(config));
	}

	@Test
	public void eachStatFollowsItsOwnToggle()
	{
		BrutusTrackerConfig config = new BrutusTrackerConfig()
		{
			@Override
			public boolean showLastKill()
			{
				return false;
			}
		};
		for (Stat stat : Stat.values())
		{
			assertEquals(stat.name(), stat != Stat.LAST_KILL, stat.visible(config));
		}
	}

	@Test
	public void noneVisibleWhenEverythingOff()
	{
		BrutusTrackerConfig config = new BrutusTrackerConfig()
		{
			@Override
			public boolean showKills()
			{
				return false;
			}

			@Override
			public boolean showAllTimeKc()
			{
				return false;
			}

			@Override
			public boolean showLastKill()
			{
				return false;
			}

			@Override
			public boolean showSessionBest()
			{
				return false;
			}

			@Override
			public boolean showPersonalBest()
			{
				return false;
			}

			@Override
			public boolean showKillsPerHour()
			{
				return false;
			}
		};
		assertFalse(Stat.anyVisible(config));
	}
}
