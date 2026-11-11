package com.brutustracker;

import java.util.Optional;
import net.runelite.api.gameval.NpcID;

public enum BrutusVariant
{
	NORMAL("Brutus", "pb-normal", NpcID.COWBOSS),
	DEMONIC("Demonic Brutus", "pb-demonic", NpcID.COWBOSS_HARDMODE);

	private final String displayName;
	private final String pbConfigKey;
	private final int npcId;

	BrutusVariant(String displayName, String pbConfigKey, int npcId)
	{
		this.displayName = displayName;
		this.pbConfigKey = pbConfigKey;
		this.npcId = npcId;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getPbConfigKey()
	{
		return pbConfigKey;
	}

	public static Optional<BrutusVariant> fromNpcId(int npcId)
	{
		for (BrutusVariant variant : values())
		{
			if (variant.npcId == npcId)
			{
				return Optional.of(variant);
			}
		}
		return Optional.empty();
	}
}
