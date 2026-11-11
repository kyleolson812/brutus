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

	@Test
	public void killDurationIsDeathTickMinusFirstDamageTick()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL, 100);
		session.onBrutusDamaged(NORMAL, 110);
		session.onBrutusDied(NORMAL, 150);

		assertEquals(1, session.getKills(NORMAL));
		assertEquals(OptionalInt.of(50), session.getLastKillTicks(NORMAL));
		assertFalse(session.isFightInProgress());
	}

	@Test
	public void variantsAreCountedSeparately()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL, 0);
		session.onBrutusDied(NORMAL, 40);
		session.onBrutusDamaged(DEMONIC, 100);
		session.onBrutusDied(DEMONIC, 180);
		session.onBrutusDamaged(DEMONIC, 200);
		session.onBrutusDied(DEMONIC, 250);

		assertEquals(1, session.getKills(NORMAL));
		assertEquals(2, session.getKills(DEMONIC));
		assertEquals(OptionalInt.of(40), session.getBestTicks(NORMAL));
		assertEquals(OptionalInt.of(50), session.getBestTicks(DEMONIC));
	}

	@Test
	public void despawnMidFightDoesNotCountAndNextFightStartsFresh()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL, 0);
		session.onBrutusDespawnedAlive();

		assertEquals(0, session.getKills(NORMAL));
		assertFalse(session.isFightInProgress());

		session.onBrutusDamaged(NORMAL, 500);
		session.onBrutusDied(NORMAL, 560);
		assertEquals(OptionalInt.of(60), session.getLastKillTicks(NORMAL));
	}

	@Test
	public void killWithoutAFightCountsButHasNoDuration()
	{
		BrutusSession session = new BrutusSession();
		assertFalse(session.onBrutusDied(NORMAL, 10));

		assertEquals(1, session.getKills(NORMAL));
		assertFalse(session.getLastKillTicks(NORMAL).isPresent());
		assertFalse(session.getPersonalBest(NORMAL).isPresent());
	}

	@Test
	public void killOfOtherVariantThanFightHasNoDuration()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL, 0);
		session.onBrutusDied(DEMONIC, 50);

		assertEquals(1, session.getKills(DEMONIC));
		assertFalse(session.getLastKillTicks(DEMONIC).isPresent());
		assertFalse(session.isFightInProgress());
	}

	@Test
	public void firstTimedKillIsAPersonalBest()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL, 0);

		assertTrue(session.onBrutusDied(NORMAL, 70));
		assertEquals(OptionalInt.of(70), session.getPersonalBest(NORMAL));
	}

	@Test
	public void onlyFasterKillsBeatTheStoredPersonalBest()
	{
		BrutusSession session = new BrutusSession();
		session.setPersonalBest(NORMAL, OptionalInt.of(60));

		session.onBrutusDamaged(NORMAL, 0);
		assertFalse(session.onBrutusDied(NORMAL, 60));
		session.onBrutusDamaged(NORMAL, 100);
		assertFalse(session.onBrutusDied(NORMAL, 170));
		session.onBrutusDamaged(NORMAL, 200);
		assertTrue(session.onBrutusDied(NORMAL, 255));

		assertEquals(OptionalInt.of(55), session.getPersonalBest(NORMAL));
		assertEquals(OptionalInt.of(55), session.getBestTicks(NORMAL));
	}

	@Test
	public void sessionBestIsIndependentOfStoredPersonalBest()
	{
		BrutusSession session = new BrutusSession();
		session.setPersonalBest(NORMAL, OptionalInt.of(10));
		session.onBrutusDamaged(NORMAL, 0);
		session.onBrutusDied(NORMAL, 80);

		assertEquals(OptionalInt.of(80), session.getBestTicks(NORMAL));
		assertEquals(OptionalInt.of(10), session.getPersonalBest(NORMAL));
	}

	@Test
	public void killsPerHourUsesTimeSinceFirstDamage()
	{
		BrutusSession session = new BrutusSession();
		session.onBrutusDamaged(NORMAL, 1000);
		session.onBrutusDied(NORMAL, 1050);
		session.onBrutusDamaged(NORMAL, 1100);
		session.onBrutusDied(NORMAL, 1150);

		// 6000 ticks = 1 hour
		assertEquals(2.0, session.getKillsPerHour(NORMAL, 7000), 1e-9);
		assertEquals(0.0, session.getKillsPerHour(DEMONIC, 7000), 1e-9);
	}

	@Test
	public void killsPerHourIsZeroWithNoElapsedTime()
	{
		BrutusSession session = new BrutusSession();
		assertEquals(0.0, session.getKillsPerHour(NORMAL, 500), 1e-9);

		session.onBrutusDamaged(NORMAL, 500);
		session.onBrutusDied(NORMAL, 500);
		assertEquals(0.0, session.getKillsPerHour(NORMAL, 500), 1e-9);
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
		session.onBrutusDamaged(NORMAL, 0);
		session.onBrutusDied(NORMAL, 40);
		assertEquals(OptionalInt.of(10), session.getAllTimeKillCount(NORMAL));

		session.setAllTimeKillCount(NORMAL, OptionalInt.of(11));
		assertEquals(OptionalInt.of(11), session.getAllTimeKillCount(NORMAL));
		assertEquals(OptionalInt.empty(), session.getAllTimeKillCount(DEMONIC));
	}
}
