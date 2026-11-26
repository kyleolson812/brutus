package com.brutustracker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class BrutusTrackerOverlay extends OverlayPanel
{
	private final Client client;
	private final BrutusTrackerPlugin plugin;
	private final BrutusTrackerConfig config;

	@Inject
	private BrutusTrackerOverlay(Client client, BrutusTrackerPlugin plugin, BrutusTrackerConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || !Stat.anyVisible(config) || !plugin.isRecentlyActive())
		{
			return null;
		}

		BrutusSession session = plugin.getSession();
		int tick = client.getTickCount();
		for (BrutusVariant variant : BrutusVariant.values())
		{
			int kills = session.getKills(variant);
			if (kills == 0)
			{
				continue;
			}
			panelComponent.getChildren().add(TitleComponent.builder().text(variant.getDisplayName()).build());
			for (Stat stat : Stat.values())
			{
				if (stat.visible(config))
				{
					addLine(stat.label(), stat.text(session, variant, tick));
				}
			}
		}
		return super.render(graphics);
	}

	private void addLine(String left, String right)
	{
		panelComponent.getChildren().add(LineComponent.builder().left(left).right(right).build());
	}
}
