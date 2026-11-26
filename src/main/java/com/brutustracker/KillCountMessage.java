package com.brutustracker;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;

final class KillCountMessage
{
	private static final Pattern KILL_COUNT = Pattern.compile("^Your (.+) kill count is: ?([0-9][0-9,]*)\\.?$");

	private final BrutusVariant variant;
	private final int killCount;

	private KillCountMessage(BrutusVariant variant, int killCount)
	{
		this.variant = variant;
		this.killCount = killCount;
	}

	BrutusVariant getVariant()
	{
		return variant;
	}

	int getKillCount()
	{
		return killCount;
	}

	static Optional<KillCountMessage> parse(String message)
	{
		Matcher m = KILL_COUNT.matcher(Text.removeTags(message));
		if (!m.matches())
		{
			return Optional.empty();
		}
		return BrutusVariant.fromBossName(m.group(1))
			.map(variant -> new KillCountMessage(variant, Integer.parseInt(m.group(2).replace(",", ""))));
	}
}
