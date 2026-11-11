package com.brutustracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(BrutusTrackerConfig.GROUP)
public interface BrutusTrackerConfig extends Config
{
	String GROUP = "brutus-tracker";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show overlay",
		description = "Show session kill stats while fighting Brutus"
	)
	default boolean showOverlay()
	{
		return true;
	}
}
