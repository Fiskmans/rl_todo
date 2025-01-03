package com.rl_todo;

import net.runelite.api.ItemID;

import java.util.Optional;

public enum Alternative
{
    MINING_TOOL("Mining tool", "mining_tool",
            ItemID.BRONZE_PICKAXE,
            ItemID.IRON_PICKAXE,
            ItemID.STEEL_PICKAXE,
            ItemID.BLACK_PICKAXE,
            ItemID.MITHRIL_PICKAXE,
            ItemID.ADAMANT_PICKAXE,
            ItemID.RUNE_PICKAXE,
            ItemID.DRAGON_PICKAXE,
            ItemID.CRYSTAL_PICKAXE,
            ItemID.GILDED_PICKAXE,
            ItemID._3RD_AGE_PICKAXE,
            ItemID.DRAGON_PICKAXE_12797,
            ItemID.DRAGON_PICKAXE_OR,
            ItemID.DRAGON_PICKAXE_OR_25376,
            ItemID.INFERNAL_PICKAXE,
            ItemID.INFERNAL_PICKAXE_OR),
    CHOPPING_TOOL("Chopping tool", "chopping_tool",
            ItemID.BRONZE_AXE,
            ItemID.IRON_AXE,
            ItemID.STEEL_AXE,
            ItemID.BLACK_AXE,
            ItemID.MITHRIL_AXE,
            ItemID.ADAMANT_AXE,
            ItemID.RUNE_AXE,
            ItemID.DRAGON_AXE,
            ItemID.CRYSTAL_AXE,
            ItemID.GILDED_AXE,
            ItemID._3RD_AGE_AXE,
            ItemID.DRAGON_AXE_OR,
            ItemID.DRAGON_AXE_OR_30352,
            ItemID.INFERNAL_AXE,
            ItemID.INFERNAL_AXE_OR),
    IBANS_STAFF("Ibans staff", "ibans_staff",
            ItemID.IBANS_STAFF,
            ItemID.IBANS_STAFF_U,
            ItemID.IBANS_STAFF_1410),
    MAGIC_DART_CASTER("Magic dart staff", "magic_dart_staff",
            ItemID.SLAYERS_STAFF,
            ItemID.SLAYERS_STAFF_E,
            ItemID.STAFF_OF_THE_DEAD,
            ItemID.STAFF_OF_THE_DEAD_23613,
            ItemID.TOXIC_STAFF_OF_THE_DEAD,
            ItemID.STAFF_OF_LIGHT,
            ItemID.STAFF_OF_BALANCE),
    HAMMER("Hammer", "hammer",
            ItemID.HAMMER,
            ItemID.IMCANDO_HAMMER),
    NAILS("Nails", "nails",
            ItemID.BRONZE_NAILS,
            ItemID.IRON_NAILS,
            ItemID.STEEL_NAILS,
            ItemID.BLACK_NAILS,
            ItemID.MITHRIL_NAILS,
            ItemID.ADAMANTITE_NAILS,
            ItemID.RUNE_NAILS);

    private final String myName;
    private final String myId;
    private final int[] myAlternatives;

    Alternative(String name, String aId, int... aAlternatives)
    {
        this.myName = name;
        this.myId = aId;
        this.myAlternatives = aAlternatives;
    }


    public static Optional<Alternative> FromID(String aId)
    {
        for (Alternative alt : values())
        {
            if (alt.myId.equals(aId))
            {
                return Optional.of(alt);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the name of the skill.
     *
     * @return the skill name
     */
    public String getName()
    {
        return myName;
    }
    public String getId()
    {
        return myId;
    }
    public int[] getAlternatives()
    {
        return myAlternatives;
    }
}
