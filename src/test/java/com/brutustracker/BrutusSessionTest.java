package com.brutustracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import java.util.OptionalInt;
import net.runelite.api.gameval.NpcID;
import org.junit.Test;

public class BrutusSessionTest
{
	private static final BrutusVariant NORMAL = BrutusVariant.NORMAL;
	private static final BrutusVariant DEMONIC = BrutusVariant.DEMONIC;

	private static void tick(BrutusSession session, int ticks)
	{
		for (int i = 0; i < ticks; i++)
		{
			session.onTick();
		}
	}

	@Test
	public void killDurationIsDeathTickMinusFirstDamageTick()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		tick(session, 10);
		session.onBrutusDamaged(NORMAL);
		tick(session, 40);
		session.onBrutusDied(NORMAL);

		assertEquals(1, session.getKills(NORMAL));
		assertEquals(OptionalInt.of(50), session.getLastKillTicks(NORMAL));
		assertFalse(session.isFightInProgress());
	}

	@Test
	public void variantsAreCountedSeparately()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		tick(session, 40);
		session.onBrutusDied(NORMAL);
		session.onBrutusDamaged(DEMONIC);
		tick(session, 80);
		session.onBrutusDied(DEMONIC);
		session.onBrutusDamaged(DEMONIC);
		tick(session, 50);
		session.onBrutusDied(DEMONIC);

		assertEquals(1, session.getKills(NORMAL));
		assertEquals(2, session.getKills(DEMONIC));
		assertEquals(OptionalInt.of(40), session.getBestTicks(NORMAL));
		assertEquals(OptionalInt.of(50), session.getBestTicks(DEMONIC));
	}

	@Test
	public void abandonedFightDoesNotCountAndNextFightStartsFresh()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		session.abandonFight();

		assertEquals(0, session.getKills(NORMAL));
		assertFalse(session.isFightInProgress());

		tick(session, 500);
		session.onBrutusDamaged(NORMAL);
		tick(session, 60);
		session.onBrutusDied(NORMAL);
		assertEquals(OptionalInt.of(60), session.getLastKillTicks(NORMAL));
	}

	@Test
	public void deathAfterLogoutOrHopRecordsNoDuration()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		tick(session, 30);
		session.abandonFight();
		tick(session, 30);
		assertFalse(session.onBrutusDied(NORMAL));

		assertEquals(1, session.getKills(NORMAL));
		assertFalse(session.getLastKillTicks(NORMAL).isPresent());
		assertFalse(session.getPersonalBest(NORMAL).isPresent());
	}

	@Test
	public void killWithoutAFightCountsButHasNoDuration()
	{
		BrutusSession session = new BrutusSession();
		assertFalse(session.onBrutusDied(NORMAL));

		assertEquals(1, session.getKills(NORMAL));
		assertFalse(session.getLastKillTicks(NORMAL).isPresent());
		assertFalse(session.getPersonalBest(NORMAL).isPresent());
	}

	@Test
	public void killOfOtherVariantThanFightHasNoDuration()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		tick(session, 50);
		session.onBrutusDied(DEMONIC);

		assertEquals(1, session.getKills(DEMONIC));
		assertFalse(session.getLastKillTicks(DEMONIC).isPresent());
		assertFalse(session.isFightInProgress());
	}

	@Test
	public void firstTimedKillIsAPersonalBest()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		tick(session, 70);

		assertTrue(session.onBrutusDied(NORMAL));
		assertEquals(OptionalInt.of(70), session.getPersonalBest(NORMAL));
	}

	@Test
	public void onlyFasterKillsBeatTheStoredPersonalBest()
	{
		BrutusSession session = new BrutusSession();
		session.setPersonalBest(NORMAL, OptionalInt.of(60));

		session.onBrutusDamaged(NORMAL);
		tick(session, 60);
		assertFalse(session.onBrutusDied(NORMAL));
		session.onBrutusDamaged(NORMAL);
		tick(session, 70);
		assertFalse(session.onBrutusDied(NORMAL));
		session.onBrutusDamaged(NORMAL);
		tick(session, 55);
		assertTrue(session.onBrutusDied(NORMAL));

		assertEquals(OptionalInt.of(55), session.getPersonalBest(NORMAL));
		assertEquals(OptionalInt.of(55), session.getBestTicks(NORMAL));
	}

	@Test
	public void sessionBestIsIndependentOfStoredPersonalBest()
	{
		BrutusSession session = new BrutusSession();
		session.setPersonalBest(NORMAL, OptionalInt.of(10));
		session.onBrutusDamaged(NORMAL);
		tick(session, 80);
		session.onBrutusDied(NORMAL);

		assertEquals(OptionalInt.of(80), session.getBestTicks(NORMAL));
		assertEquals(OptionalInt.of(10), session.getPersonalBest(NORMAL));
	}

	@Test
	public void killsPerHourUsesSessionTicksSinceFirstDamage()
	{
		BrutusSession session = new BrutusSession();
		tick(session, 1000);
		session.onBrutusDamaged(NORMAL);
		tick(session, 50);
		session.onBrutusDied(NORMAL);
		session.onBrutusDamaged(NORMAL);
		tick(session, 50);
		session.onBrutusDied(NORMAL);
		tick(session, 5900);

		// 6000 ticks since first damage = 1 hour
		assertEquals(2.0, session.getKillsPerHour(NORMAL), 1e-9);
		assertEquals(0.0, session.getKillsPerHour(DEMONIC), 1e-9);
	}

	@Test
	public void killsPerHourIsZeroWithNoElapsedTime()
	{
		BrutusSession session = new BrutusSession();
		tick(session, 500);
		assertEquals(0.0, session.getKillsPerHour(NORMAL), 1e-9);

		session.onBrutusDamaged(NORMAL);
		session.onBrutusDied(NORMAL);
		assertEquals(0.0, session.getKillsPerHour(NORMAL), 1e-9);
	}

	@Test
	public void neverSeenIsNotRecentlyActive()
	{
		BrutusSession session = new BrutusSession();
		assertFalse(session.isRecentlyActive());
		tick(session, 1_000_000);
		assertFalse(session.isRecentlyActive());
	}

	@Test
	public void seenIsRecentOnlyWithinOneHundredTicks()
	{
		BrutusSession session = new BrutusSession();
		session.markSeen();
		tick(session, 100);
		assertTrue(session.isRecentlyActive());
		tick(session, 1);
		assertFalse(session.isRecentlyActive());
	}

	@Test
	public void fightInProgressIsActiveRegardlessOfLastSeen()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL);
		tick(session, 500);
		assertTrue(session.isRecentlyActive());
	}

	@Test
	public void npcIdsMapToVariants()
	{
		assertEquals(Optional.of(NORMAL), BrutusVariant.fromNpcId(NpcID.COWBOSS));
		assertEquals(Optional.of(DEMONIC), BrutusVariant.fromNpcId(NpcID.COWBOSS_HARDMODE));
		assertEquals(Optional.empty(), BrutusVariant.fromNpcId(-1));
	}

	@Test
	public void allTimeKillCountIsEmptyUntilSetThenReplacedNotIncremented()
	{
		BrutusSession session = new BrutusSession();
		assertEquals(OptionalInt.empty(), session.getAllTimeKillCount(NORMAL));

		session.setAllTimeKillCount(NORMAL, OptionalInt.of(10));
		session.onBrutusDamaged(NORMAL);
		tick(session, 40);
		session.onBrutusDied(NORMAL);
		assertEquals(OptionalInt.of(10), session.getAllTimeKillCount(NORMAL));

		session.setAllTimeKillCount(NORMAL, OptionalInt.of(11));
		assertEquals(OptionalInt.of(11), session.getAllTimeKillCount(NORMAL));
		assertEquals(OptionalInt.empty(), session.getAllTimeKillCount(DEMONIC));
	}
}
