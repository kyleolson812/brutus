package com.brutustracker;

import java.util.Optional;
import net.runelite.api.gameval.NpcID;

public enum BrutusVariant
{
	NORMAL("Brutus", "pb-normal", "kc-normal", NpcID.COWBOSS),
	DEMONIC("Demonic Brutus", "pb-demonic", "kc-demonic", NpcID.COWBOSS_HARDMODE);

	private final String displayName;
	private final String pbConfigKey;
	private final String kcConfigKey;
	private final int npcId;

	BrutusVariant(String displayName, String pbConfigKey, String kcConfigKey, int npcId)
	{
		this.displayName = displayName;
		this.pbConfigKey = pbConfigKey;
		this.kcConfigKey = kcConfigKey;
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

	public String getKcConfigKey()
	{
		return kcConfigKey;
	}

	public static Optional<BrutusVariant> fromBossName(String name)
	{
		for (BrutusVariant variant : values())
		{
			if (variant.displayName.equals(name))
			{
				return Optional.of(variant);
			}
		}
		return Optional.empty();
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
