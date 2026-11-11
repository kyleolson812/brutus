package com.brutustracker;

import com.google.inject.Provides;
import java.util.Optional;
import java.util.OptionalInt;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Brutus Tracker",
	description = "Session kill stats for Brutus and Demonic Brutus",
	tags = {"brutus", "cow", "boss", "kc", "tracker"}
)
public class BrutusTrackerPlugin extends Plugin
{
	private static final int RECENT_TICKS = 100;
	private static final int NEVER_SEEN = Integer.MIN_VALUE;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BrutusTrackerOverlay overlay;

	private final BrutusSession session = new BrutusSession();
	private NPC brutus;
	private int lastSeenTick = NEVER_SEEN;

	@Override
	protected void startUp()
	{
		loadPersonalBests();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		brutus = null;
		lastSeenTick = NEVER_SEEN;
		session.onBrutusDespawnedAlive();
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
		return session.isFightInProgress() || client.getTickCount() - lastSeenTick <= RECENT_TICKS;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			loadPersonalBests();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (BrutusVariant.fromNpcId(npc.getId()).isPresent())
		{
			brutus = npc;
			lastSeenTick = client.getTickCount();
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (brutus == null || event.getActor() != brutus)
		{
			return;
		}
		int tick = client.getTickCount();
		lastSeenTick = tick;
		variantOf(brutus).ifPresent(variant -> session.onBrutusDamaged(variant, tick));
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (brutus == null || event.getActor() != brutus)
		{
			return;
		}
		int tick = client.getTickCount();
		lastSeenTick = tick;
		BrutusVariant variant = variantOf(brutus).orElse(null);
		// Cleared so the despawn that follows the death is not mistaken for a mid-fight despawn.
		brutus = null;
		if (variant == null)
		{
			return;
		}
		if (session.onBrutusDied(variant, tick))
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
			session.onBrutusDespawnedAlive();
		}
	}

	private Optional<BrutusVariant> variantOf(NPC npc)
	{
		return BrutusVariant.fromNpcId(npc.getId());
	}

	private void loadPersonalBests()
	{
		for (BrutusVariant variant : BrutusVariant.values())
		{
			Integer ticks = configManager.getRSProfileConfiguration(
				BrutusTrackerConfig.GROUP, variant.getPbConfigKey(), Integer.class);
			session.setPersonalBest(variant, ticks == null ? OptionalInt.empty() : OptionalInt.of(ticks));
		}
	}
}
