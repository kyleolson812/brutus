package com.brutustracker;

import com.google.inject.Provides;
import java.util.Optional;
import java.util.OptionalInt;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Brutus Tracker",
	description = "Kill count, kill times, personal bests and kills per hour for Brutus and Demonic Brutus",
	tags = {"brutus", "cow", "boss", "kc", "tracker"}
)
public class BrutusTrackerPlugin extends Plugin
{
	private static final long NO_ACCOUNT = -1;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BrutusTrackerOverlay overlay;

	// The overlay reads this from the render thread.
	private volatile BrutusSession session = new BrutusSession();
	private NPC brutus;
	private long lastAccountHash = NO_ACCOUNT;

	@Override
	protected void startUp()
	{
		session = new BrutusSession();
		lastAccountHash = client.getAccountHash();
		loadStoredStats();
		overlayManager.add(overlay);
		clientThread.invokeLater(this::adoptBrutusOnScreen);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		brutus = null;
		session.abandonFight();
	}

	@Provides
	BrutusTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BrutusTrackerConfig.class);
	}

	BrutusSession getSession()
	{
		return session;
	}

	boolean isRecentlyActive()
	{
		return session.isRecentlyActive();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		session.onTick();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			brutus = null;
			session.abandonFight();
		}
		else if (state == GameState.LOGGED_IN)
		{
			long accountHash = client.getAccountHash();
			if (accountHash != NO_ACCOUNT)
			{
				if (lastAccountHash != NO_ACCOUNT && accountHash != lastAccountHash)
				{
					session = new BrutusSession();
				}
				lastAccountHash = accountHash;
			}
			loadStoredStats();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		KillCountMessage.parse(event.getMessage()).ifPresent(kc ->
		{
			session.setAllTimeKillCount(kc.getVariant(), OptionalInt.of(kc.getKillCount()));
			configManager.setRSProfileConfiguration(
				BrutusTrackerConfig.GROUP, kc.getVariant().getKcConfigKey(), kc.getKillCount());
		});
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (BrutusVariant.fromNpcId(npc.getId()).isPresent())
		{
			brutus = npc;
			session.markSeen();
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (brutus == null || event.getActor() != brutus)
		{
			return;
		}
		variantOf(brutus).ifPresent(session::onBrutusDamaged);
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (brutus == null || event.getActor() != brutus)
		{
			return;
		}
		BrutusVariant variant = variantOf(brutus).orElse(null);
		// Cleared so the despawn that follows the death is not mistaken for a mid-fight despawn.
		brutus = null;
		if (variant == null)
		{
			return;
		}
		if (session.onBrutusDied(variant))
		{
			configManager.setRSProfileConfiguration(
				BrutusTrackerConfig.GROUP, variant.getPbConfigKey(), session.getPersonalBest(variant).getAsInt());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (brutus != null && event.getNpc() == brutus)
		{
			brutus = null;
			session.abandonFight();
		}
	}

	private void adoptBrutusOnScreen()
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getTopLevelWorldView() == null)
		{
			return;
		}
		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			if (BrutusVariant.fromNpcId(npc.getId()).isPresent())
			{
				brutus = npc;
				session.markSeen();
				return;
			}
		}
	}

	private Optional<BrutusVariant> variantOf(NPC npc)
	{
		return BrutusVariant.fromNpcId(npc.getId());
	}

	private void loadStoredStats()
	{
		for (BrutusVariant variant : BrutusVariant.values())
		{
			Integer ticks = configManager.getRSProfileConfiguration(
				BrutusTrackerConfig.GROUP, variant.getPbConfigKey(), Integer.class);
			session.setPersonalBest(variant, ticks == null ? OptionalInt.empty() : OptionalInt.of(ticks));
			Integer kc = configManager.getRSProfileConfiguration(
				BrutusTrackerConfig.GROUP, variant.getKcConfigKey(), Integer.class);
			session.setAllTimeKillCount(variant, kc == null ? OptionalInt.empty() : OptionalInt.of(kc));
		}
	}
}
