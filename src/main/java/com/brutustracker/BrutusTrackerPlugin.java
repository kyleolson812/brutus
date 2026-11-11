package com.brutustracker;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Brutus Tracker",
	description = "Session kill stats for Brutus and Demonic Brutus",
	tags = {"brutus", "cow", "boss", "kc", "tracker"}
)
public class BrutusTrackerPlugin extends Plugin
{
	@Provides
	BrutusTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BrutusTrackerConfig.class);
	}
}
