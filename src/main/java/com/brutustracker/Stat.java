package com.brutustracker;

import java.util.OptionalInt;
import java.util.function.Predicate;

enum Stat
{
	KILLS("Kills", BrutusTrackerConfig::showKills,
		(session, variant) -> Integer.toString(session.getKills(variant))),
	ALL_TIME_KC("KC (all time)", BrutusTrackerConfig::showAllTimeKc,
		(session, variant) -> count(session.getAllTimeKillCount(variant))),
	LAST_KILL("Last", BrutusTrackerConfig::showLastKill,
		(session, variant) -> time(session.getLastKillTicks(variant))),
	SESSION_BEST("Best (session)", BrutusTrackerConfig::showSessionBest,
		(session, variant) -> time(session.getBestTicks(variant))),
	PERSONAL_BEST("PB", BrutusTrackerConfig::showPersonalBest,
		(session, variant) -> time(session.getPersonalBest(variant))),
	KILLS_PER_HOUR("Kills/hr", BrutusTrackerConfig::showKillsPerHour,
		(session, variant) -> String.format("%.1f", session.getKillsPerHour(variant)));

	@FunctionalInterface
	interface ValueFormatter
	{
		String format(BrutusSession session, BrutusVariant variant);
	}

	static final String NO_VALUE = "-";

	private final String label;
	private final Predicate<BrutusTrackerConfig> enabled;
	private final ValueFormatter formatter;

	Stat(String label, Predicate<BrutusTrackerConfig> enabled, ValueFormatter formatter)
	{
		this.label = label;
		this.enabled = enabled;
		this.formatter = formatter;
	}

	String label()
	{
		return label;
	}

	boolean visible(BrutusTrackerConfig config)
	{
		return enabled.test(config);
	}

	String text(BrutusSession session, BrutusVariant variant)
	{
		return formatter.format(session, variant);
	}

	static boolean anyVisible(BrutusTrackerConfig config)
	{
		for (Stat stat : values())
		{
			if (stat.visible(config))
			{
				return true;
			}
		}
		return false;
	}

	private static String count(OptionalInt value)
	{
		return value.isPresent() ? Integer.toString(value.getAsInt()) : NO_VALUE;
	}

	private static String time(OptionalInt ticks)
	{
		return ticks.isPresent() ? TickFormat.format(ticks.getAsInt()) : NO_VALUE;
	}
}
