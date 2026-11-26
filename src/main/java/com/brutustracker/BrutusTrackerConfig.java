package com.brutustracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(BrutusTrackerConfig.GROUP)
public interface BrutusTrackerConfig extends Config
{
	String GROUP = "brutus-tracker";
	String STATS_SECTION = "stats";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show overlay",
		description = "Show session kill stats while fighting Brutus"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigSection(
		name = "Displayed stats",
		description = "Choose which stat lines the overlay shows",
		position = 1
	)
	String statsSection = STATS_SECTION;

	@ConfigItem(
		keyName = "showKills",
		name = "Kills",
		description = "Show the session kill count",
		section = STATS_SECTION,
		position = 0
	)
	default boolean showKills()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showAllTimeKc",
		name = "KC (all time)",
		description = "Show the all-time kill count read from chat",
		section = STATS_SECTION,
		position = 1
	)
	default boolean showAllTimeKc()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showLastKill",
		name = "Last",
		description = "Show the time of your last kill",
		section = STATS_SECTION,
		position = 2
	)
	default boolean showLastKill()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSessionBest",
		name = "Best (session)",
		description = "Show your fastest kill this session",
		section = STATS_SECTION,
		position = 3
	)
	default boolean showSessionBest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPersonalBest",
		name = "PB",
		description = "Show your personal best kill time",
		section = STATS_SECTION,
		position = 4
	)
	default boolean showPersonalBest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showKillsPerHour",
		name = "Kills/hr",
		description = "Show kills per hour this session",
		section = STATS_SECTION,
		position = 5
	)
	default boolean showKillsPerHour()
	{
		return true;
	}
}
