package com.rl_todo.methods;

import com.rl_todo.Alternative;
import com.rl_todo.IdBuilder;
import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public class Crafting
{
    static void AddAll(MethodManager aManager, TodoPlugin aPlugin)
    {

        // spinning wheel
        {
            class Spinnable
            {
                int myItem;
                int myResult;
                int myLevel;
                int myXp;
                public Spinnable(int aItem, int aResult,int aLevel, int aXP)
                {
                    myItem = aItem;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXp = aXP;
                }
            }


            for (Spinnable spin : new Spinnable[]{
                    new Spinnable(ItemID.WOOL, ItemID.BALL_OF_WOOL, 1, 250),
                    new Spinnable(ItemID.FLAX, ItemID.BOW_STRING, 10, 1500),
                    new Spinnable(ItemID.SINEW, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.OAK_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.WILLOW_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.MAPLE_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.YEW_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.MAGIC_ROOTS, ItemID.MAGIC_STRING, 19, 3000),
                    new Spinnable(ItemID.HAIR, ItemID.ROPE, 30, 2500),

            })
            {
                aManager.AddMethod(new Method("Spin " + aPlugin.myItemManager.getItemComposition(spin.myItem).getName(), "crafting/spinning")
                        .takes(spin.myItem)
                        .makes(spin.myResult)
                        .makes(Skill.CRAFTING, spin.myXp)
                        .requires(Skill.CRAFTING, spin.myLevel)
                        .build());
            }
        }

        // Loom
        {
            class Weaveable
            {
                int myItem;
                int myItemAmount;
                int myResult;
                int myLevel;
                int myXp;
                public Weaveable(int aItem, int aItemAmount, int aResult,int aLevel, int aXP)
                {
                    myItem = aItem;
                    myItemAmount = aItemAmount;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXp = aXP;
                }
            }

            for (Weaveable weave : new Weaveable[]{
                    new Weaveable(ItemID.BALL_OF_WOOL, 2, ItemID.STRIP_OF_CLOTH, 10, 1200),
                    new Weaveable(ItemID.JUTE_FIBRE, 4, ItemID.STRIP_OF_CLOTH, 21, 3800),
                    new Weaveable(ItemID.JUTE_FIBRE, 2, ItemID.DRIFT_NET, 26, 5500),
                    new Weaveable(ItemID.BASKET, 2, ItemID.STRIP_OF_CLOTH, 36, 5600)
            })
            {
                aManager.AddMethod(new Method("Weave " + aPlugin.myItemManager.getItemComposition(weave.myItem).getName() + " into " + aPlugin.myItemManager.getItemComposition(weave.myResult).getName(), "crafting/weaving")
                        .takes(weave.myItem, weave.myItemAmount)
                        .makes(weave.myResult)
                        .makes(Skill.CRAFTING, weave.myXp)
                        .requires(Skill.CRAFTING, weave.myLevel)
                        .build());
            }
        }

        // Pottery wheel
        {
            class Pottery
            {
                int myIntermediate;
                int myResult;
                int myLevel;
                int myFirstXp;
                int mySecondXp;
                public Pottery(int aIntermediate, int aResult, int aLevel, int aFirstXP, int aSecondXP)
                {
                    myIntermediate = aIntermediate;
                    myResult = aResult;
                    myLevel = aLevel;
                    myFirstXp = aFirstXP;
                    mySecondXp = aSecondXP;
                }
            }

            for (Pottery pot : new Pottery[]{
                    new Pottery(ItemID.UNFIRED_POT, ItemID.POT, 1, 630, 630),
                    new Pottery(ItemID.UNFIRED_PIE_DISH, ItemID.PIE_DISH, 7, 1500, 1000),
                    new Pottery(ItemID.UNFIRED_BOWL, ItemID.BOWL, 8, 1800, 1500),
                    new Pottery(ItemID.UNFIRED_PLANT_POT, ItemID.PLANT_POT, 19, 2000, 1750)
            })
            {
                aManager.AddMethod(new Method("Shape Soft clay into " + aPlugin.myItemManager.getItemComposition(pot.myIntermediate).getName(), "crafting/clay")
                        .takes(ItemID.SOFT_CLAY)
                        .makes(pot.myIntermediate)
                        .makes(Skill.CRAFTING, pot.myFirstXp)
                        .requires(Skill.CRAFTING, pot.myLevel)
                        .build());

                aManager.AddMethod(new Method("Fire " + aPlugin.myItemManager.getItemComposition(pot.myIntermediate).getName(), "crafting/clay")
                        .takes(pot.myIntermediate)
                        .makes(pot.myResult)
                        .makes(Skill.CRAFTING, pot.mySecondXp)
                        .requires(Skill.CRAFTING, pot.myLevel)
                        .build());
            }

            {
                aManager.AddMethod(new Method("Shape Soft clay into " + aPlugin.myItemManager.getItemComposition(ItemID.UNFIRED_POT_LID).getName(), "crafting/clay")
                        .takes(ItemID.SOFT_CLAY)
                        .makes(ItemID.UNFIRED_POT_LID)
                        .makes(Skill.CRAFTING, 2000)
                        .requires(Skill.CRAFTING, 25)
                        .requires(Quest.ONE_SMALL_FAVOUR)
                        .build());

                aManager.AddMethod(new Method("Fire " + aPlugin.myItemManager.getItemComposition(ItemID.UNFIRED_POT_LID).getName(), "crafting/clay")
                        .takes(ItemID.UNFIRED_POT_LID)
                        .makes(ItemID.POT_LID)
                        .makes(Skill.CRAFTING, 2000)
                        .requires(Skill.CRAFTING, 25)
                        .requires(Quest.ONE_SMALL_FAVOUR)
                        .build());
            }
        }

        // Sewing
        {
            class Sewable
            {
                int myItem;
                int myItemsNeeded;
                int myResult;
                int myLevel;
                int myXP;
                public Sewable(int aItem, int aItemsNeeded, int aResult, int aLevel, int aXP)
                {
                    myItem = aItem;
                    myItemsNeeded = aItemsNeeded;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for (Sewable sew : new Sewable[]{
                    new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_GLOVES, 1, 1380),
                    new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_BOOTS, 7, 1620),
                    new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_COWL, 9, 1850),
                    new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_VAMBRACES, 14, 2200),
                    new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_BODY, 11, 2500),
                    new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_CHAPS, 18, 2700),
                    new Sewable(ItemID.HARD_LEATHER, 1, ItemID.LEATHER_GLOVES, 28, 3500),
                    new Sewable(ItemID.LEATHER, 1, ItemID.COIF, 38, 3700),

                    new Sewable(ItemID.GREEN_DRAGON_LEATHER, 1, ItemID.GREEN_DHIDE_VAMBRACES, 57, 6200),
                    new Sewable(ItemID.GREEN_DRAGON_LEATHER, 2, ItemID.GREEN_DHIDE_CHAPS, 60, 12400),
                    new Sewable(ItemID.GREEN_DRAGON_LEATHER, 3, ItemID.GREEN_DHIDE_BODY, 62, 18600),

                    new Sewable(ItemID.BLUE_DRAGON_LEATHER, 1, ItemID.BLUE_DHIDE_VAMBRACES, 66, 7000),
                    new Sewable(ItemID.BLUE_DRAGON_LEATHER, 2, ItemID.BLUE_DHIDE_CHAPS, 68, 14000),
                    new Sewable(ItemID.BLUE_DRAGON_LEATHER, 3, ItemID.BLUE_DHIDE_BODY, 71, 21000),

                    new Sewable(ItemID.RED_DRAGON_LEATHER, 1, ItemID.RED_DHIDE_VAMBRACES, 73, 7800),
                    new Sewable(ItemID.RED_DRAGON_LEATHER, 2, ItemID.RED_DHIDE_CHAPS, 75, 15600),
                    new Sewable(ItemID.RED_DRAGON_LEATHER, 3, ItemID.RED_DHIDE_BODY, 77, 23400),

                    new Sewable(ItemID.BLACK_DRAGON_LEATHER, 1, ItemID.BLACK_DHIDE_VAMBRACES, 79, 8600),
                    new Sewable(ItemID.BLACK_DRAGON_LEATHER, 2, ItemID.BLACK_DHIDE_CHAPS, 82, 17200),
                    new Sewable(ItemID.BLACK_DRAGON_LEATHER, 3, ItemID.BLACK_DHIDE_BODY, 84, 25800),

                    new Sewable(ItemID.SNAKESKIN, 6, ItemID.SNAKESKIN_BOOTS, 45, 3000),
                    new Sewable(ItemID.SNAKESKIN, 8, ItemID.SNAKESKIN_VAMBRACES, 47, 3500),
                    new Sewable(ItemID.SNAKESKIN, 5, ItemID.SNAKESKIN_BANDANA, 48, 4500),
                    new Sewable(ItemID.SNAKESKIN, 12, ItemID.SNAKESKIN_CHAPS, 51, 5000),
                    new Sewable(ItemID.SNAKESKIN, 15, ItemID.SNAKESKIN_BODY, 53, 5500),

                    new Sewable(ItemID.XERICIAN_FABRIC, 3, ItemID.XERICIAN_HAT, 14, 6600),
                    new Sewable(ItemID.XERICIAN_FABRIC, 4, ItemID.XERICIAN_ROBE, 17, 8800),
                    new Sewable(ItemID.XERICIAN_FABRIC, 5, ItemID.XERICIAN_TOP, 22, 11000),
            })
            {
                aManager.AddMethod(new Method("Sew " + aPlugin.myItemManager.getItemComposition(sew.myItem).getName() + " into " + aPlugin.myItemManager.getItemComposition(sew.myResult).getName(), "crafting/sewing")
                        .takes(sew.myItem, sew.myItemsNeeded)
                        .takes(ItemID.THREAD, 0.2)
                        .makes(sew.myResult)
                        .makes(Skill.CRAFTING, sew.myXP)
                        .requires(Skill.CRAFTING, sew.myLevel)
                        .requires(ItemID.NEEDLE)
                        .build());
            }
            {
                aManager.AddMethod(new Method("Sew " + aPlugin.myItemManager.getItemComposition(ItemID.CURED_YAKHIDE).getName() + " into " + aPlugin.myItemManager.getItemComposition(ItemID.YAKHIDE_ARMOUR).getName() + " (top)", "crafting/sewing")
                        .takes(ItemID.CURED_YAKHIDE, 2)
                        .takes(ItemID.THREAD, 0.2)
                        .makes(ItemID.YAKHIDE_ARMOUR)
                        .makes(Skill.CRAFTING, 3200)
                        .requires(Skill.CRAFTING, 46)
                        .requires(ItemID.NEEDLE)
                        .build());
            }
            {
                aManager.AddMethod(new Method("Sew " + aPlugin.myItemManager.getItemComposition(ItemID.CURED_YAKHIDE).getName() + " into " + aPlugin.myItemManager.getItemComposition(ItemID.YAKHIDE_ARMOUR).getName() + " (legs)", "crafting/sewing")
                        .takes(ItemID.CURED_YAKHIDE)
                        .takes(ItemID.THREAD, 0.2)
                        .makes(ItemID.YAKHIDE_ARMOUR_10824)
                        .makes(Skill.CRAFTING, 3200)
                        .requires(Skill.CRAFTING, 43)
                        .requires(ItemID.NEEDLE)
                        .build());
            }


        }

        // Shields
        {
            class Shield
            {
                int myItem;
                int myNailType;
                int myHideType;
                int myResult;
                int myLevel;
                int myXP;
                public Shield(int aItem, int aNailType, int aHideType, int aResult, int aLevel, int aXP)
                {
                    myItem = aItem;
                    myNailType = aNailType;
                    myHideType = aHideType;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Shield shield : new Shield[]{
                    new Shield(ItemID.OAK_SHIELD, ItemID.BRONZE_NAILS, ItemID.HARD_LEATHER, ItemID.HARD_LEATHER_SHIELD, 41, 7000),
                    new Shield(ItemID.MAPLE_SHIELD, ItemID.STEEL_NAILS, ItemID.GREEN_DRAGON_LEATHER, ItemID.GREEN_DHIDE_SHIELD, 62, 12400),
                    new Shield(ItemID.YEW_SHIELD, ItemID.MITHRIL_NAILS, ItemID.BLUE_DRAGON_LEATHER, ItemID.BLUE_DHIDE_SHIELD, 69, 14000),
                    new Shield(ItemID.MAGIC_SHIELD, ItemID.ADAMANTITE_NAILS, ItemID.RED_DRAGON_LEATHER, ItemID.RED_DHIDE_SHIELD, 76, 15600),
                    new Shield(ItemID.REDWOOD_SHIELD, ItemID.RUNE_NAILS, ItemID.BLACK_DRAGON_LEATHER, ItemID.BLACK_DHIDE_SHIELD, 83, 17200),
                    new Shield(ItemID.WILLOW_SHIELD, ItemID.IRON_NAILS, ItemID.SNAKESKIN, ItemID.SNAKESKIN_SHIELD, 56, 10000)
            })
            {
                aManager.AddMethod(new Method("Make " + aPlugin.myItemManager.getItemComposition(shield.myItem).getName() + " into " + aPlugin.myItemManager.getItemComposition(shield.myResult).getName(), "crafting/misc")
                        .takes(shield.myItem)
                        .takes(shield.myHideType, 2)
                        .takes(shield.myNailType, 15)
                        .makes(shield.myResult)
                        .makes(Skill.CRAFTING, shield.myXP)
                        .requires(Skill.CRAFTING, shield.myLevel)
                        .requires(ItemID.HAMMER)
                        .build());
            }

            class BroodooShield
            {
                int myItem;
                int myResult;
                int myLevel;
                int myXP;
                String mySuffix;
                public BroodooShield(int aItem, int aResult, int aLevel, int aXP, String aSuffix)
                {
                    myItem = aItem;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                    mySuffix = aSuffix;
                }
            }

            for(BroodooShield shield : new BroodooShield[]{
                    new BroodooShield(ItemID.TRIBAL_MASK,ItemID.BROODOO_SHIELD_10, 35, 100000, " (combat)"),
                    new BroodooShield(ItemID.TRIBAL_MASK_6337, ItemID.BROODOO_SHIELD_10_6237, 35, 100000, " (disease)"),
                    new BroodooShield(ItemID.TRIBAL_MASK_6339, ItemID.BROODOO_SHIELD_10_6259, 35, 100000, " (poison)"),
            })
            {
                aManager.AddMethod(new Method("Make " + aPlugin.myItemManager.getItemComposition(shield.myItem).getName() + shield.mySuffix + " into " + aPlugin.myItemManager.getItemComposition(shield.myResult).getName() + shield.mySuffix, "crafting/misc")
                        .takes(shield.myItem)
                        .takes(ItemID.SNAKESKIN, 2)
                        .takes(Alternative.NAILS, 8)
                        .makes(shield.myResult)
                        .makes(Skill.CRAFTING, shield.myXP)
                        .requires(Skill.CRAFTING, shield.myLevel)
                        .requires(ItemID.HAMMER)
                        .build());
            }

        }

        // Combining
        {
            class Combinable
            {
                int myFirstItem;
                int mySecondItem;
                int myResult;
                int myLevel;
                int myXP;
                public Combinable(int aFirstItem, int aSecondItem, int aResult, int aLevel, int aXP)
                {
                    myFirstItem = aFirstItem;
                    mySecondItem = aSecondItem;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for (Combinable combine : new Combinable[]{
                    new Combinable(ItemID.STEEL_STUDS, ItemID.LEATHER_BODY, ItemID.STUDDED_BODY, 41, 4000),
                    new Combinable(ItemID.STEEL_STUDS, ItemID.LEATHER_VAMBRACES, ItemID.STUDDED_CHAPS, 44, 4200),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.LEATHER_VAMBRACES, ItemID.SPIKY_VAMBRACES, 32, 600),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.RED_DHIDE_VAMBRACES, ItemID.RED_SPIKY_VAMBRACES, 32, 580),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.GREEN_DHIDE_VAMBRACES, ItemID.GREEN_SPIKY_VAMBRACES, 32, 500),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.BLUE_DHIDE_VAMBRACES, ItemID.BLUE_SPIKY_VAMBRACES, 32, 600),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.BLACK_DHIDE_VAMBRACES, ItemID.BLACK_SPIKY_VAMBRACES, 32, 600),
                    new Combinable(ItemID.EMPTY_OIL_LAMP, ItemID.OIL_LANTERN_FRAME, ItemID.OIL_LANTERN, 26, 5000),
                    new Combinable(ItemID.EMPTY_LIGHT_ORB, ItemID.CAVE_GOBLIN_WIRE, ItemID.LIGHT_ORB, 87, 10400),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.WATER_ORB, ItemID.WATER_BATTLESTAFF, 54, 10000),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.EARTH_ORB, ItemID.EARTH_BATTLESTAFF, 58, 11250),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.FIRE_ORB, ItemID.FIRE_BATTLESTAFF, 62, 12500),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.AIR_ORB, ItemID.AIR_BATTLESTAFF, 66, 13750),
            })
            {
                aManager.AddMethod(new Method("Combine " + aPlugin.myItemManager.getItemComposition(combine.myFirstItem).getName() + " with " + aPlugin.myItemManager.getItemComposition(combine.mySecondItem).getName(), "crafting/misc")
                        .takes(combine.myFirstItem)
                        .takes(combine.mySecondItem)
                        .makes(combine.myResult)
                        .makes(Skill.CRAFTING, combine.myXP)
                        .requires(Skill.CRAFTING, combine.myLevel)
                        .build());
            }
        }

        // Chisel
        {
            class Chiselable
            {
                int myItem;
                int myResult;
                int myAmount;
                int myLevel;
                int myXP;
                String mySuffix;
                public Chiselable(int aItem, int aResult, int aAmount, int aLevel, int aXP, String aSuffix)
                {
                    myItem = aItem;
                    myResult = aResult;
                    myAmount = aAmount;
                    myLevel = aLevel;
                    myXP = aXP;
                    mySuffix = aSuffix;
                }
            }

            for(Chiselable chisel : new Chiselable[]{
                    new Chiselable(ItemID.BLOODNTAR_SNELM, ItemID.BLAMISH_RED_SHELL, 1, 15, 3250, " (round)"),
                    new Chiselable(ItemID.BLOODNTAR_SNELM_3339, ItemID.BLAMISH_RED_SHELL_3357, 1, 15, 3250, " (pointed)"),
                    new Chiselable(ItemID.BROKEN_BARK_SNELM, ItemID.BLAMISH_BARK_SHELL, 1, 15, 3250, ""),
                    new Chiselable(ItemID.BRUISE_BLUE_SNELM, ItemID.BLAMISH_BLUE_SHELL, 1, 15, 3250, " (round)"),
                    new Chiselable(ItemID.BRUISE_BLUE_SNELM_3343, ItemID.BLAMISH_BLUE_SHELL_3361, 1, 15, 3250, " (pointed)"),
                    new Chiselable(ItemID.MYRE_SNELM, ItemID.BLAMISH_MYRE_SHELL, 1, 15, 3250, " (round)"),
                    new Chiselable(ItemID.MYRE_SNELM_3337, ItemID.BLAMISH_MYRE_SHELL_3355, 1, 15, 3250, " (pointed)"),
                    new Chiselable(ItemID.OCHRE_SNELM, ItemID.BLAMISH_OCHRE_SHELL, 1, 15, 3250, " (round)"),
                    new Chiselable(ItemID.OCHRE_SNELM_3341, ItemID.BLAMISH_OCHRE_SHELL_3359, 1, 15, 3250, " (pointed)"),


                    new Chiselable(ItemID.FRESH_CRAB_SHELL, ItemID.CRAB_HELMET, 1, 15, 3250, ""),
                    new Chiselable(ItemID.FRESH_CRAB_CLAW, ItemID.CRAB_CLAW, 1, 15, 3250, ""),

                    new Chiselable(ItemID.UNCUT_OPAL, ItemID.OPAL, 1, 1, 1500, ""),
                    new Chiselable(ItemID.UNCUT_JADE, ItemID.JADE, 1, 13, 2000, ""),
                    new Chiselable(ItemID.UNCUT_RED_TOPAZ, ItemID.RED_TOPAZ, 1, 16, 2500, ""),
                    new Chiselable(ItemID.UNCUT_SAPPHIRE, ItemID.SAPPHIRE, 1, 20, 5000, ""),
                    new Chiselable(ItemID.UNCUT_EMERALD, ItemID.EMERALD, 1, 27, 6750, ""),
                    new Chiselable(ItemID.UNCUT_RUBY, ItemID.RUBY, 1, 34, 8500, ""),
                    new Chiselable(ItemID.UNCUT_DIAMOND, ItemID.DIAMOND, 1, 43, 10750, ""),
                    new Chiselable(ItemID.UNCUT_DRAGONSTONE, ItemID.DRAGONSTONE, 1, 55, 13750, ""),
                    new Chiselable(ItemID.UNCUT_ONYX, ItemID.ONYX, 1, 67, 16750, ""),
                    new Chiselable(ItemID.UNCUT_ZENYTE, ItemID.ZENYTE, 1, 89, 20000, ""),

                    new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_BOLT_TIPS, 15, 83, 6000, " into Bolt tips"),
                    new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_ARROWTIPS, 15, 85, 6000, " into Arrow tips"),
                    new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_JAVELIN_HEADS, 5, 87, 6000, " into Javelin heads"),
                    new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_DART_TIP, 8, 89, 6000, " into Dart tips")
            })
            {
                aManager.AddMethod(new Method("Chisel " + aPlugin.myItemManager.getItemComposition(chisel.myItem).getName() + chisel.mySuffix, "crafting/chissel")
                        .takes(chisel.myItem)
                        .makes(chisel.myResult, chisel.myAmount)
                        .makes(Skill.CRAFTING, chisel.myXP)
                        .requires(Skill.CRAFTING, chisel.myLevel)
                        .requires(ItemID.CHISEL)
                        .build());
            }

        }

        // Splitbark
        {
            class Splitbark
            {
                int myAmount;
                int myResult;
                int myLevel;
                int myXP;
                public Splitbark(int aAmount, int aResult, int aLevel, int aXP)
                {
                    myAmount = aAmount;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for (Splitbark bark : new Splitbark[]{
                    new Splitbark(1, ItemID.SPLITBARK_GAUNTLETS, 60, 6200),
                    new Splitbark(1, ItemID.SPLITBARK_BOOTS, 60, 6200),
                    new Splitbark(2, ItemID.SPLITBARK_HELM, 61, 12400),
                    new Splitbark(3, ItemID.SPLITBARK_LEGS, 62, 18600),
                    new Splitbark(4, ItemID.SPLITBARK_BODY, 62, 24800)
            })
            {
                aManager.AddMethod(new Method("Sew " + aPlugin.myItemManager.getItemComposition(ItemID.FINE_CLOTH).getName() + " into " + aPlugin.myItemManager.getItemComposition(bark.myResult).getName(), "crafting/sewing")
                        .takes(ItemID.FINE_CLOTH, bark.myAmount)
                        .takes(ItemID.BARK, bark.myAmount)
                        .takes(ItemID.THREAD, 0.2)
                        .makes(bark.myResult)
                        .makes(Skill.CRAFTING, bark.myXP)
                        .requires(Skill.CRAFTING, bark.myLevel)
                        .requires(ItemID.NEEDLE)
                        .build());
            }
        }

        // Glassblowing
        {
            class Blowable
            {
                int myResult;
                int myLevel;
                int myXP;
                public Blowable(int aResult, int aLevel, int aXP)
                {
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Blowable blow : new Blowable[]{
                    new Blowable(ItemID.BEER_GLASS, 1, 1750),
                    new Blowable(ItemID.EMPTY_CANDLE_LANTERN, 4, 1900),
                    new Blowable(ItemID.EMPTY_OIL_LAMP, 12, 2500),
                    new Blowable(ItemID.VIAL, 33, 3500),
                    new Blowable(ItemID.FISHBOWL, 42, 4250),
                    new Blowable(ItemID.UNPOWERED_ORB, 46, 5250),
                    new Blowable(ItemID.LANTERN_LENS, 49, 5500),
                    new Blowable(ItemID.EMPTY_LIGHT_ORB, 87, 7000)
            })
            {
                aManager.AddMethod(new Method("Blow " + aPlugin.myItemManager.getItemComposition(ItemID.MOLTEN_GLASS).getName() + " into " + aPlugin.myItemManager.getItemComposition(blow.myResult).getName(), "crafting/glass")
                        .takes(ItemID.MOLTEN_GLASS)
                        .makes(blow.myResult)
                        .makes(Skill.CRAFTING, blow.myXP)
                        .requires(Skill.CRAFTING, blow.myLevel)
                        .requires(ItemID.GLASSBLOWING_PIPE)
                        .build());
            }
        }

        // Jewelery
        {
            class Jewelery
            {
                int myMetal;
                int myGem;
                int myMould;
                int myResult;
                int myLevel;
                int myXP;

                public Jewelery(int aMetal, int aGem, int aMould, int aResult, int aLevel, int aXP)
                {
                    myMetal = aMetal;
                    myGem = aGem;
                    myMould = aMould;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Jewelery jewel : new Jewelery[]{
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.RING_MOULD, ItemID.OPAL_RING, 1, 1000),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.NECKLACE_MOULD, ItemID.OPAL_NECKLACE, 16, 3500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.BRACELET_MOULD, ItemID.OPAL_BRACELET, 22, 4500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.AMULET_MOULD, ItemID.OPAL_AMULET_U, 27, 5500),

                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.RING_MOULD, ItemID.JADE_RING, 13, 3200),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.NECKLACE_MOULD, ItemID.JADE_NECKLACE, 25, 5400),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.BRACELET_MOULD, ItemID.JADE_BRACELET, 29, 6000),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.AMULET_MOULD, ItemID.JADE_AMULET_U, 34, 7000),

                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.RING_MOULD, ItemID.TOPAZ_RING, 16, 3500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.NECKLACE_MOULD, ItemID.TOPAZ_NECKLACE, 32, 7000),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.BRACELET_MOULD, ItemID.TOPAZ_BRACELET, 38, 7500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.AMULET_MOULD, ItemID.TOPAZ_AMULET_U, 45, 8000),


                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.RING_MOULD, ItemID.SAPPHIRE_RING, 20, 4000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.NECKLACE_MOULD, ItemID.SAPPHIRE_NECKLACE, 22, 5500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.BRACELET_MOULD, ItemID.SAPPHIRE_BRACELET, 23, 6000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.AMULET_MOULD, ItemID.SAPPHIRE_AMULET_U, 24, 6500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.RING_MOULD, ItemID.EMERALD_RING, 27, 5500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.NECKLACE_MOULD, ItemID.EMERALD_NECKLACE, 29, 6000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.BRACELET_MOULD, ItemID.EMERALD_BRACELET, 30, 6500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.AMULET_MOULD, ItemID.EMERALD_AMULET_U, 31, 7000),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.RING_MOULD, ItemID.RUBY_RING, 34, 7000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.NECKLACE_MOULD, ItemID.RUBY_NECKLACE, 40, 7500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.BRACELET_MOULD, ItemID.RUBY_BRACELET, 42, 8000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.AMULET_MOULD, ItemID.RUBY_AMULET_U, 50, 8500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.RING_MOULD, ItemID.DIAMOND_RING, 43, 8500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.NECKLACE_MOULD, ItemID.DIAMOND_NECKLACE, 56, 9000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.BRACELET_MOULD, ItemID.DIAMOND_BRACELET, 58, 9500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.AMULET_MOULD, ItemID.DIAMOND_AMULET_U, 70, 10000),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.RING_MOULD, ItemID.DRAGONSTONE_RING, 55, 10000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.NECKLACE_MOULD, ItemID.DRAGON_NECKLACE, 72, 10500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.BRACELET_MOULD, ItemID.DRAGONSTONE_BRACELET, 74, 11000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.AMULET_MOULD, ItemID.DRAGONSTONE_AMULET_U, 80, 15000),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.ENCHANTED_GEM, ItemID.RING_MOULD, ItemID.SLAYER_RING_8, 75, 1500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ETERNAL_GEM, ItemID.RING_MOULD, ItemID.SLAYER_RING_ETERNAL, 75, 1500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.RING_MOULD, ItemID.ONYX_RING, 67, 11500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.NECKLACE_MOULD, ItemID.ONYX_NECKLACE, 82, 12000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.BRACELET_MOULD, ItemID.ONYX_BRACELET, 84, 12500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.AMULET_MOULD, ItemID.ONYX_AMULET_U, 90, 16500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.RING_MOULD, ItemID.ZENYTE_RING, 89, 15000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.NECKLACE_MOULD, ItemID.ZENYTE_NECKLACE, 92, 16500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.BRACELET_MOULD, ItemID.ZENYTE_BRACELET, 95, 18000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.AMULET_MOULD, ItemID.ZENYTE_AMULET_U, 98, 20000)
            })
            {
                aManager.AddMethod(new Method("Craft " + aPlugin.myItemManager.getItemComposition(jewel.myResult).getName(), "crafting/moulds")
                        .takes(jewel.myGem)
                        .takes(jewel.myMetal)
                        .makes(jewel.myResult)
                        .makes(Skill.CRAFTING, jewel.myXP)
                        .requires(Skill.CRAFTING, jewel.myLevel)
                        .requires(jewel.myMould)
                        .build());
            }
        }

        // Moulds
        {
            class Smeltable
            {
                int myMetal;
                int myMould;
                int myResult;
                int myAmount;
                int myLevel;
                int myXP;

                public Smeltable(int aMetal, int aMould, int aResult, int aAmount, int aLevel, int aXP)
                {
                    myMetal = aMetal;
                    myMould = aMould;
                    myResult = aResult;
                    myAmount = aAmount;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }
            for(Smeltable smelt : new Smeltable[]{
                    new Smeltable(ItemID.GOLD_BAR, ItemID.RING_MOULD, ItemID.GOLD_RING, 1, 5, 1500),
                    new Smeltable(ItemID.GOLD_BAR, ItemID.NECKLACE_MOULD, ItemID.GOLD_NECKLACE, 1, 6, 2000),
                    new Smeltable(ItemID.GOLD_BAR, ItemID.BRACELET_MOULD, ItemID.GOLD_BRACELET, 1, 5, 2500),
                    new Smeltable(ItemID.GOLD_BAR, ItemID.AMULET_MOULD, ItemID.GOLD_AMULET, 1, 7,3000),
                    new Smeltable(ItemID.GOLD_BAR, ItemID.TIARA_MOULD, ItemID.GOLD_TIARA, 1, 42, 3500),

                    new Smeltable(ItemID.SILVER_BAR, ItemID.SICKLE_MOULD, ItemID.SILVER_SICKLE, 1, 18, 5000),
                    new Smeltable(ItemID.SILVER_BAR, ItemID.HOLY_MOULD, ItemID.UNSTRUNG_SYMBOL, 1, 16, 5000),
                    new Smeltable(ItemID.SILVER_BAR, ItemID.UNHOLY_MOULD, ItemID.UNSTRUNG_EMBLEM, 1, 17, 5000),
                    new Smeltable(ItemID.SILVER_BAR, ItemID.TIARA_MOULD, ItemID.TIARA, 1, 23, 5250),

                    new Smeltable(ItemID.SILVER_BAR, ItemID.BOLT_MOULD, ItemID.SILVER_BOLTS, 10, 21, 5000),
            })
            {
                aManager.AddMethod(new Method("Craft " + aPlugin.myItemManager.getItemComposition(smelt.myResult).getName(), "crafting/moulds")
                        .takes(smelt.myMetal)
                        .makes(smelt.myResult)
                        .makes(Skill.CRAFTING, smelt.myXP)
                        .requires(Skill.CRAFTING, smelt.myLevel)
                        .requires(smelt.myMould)
                        .build());
            }
        }

        // Birdhouses
        {
            class Birdhouse
            {
                int myLog;
                int myResult;
                int myLevel;
                int myXP;

                public Birdhouse(int aLog, int aResult, int aLevel, int aXP)
                {
                    myLog = aLog;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Birdhouse birdhouse : new Birdhouse[]{
                    new Birdhouse(ItemID.LOGS, ItemID.BIRD_HOUSE, 5, 1500),
                    new Birdhouse(ItemID.OAK_LOGS, ItemID.OAK_BIRD_HOUSE, 15, 2000),
                    new Birdhouse(ItemID.WILLOW_LOGS, ItemID.WILLOW_BIRD_HOUSE, 25, 2500),
                    new Birdhouse(ItemID.TEAK_LOGS, ItemID.TEAK_BIRD_HOUSE, 35, 3000),
                    new Birdhouse(ItemID.MAPLE_LOGS, ItemID.MAPLE_BIRD_HOUSE, 45, 3500),
                    new Birdhouse(ItemID.MAHOGANY_LOGS, ItemID.MAHOGANY_BIRD_HOUSE, 50, 4000),
                    new Birdhouse(ItemID.YEW_LOGS, ItemID.YEW_BIRD_HOUSE, 60, 4500),
                    new Birdhouse(ItemID.MAGIC_LOGS, ItemID.MAGIC_BIRD_HOUSE, 75, 5000),
                    new Birdhouse(ItemID.REDWOOD_LOGS, ItemID.REDWOOD_BIRD_HOUSE, 90, 5500)
            })
            {
                aManager.AddMethod(new Method("Craft " + aPlugin.myItemManager.getItemComposition(birdhouse.myResult).getName(), "crafting/birdhouses")
                        .takes(birdhouse.myLog)
                        .takes(ItemID.CLOCKWORK)
                        .makes(birdhouse.myResult)
                        .makes(Skill.CRAFTING, birdhouse.myXP)
                        .requires(Skill.CRAFTING, birdhouse.myLevel)
                        .requires(ItemID.CHISEL)
                        .requires(Alternative.HAMMER)
                        .build());
            }
        }

        {
            aManager.AddMethod(new Method("Sing Celestial ring and Elven signet", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 100)
                    .takes(ItemID.STARDUST, 1000)
                    .takes(ItemID.CELESTIAL_RING)
                    .takes(ItemID.ELVEN_SIGNET)
                    .makes(ItemID.CELESTIAL_SIGNET)
                    .makes(Skill.CRAFTING, 500000)
                    .makes(Skill.SMITHING, 500000)
                    .requires(Skill.CRAFTING, 70)
                    .requires(Skill.SMITHING, 70)
                    .build());

            aManager.AddMethod(new Method("Sing One Crystal armour seed", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 50)
                    .takes(ItemID.CRYSTAL_ARMOUR_SEED)
                    .makes(ItemID.CRYSTAL_HELM)
                    .makes(Skill.CRAFTING, 250000)
                    .makes(Skill.SMITHING, 250000)
                    .requires(Skill.CRAFTING, 70)
                    .requires(Skill.SMITHING, 70)
                    .build());

            aManager.AddMethod(new Method("Sing Two Crystal armour seeds", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 100)
                    .takes(ItemID.CRYSTAL_ARMOUR_SEED, 2)
                    .makes(ItemID.CRYSTAL_LEGS)
                    .makes(Skill.CRAFTING, 500000)
                    .makes(Skill.SMITHING, 500000)
                    .requires(Skill.CRAFTING, 72)
                    .requires(Skill.SMITHING, 72)
                    .build());

            aManager.AddMethod(new Method("Sing Three Crystal armour seeds", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 150)
                    .takes(ItemID.CRYSTAL_ARMOUR_SEED, 3)
                    .makes(ItemID.CRYSTAL_BODY)
                    .makes(Skill.CRAFTING, 750000)
                    .makes(Skill.SMITHING, 750000)
                    .requires(Skill.CRAFTING, 74)
                    .requires(Skill.SMITHING, 74)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal tool seed and Dragon axe", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 120)
                    .takes(ItemID.CRYSTAL_TOOL_SEED)
                    .takes(ItemID.DRAGON_AXE)
                    .makes(ItemID.CRYSTAL_AXE)
                    .makes(Skill.CRAFTING, 600000)
                    .makes(Skill.SMITHING, 600000)
                    .requires(Skill.CRAFTING, 76)
                    .requires(Skill.SMITHING, 76)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal tool seed and Dragon harpoon", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 120)
                    .takes(ItemID.CRYSTAL_TOOL_SEED)
                    .takes(ItemID.DRAGON_HARPOON)
                    .makes(ItemID.CRYSTAL_HARPOON)
                    .makes(Skill.CRAFTING, 600000)
                    .makes(Skill.SMITHING, 600000)
                    .requires(Skill.CRAFTING, 76)
                    .requires(Skill.SMITHING, 76)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal tool seed and Dragon pickaxe", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 120)
                    .takes(ItemID.CRYSTAL_TOOL_SEED)
                    .takes(ItemID.DRAGON_PICKAXE)
                    .makes(ItemID.CRYSTAL_PICKAXE)
                    .makes(Skill.CRAFTING, 600000)
                    .makes(Skill.SMITHING, 600000)
                    .requires(Skill.CRAFTING, 76)
                    .requires(Skill.SMITHING, 76)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal weapon seed Into Crystal bow", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 40)
                    .takes(ItemID.CRYSTAL_WEAPON_SEED)
                    .makes(ItemID.CRYSTAL_BOW)
                    .makes(Skill.CRAFTING, 200000)
                    .makes(Skill.SMITHING, 200000)
                    .requires(Skill.CRAFTING, 78)
                    .requires(Skill.SMITHING, 78)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal weapon seed Into Crystal halberd", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 40)
                    .takes(ItemID.CRYSTAL_WEAPON_SEED)
                    .makes(ItemID.CRYSTAL_HALBERD)
                    .makes(Skill.CRAFTING, 200000)
                    .makes(Skill.SMITHING, 200000)
                    .requires(Skill.CRAFTING, 78)
                    .requires(Skill.SMITHING, 78)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal weapon seed Into Crystal shield", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 40)
                    .takes(ItemID.CRYSTAL_WEAPON_SEED)
                    .makes(ItemID.CRYSTAL_SHIELD)
                    .makes(Skill.CRAFTING, 200000)
                    .makes(Skill.SMITHING, 200000)
                    .requires(Skill.CRAFTING, 78)
                    .requires(Skill.SMITHING, 78)
                    .build());

            aManager.AddMethod(new Method("Sing Crystal key", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 10)
                    .takes(ItemID.CRYSTAL_KEY)
                    .makes(ItemID.ENHANCED_CRYSTAL_KEY)
                    .makes(Skill.CRAFTING, 50000)
                    .makes(Skill.SMITHING, 50000)
                    .requires(Skill.CRAFTING, 80)
                    .requires(Skill.SMITHING, 80)
                    .build());

            aManager.AddMethod(new Method("Sing Enhanced crystal teleport seed", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 100)
                    .takes(ItemID.ENHANCED_CRYSTAL_TELEPORT_SEED)
                    .makes(ItemID.ENHANCED_CRYSTAL_KEY)
                    .makes(Skill.CRAFTING, 500000)
                    .makes(Skill.SMITHING, 500000)
                    .requires(Skill.CRAFTING, 80)
                    .requires(Skill.SMITHING, 80)
                    .build());

            aManager.AddMethod(new Method("Sing Enhanced crystal weapon seed into Blade of sealdor", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 100)
                    .takes(ItemID.ENHANCED_CRYSTAL_WEAPON_SEED)
                    .makes(ItemID.BLADE_OF_SAELDOR)
                    .makes(Skill.CRAFTING, 500000)
                    .makes(Skill.SMITHING, 500000)
                    .requires(Skill.CRAFTING, 82)
                    .requires(Skill.SMITHING, 82)
                    .build());

            aManager.AddMethod(new Method("Sing Enhanced crystal weapon seed into Bow of faerdhinen", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 100)
                    .takes(ItemID.ENHANCED_CRYSTAL_WEAPON_SEED)
                    .makes(ItemID.BOW_OF_FAERDHINEN)
                    .makes(Skill.CRAFTING, 500000)
                    .makes(Skill.SMITHING, 500000)
                    .requires(Skill.CRAFTING, 82)
                    .requires(Skill.SMITHING, 82)
                    .build());

            aManager.AddMethod(new Method("Corrupt Blade of sealdor", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 1000)
                    .takes(ItemID.BLADE_OF_SAELDOR)
                    .makes(ItemID.BLADE_OF_SAELDOR_C)
                    .requires(Skill.CRAFTING, 82)
                    .requires(Skill.SMITHING, 82)
                    .build());

            aManager.AddMethod(new Method("Corrupt Bow of faerdhinen", "crafting/crystal")
                    .takes(ItemID.CRYSTAL_SHARD, 1000)
                    .takes(ItemID.BOW_OF_FAERDHINEN)
                    .makes(ItemID.BOW_OF_FAERDHINEN_C)
                    .requires(Skill.CRAFTING, 82)
                    .requires(Skill.SMITHING, 82)
                    .build());
        }

        // glass smelting
        {
            aManager.AddMethod(new Method("Smelt glass", "crafting/glass")
                    .takes(ItemID.SODA_ASH)
                    .takes(ItemID.BUCKET_OF_SAND)
                    .makes(ItemID.MOLTEN_GLASS)
                    .makes(Skill.CRAFTING, 2000)
                    .build());

            aManager.AddMethod(new Method("Superglass make with Giant seaweed", "crafting/glass")
                    .takes(ItemID.GIANT_SEAWEED, 3)
                    .takes(ItemID.BUCKET_OF_SAND, 18)
                    .takes(ItemID.ASTRAL_RUNE, 2)
                    .takes(ItemID.FIRE_RUNE, 6)
                    .takes(ItemID.AIR_RUNE, 10)
                    .makes(ItemID.MOLTEN_GLASS, 28.8)
                    .makes(Skill.CRAFTING, 18000)
                    .makes(Skill.MAGIC, 7800)
                    .requires(Skill.CRAFTING, 61)
                    .requires(Skill.MAGIC, 77)
                    .requires(Quest.LUNAR_DIPLOMACY));

            aManager.AddMethod(new Method("Superglass make with Seaweed", "crafting/glass")
                    .takes(ItemID.SEAWEED, 13)
                    .takes(ItemID.BUCKET_OF_SAND, 13)
                    .takes(ItemID.ASTRAL_RUNE, 2)
                    .takes(ItemID.FIRE_RUNE, 6)
                    .takes(ItemID.AIR_RUNE, 10)
                    .makes(ItemID.MOLTEN_GLASS, 16.9)
                    .makes(Skill.CRAFTING, 18000)
                    .makes(Skill.MAGIC, 7800)
                    .requires(Skill.CRAFTING, 61)
                    .requires(Skill.MAGIC, 77)
                    .requires(Quest.LUNAR_DIPLOMACY));

            aManager.AddMethod(new Method("Superglass make with Soda ash", "crafting/glass")
                    .takes(ItemID.SODA_ASH, 13)
                    .takes(ItemID.BUCKET_OF_SAND, 13)
                    .takes(ItemID.ASTRAL_RUNE, 2)
                    .takes(ItemID.FIRE_RUNE, 6)
                    .takes(ItemID.AIR_RUNE, 10)
                    .makes(ItemID.MOLTEN_GLASS, 16.9)
                    .makes(Skill.CRAFTING, 18000)
                    .makes(Skill.MAGIC, 7800)
                    .requires(Skill.CRAFTING, 61)
                    .requires(Skill.MAGIC, 77)
                    .requires(Quest.LUNAR_DIPLOMACY));

            aManager.AddMethod(new Method("Superglass make with Swamp weed", "crafting/glass")
                    .takes(ItemID.SWAMP_WEED, 13)
                    .takes(ItemID.BUCKET_OF_SAND, 13)
                    .takes(ItemID.ASTRAL_RUNE, 2)
                    .takes(ItemID.FIRE_RUNE, 6)
                    .takes(ItemID.AIR_RUNE, 10)
                    .makes(ItemID.MOLTEN_GLASS, 16.9)
                    .makes(Skill.CRAFTING, 18000)
                    .makes(Skill.MAGIC, 7800)
                    .requires(Skill.CRAFTING, 61)
                    .requires(Skill.MAGIC, 77)
                    .requires(Quest.LUNAR_DIPLOMACY));
        }
    }
}
