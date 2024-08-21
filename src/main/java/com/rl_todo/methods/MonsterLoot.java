package com.rl_todo.methods;

import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;

public class MonsterLoot
{
    public static void AddAll(MethodManager aManager, TodoPlugin aPlugin)
    {
        aManager.AddMethod(new Method("Glough's experiments", "kill")
                .makes(ItemID.ZENYTE_SHARD, 1/300.0)
                .makes(ItemID.BALLISTA_LIMBS, 1/500.0)
                .makes(ItemID.BALLISTA_SPRING, 1/500.0)
                .makes(ItemID.LIGHT_FRAME, 1/750.0)
                .makes(ItemID.HEAVY_FRAME, 1/1500.0)
                .makes(ItemID.MONKEY_TAIL, 1/1500.0)

                .requires(Quest.MONKEY_MADNESS_II)
                .build());
    }
}
