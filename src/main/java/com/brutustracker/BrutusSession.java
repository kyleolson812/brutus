package com.brutustracker;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

public class BrutusSession
{
	private static final double SECONDS_PER_TICK = 0.6;
	private static final double SECONDS_PER_HOUR = 3600;
	private static final int RECENT_TICKS = 100;

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
		private OptionalInt allTimeKc = OptionalInt.empty();
	}

	private final Map<BrutusVariant, Stats> stats = new EnumMap<>(BrutusVariant.class);
	private Fight fight;
	private int clock;
	private OptionalInt sessionStartTick = OptionalInt.empty();
	private OptionalInt lastSeenTick = OptionalInt.empty();

	public BrutusSession()
	{
		for (BrutusVariant variant : BrutusVariant.values())
		{
			stats.put(variant, new Stats());
		}
	}

	/** The session's own game-tick clock, so stored timestamps never depend on the client's tick counter. */
	public void onTick()
	{
		clock++;
	}

	public void markSeen()
	{
		lastSeenTick = OptionalInt.of(clock);
	}

	public boolean isRecentlyActive()
	{
		return fight != null || (lastSeenTick.isPresent() && clock - lastSeenTick.getAsInt() <= RECENT_TICKS);
	}

	public void onBrutusDamaged(BrutusVariant variant)
	{
		startSessionIfNeeded();
		markSeen();
		if (fight == null)
		{
			fight = new Fight(variant, clock);
		}
	}

	/**
	 * @return true when this kill set a new all-time personal best
	 */
	public boolean onBrutusDied(BrutusVariant variant)
	{
		startSessionIfNeeded();
		markSeen();
		Stats s = stats.get(variant);
		s.kills++;

		boolean timed = fight != null && fight.variant == variant;
		int duration = timed ? clock - fight.startTick : 0;
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

	public void abandonFight()
	{
		fight = null;
	}

	public void setPersonalBest(BrutusVariant variant, OptionalInt ticks)
	{
		stats.get(variant).pbTicks = ticks;
	}

	public void setAllTimeKillCount(BrutusVariant variant, OptionalInt kc)
	{
		stats.get(variant).allTimeKc = kc;
	}

	public OptionalInt getAllTimeKillCount(BrutusVariant variant)
	{
		return stats.get(variant).allTimeKc;
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

	public double getKillsPerHour(BrutusVariant variant)
	{
		if (!sessionStartTick.isPresent())
		{
			return 0;
		}
		int elapsedTicks = clock - sessionStartTick.getAsInt();
		if (elapsedTicks <= 0)
		{
			return 0;
		}
		return stats.get(variant).kills / (elapsedTicks * SECONDS_PER_TICK / SECONDS_PER_HOUR);
	}

	private void startSessionIfNeeded()
	{
		if (!sessionStartTick.isPresent())
		{
			sessionStartTick = OptionalInt.of(clock);
		}
	}
}
