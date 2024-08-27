package com.rl_todo.methods;

import com.rl_todo.*;
import net.runelite.api.*;
import net.runelite.client.plugins.skillcalculator.skills.MiningAction;
import net.runelite.client.plugins.skillcalculator.skills.SmithingAction;

import java.util.*;

class Costs
{
    int myCost;
    int myResult;

    public Costs(int aCosts, int aResult)
    {
        myCost = aCosts;
        myResult = aResult;
    }
}

class WildcardRecipe
{
    public String myPath;
    public Method myMethod;

    public WildcardRecipe(String aPath, Method aMethod)
    {
        myPath = aPath;
        myMethod = aMethod;
    }
}

public class MethodManager
{
    private TodoPlugin myPlugin;

    private Map<String, Method> myMethods = new HashMap<>();
    private List<WildcardRecipe> myDefaultMethod = new ArrayList<>();

    private Map<String, List<Method>> myLookup = new HashMap<>();
    final private List<Method> myEmptyList = new ArrayList<>();

    public MethodManager(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;

        LoadAllLevels();
        LoadAllSkills();
        LoadCapesOfAccomplishments();
        LoadNMZInfusions();

        QuestsMethods.AddAll(this, myPlugin);
        MonsterLoot.AddAll(this, myPlugin);

        BuildTables();
    }

    public Method GetMethodByName(String aName) { return myMethods.get(aName.toLowerCase()); }

    public List<Method> GetAvailableMethods(String aId)
    {
        return myLookup.getOrDefault(aId, myEmptyList);
    }

    protected void AddDefaultMethod(String aId, Method aMethod)
    {
        myDefaultMethod.add(new WildcardRecipe(aId, aMethod));
        AddMethod(aMethod);
    }

    protected void AddMethod(Method aMethod)
    {
        Object prev = myMethods.putIfAbsent(aMethod.myName.toLowerCase(), aMethod);
        assert prev == null : "Recipe with duplicate key " + aMethod.myName.toLowerCase();
    }

    private void BuildTables()
    {
        for (Method method : myMethods.values())
        {
            for(String resourceId : method.myMakes.Ids())
            {
                myLookup.putIfAbsent(resourceId, new ArrayList<>());
                myLookup.get(resourceId).add(method);
            }
        }

        for (List<Method> list : myLookup.values())
            list.sort(Comparator.comparing((Method a) -> a.myName));

        TodoPlugin.debug("Loaded " + myMethods.size() + " recipes", 1);
    }

    private void LoadAllLevels()
    {
        for(Skill skill : Skill.values())
        {
            AddDefaultMethod(
                    IdBuilder.levelId(skill),
                    new LevelMethod(myPlugin.myClient, skill));

            AddMethod(new StewBoostingMethod(skill));
        }
    }

    private void LoadCapesOfAccomplishments()
    {
        {
            Method method = new Method("Buy Max Cape", "purchase/skillcapes");

            method.takes(ItemID.COINS_995, 2277000);
            method.makes(ItemID.MAX_CAPE);

            for(Skill skill : Skill.values())
                method.requires(skill, 99);

            AddDefaultMethod(
                            IdBuilder.itemId(ItemID.MAX_CAPE),
                            method.build());
        }

        {
            Method method = new Method("Buy Quest Cape", "purchase/skillcapes");

            method.takes(ItemID.COINS_995, 99000);
            method.makes(ItemID.QUEST_POINT_CAPE);

            for(Quest quest : Quest.values())
                if (!Quests.MiniQuests.contains(quest))
                    method.requires(quest);

            AddDefaultMethod(IdBuilder.itemId(ItemID.QUEST_POINT_CAPE), method.build());
        }

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.ATTACK_CAPE),
            new Method("Buy Attack Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.ATTACK_CAPE)
                .requires(Skill.ATTACK, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.STRENGTH_CAPE),
            new Method("Buy Strength Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.STRENGTH_CAPE)
                .requires(Skill.STRENGTH, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.DEFENCE_CAPE),
            new Method("Buy Defence Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.DEFENCE_CAPE)
                .requires(Skill.DEFENCE, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.RANGING_CAPE),
            new Method("Buy Ranging Cape", "purchase/skillcapes")
                    .takes(ItemID.COINS_995, 99000)
                    .makes(ItemID.RANGING_CAPE)
                    .requires(Skill.RANGED, 99)
                    .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.PRAYER_CAPE),
            new Method("Buy Prayer Cape", "purchase/skillcapes")
                    .takes(ItemID.COINS_995, 99000)
                    .makes(ItemID.PRAYER_CAPE)
                    .requires(Skill.PRAYER, 99)
                    .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.MAGIC_CAPE),
            new Method("Buy Magic Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.MAGIC_CAPE)
                .requires(Skill.MAGIC, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.RUNECRAFT_CAPE),
            new Method("Buy Runecraft Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.RUNECRAFT_CAPE)
                .requires(Skill.RUNECRAFT, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.CONSTRUCT_CAPE),
            new Method("Buy Construction Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.CONSTRUCT_CAPE)
                .requires(Skill.CONSTRUCTION, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.HITPOINTS_CAPE),
            new Method("Buy Hitpoints Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.HITPOINTS_CAPE)
                .requires(Skill.HITPOINTS, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.AGILITY_CAPE),
            new Method("Buy Agility Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.AGILITY_CAPE)
                .requires(Skill.AGILITY, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.HERBLORE_CAPE),
            new Method("Buy Herblore Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.HERBLORE_CAPE)
                .requires(Skill.HERBLORE, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.THIEVING_CAPE),
            new Method("Buy Thieving Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.THIEVING_CAPE)
                .requires(Skill.THIEVING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.CRAFTING_CAPE),
            new Method("Buy Crafting Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.CRAFTING_CAPE)
                .requires(Skill.CRAFTING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.FLETCHING_CAPE),
            new Method("Buy Fletching Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.FLETCHING_CAPE)
                .requires(Skill.FLETCHING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.SLAYER_CAPE),
            new Method("Buy Slayer Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.SLAYER_CAPE)
                .requires(Skill.SLAYER, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.HUNTER_CAPE),
            new Method("Buy Hunter Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.HUNTER_CAPE)
                .requires(Skill.HUNTER, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.MINING_CAPE),
            new Method("Buy Mining Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.MINING_CAPE)
                .requires(Skill.MINING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.SMITHING_CAPE),
            new Method("Buy Smithing Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.SMITHING_CAPE)
                .requires(Skill.SMITHING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.FISHING_CAPE),
            new Method("Buy Fishing Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.FISHING_CAPE)
                .requires(Skill.FISHING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.COOKING_CAPE),
            new Method("Buy Cooking Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.COOKING_CAPE)
                .requires(Skill.COOKING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.FIREMAKING_CAPE),
            new Method("Buy Firemaking Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.FIREMAKING_CAPE)
                .requires(Skill.FIREMAKING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.WOODCUTTING_CAPE),
            new Method("Buy Woodcutting Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.WOODCUTTING_CAPE)
                .requires(Skill.WOODCUTTING, 99)
                .build());

        AddDefaultMethod(
            IdBuilder.itemId(ItemID.FARMING_CAPE),
            new Method("Buy Farming Cape", "purchase/skillcapes")
                .takes(ItemID.COINS_995, 99000)
                .makes(ItemID.FARMING_CAPE)
                .requires(Skill.FARMING, 99)
                .build());

    }

    private void LoadAllSkills()
    {
        LoadMining();
        LoadSmithing();
        LoadMagic();
        LoadCooking();
        Crafting.AddAll(this, myPlugin);
        Prayer.AddAll(this, myPlugin);
    }

    private void LoadMining()
    {
        for(MiningAction action : MiningAction.values())
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.MINING, (int)action.getXp() * 100));
            products.add(IdBuilder.itemResource(action.getItemId(), 1));


            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.itemResource(ItemID.RUNE_PICKAXE, 1));
            requirements.add(IdBuilder.levelResource(Skill.MINING, action.getLevel()));

            AddMethod(new Method("Mine " + action.getName(myPlugin.myItemManager), "mining")
                    .makes(action.getItemId())
                    .makes(Skill.MINING, (int)action.getXp())
                    .requires(Skill.MINING, action.getLevel())
                    .requires(Alternative.MINING_TOOL)
                    .build());
        }
    }

    private void LoadCooking()
    {
        class Cookable
        {
            int myItem;
            int myResult;
            int myXp;
            int myLevelReq;

            public Cookable(int aItem, int aResult, int aXp, int aLevelReq)
            {
                myItem = aItem;
                myResult = aResult;
                myXp = aXp;
                myLevelReq = aLevelReq;
            }
        }

        for(Cookable cook : new Cookable[]{
                new Cookable(ItemID.RAW_BEEF, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_BEAR_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_BOAR_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_RAT_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_YAK_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_SHRIMPS, ItemID.SHRIMPS, 3000, 1),
                new Cookable(ItemID.RAW_CHICKEN, ItemID.COOKED_CHICKEN, 3000, 1),
                new Cookable(ItemID.RAW_RABBIT, ItemID.COOKED_RABBIT, 3000, 1),
                new Cookable(ItemID.RAW_ANCHOVIES, ItemID.ANCHOVIES, 3000, 1),
                new Cookable(ItemID.RAW_SARDINE, ItemID.SARDINE, 4000, 1),
                new Cookable(ItemID.RAW_UGTHANKI_MEAT, ItemID.UGTHANKI_MEAT, 4000, 1),
                new Cookable(ItemID.RAW_HERRING, ItemID.HERRING, 5000, 5),
                new Cookable(ItemID.RAW_MACKEREL, ItemID.MACKEREL, 6000, 10),
                new Cookable(ItemID.RAW_BIRD_MEAT, ItemID.ROAST_BIRD_MEAT, 6000, 11),
                new Cookable(ItemID.THIN_SNAIL_MEAT, ItemID.THIN_SNAIL, 7000, 12),
                new Cookable(ItemID.RAW_TROUT, ItemID.TROUT, 7000, 15),
                new Cookable(ItemID.SPIDER_ON_STICK_6297, ItemID.SPIDER_ON_STICK, 8000, 16),
                new Cookable(ItemID.SPIDER_ON_SHAFT_6299, ItemID.SPIDER_ON_SHAFT, 8000, 16),
                new Cookable(ItemID.SKEWERED_RABBIT, ItemID.ROAST_RABBIT, 7000, 16),
                new Cookable(ItemID.LEAN_SNAIL_MEAT, ItemID.LEAN_SNAIL, 8000, 17),
                new Cookable(ItemID.RAW_COD, ItemID.COD, 7500, 18),
                new Cookable(ItemID.RAW_PIKE, ItemID.PIKE, 8000, 20),
                new Cookable(ItemID.SKEWERED_BEAST, ItemID.ROAST_BEAST_MEAT, 8250, 21),
                new Cookable(ItemID.CRAB_MEAT, ItemID.COOKED_CRAB_MEAT, 10000, 21),
                new Cookable(ItemID.FAT_SNAIL_MEAT, ItemID.FAT_SNAIL, 9500, 22),
                new Cookable(ItemID.RAW_SALMON, ItemID.SALMON, 9000, 25),
                new Cookable(ItemID.RAW_SLIMY_EEL, ItemID.COOKED_SLIMY_EEL, 9500, 28),
                new Cookable(ItemID.RAW_TUNA, ItemID.TUNA, 10000, 30),
                new Cookable(ItemID.RAW_KARAMBWAN, ItemID.COOKED_KARAMBWAN, 19000, 30),
                new Cookable(ItemID.RAW_CHOMPY, ItemID.COOKED_CHOMPY, 10000, 30),
                new Cookable(ItemID.RAW_FISHCAKE, ItemID.COOKED_FISHCAKE, 10000, 31),
                new Cookable(ItemID.RAW_RAINBOW_FISH, ItemID.RAINBOW_FISH, 11000, 35),
                new Cookable(ItemID.RAW_CAVE_EEL, ItemID.CAVE_EEL, 11500, 38),
                new Cookable(ItemID.RAW_LOBSTER, ItemID.LOBSTER, 12000, 40),
                new Cookable(ItemID.RAW_JUBBLY, ItemID.COOKED_JUBBLY, 16000, 41),
                new Cookable(ItemID.RAW_BASS, ItemID.BASS, 13000, 43),
                new Cookable(ItemID.RAW_SWORDFISH, ItemID.SWORDFISH, 14000, 45),
                new Cookable(ItemID.RAW_LAVA_EEL, ItemID.LAVA_EEL, 3000, 53),
                new Cookable(ItemID.RAW_MONKFISH, ItemID.MONKFISH, 15000, 62),
                new Cookable(ItemID.RAW_SHARK, ItemID.SHARK, 21000, 80),
                new Cookable(ItemID.RAW_SEA_TURTLE, ItemID.SEA_TURTLE, 21130, 82),
                new Cookable(ItemID.RAW_ANGLERFISH, ItemID.ANGLERFISH, 23000, 84),
                new Cookable(ItemID.RAW_DARK_CRAB, ItemID.DARK_CRAB, 21500, 90),
                new Cookable(ItemID.RAW_MANTA_RAY, ItemID.MANTA_RAY, 21620, 91),

                //pies
                new Cookable(ItemID.UNCOOKED_BERRY_PIE, ItemID.REDBERRY_PIE, 7800, 10),
                new Cookable(ItemID.UNCOOKED_MEAT_PIE, ItemID.MEAT_PIE, 11000, 20),
                new Cookable(ItemID.RAW_MUD_PIE, ItemID.MUD_PIE, 12800, 29),
                new Cookable(ItemID.UNCOOKED_APPLE_PIE, ItemID.APPLE_PIE, 13000, 30),
                new Cookable(ItemID.RAW_GARDEN_PIE, ItemID.GARDEN_PIE, 13800, 34),
                new Cookable(ItemID.RAW_FISH_PIE, ItemID.FISH_PIE, 16400, 47),
                new Cookable(ItemID.UNCOOKED_BOTANICAL_PIE, ItemID.BOTANICAL_PIE, 18000, 52),
                new Cookable(ItemID.UNCOOKED_MUSHROOM_PIE, ItemID.MUSHROOM_PIE, 200000, 60),
                new Cookable(ItemID.RAW_ADMIRAL_PIE, ItemID.ADMIRAL_PIE, 210000, 70),
                new Cookable(ItemID.UNCOOKED_DRAGONFRUIT_PIE, ItemID.DRAGONFRUIT_PIE, 220000, 73),
                new Cookable(ItemID.RAW_WILD_PIE, ItemID.WILD_PIE, 240000, 85),
                new Cookable(ItemID.RAW_SUMMER_PIE, ItemID.SUMMER_PIE, 260000, 95),
        })
        {
            AddMethod(new Method("Cook "+ myPlugin.myItemManager.getItemComposition(cook.myItem).getName(), "cooking/range")
                .takes(cook.myItem)
                .makes(cook.myResult)
                .makes(cook.myXp)
                .requires(Skill.COOKING, cook.myLevelReq).build());
        }

        for(Cookable cook : new Cookable[]
                {
                        new Cookable(ItemID.RAW_BEEF, ItemID.SINEW, 3, 1),
                        new Cookable(ItemID.RAW_BEAR_MEAT, ItemID.SINEW, 3, 1),
                        new Cookable(ItemID.RAW_BOAR_MEAT, ItemID.SINEW, 3, 1)
                })
        {
            AddMethod(new Method("Cook "+ myPlugin.myItemManager.getItemComposition(cook.myItem).getName() + " into Sinew", "cooking/range")
                .takes(cook.myItem)
                .makes(cook.myResult)
                .makes(Skill.COOKING, cook.myXp)
                .requires(Skill.COOKING, cook.myLevelReq).build());
        }

        // TODO Bread
        // TODO Pitta
        // TODO new Cookable(ItemID.SACRED_EEL, ItemID.ZULRAHS_SCALES, 15000, 62),
        // TODO new Cookable(ItemID.RAW_KARAMBWAN, ItemID.POISON_KARAMBWAN, 8000, 1),
        // TODO gubby
        // TODO cavefish
        // TODO tetra
        // TODO catfish

        // Pies
        {
            AddMethod(new Method("Make Uncooked berry pie", "cooking/pies")
                .takes(ItemID.REDBERRIES)
                .takes(ItemID.PIE_SHELL)
                .makes(ItemID.UNCOOKED_BERRY_PIE)
                .requires(Skill.COOKING, 10)
                .build());

            AddMethod(new Method("Make Uncooked meat pie", "cooking/pies")
                .takes(ItemID.COOKED_MEAT)
                .takes(ItemID.PIE_SHELL)
                .makes(ItemID.UNCOOKED_MEAT_PIE)
                .requires(Skill.COOKING, 20)
                .build());

            AddMethod(new Method("Make Raw mud pie with Bucket of Water", "cooking/pies")
                .takes(ItemID.COMPOST)
                .takes(ItemID.BUCKET_OF_WATER)
                .takes(ItemID.CLAY)
                .takes(ItemID.PIE_SHELL)
                .makes(ItemID.RAW_MUD_PIE)
                .requires(Skill.COOKING, 29)
                    .build());

            AddMethod(new Method("Make Raw mud pie with Bowl of Water", "cooking/pies")
                .takes(ItemID.COMPOST)
                .takes(ItemID.BOWL_OF_WATER)
                .takes(ItemID.CLAY)
                .takes(ItemID.PIE_SHELL)
                .makes(ItemID.RAW_MUD_PIE)
                .requires(Skill.COOKING, 29)
                .build());

            AddMethod(new Method("Make Uncooked apple pie", "cooking/pies")
                .takes(ItemID.COOKING_APPLE)
                .takes(ItemID.PIE_SHELL)
                .makes(ItemID.UNCOOKED_APPLE_PIE)
                .requires(Skill.COOKING, 30)
                .build());

            AddMethod(new Method("Make Raw garden pie", "cooking/pies")
                .takes(ItemID.TOMATO)
                .takes(ItemID.ONION)
                .takes(ItemID.CABBAGE)
                .takes(ItemID.PIE_SHELL)
                .makes(ItemID.RAW_GARDEN_PIE)
                .requires(Skill.COOKING, 34)
                .build());

            AddMethod(new Method("Make Raw fish pie", "cooking/pies")
                    .takes(ItemID.TROUT)
                    .takes(ItemID.COD)
                    .takes(ItemID.POTATO)
                    .takes(ItemID.PIE_SHELL)
                    .makes(ItemID.RAW_FISH_PIE)
                    .requires(Skill.COOKING, 47)
                    .build());

            AddMethod(new Method("Make Uncooked mushroom pie", "cooking/pies")
                    .takes(ItemID.SULLIUSCEP_CAP)
                    .takes(ItemID.PIE_SHELL)
                    .makes(ItemID.UNCOOKED_MUSHROOM_PIE)
                    .requires(Skill.COOKING, 50)
                    .build());

            AddMethod(new Method("Make Raw admiral pie", "cooking/pies")
                    .takes(ItemID.SALMON)
                    .takes(ItemID.TUNA)
                    .takes(ItemID.POTATO)
                    .takes(ItemID.PIE_SHELL)
                    .makes(ItemID.RAW_ADMIRAL_PIE)
                    .requires(Skill.COOKING, 70)
                    .build());

            AddMethod(new Method("Make Uncooked dragonfruit pie", "cooking/pies")
                    .takes(ItemID.DRAGONFRUIT)
                    .takes(ItemID.PIE_SHELL)
                    .makes(ItemID.UNCOOKED_DRAGONFRUIT_PIE)
                    .requires(Skill.COOKING, 73)
                    .build());

            AddMethod(new Method("Make Raw wild pie", "cooking/pies")
                    .takes(ItemID.RAW_BEAR_MEAT)
                    .takes(ItemID.RAW_CHOMPY)
                    .takes(ItemID.RAW_RABBIT)
                    .takes(ItemID.PIE_SHELL)
                    .makes(ItemID.RAW_WILD_PIE)
                    .requires(Skill.COOKING, 85)
                    .build());

            AddMethod(new Method("Make Raw summer pie", "cooking/pies")
                    .takes(ItemID.STRAWBERRY)
                    .takes(ItemID.WATERMELON)
                    .takes(ItemID.COOKING_APPLE)
                    .takes(ItemID.PIE_SHELL)
                    .makes(ItemID.RAW_SUMMER_PIE)
                    .requires(Skill.COOKING, 95)
                    .build());
        }
    }

    private void LoadMagic()
    {
        LoadStandardSpellbook();

        //TODO all the other spellbooks :(
    }

    private void LoadStandardSpellbook()
    {
        // Combat
        {
            // Offensive
            {
                //Strikes
                {
                    AddMethod(new Method("Cast Wind Strike", "magic/standard/offensive")
                        .takes(ItemID.MIND_RUNE)
                        .takes(ItemID.AIR_RUNE)
                        .makes(Skill.MAGIC, 5.5)
                        .build());

                    AddMethod(new Method("Cast Water Strike", "magic/standard/offensive")
                        .takes(ItemID.MIND_RUNE)
                        .takes(ItemID.AIR_RUNE)
                        .takes(ItemID.WATER_RUNE)
                        .makes(Skill.MAGIC, 7.5)
                        .requires(Skill.MAGIC, 5)
                        .build());

                    AddMethod(new Method("Cast Earth Strike", "magic/standard/offensive")
                        .takes(ItemID.MIND_RUNE)
                        .takes(ItemID.AIR_RUNE)
                        .takes(ItemID.EARTH_RUNE, 2)
                        .makes(Skill.MAGIC, 9.5)
                        .requires(Skill.MAGIC, 9)
                        .build());

                    AddMethod(new Method("Cast Fire Strike", "magic/standard/offensive")
                        .takes(ItemID.MIND_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .takes(ItemID.FIRE_RUNE, 3)
                        .makes(Skill.MAGIC, 11.5)
                        .requires(Skill.MAGIC, 13)
                        .build());
                }

                // Bolts
                {
                    AddMethod(new Method("Cast Wind Bolt", "magic/standard/offensive")
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .makes(Skill.MAGIC, 13.5)
                        .requires(Skill.MAGIC, 17)
                        .build());

                    AddMethod(new Method("Cast Water Bolt", "magic/standard/offensive")
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .takes(ItemID.WATER_RUNE, 2)
                        .makes(Skill.MAGIC, 16.5)
                        .requires(Skill.MAGIC, 23)
                        .build());

                    AddMethod(new Method("Cast Earth Bolt", "magic/standard/offensive")
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .takes(ItemID.WATER_RUNE, 3)
                        .makes(Skill.MAGIC, 19.5)
                        .requires(Skill.MAGIC, 29)
                        .build());

                    AddMethod(new Method("Cast Fire Bolt", "magic/standard/offensive")
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.FIRE_RUNE, 4)
                        .makes(Skill.MAGIC, 22.5)
                        .requires(Skill.MAGIC, 35)
                        .build());
                }

                // Blasts
                {
                    AddMethod(new Method("Cast Wind Blast", "magic/standard/offensive")
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .makes(Skill.MAGIC, 25.5)
                        .requires(Skill.MAGIC, 41)
                        .build());

                    AddMethod(new Method("Cast Water Blast", "magic/standard/offensive")
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.WATER_RUNE, 3)
                        .makes(Skill.MAGIC, 28.5)
                        .requires(Skill.MAGIC, 47)
                        .build());

                    AddMethod(new Method("Cast Earth Blast", "magic/standard/offensive")
                        .takes(ItemID.DEATH_RUNE, 1)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.EARTH_RUNE, 4)
                        .makes(Skill.MAGIC, 31.5)
                        .requires(Skill.MAGIC, 53)
                        .build());

                    AddMethod(new Method("Cast Fire Blast", "magic/standard/offensive")
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 4)
                        .takes(ItemID.FIRE_RUNE, 5)
                        .makes(Skill.MAGIC, 34.5)
                        .requires(Skill.MAGIC, 59)
                        .build());
                }

                // Waves
                {
                    AddMethod(new Method("Cast Wind Wave", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE)
                        .takes(ItemID.AIR_RUNE, 5)
                        .makes(Skill.MAGIC, 36)
                        .requires(Skill.MAGIC, 62)
                        .build());

                    AddMethod(new Method("Cast Water Wave", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE)
                        .takes(ItemID.AIR_RUNE, 5)
                        .takes(ItemID.WATER_RUNE, 7)
                        .makes(Skill.MAGIC, 37.5)
                        .requires(Skill.MAGIC, 65)
                        .build());

                    AddMethod(new Method("Cast Earth Wave", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE)
                        .takes(ItemID.AIR_RUNE, 5)
                        .takes(ItemID.EARTH_RUNE, 7)
                        .makes(Skill.MAGIC, 40)
                        .requires(Skill.MAGIC, 70)
                        .build());

                    AddMethod(new Method("Cast Fire Wave", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE, 1)
                        .takes(ItemID.AIR_RUNE, 5)
                        .takes(ItemID.FIRE_RUNE, 7)
                        .makes(Skill.MAGIC, 42.5)
                        .requires(Skill.MAGIC, 75)
                        .build());
                }

                // Surges
                {
                    AddMethod(new Method("Cast Wind Surge", "magic/standard/offensive")
                        .takes(ItemID.WRATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 7)
                        .makes(Skill.MAGIC, 44.5)
                        .requires(Skill.MAGIC, 81)
                        .build());

                    AddMethod(new Method("Cast Water Surge", "magic/standard/offensive")
                        .takes(ItemID.WRATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 7)
                        .takes(ItemID.WATER_RUNE, 10)
                        .makes(Skill.MAGIC, 46.5)
                        .requires(Skill.MAGIC, 85)
                        .build());

                    AddMethod(new Method("Cast Earth Surge", "magic/standard/offensive")
                        .takes(ItemID.WRATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 7)
                        .takes(ItemID.EARTH_RUNE, 10)
                        .makes(Skill.MAGIC, 48.2)
                        .requires(Skill.MAGIC, 90)
                        .build());

                    AddMethod(new Method("Cast Fire Surge", "magic/standard/offensive")
                        .takes(ItemID.WRATH_RUNE)
                        .takes(ItemID.AIR_RUNE, 7)
                        .takes(ItemID.FIRE_RUNE, 10)
                        .makes(Skill.MAGIC, 50.5)
                        .requires(Skill.MAGIC, 95)
                        .build());
                }

                // God spells
                {
                    AddMethod(new Method("Cast Saradomin Strike", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE, 2)
                        .takes(ItemID.AIR_RUNE, 4)
                        .takes(ItemID.FIRE_RUNE, 2)
                        .makes(Skill.MAGIC, 35)
                        .requires(Skill.MAGIC, 60)
                        .requires(ItemID.SARADOMIN_STAFF)
                        .build());

                    AddMethod(new Method("Cast Flames of Zamorak", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE, 2)
                        .takes(ItemID.AIR_RUNE)
                        .takes(ItemID.FIRE_RUNE, 4)
                        .makes(Skill.MAGIC, 35)
                        .requires(Skill.MAGIC, 60)
                        .requires(ItemID.ZAMORAK_STAFF)
                        .build());

                    AddMethod(new Method("Cast Claws of Guthix", "magic/standard/offensive")
                        .takes(ItemID.BLOOD_RUNE, 2)
                        .takes(ItemID.AIR_RUNE, 4)
                        .takes(ItemID.FIRE_RUNE)
                        .makes(Skill.MAGIC, 35)
                        .requires(Skill.MAGIC, 60)
                        .requires(ItemID.GUTHIX_STAFF)
                        .build());
                }

                // Misc
                {
                    AddMethod(new Method("Cast Crumble Undead", "magic/standard/offensive")
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .takes(ItemID.EARTH_RUNE, 2)
                        .makes(Skill.MAGIC, 24.5)
                        .requires(Skill.MAGIC, 39)
                        .build());

                    AddMethod(new Method("Cast Ibans Blast", "magic/standard/offensive")
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.FIRE_RUNE, 5)
                        .makes(Skill.MAGIC, 30)
                        .requires(Skill.MAGIC, 50)
                        .requires(Alternative.IBANS_STAFF)
                        .build());

                    AddMethod(new Method("Cast Magic Dart", "magic/standard/offensive")
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.MIND_RUNE, 4)
                        .makes(Skill.MAGIC, 30)
                        .requires(Skill.MAGIC, 50)
                        .requires(Alternative.MAGIC_DART_CASTER)
                        .build());

                    AddMethod(new Method("Cast Charge", "magic/standard/buffs")
                        .takes(ItemID.BLOOD_RUNE, 3)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.FIRE_RUNE, 3)
                        .makes(Skill.MAGIC, 180)
                        .requires(Skill.MAGIC, 80)
                        .build());
                }
            }

            // Curses
            {
                // Low weakens
                {
                    AddMethod(new Method("Cast Confuse", "magic/standard/debuffs")
                            .takes(ItemID.BODY_RUNE)
                            .takes(ItemID.EARTH_RUNE, 2)
                            .takes(ItemID.WATER_RUNE, 3)
                            .makes(Skill.MAGIC, 13)
                            .requires(Skill.MAGIC, 3)
                            .build());

                    AddMethod(new Method("Cast Weaken", "magic/standard/debuffs")
                            .takes(ItemID.BODY_RUNE)
                            .takes(ItemID.EARTH_RUNE, 2)
                            .takes(ItemID.WATER_RUNE, 3)
                            .makes(Skill.MAGIC, 21)
                            .requires(Skill.MAGIC, 11)
                            .build());

                    AddMethod(new Method("Cast Curse", "magic/standard/debuffs")
                            .takes(ItemID.BODY_RUNE)
                            .takes(ItemID.EARTH_RUNE, 3)
                            .takes(ItemID.WATER_RUNE, 2)
                            .makes(Skill.MAGIC, 29)
                            .requires(Skill.MAGIC, 19)
                            .build());
                }

                // High weakens
                {
                    AddMethod(new Method("Cast Vulnerability", "magic/standard/debuffs")
                            .takes(ItemID.SOUL_RUNE)
                            .takes(ItemID.EARTH_RUNE, 5)
                            .takes(ItemID.WATER_RUNE, 5)
                            .makes(Skill.MAGIC, 76)
                            .requires(Skill.MAGIC, 66)
                            .build());

                    AddMethod(new Method("Cast Enfeeble", "magic/standard/debuffs")
                            .takes(ItemID.SOUL_RUNE)
                            .takes(ItemID.EARTH_RUNE, 8)
                            .takes(ItemID.WATER_RUNE, 8)
                            .makes(Skill.MAGIC, 83)
                            .requires(Skill.MAGIC, 73)
                            .build());

                    AddMethod(new Method("Cast Stun", "magic/standard/debuffs")
                            .takes(ItemID.SOUL_RUNE)
                            .takes(ItemID.EARTH_RUNE, 12)
                            .takes(ItemID.WATER_RUNE, 12)
                            .makes(Skill.MAGIC, 90)
                            .requires(Skill.MAGIC, 80)
                            .build());
                }

                // Snares
                {
                    AddMethod(new Method("Cast Bind", "magic/standard/debuffs")
                            .takes(ItemID.NATURE_RUNE, 2)
                            .takes(ItemID.EARTH_RUNE, 3)
                            .takes(ItemID.WATER_RUNE, 3)
                            .makes(Skill.MAGIC, 30)
                            .requires(Skill.MAGIC, 20)
                            .build());

                    AddMethod(new Method("Cast Snare", "magic/standard/debuffs")
                            .takes(ItemID.NATURE_RUNE, 3)
                            .takes(ItemID.EARTH_RUNE, 4)
                            .takes(ItemID.WATER_RUNE, 4)
                            .makes(Skill.MAGIC, 60)
                            .requires(Skill.MAGIC, 50)
                            .build());

                    AddMethod(new Method("Cast Entangle", "magic/standard/debuffs")
                            .takes(ItemID.NATURE_RUNE, 4)
                            .takes(ItemID.EARTH_RUNE, 5)
                            .takes(ItemID.WATER_RUNE, 6)
                            .makes(Skill.MAGIC, 89)
                            .requires(Skill.MAGIC, 79)
                            .build());
                }

                AddMethod(new Method("Cast Tele Block", "magic/standard/debuffs")
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.LAW_RUNE)
                        .makes(Skill.MAGIC, 80)
                        .requires(Skill.MAGIC, 85)
                        .build());
            }

            // Teleports
            {
                AddMethod(new Method("Cast Varrock Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.FIRE_RUNE)
                        .makes(Skill.MAGIC, 35)
                        .requires(Skill.MAGIC, 25)
                        .build());

                AddMethod(new Method("Cast Lumbridge Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.EARTH_RUNE)
                        .makes(Skill.MAGIC, 41)
                        .requires(Skill.MAGIC, 31)
                        .build());

                AddMethod(new Method("Cast Falador Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.WATER_RUNE)
                        .makes(Skill.MAGIC, 47)
                        .requires(Skill.MAGIC, 37)
                        .build());

                AddMethod(new Method("Cast Teleport to House", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.AIR_RUNE)
                        .takes(ItemID.EARTH_RUNE)
                        .makes(Skill.MAGIC, 30)
                        .requires(Skill.MAGIC, 40)
                        .build());

                AddMethod(new Method("Cast Camelot Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.AIR_RUNE, 5)
                        .makes(Skill.MAGIC, 55.5)
                        .requires(Skill.MAGIC, 40)
                        .build());

                AddMethod(new Method("Cast Ardougne Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.WATER_RUNE, 2)
                        .makes(Skill.MAGIC, 61)
                        .requires(Skill.MAGIC, 51)
                        .requires(Quest.PLAGUE_CITY)
                        .build());

                AddMethod(new Method("Cast Watchtower Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.EARTH_RUNE, 2)
                        .makes(Skill.MAGIC, 68)
                        .requires(Skill.MAGIC, 58)
                        .requires(Quest.WATCHTOWER)
                        .build());

                AddMethod(new Method("Cast Trollheim Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.FIRE_RUNE, 2)
                        .makes(Skill.MAGIC, 68)
                        .requires(Skill.MAGIC, 61)
                        .requires(Quest.EADGARS_RUSE)
                        .build());

                AddMethod(new Method("Cast Ape Atoll Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.WATER_RUNE, 2)
                        .takes(ItemID.FIRE_RUNE, 2)
                        .takes(ItemID.BANANA)
                        .makes(Skill.MAGIC, 74)
                        .requires(Skill.MAGIC, 64)
                        .requires(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI)
                        .build());

                AddMethod(new Method("Cast Kourend Castle Teleport", "magic/standard/teleports")
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.WATER_RUNE, 4)
                        .takes(ItemID.FIRE_RUNE, 5)
                        .takes(ItemID.SOUL_RUNE, 2)
                        .makes(Skill.MAGIC, 82)
                        .requires(Skill.MAGIC, 69)
                        .build());
            }

            // Odd Teleports
            {
                AddMethod(new Method("Cast Teleother Lumbridge", "magic/standard/teleother")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.EARTH_RUNE)
                        .takes(ItemID.SOUL_RUNE)
                        .makes(Skill.MAGIC, 84)
                        .requires(Skill.MAGIC, 64)
                        .build());

                AddMethod(new Method("Cast Teleother Falador", "magic/standard/teleother")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.WATER_RUNE)
                        .takes(ItemID.SOUL_RUNE)
                        .makes(Skill.MAGIC, 92)
                        .requires(Skill.MAGIC, 82)
                        .build());

                AddMethod(new Method("Cast Teleother Camelot", "magic/standard/teleother")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.SOUL_RUNE, 2)
                        .makes(Skill.MAGIC, 100)
                        .requires(Skill.MAGIC, 90)
                        .build());

                AddMethod(new Method("Cast Teleport to Target", "magic/standard/teleother")
                        .takes(ItemID.LAW_RUNE)
                        .takes(ItemID.CHAOS_RUNE)
                        .takes(ItemID.SOUL_RUNE)
                        .makes(Skill.MAGIC, 45)
                        .requires(Skill.MAGIC, 85)
                        .build());
            }
        }

        // Utility
        {

            AddMethod(new Method("Cast Bones to Banans", "magic/standard/misc")
                    .takes(ItemID.NATURE_RUNE)
                    .takes(ItemID.WATER_RUNE, 2)
                    .takes(ItemID.EARTH_RUNE, 2)
                    .makes(Skill.MAGIC, 25)
                    .requires(Skill.MAGIC, 15)
                    .build());

            AddMethod(new Method("Cast Low Level Alchemy", "magic/standard/misc")
                    .takes(ItemID.NATURE_RUNE)
                    .takes(ItemID.FIRE_RUNE, 3)
                    .makes(Skill.MAGIC, 31)
                    .requires(Skill.MAGIC, 21)
                    .build());

            // superheat
            {
                class SuperHeatable
                {
                    public int myResult;
                    public int myItem;
                    public int myCoalCount;
                    public double mySmithingXp;
                    public int mySmithingLevel;

                    SuperHeatable(int aResult, int aItem, int aCoalCount, double aSmithingXp, int aSmithingLevel)
                    {
                        myResult = aResult;
                        myItem = aItem;
                        myCoalCount = aCoalCount;
                        mySmithingXp = aSmithingXp;
                        mySmithingLevel = aSmithingLevel;
                    }
                }

                for(SuperHeatable heatable : new SuperHeatable[]
                        {
                                new SuperHeatable(ItemID.IRON_BAR, ItemID.IRON_ORE, 0, 12.50, 15),
                                new SuperHeatable(ItemID.SILVER_BAR, ItemID.SILVER_ORE, 0, 13.70, 20),
                                new SuperHeatable(ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0, 22.50, 40),
                                new SuperHeatable(ItemID.LOVAKITE_BAR, ItemID.LOVAKITE_ORE, 2, 20, 45),
                                new SuperHeatable(ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, 4, 30, 50),
                                new SuperHeatable(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, 6, 37.5, 70),
                                new SuperHeatable(ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, 8, 50, 85)
                        })
                {
                    Method method = new Method("Cast Superheat Item on " + myPlugin.myItemManager.getItemComposition(heatable.myItem).getName(), "magic/standard/misc");

                    if (heatable.myCoalCount != 0)
                        method.takes(ItemID.COAL, heatable.myCoalCount);

                    AddMethod(method
                            .takes(ItemID.NATURE_RUNE)
                            .takes(ItemID.FIRE_RUNE, 4)
                            .takes(heatable.myItem)

                            .makes(heatable.myResult)
                            .makes(Skill.MAGIC, 53)
                            .makes(Skill.SMITHING, heatable.mySmithingXp)

                            .requires(Skill.MAGIC, 43)
                            .requires(Skill.SMITHING, heatable.mySmithingLevel)
                            .build());
                }

                {
                    AddMethod(new Method("Cast Superheat Item on Copper and Tin Ore", "magic/standard/misc")
                            .takes(ItemID.COPPER_ORE)
                            .takes(ItemID.TIN_ORE)
                            .takes(ItemID.NATURE_RUNE)
                            .takes(ItemID.FIRE_RUNE, 4)

                            .makes(ItemID.BRONZE_BAR)
                            .makes(Skill.MAGIC, 53)
                            .makes(Skill.SMITHING, 6.2)

                            .requires(Skill.MAGIC, 43));
                }

                {
                    AddMethod(new Method("Cast Superheat Item on Iron Ore with Coal available", "magic/standard/misc")
                            .takes(ItemID.IRON_ORE)
                            .takes(ItemID.COAL, 2)
                            .takes(ItemID.NATURE_RUNE)
                            .takes(ItemID.FIRE_RUNE, 4)

                            .makes(ItemID.STEEL_BAR)
                            .makes(Skill.MAGIC, 53)
                            .makes(Skill.SMITHING, 17.5)

                            .requires(Skill.MAGIC, 43)
                            .requires(Skill.SMITHING, 30));
                }

                {
                    AddMethod(new Method("Cast Superheat Item on Gold Ore with Goldsmith Guantlets", "magic/standard/misc")
                            .takes(ItemID.GOLD_ORE)
                            .takes(ItemID.NATURE_RUNE)
                            .takes(ItemID.FIRE_RUNE, 4)

                            .makes(ItemID.GOLD_BAR)
                            .makes(Skill.MAGIC, 53)
                            .makes(Skill.SMITHING, 56.2)

                            .requires(Skill.MAGIC, 43)
                            .requires(Skill.SMITHING, 40)
                            .requires(ItemID.GOLDSMITH_GAUNTLETS));
                }
            }

            AddMethod(new Method("Cast High Level Alchemy", "magic/standard/misc")
                    .takes(ItemID.NATURE_RUNE)
                    .takes(ItemID.FIRE_RUNE, 5)
                    .makes(Skill.MAGIC, 65)
                    .requires(Skill.MAGIC, 55)
                    .build());

            AddMethod(new Method("Cast Bones to Peaches", "magic/standard/misc")
                    .takes(ItemID.NATURE_RUNE)
                    .takes(ItemID.EARTH_RUNE, 2)
                    .takes(ItemID.WATER_RUNE, 4)
                    .makes(Skill.MAGIC, 65)
                    .requires(Skill.MAGIC, 60)
                    .build());

            AddMethod(new Method("Cast Telekinetic Grab", "magic/standard/misc")
                    .takes(ItemID.LAW_RUNE)
                    .takes(ItemID.AIR_RUNE, 2)
                    .makes(Skill.MAGIC, 43)
                    .requires(Skill.MAGIC, 33)
                    .build());

            AddMethod(new Method("Cast Telekinetic Grab on Wine of Zamorak", "magic/standard/misc")
                    .takes(ItemID.LAW_RUNE)
                    .takes(ItemID.AIR_RUNE, 2)
                    .makes(ItemID.WINE_OF_ZAMORAK)
                    .makes(Skill.MAGIC, 43)
                    .requires(Skill.MAGIC, 33)
                    .build());
        }

        // Orbs
        {
            AddMethod(new Method("Cast Charge Water Orb", "magic/standard/charge")
                    .takes(ItemID.COSMIC_RUNE, 3)
                    .takes(ItemID.WATER_RUNE, 30)
                    .takes(ItemID.UNPOWERED_ORB)
                    .makes(ItemID.WATER_ORB)
                    .makes(Skill.MAGIC, 56)
                    .requires(Skill.MAGIC, 56)
                    .build());

            AddMethod(new Method("Cast Charge Earth Orb", "magic/standard/charge")
                    .takes(ItemID.COSMIC_RUNE, 3)
                    .takes(ItemID.EARTH_RUNE, 30)
                    .takes(ItemID.UNPOWERED_ORB)
                    .makes(ItemID.EARTH_ORB)
                    .makes(Skill.MAGIC, 70)
                    .requires(Skill.MAGIC, 60)
                    .build());

            AddMethod(new Method("Cast Charge Fire Orb", "magic/standard/charge")
                    .takes(ItemID.COSMIC_RUNE, 3)
                    .takes(ItemID.FIRE_RUNE, 30)
                    .takes(ItemID.UNPOWERED_ORB)
                    .makes(ItemID.FIRE_ORB)
                    .makes(Skill.MAGIC, 73)
                    .requires(Skill.MAGIC, 63)
                    .build());

            AddMethod(new Method("Cast Charge Air Orb", "magic/standard/charge")
                    .takes(ItemID.COSMIC_RUNE, 3)
                    .takes(ItemID.AIR_RUNE, 30)
                    .takes(ItemID.UNPOWERED_ORB)
                    .makes(ItemID.AIR_ORB)
                    .makes(Skill.MAGIC, 76)
                    .requires(Skill.MAGIC, 66)
                    .build());
        }

        // Enchants
        {
            class Enchantable
            {
                int myTarget;
                int myResult;

                public Enchantable(int aTarget, int aResult)
                {
                    myTarget = aTarget;
                    myResult = aResult;
                }
            }

            // level 1
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.SAPPHIRE_RING, ItemID.RING_OF_RECOIL),
                                new Enchantable(ItemID.SAPPHIRE_BRACELET, ItemID.BRACELET_OF_CLAY),
                                new Enchantable(ItemID.SAPPHIRE_NECKLACE, ItemID.GAMES_NECKLACE8),
                                new Enchantable(ItemID.SAPPHIRE_AMULET, ItemID.AMULET_OF_MAGIC),
                                new Enchantable(ItemID.SILVTHRILL_ROD, ItemID.SILVTHRILL_ROD_7638),
                                new Enchantable(ItemID.OPAL_RING, ItemID.RING_OF_PURSUIT),
                                new Enchantable(ItemID.OPAL_BRACELET, ItemID.EXPEDITIOUS_BRACELET),
                                new Enchantable(ItemID.OPAL_NECKLACE, ItemID.DODGY_NECKLACE),
                                new Enchantable(ItemID.OPAL_AMULET, ItemID.AMULET_OF_BOUNTY),

                        })
                {
                    AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/lv1")
                            .takes(ItemID.COSMIC_RUNE)
                            .takes(ItemID.WATER_RUNE)
                            .takes(ench.myTarget)
                            .makes(ench.myResult)
                            .makes(Skill.MAGIC, 17.5)
                            .requires(Skill.MAGIC, 7)
                            .build());
                }
            }

            // Level 2
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.EMERALD_RING, ItemID.RING_OF_DUELING8),
                                new Enchantable(ItemID.EMERALD_NECKLACE, ItemID.BINDING_NECKLACE),
                                new Enchantable(ItemID.EMERALD_BRACELET, ItemID.CASTLE_WARS_BRACELET3),
                                new Enchantable(ItemID.EMERALD_AMULET, ItemID.AMULET_OF_DEFENCE),
                                new Enchantable(ItemID.PRENATURE_AMULET, ItemID.AMULET_OF_NATURE),
                                new Enchantable(ItemID.EMERALD_SICKLE_B, ItemID.ENCHANTED_EMERALD_SICKLE_B),
                                new Enchantable(ItemID.JADE_RING, ItemID.RING_OF_RETURNING5),
                                new Enchantable(ItemID.JADE_BRACELET, ItemID.FLAMTAER_BRACELET),
                                new Enchantable(ItemID.JADE_NECKLACE, ItemID.NECKLACE_OF_PASSAGE5),
                                new Enchantable(ItemID.JADE_AMULET, ItemID.AMULET_OF_CHEMISTRY),

                        })
                {
                    AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/lv2")
                            .takes(ItemID.COSMIC_RUNE)
                            .takes(ItemID.AIR_RUNE, 3)
                            .takes(ench.myTarget)
                            .makes(ench.myResult)
                            .makes(Skill.MAGIC, 37)
                            .requires(Skill.MAGIC, 27)
                            .build());
                }
            }

            // Level 3
            {

                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.RUBY_RING, ItemID.RING_OF_FORGING),
                                new Enchantable(ItemID.RUBY_BRACELET, ItemID.INOCULATION_BRACELET),
                                new Enchantable(ItemID.RUBY_AMULET, ItemID.AMULET_OF_STRENGTH),
                                new Enchantable(ItemID.RUBY_SICKLE_B, ItemID.ENCHANTED_RUBY_SICKLE_B),
                                new Enchantable(ItemID.TOPAZ_RING, ItemID.EFARITAYS_AID),
                                new Enchantable(ItemID.TOPAZ_BRACELET, ItemID.BRACELET_OF_SLAUGHTER),
                                new Enchantable(ItemID.TOPAZ_NECKLACE, ItemID.NECKLACE_OF_FAITH),
                                new Enchantable(ItemID.TOPAZ_AMULET, ItemID.BURNING_AMULET5),

                        })
                {
                    AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/lv3")
                            .takes(ItemID.COSMIC_RUNE)
                            .takes(ItemID.FIRE_RUNE, 5)
                            .takes(ench.myTarget)
                            .makes(ench.myResult)
                            .makes(Skill.MAGIC, 59)
                            .requires(Skill.MAGIC, 49)
                            .build());
                }

                AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ItemID.RUBY_NECKLACE).getName(), "magic/standard/enchant/lv3")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 5)
                        .takes(ItemID.RUBY_NECKLACE)
                        .makes(ItemID.DIGSITE_PENDANT_5)
                        .makes(Skill.MAGIC, 59)
                        .requires(Skill.MAGIC, 49)
                        .requires(Quest.THE_DIG_SITE)
                        .build());
            }

            // Level 4
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.DIAMOND_RING, ItemID.RING_OF_LIFE),
                                new Enchantable(ItemID.DIAMOND_NECKLACE, ItemID.PHOENIX_NECKLACE),
                                new Enchantable(ItemID.DIAMOND_BRACELET, ItemID.ABYSSAL_BRACELET5),
                                new Enchantable(ItemID.DIAMOND_AMULET, ItemID.AMULET_OF_POWER)
                        })
                {
                    AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/bolts")
                            .takes(ItemID.COSMIC_RUNE)
                            .takes(ItemID.EARTH_RUNE, 10)
                            .takes(ench.myTarget)
                            .makes(ench.myResult)
                            .makes(Skill.MAGIC, 67)
                            .requires(Skill.MAGIC, 57)
                            .build());
                }
            }

            // Level 5
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.DRAGONSTONE_RING, ItemID.RING_OF_WEALTH),
                                new Enchantable(ItemID.DRAGON_NECKLACE, ItemID.SKILLS_NECKLACE),
                                new Enchantable(ItemID.DRAGONSTONE_BRACELET, ItemID.COMBAT_BRACELET),
                                new Enchantable(ItemID.DRAGONSTONE_AMULET, ItemID.AMULET_OF_GLORY)
                        })
                {
                    AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/lv5")
                            .takes(ItemID.COSMIC_RUNE)
                            .takes(ItemID.EARTH_RUNE, 15)
                            .takes(ItemID.WATER_RUNE, 15)
                            .takes(ench.myTarget)
                            .makes(ench.myResult)
                            .makes(Skill.MAGIC, 78)
                            .requires(Skill.MAGIC, 68)
                            .build());
                }

                // Level 6
                {
                    for (Enchantable ench : new Enchantable[]
                            {
                                    new Enchantable(ItemID.ONYX_RING, ItemID.RING_OF_STONE),
                                    new Enchantable(ItemID.ONYX_NECKLACE, ItemID.BERSERKER_NECKLACE),
                                    new Enchantable(ItemID.ONYX_BRACELET, ItemID.REGEN_BRACELET),
                                    new Enchantable(ItemID.ONYX_AMULET, ItemID.AMULET_OF_FURY)
                            }) {
                        AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/lv6")
                                .takes(ItemID.COSMIC_RUNE)
                                .takes(ItemID.EARTH_RUNE, 20)
                                .takes(ItemID.FIRE_RUNE, 20)
                                .takes(ench.myTarget)
                                .makes(ench.myResult)
                                .makes(Skill.MAGIC, 97)
                                .requires(Skill.MAGIC, 87)
                                .build());
                    }
                }

                // Level 7
                {
                    for (Enchantable ench : new Enchantable[]
                            {
                                    new Enchantable(ItemID.ZENYTE_RING, ItemID.RING_OF_SUFFERING),
                                    new Enchantable(ItemID.ZENYTE_NECKLACE, ItemID.NECKLACE_OF_ANGUISH),
                                    new Enchantable(ItemID.ZENYTE_BRACELET, ItemID.TORMENTED_BRACELET),
                                    new Enchantable(ItemID.ZENYTE_AMULET, ItemID.AMULET_OF_TORTURE)
                            }) {
                        AddMethod(new Method("Enchant " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(), "magic/standard/enchant/lv7")
                                .takes(ItemID.COSMIC_RUNE)
                                .takes(ItemID.BLOOD_RUNE, 20)
                                .takes(ItemID.SOUL_RUNE, 20)
                                .takes(ench.myTarget)
                                .makes(ench.myResult)
                                .makes(Skill.MAGIC, 110)
                                .requires(Skill.MAGIC, 93)
                                .build());
                    }
                }
            }
        }

        // Enchant bolts
        {
            // opal
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.OPAL_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .takes(ItemID.OPAL_BOLTS, 10)
                        .makes(ItemID.OPAL_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 9)
                        .requires(Skill.MAGIC, 4)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.OPAL_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.AIR_RUNE, 2)
                        .takes(ItemID.OPAL_DRAGON_BOLTS, 10)
                        .makes(ItemID.OPAL_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 9)
                        .requires(Skill.MAGIC, 4)
                        .build());
            }

            // Sapphire
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.SAPPHIRE_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.WATER_RUNE)
                        .takes(ItemID.MIND_RUNE)
                        .takes(ItemID.SAPPHIRE_BOLTS, 10)
                        .makes(ItemID.SAPPHIRE_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 17.5)
                        .requires(Skill.MAGIC, 7)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.SAPPHIRE_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.WATER_RUNE)
                        .takes(ItemID.MIND_RUNE)
                        .takes(ItemID.SAPPHIRE_DRAGON_BOLTS, 10)
                        .makes(ItemID.SAPPHIRE_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 17.5)
                        .requires(Skill.MAGIC, 7)
                        .build());
            }

            // Jade
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.JADE_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.EARTH_RUNE, 2)
                        .takes(ItemID.JADE_BOLTS, 10)
                        .makes(ItemID.JADE_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 19)
                        .requires(Skill.MAGIC, 14)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.JADE_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.EARTH_RUNE, 2)
                        .takes(ItemID.JADE_DRAGON_BOLTS, 10)
                        .makes(ItemID.JADE_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 19)
                        .requires(Skill.MAGIC, 14)
                        .build());
            }

            // Pearl
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.PEARL_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.WATER_RUNE, 2)
                        .takes(ItemID.PEARL_BOLTS, 10)
                        .makes(ItemID.PEARL_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 29)
                        .requires(Skill.MAGIC, 24)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.PEARL_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.WATER_RUNE, 2)
                        .takes(ItemID.PEARL_DRAGON_BOLTS, 10)
                        .makes(ItemID.PEARL_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 29)
                        .requires(Skill.MAGIC, 24)
                        .build());
            }

            // Emerald
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.EMERALD_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.NATURE_RUNE)
                        .takes(ItemID.EMERALD_BOLTS, 10)
                        .makes(ItemID.EMERALD_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 37)
                        .requires(Skill.MAGIC, 27)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.EMERALD_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.AIR_RUNE, 3)
                        .takes(ItemID.NATURE_RUNE)
                        .takes(ItemID.EMERALD_DRAGON_BOLTS, 10)
                        .makes(ItemID.EMERALD_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 37)
                        .requires(Skill.MAGIC, 27)
                        .build());
            }

            // Red Topaz
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.TOPAZ_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 2)
                        .takes(ItemID.TOPAZ_BOLTS, 10)
                        .makes(ItemID.TOPAZ_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 33)
                        .requires(Skill.MAGIC, 29)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.TOPAZ_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 2)
                        .takes(ItemID.TOPAZ_DRAGON_BOLTS, 10)
                        .makes(ItemID.TOPAZ_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 33)
                        .requires(Skill.MAGIC, 29)
                        .build());
            }

            // Ruby
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.RUBY_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 5)
                        .takes(ItemID.BLOOD_RUNE)
                        .takes(ItemID.RUBY_BOLTS, 10)
                        .makes(ItemID.RUBY_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 59)
                        .requires(Skill.MAGIC, 49)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.RUBY_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 5)
                        .takes(ItemID.BLOOD_RUNE)
                        .takes(ItemID.RUBY_DRAGON_BOLTS, 10)
                        .makes(ItemID.RUBY_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 59)
                        .requires(Skill.MAGIC, 49)
                        .build());
            }

            // Diamond
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DIAMOND_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.EARTH_RUNE, 10)
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.DIAMOND_BOLTS, 10)
                        .makes(ItemID.DIAMOND_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 67)
                        .requires(Skill.MAGIC, 57)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DIAMOND_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.EARTH_RUNE, 10)
                        .takes(ItemID.LAW_RUNE, 2)
                        .takes(ItemID.DIAMOND_DRAGON_BOLTS, 10)
                        .makes(ItemID.DIAMOND_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 67)
                        .requires(Skill.MAGIC, 57)
                        .build());
            }

            // DragonStone
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DRAGONSTONE_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.EARTH_RUNE, 15)
                        .takes(ItemID.SOUL_RUNE)
                        .takes(ItemID.DRAGONSTONE_BOLTS, 10)
                        .makes(ItemID.DRAGONSTONE_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 78)
                        .requires(Skill.MAGIC, 68)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DRAGONSTONE_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.EARTH_RUNE, 15)
                        .takes(ItemID.SOUL_RUNE)
                        .takes(ItemID.DRAGONSTONE_DRAGON_BOLTS, 10)
                        .makes(ItemID.DRAGONSTONE_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 78)
                        .requires(Skill.MAGIC, 68)
                        .build());
            }

            // Onyx
            {
                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.ONYX_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 20)
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.ONYX_BOLTS, 10)
                        .makes(ItemID.ONYX_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 97)
                        .requires(Skill.MAGIC, 87)
                        .build());

                AddMethod(new Method("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.ONYX_DRAGON_BOLTS).getName(), "magic/standard/enchant/bolts")
                        .takes(ItemID.COSMIC_RUNE)
                        .takes(ItemID.FIRE_RUNE, 20)
                        .takes(ItemID.DEATH_RUNE)
                        .takes(ItemID.ONYX_DRAGON_BOLTS, 10)
                        .makes(ItemID.ONYX_DRAGON_BOLTS_E, 10)
                        .makes(Skill.MAGIC, 97)
                        .requires(Skill.MAGIC, 87)
                        .build());
            }
        }
    }

    private void LoadSmithing()
    {
        Map<String, Costs> barCosts = new HashMap<>();

        barCosts.put("DAGGER", new Costs(1,1));
        barCosts.put("AXE", new Costs(1,1));
        barCosts.put("MACE", new Costs(1,1));
        barCosts.put("MED_HELM", new Costs(1,1));
        barCosts.put("BOLTS_UNF", new Costs(1,10));
        barCosts.put("SWORD", new Costs(1,1));
        barCosts.put("DART_TIP", new Costs(1,10));
        barCosts.put("WIRE", new Costs(1,1));
        barCosts.put("NAILS", new Costs(1,15));
        barCosts.put("ARROWTIPS", new Costs(1,15));
        barCosts.put("SCIMITAR", new Costs(2,1));
        barCosts.put("HASTA", new Costs(1,1));
        barCosts.put("SPEAR", new Costs(1,1));
        barCosts.put("JAVELIN_HEADS", new Costs(1,5));
        barCosts.put("LIMBS", new Costs(1,1));
        barCosts.put("KNIFE", new Costs(1,5));
        barCosts.put("FULL_HELM", new Costs(2,1));
        barCosts.put("SQ_SHIELD", new Costs(2,1));
        barCosts.put("WARHAMMER", new Costs(3,1));
        barCosts.put("LONGSWORD", new Costs(2,1));
        barCosts.put("BATTLEAXE", new Costs(3,1));
        barCosts.put("CHAINBODY", new Costs(3,1));
        barCosts.put("KITESHIELD", new Costs(3,1));
        barCosts.put("CLAWS", new Costs(2,1));
        barCosts.put("2H_SWORD", new Costs(3,1));
        barCosts.put("PLATELEGS", new Costs(3,1));
        barCosts.put("PLATESKIRT", new Costs(3,1));
        barCosts.put("PLATEBODY", new Costs(5,1));
        barCosts.put("SPIT", new Costs(1,1));
        barCosts.put("STUDS", new Costs(1,1));
        barCosts.put("GRAPPLE_TIP", new Costs(1,1));
        barCosts.put("LANTERN_UNF", new Costs(1,1));
        barCosts.put("LANTERN_FRAME", new Costs(1,1));

        Map<String, Integer> metals = new HashMap<>();

        metals.put("BRONZE", ItemID.BRONZE_BAR);
        metals.put("BLURITE", ItemID.BLURITE_BAR);
        metals.put("IRON", ItemID.IRON_BAR);
        metals.put("SILVER", ItemID.SILVER_BAR);
        metals.put("STEEL", ItemID.STEEL_BAR);
        metals.put("MITHRIL", ItemID.MITHRIL_BAR);
        metals.put("MITH", ItemID.MITHRIL_BAR);
        metals.put("ADAMANT", ItemID.ADAMANTITE_BAR);
        metals.put("ADAMANTITE", ItemID.ADAMANTITE_BAR);
        metals.put("RUNE", ItemID.RUNITE_BAR);
        metals.put("RUNITE", ItemID.RUNITE_BAR);
        metals.put("BULLSEYE", ItemID.STEEL_BAR);
        metals.put("OIL", ItemID.STEEL_BAR);

        String[] ignored = new String[]{
                "BARRONITE_DEPOSITS",
                "CANNONBALL",
                "GOLD_BAR_GOLDSMITH_GAUNTLETS",
                "DRAGON_SQ_SHIELD",
                "DRAGONFIRE_SHIELD",
        };

        for(SmithingAction action : SmithingAction.values())
        {
            if (Arrays.asList(ignored).contains(action.name()))
            {
                continue;
            }

            int del = action.name().indexOf("_");
            if (del == -1)
            {
                TodoPlugin.debug("Weird action [" + action.name() + "]", 1);
                continue;
            }

            String metal = action.name().substring(0, del);
            String type = action.name().substring(del + 1);

            if (type.equals("BAR"))
                continue;

            Costs costs = barCosts.get(type);
            if (costs == null)
            {
                TodoPlugin.debug("Missing bar cost for " + action.name(), 1);
                continue;
            }

            Integer barId = metals.get(metal);
            if (barId == null)
            {
                TodoPlugin.debug("Missing bar type for " + action.name(), 1);
                continue;
            }

            AddMethod(new Method("Smith " + action.getName(myPlugin.myItemManager), "smithing/items/" + metal.toLowerCase())
                    .takes(barId, costs.myCost)
                    .makes(Skill.SMITHING, action.getXp())
                    .makes(action.getItemId(), costs.myResult)
                    .requires(Alternative.HAMMER)
                    .requires(Skill.SMITHING, action.getLevel()));
        }


        class BarCraft
        {
            public int myBarId;
            public int myOreId;
            public int myCoalAmount;
            public double myXP;
            public int myLevel;

            BarCraft(int aBarId, int aOreId, int aCoalAmount, double aXP, int aLevel)
            {
                myBarId = aBarId;
                myOreId = aOreId;
                myCoalAmount = aCoalAmount;
                myXP = aXP;
                myLevel = aLevel;
            }
        }

        //Bars in furnace
        for(BarCraft bar : new BarCraft[] {
                new BarCraft(ItemID.SILVER_BAR, ItemID.SILVER_ORE, 0, 13.7, 20),
                new BarCraft(ItemID.ELEMENTAL_METAL, ItemID.ELEMENTAL_ORE, 4, 7.5, 20),
                new BarCraft(ItemID.STEEL_BAR, ItemID.IRON_ORE, 2, 17.5, 30),
                new BarCraft(ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0, 22.5, 40),
                new BarCraft(ItemID.LOVAKITE_BAR, ItemID.LOVAKITE_ORE, 2, 20, 45),
                new BarCraft(ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, 4, 30, 50),
                new BarCraft(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, 6, 37.5, 70),
                new BarCraft(ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, 8, 50, 85)
        })
        {
            Method method = new Method("Smelt " + myPlugin.myItemManager.getItemComposition(bar.myBarId).getMembersName(), "smithing/bars");

            if (bar.myCoalAmount > 0)
                method.takes(ItemID.COAL, bar.myCoalAmount);

            AddMethod(method
                    .takes(bar.myOreId)
                    .makes(bar.myBarId)
                    .makes(Skill.SMITHING, bar.myXP)
                    .requires(Skill.SMITHING, bar.myLevel)
                    .build());
        }

        AddMethod(new Method("Smelt Bronze Bar", "smithing/bars")
                .takes(ItemID.TIN_ORE)
                .takes(ItemID.COPPER_ORE)
                .makes(ItemID.BRONZE_BAR)
                .makes(Skill.SMITHING, 6.2)
                .build());

        AddMethod(new Method("Smelt Bronze Bar at Blast Furnace", "smithing/bars")
                .takes(ItemID.TIN_ORE)
                .takes(ItemID.COPPER_ORE)
                .makes(ItemID.BRONZE_BAR)
                .makes(Skill.SMITHING, 6.2)
                .requires(Quest.THE_GIANT_DWARF)
                .build());

        AddMethod(new Method("Smelt Blurite Bar", "smithing/bars")
                .takes(ItemID.BLURITE_ORE)
                .makes(ItemID.BLURITE_BAR)
                .makes(Skill.SMITHING, 8)
                .requires(Skill.SMITHING, 13)
                .build());

        AddMethod(new Method("Smelt Iron Bar", "smithing/bars")
                .takes(ItemID.IRON_ORE, 2)
                .makes(ItemID.IRON_BAR)
                .makes(Skill.SMITHING, 12.5)
                .requires(Skill.SMITHING, 15)
                .build());

        AddMethod(new Method("Smelt Iron Bar with Ring of forging", "smithing/bars")
                .takes(ItemID.IRON_ORE)
                .takes(ItemID.RING_OF_FORGING, 1.0/140.0)
                .makes(ItemID.IRON_BAR)
                .makes(Skill.SMITHING, 12.5)
                .requires(Skill.SMITHING, 15)
                .build());

        //Bars in blast furnace
        for(BarCraft bar : new BarCraft[] {
                new BarCraft(ItemID.SILVER_BAR, ItemID.SILVER_ORE, 0, 13.7, 20),
                new BarCraft(ItemID.IRON_BAR, ItemID.IRON_ORE, 0, 12.5, 15),
                new BarCraft(ItemID.STEEL_BAR, ItemID.IRON_ORE, 2, 17.5, 30),
                new BarCraft(ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0, 22.5, 40),
                new BarCraft(ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, 4, 30, 50),
                new BarCraft(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, 6, 37.5, 70),
                new BarCraft(ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, 8, 50, 85)
        })
        {
            {
                Method method = new Method("Smelt " + myPlugin.myItemManager.getItemComposition(bar.myBarId).getMembersName() + " at blast furnace", "smithing/bars");

                if (bar.myCoalAmount > 0)
                    method.takes(ItemID.COAL, bar.myCoalAmount / 2);

                AddMethod(method
                    .takes(bar.myOreId)
                    .makes(bar.myBarId)
                    .makes(Skill.SMITHING, bar.myXP)
                    .requires(Skill.SMITHING, bar.myLevel)
                    .requires(Quest.THE_GIANT_DWARF)
                    .build());
            }
        }

        AddMethod(new Method("Smelt Gold Bar with Goldsmith Gauntlets", "smithing/bars")
                        .takes(ItemID.GOLD_ORE)
                        .makes(ItemID.GOLD_BAR)
                        .makes(Skill.SMITHING, 56.2)
                        .requires(Skill.SMITHING, 40)
                        .requires(ItemID.GOLDSMITH_GAUNTLETS)
                        .build());

        AddMethod(new Method("Smelt Gold Bar with Goldsmith Gauntlets at Blast Furnace", "smithing/blast furnace")
                .takes(ItemID.GOLD_ORE)
                .makes(ItemID.GOLD_BAR)
                .makes(Skill.SMITHING, 56.2)
                .requires(Skill.SMITHING, 40)
                .requires(ItemID.GOLDSMITH_GAUNTLETS)
                .requires(Quest.THE_GIANT_DWARF)
                .build());

        AddMethod(new Method("Smelt cannonballs", "smithing/mould")
                .takes(ItemID.STEEL_BAR)
                .makes(ItemID.CANNONBALL, 4)
                .makes(Skill.SMITHING, 25.6)
                .requires(ItemID.AMMO_MOULD)
                .requires(Skill.SMITHING, 35)
                .build());

        AddMethod(new Method("Smelt cannonballs with Double ammo mould", "smithing/mould")
                .takes(ItemID.STEEL_BAR,2)
                .makes(ItemID.CANNONBALL, 8)
                .makes(Skill.SMITHING, 51.2)
                .requires(ItemID.DOUBLE_AMMO_MOULD)
                .requires(Skill.SMITHING, 35)
                .requires(Quest.SLEEPING_GIANTS)
                .build());

        // TODO Shazien supply
    }

    private void LoadNMZInfusions()
    {
        class Infusable
        {
            public int myItem;
            public int myResult;
            public int myPoints;

            public Infusable(int aItem, int aResult, int aPoints)
            {
                myItem = aItem;
                myResult = aResult;
                myPoints = aPoints;
            }
        }

        for (Infusable inf : new Infusable[]
                {
                        new Infusable(ItemID.BLACK_MASK_10, ItemID.BLACK_MASK_10_I, 1250000),
                        new Infusable(ItemID.SLAYER_HELMET, ItemID.SLAYER_HELMET_I, 1250000),
                        new Infusable(ItemID.SALVE_AMULET, ItemID.SALVE_AMULETI, 800000),
                        new Infusable(ItemID.SALVE_AMULET_E, ItemID.SALVE_AMULETEI, 800000),
                        new Infusable(ItemID.RING_OF_SUFFERING, ItemID.RING_OF_SUFFERING_I, 725000),
                        new Infusable(ItemID.RING_OF_THE_GODS, ItemID.RING_OF_THE_GODS_I, 650000),
                        new Infusable(ItemID.BERSERKER_RING, ItemID.BERSERKER_RING_I, 650000),
                        new Infusable(ItemID.WARRIOR_RING, ItemID.WARRIOR_RING_I, 650000),
                        new Infusable(ItemID.ARCHERS_RING, ItemID.ARCHERS_RING_I, 650000),
                        new Infusable(ItemID.SEERS_RING, ItemID.SEERS_RING_I, 650000),
                        new Infusable(ItemID.TYRANNICAL_RING, ItemID.TYRANNICAL_RING_I, 650000),
                        new Infusable(ItemID.TREASONOUS_RING, ItemID.TREASONOUS_RING_I, 650000),
                        new Infusable(ItemID.GRANITE_RING, ItemID.GRANITE_RING_I, 500000),
                })
        {
            AddMethod(new Method("Infuse " + myPlugin.myItemManager.getItemComposition(inf.myItem).getName() + " at NMZ", "minigame")
                    .takes(inf.myItem)
                    .takes(IdBuilder.NMZPoints(inf.myPoints))
                    .makes(inf.myResult)
                    .build());
        }
    }
}
