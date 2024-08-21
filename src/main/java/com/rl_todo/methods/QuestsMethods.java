package com.rl_todo.methods;

import com.rl_todo.Alternative;
import com.rl_todo.IdBuilder;
import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public class QuestsMethods
{
    static void AddAll(MethodManager aManager, TodoPlugin aPlugin)
    {
        aManager.AddDefaultMethod(IdBuilder.questId(Quest.IN_SEARCH_OF_KNOWLEDGE),
                new Method("Complete: " + Quest.IN_SEARCH_OF_KNOWLEDGE.getName(), "quest")
                        .takes(ItemID.TATTERED_SUN_PAGE, 4)
                        .takes(ItemID.TATTERED_MOON_PAGE, 4)
                        .takes(ItemID.TATTERED_TEMPLE_PAGE, 4)
                        .makes(Quest.IN_SEARCH_OF_KNOWLEDGE));

        aManager.AddDefaultMethod(IdBuilder.questId(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE),
                new Method("Complete: " + Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE.getName(), "quest")
                        // Levels
                        .requires(Skill.FIREMAKING, 75)
                        .requires(Skill.MAGIC, 75)
                        .requires(Skill.THIEVING, 70)
                        .requires(Skill.HERBLORE, 62)
                        .requires(Skill.RUNECRAFT, 60)
                        .requires(Skill.CONSTRUCTION, 60)

                        // Quests
                        .requires(Quest.DESERT_TREASURE_I)
                        .requires(Quest.SECRETS_OF_THE_NORTH)
                        .requires(Quest.ENAKHRAS_LAMENT)
                        .requires(Quest.TEMPLE_OF_THE_EYE)
                        .requires(Quest.THE_GARDEN_OF_DEATH)
                        .requires(Quest.BELOW_ICE_MOUNTAIN)
                        .requires(Quest.HIS_FAITHFUL_SERVANTS)

                        // Burst spells
                        .takes(ItemID.CHAOS_RUNE, 16)
                        .takes(ItemID.DEATH_RUNE, 8)
                        .takes(ItemID.BLOOD_RUNE, 2)
                        .takes(ItemID.SOUL_RUNE, 2)
                        .takes(ItemID.WATER_RUNE, 4)
                        .takes(ItemID.FIRE_RUNE, 2)
                        .takes(ItemID.AIR_RUNE, 3)

                        // Items needed
                        .requires(ItemID.RING_OF_VISIBILITY)
                        .requires(ItemID.TINDERBOX)
                        .requires(ItemID.PESTLE_AND_MORTAR)
                        .requires(Alternative.MINING_TOOL)

                        .makes(Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE));
    }
}
