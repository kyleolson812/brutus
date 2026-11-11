package com.brutustracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import org.junit.Test;

public class KillCountMessageTest
{
	@Test
	public void parsesNormalMessageWithColourTags()
	{
		KillCountMessage kc = KillCountMessage.parse("Your Brutus kill count is: <col=ff0000>12</col>.").get();
		assertEquals(BrutusVariant.NORMAL, kc.getVariant());
		assertEquals(12, kc.getKillCount());
	}

	@Test
	public void demonicIsNotMistakenForNormal()
	{
		KillCountMessage kc = KillCountMessage.parse("Your Demonic Brutus kill count is: <col=ff0000>7</col>.").get();
		assertEquals(BrutusVariant.DEMONIC, kc.getVariant());
		assertEquals(7, kc.getKillCount());
	}

	@Test
	public void parsesCommaSeparatedCount()
	{
		KillCountMessage kc = KillCountMessage.parse("Your Brutus kill count is: <col=ff0000>1,234</col>.").get();
		assertEquals(1234, kc.getKillCount());
	}

	@Test
	public void ignoresOtherBosses()
	{
		assertEquals(Optional.empty(), KillCountMessage.parse("Your Giant Mole kill count is: 5."));
	}

	@Test
	public void ignoresNonKillCountMessages()
	{
		assertFalse(KillCountMessage.parse("Brutus looks at you angrily.").isPresent());
		assertFalse(KillCountMessage.parse("Your Brutus personal best is: <col=ff0000>1:02</col>.").isPresent());
	}

	@Test
	public void bossNameLookupIsExact()
	{
		assertTrue(BrutusVariant.fromBossName("Brutus").isPresent());
		assertFalse(BrutusVariant.fromBossName("brutus").isPresent());
		assertFalse(BrutusVariant.fromBossName("Demonic").isPresent());
	}
}
