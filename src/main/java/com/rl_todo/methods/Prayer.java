package com.rl_todo.methods;

import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public class Prayer
{
    public static void AddAll(MethodManager aManager, TodoPlugin aPlugin)
    {
        class Bones
        {
            int myItem;
            double myBaseXP;

            Bones(int aItem, double aBaseXP)
            {
                myItem = aItem;
                myBaseXP = aBaseXP;
            }
        }

        for(Bones bones : new Bones[]{
            new Bones(ItemID.BONES, 4.5),
            new Bones(ItemID.WOLF_BONES, 4.5),
            new Bones(ItemID.BURNT_BONES, 4.5),
            new Bones(ItemID.MONKEY_BONES, 5),
            new Bones(ItemID.BAT_BONES, 5.3),
            new Bones(ItemID.BIG_BONES, 15),
            new Bones(ItemID.JOGRE_BONES, 15),
            new Bones(ItemID.ZOGRE_BONE, 22.5),
            new Bones(ItemID.SHAIKAHAN_BONES, 25),
            new Bones(ItemID.BABY_DRAGON_BONE, 30),
            new Bones(ItemID.WYRM_BONES, 50),
            new Bones(ItemID.WYVERN_BONES, 72),
            new Bones(ItemID.DRAGON_BONES, 72),
            new Bones(ItemID.DRAKE_BONES, 80),
            new Bones(ItemID.FAYRG_BONES, 84),
            new Bones(ItemID.LAVA_DRAGON_BONES, 85),
            new Bones(ItemID.RAURG_BONES, 96),
            new Bones(ItemID.HYDRA_BONES, 110),
            new Bones(ItemID.DAGANNOTH_BONES, 125),
            new Bones(ItemID.OURG_BONES, 140)
        })
        {
            aManager.AddMethod(new Method("Bury " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName(), "prayer/bury")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP)
                    .build());

            aManager.AddMethod(new Method("Sinister offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName(), "prayer/sinister offer")
                    .takes(bones.myItem, 3)
                    .takes(ItemID.BLOOD_RUNE)
                    .takes(ItemID.WRATH_RUNE)
                    .makes(Skill.PRAYER, bones.myBaseXP * 9.0)
                    .makes(Skill.MAGIC, 180)
                    .requires(Skill.MAGIC, 92)
                    .requires(Quest.A_KINGDOM_DIVIDED)
                    .build());

            aManager.AddMethod(new Method("Burn " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at sacred bone burner", "prayer/burn")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 3)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at gilded altar", "prayer/gilded")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 3.5)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at chaos altar", "prayer/chaos")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 7)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at ectofuntus", "prayer/ectofuntus")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 4)
                    .build());
        }
        {
            // requires lvl 70
            Bones bones = new Bones(ItemID.SUPERIOR_DRAGON_BONES, 150);
            aManager.AddMethod(new Method("Bury " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName(), "prayer/bury")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP)
                    .requires(Skill.PRAYER, 70)
                    .build());

            aManager.AddMethod(new Method("Sinister offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName(), "prayer/sinister offer")
                    .takes(bones.myItem, 3)
                    .takes(ItemID.BLOOD_RUNE)
                    .takes(ItemID.WRATH_RUNE)
                    .makes(Skill.PRAYER, bones.myBaseXP * 9.0)
                    .makes(Skill.MAGIC, 180)
                    .requires(Skill.MAGIC, 92)
                    .requires(Quest.A_KINGDOM_DIVIDED)
                    .requires(Skill.PRAYER, 70)
                    .build());

            aManager.AddMethod(new Method("Burn " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at sacred bone burner", "prayer/burn")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 3)
                    .requires(Skill.PRAYER, 70)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at gilded altar", "prayer/gilded")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 3.5)
                    .requires(Skill.PRAYER, 70)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at chaos altar", "prayer/chaos")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 7)
                    .requires(Skill.PRAYER, 70)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at ectofuntus", "prayer/ectofuntus")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 4)
                    .requires(Skill.PRAYER, 70)
                    .build());
        }

        for(Bones bones : new Bones[]{
                new Bones(ItemID.BONES_3187, 3),
                new Bones(ItemID.SMALL_ZOMBIE_MONKEY_BONES, 5),
                new Bones(ItemID.LARGE_ZOMBIE_MONKEY_BONES, 5),
                new Bones(ItemID.SMALL_NINJA_MONKEY_BONES, 16),
                new Bones(ItemID.MEDIUM_NINJA_MONKEY_BONES, 18),
                new Bones(ItemID.GORILLA_BONES, 18),
                new Bones(ItemID.BEARDED_GORILLA_BONES, 18)
        })
        {
            aManager.AddMethod(new Method("Bury " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName(), "prayer/bury")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP)
                    .requires(Quest.MONKEY_MADNESS_I)
                    .build());

            aManager.AddMethod(new Method("Sinister offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName(), "prayer/sinister offer")
                    .takes(bones.myItem, 3)
                    .takes(ItemID.BLOOD_RUNE)
                    .takes(ItemID.WRATH_RUNE)
                    .makes(Skill.PRAYER, bones.myBaseXP * 9.0)
                    .makes(Skill.MAGIC, 180)
                    .requires(Skill.MAGIC, 92)
                    .requires(Quest.A_KINGDOM_DIVIDED)
                    .requires(Quest.MONKEY_MADNESS_I)
                    .build());

            aManager.AddMethod(new Method("Burn " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at sacred bone burner", "prayer/burn")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 3)
                    .requires(Quest.MONKEY_MADNESS_I)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at gilded altar", "prayer/gilded")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 3.5)
                    .requires(Quest.MONKEY_MADNESS_I)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at chaos altar", "prayer/chaos")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 7)
                    .requires(Quest.MONKEY_MADNESS_I)
                    .build());

            aManager.AddMethod(new Method("offer " + aPlugin.myItemManager.getItemComposition(bones.myItem).getName() + " at ectofuntus", "prayer/ectofuntus")
                    .takes(bones.myItem)
                    .makes(Skill.PRAYER, bones.myBaseXP * 4)
                    .requires(Quest.MONKEY_MADNESS_I)
                    .build());
        }

        class Ashes
        {
            int myItem;
            double myBaseXP;

            Ashes(int aItem, double aBaseXP)
            {
                myItem = aItem;
                myBaseXP = aBaseXP;
            }
        }

        for (Ashes ashes : new Ashes[]{
            new Ashes(ItemID.FIENDISH_ASHES, 10),
            new Ashes(ItemID.VILE_ASHES, 25),
            new Ashes(ItemID.MALICIOUS_ASHES, 65),
            new Ashes(ItemID.ABYSSAL_ASHES, 85),
            new Ashes(ItemID.INFERNAL_ASHES, 110)
        })
        {
            aManager.AddMethod(new Method("Scatter " + aPlugin.myItemManager.getItemComposition(ashes.myItem).getName(), "prayer/scatter")
                    .takes(ashes.myItem)
                    .makes(Skill.PRAYER, ashes.myBaseXP)
                    .build());

            aManager.AddMethod(new Method("Demonic offer " + aPlugin.myItemManager.getItemComposition(ashes.myItem).getName(), "prayer/scatter")
                    .takes(ashes.myItem, 3)
                    .takes(ItemID.SOUL_RUNE)
                    .takes(ItemID.WRATH_RUNE)
                    .makes(Skill.PRAYER, ashes.myBaseXP * 3)
                    .makes(Skill.MAGIC, 175)
                    .requires(Skill.MAGIC, 84)
                    .build());
        }
    }
}
