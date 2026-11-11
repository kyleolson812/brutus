package com.brutustracker;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

public class BrutusSession
{
	private static final double SECONDS_PER_TICK = 0.6;
	private static final double SECONDS_PER_HOUR = 3600;
	private static final int NOT_STARTED = -1;

	private static final class Fight
	{
		private final BrutusVariant variant;
		private final int startTick;

		private Fight(BrutusVariant variant, int startTick)
		{
			this.variant = variant;
			this.startTick = startTick;
		}
	}

	private static final class Stats
	{
		private int kills;
		private OptionalInt lastKillTicks = OptionalInt.empty();
		private OptionalInt bestTicks = OptionalInt.empty();
		private OptionalInt pbTicks = OptionalInt.empty();
	}

	private final Map<BrutusVariant, Stats> stats = new EnumMap<>(BrutusVariant.class);
	private Fight fight;
	private int sessionStartTick = NOT_STARTED;

	public BrutusSession()
	{
		for (BrutusVariant variant : BrutusVariant.values())
		{
			stats.put(variant, new Stats());
		}
	}

	public void onBrutusDamaged(BrutusVariant variant, int tick)
	{
		startSessionIfNeeded(tick);
		if (fight == null)
		{
			fight = new Fight(variant, tick);
		}
	}

	/**
	 * @return true when this kill set a new all-time personal best
	 */
	public boolean onBrutusDied(BrutusVariant variant, int tick)
	{
		startSessionIfNeeded(tick);
		Stats s = stats.get(variant);
		s.kills++;

		boolean timed = fight != null && fight.variant == variant;
		int duration = timed ? tick - fight.startTick : 0;
		fight = null;

		if (!timed)
		{
			s.lastKillTicks = OptionalInt.empty();
			return false;
		}

		s.lastKillTicks = OptionalInt.of(duration);
		if (!s.bestTicks.isPresent() || duration < s.bestTicks.getAsInt())
		{
			s.bestTicks = OptionalInt.of(duration);
		}
		if (!s.pbTicks.isPresent() || duration < s.pbTicks.getAsInt())
		{
			s.pbTicks = OptionalInt.of(duration);
			return true;
		}
		return false;
	}

	public void onBrutusDespawnedAlive()
	{
		fight = null;
	}

	public void setPersonalBest(BrutusVariant variant, OptionalInt ticks)
	{
		stats.get(variant).pbTicks = ticks;
	}

	public boolean isFightInProgress()
	{
		return fight != null;
	}

	public int getKills(BrutusVariant variant)
	{
		return stats.get(variant).kills;
	}

	public OptionalInt getLastKillTicks(BrutusVariant variant)
	{
		return stats.get(variant).lastKillTicks;
	}

	public OptionalInt getBestTicks(BrutusVariant variant)
	{
		return stats.get(variant).bestTicks;
	}

	public OptionalInt getPersonalBest(BrutusVariant variant)
	{
		return stats.get(variant).pbTicks;
	}

	public double getKillsPerHour(BrutusVariant variant, int currentTick)
	{
		if (sessionStartTick == NOT_STARTED)
		{
			return 0;
		}
		int elapsedTicks = currentTick - sessionStartTick;
		if (elapsedTicks <= 0)
		{
			return 0;
		}
		return stats.get(variant).kills / (elapsedTicks * SECONDS_PER_TICK / SECONDS_PER_HOUR);
	}

	private void startSessionIfNeeded(int tick)
	{
		if (sessionStartTick == NOT_STARTED)
		{
			sessionStartTick = tick;
		}
	}
}
